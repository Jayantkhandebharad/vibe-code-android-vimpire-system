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

        val extraCount = when {
            level >= 20 -> 4
            level >= 10 -> 3
            else -> 2
        }
        val extras: List<String> = if (unlocked) {
            val pool = db.abilityDao().getAllOnce()
                .filter { it.group != AbilityGroup.FOUNDATIONS && it.unlockLevel <= level }
                .map { it.id }
                .sorted()
            val seed = (date.hashCode() * 31 + level).toLong()
            val rnd = java.util.Random(seed)
            pool.shuffled(rnd).take(extraCount)
        } else emptyList()

        val taskIds = db.levelTaskDao().forLevel(level).map { "QI_${date}_${it.id}" }

        val abilityIds = (foundationIds + extras)
        val targetAbilityQiIds = abilityIds.map { "QI_${date}_${it}" }
        val targetIds = (targetAbilityQiIds + taskIds).toSet()

        abilityIds.forEach { ensureQuestForAbility(it, level, date, daily = true) }
        db.levelTaskDao().forLevel(level).forEach { ensureAdHocTask(it, level, date) }

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
        val existing = db.questInstanceDao().byId(id)
        if (existing == null) {
            db.questInstanceDao().upsert(
                QuestInstanceEntity(
                    id = id,
                    date = date,
                    levelId = level,
                    templateId = task.id,
                    abilityId = task.abilityId,
                    status = QuestStatus.PENDING,
                    xpAwarded = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
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


