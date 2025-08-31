package com.example.vampire_system.domain.engine

import com.example.vampire_system.data.db.*
import com.example.vampire_system.data.model.*
import com.example.vampire_system.domain.repo.LevelRepo
import com.example.vampire_system.util.Dates
import java.util.*

class QuestEngine(
    private val db: AppDatabase,
    private val levelRepo: LevelRepo
) {
    private val abilityDao = db.abilityDao()
    private val levelTaskDao = db.levelTaskDao()
    private val qiDao = db.questInstanceDao()

    suspend fun generateDaily(date: String = Dates.todayLocal()) {
        val level = levelRepo.getCurrent().levelId
        val unlocked = CoreGateEngine(db).isUnlockedForToday(level, date)

        val foundationIds = Xp.FOUNDATIONS

        val extras: List<String> = if (unlocked) {
            // Show ALL unlocked abilities for the current level, not just random ones
            db.abilityDao().getAllOnce()
                .filter { it.group != AbilityGroup.FOUNDATIONS && it.unlockLevel <= level }
                .map { it.id }
                .sorted()
        } else emptyList()

        // Get level tasks for current level AND previous levels (to keep important tasks available)
        val currentLevelTasks = db.levelTaskDao().forLevel(level)
        val previousLevelTasks = (1 until level).flatMap { prevLevel ->
            db.levelTaskDao().forLevelOnce(prevLevel)
        }
        val allRelevantTasks = currentLevelTasks + previousLevelTasks
        
        val taskIds = allRelevantTasks.map { "QI_${date}_${it.id}" }

        val abilityIds = (foundationIds + extras)
        val targetAbilityQiIds = abilityIds.map { "QI_${date}_${it}" }
        val targetIds = (targetAbilityQiIds + taskIds).toSet()

        abilityIds.forEach { ensureQuestForAbility(it, level, date, daily = true) }
        allRelevantTasks.forEach { task ->
            android.util.Log.d("QuestEngine", "Creating quest for level task: ${task.id} (level ${task.levelId})")
            ensureAdHocTask(task, level, date)
        }

        val existing = db.questInstanceDao().listForDate(date)
        val pendingStrays = existing.filter { it.status == QuestStatus.PENDING && it.id !in targetIds }.map { it.id }
        if (pendingStrays.isNotEmpty()) {
            db.questInstanceDao().deletePendingNotIn(date, targetIds.toList())
        }
    }

    suspend fun ensureQuestForAbility(abilityId: String, level: Int, date: String, daily: Boolean) {
        // To keep it simple: create a PENDING QuestInstance if not already present
        val id = "QI_${date}_$abilityId"
        val existing = db.questInstanceDao().byId(id)
        if (existing == null) {
            db.questInstanceDao().upsert(
                QuestInstanceEntity(
                    id = id,
                    date = date,
                    levelId = level,
                    templateId = null,
                    abilityId = abilityId,
                    status = QuestStatus.PENDING,
                    xpAwarded = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // QuestEngine.kt
    suspend fun ensureDailyFoundations(date: String = com.example.vampire_system.util.Dates.todayLocal()) {
        val level = levelRepo.getCurrent().levelId
        com.example.vampire_system.data.model.Xp.FOUNDATIONS.forEach {
            ensureQuestForAbility(it, level, date, daily = true) // method already exists in your file
        }
    }

    private suspend fun ensureAdHocTask(task: LevelTaskEntity, level: Int, date: String) {
        val id = "QI_${date}_${task.id}"
        android.util.Log.d("QuestEngine", "Ensuring ad-hoc task quest: $id for task: ${task.id}")
        val existing = db.questInstanceDao().byId(id)
        if (existing == null) {
            val questEntity = QuestInstanceEntity(
                id = id,
                date = date,
                levelId = level,
                templateId = task.id,
                abilityId = task.abilityId,
                status = QuestStatus.PENDING,
                xpAwarded = 0, // XP should be 0 until quest is completed
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            db.questInstanceDao().upsert(questEntity)
            android.util.Log.d("QuestEngine", "Created quest instance: $id with templateId: ${task.id}, abilityId: ${task.abilityId}, category: ${task.category}, xpReward: ${task.xpReward}, xpAwarded: ${questEntity.xpAwarded} (will be set when completed)")
            // Seed stage items from task.acceptance (one checkbox per acceptance line)
            val existingStages = db.stageDao().forQuest(id)
            if (existingStages.isEmpty()) {
                val stages = task.acceptance.map { acc ->
                    StageItemEntity(
                        id = java.util.UUID.randomUUID().toString(),
                        questInstanceId = id,
                        label = acc,
                        done = false,
                        completedAt = null
                    )
                }
                db.stageDao().upsertAll(stages)
            }
        }
    }
}


