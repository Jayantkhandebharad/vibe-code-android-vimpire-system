package com.example.vampire_system.domain.engine

import com.example.vampire_system.data.db.*
import com.example.vampire_system.data.model.*
import com.example.vampire_system.util.Dates
import com.example.vampire_system.domain.repo.LevelRepo

class XpEngine(
    private val db: AppDatabase,
    private val levelRepo: LevelRepo
) {
    private val abilityDao = db.abilityDao()
    private val qiDao = db.questInstanceDao()
    private val ledger = db.xpLedgerDao()
    private val levelTaskDao = db.levelTaskDao()

    private fun normalizedUnitsForPerUnit(ability: AbilityEntity, amount: Double): Double =
        when (ability.id) {
            "notes" -> amount / 250.0   // "250w" means 1 unit = 250 words
            else    -> amount
        }

    suspend fun award(
        abilityId: String,
        amount: Double,                  // reps, pages, minutes, etc.
        evidenceOk: Boolean,
        date: String = Dates.todayLocal()
    ): Int {
        val ability = abilityDao.byId(abilityId) ?: return 0
        // Core Gate: block non-foundations if locked
        if (!CoreGateEngine(db).isUnlockedForToday(levelRepo.getCurrent().levelId, date) &&
            ability.group != AbilityGroup.FOUNDATIONS
        ) {
            // Record attempt as 0 XP quest? We simply return 0.
            return 0
        }

        var base = when (val rule = ability.xpRule) {
            is XpRule.PerUnit   -> (rule.xpPerUnit * normalizedUnitsForPerUnit(ability, amount)).toInt()
            is XpRule.PerMinutes-> {
                val blocks = (amount / 10.0)
                (rule.xpPer10Min * blocks).toInt()
            }
            is XpRule.Flat      -> rule.xp
        }

        // Apply seriousness
        if (ability.seriousness != null) {
            // If evidence missing or required log flags unmet, pay casual or 0
            if (!evidenceOk) {
                // fall back to casual: half if defined with Flat, else 0
                base = if (ability.xpRule is XpRule.Flat) (base / 2) else 0
            }
        }

        // Apply daily cap for that ability (simple cap by xp)
        ability.dailyCapXp?.let { cap ->
            // Sum existing awarded XP for this ability today
            // (Simplify: skip; full impl would query qi for abilityId and date)
            if (base > cap) base = cap
        }

        // Persist quest instance (DONE)
        val qi = QuestInstanceEntity(
            id = java.util.UUID.randomUUID().toString(),
            date = date,
            levelId = levelRepo.getCurrent().levelId,
            templateId = null,
            abilityId = abilityId,
            status = QuestStatus.DONE,
            xpAwarded = base,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        qiDao.upsert(qi)
        // Use XpService for idempotent awarding
        val xpService = com.example.vampire_system.domain.xp.XpService(db)
        xpService.awardForQuest(
            date = date,
            type = LedgerType.AWARD,
            delta = base,
            qiId = qi.id,
            abilityId = abilityId,
            note = "award ${abilityId}"
        )

        return base
    }

    suspend fun awardForQuest(qiId: String, abilityId: String, amount: Double): Int {
        val ability = db.abilityDao().byId(abilityId) ?: return 0
        val hasRequiredEvidence = if (ability.seriousness != null) {
            val kinds = ability.evidenceKinds
            if (kinds.isEmpty()) false else {
                val placeholders = kinds.joinToString(",") { "?" }
                val args = arrayOf(qiId) + kinds.map { it.name }
                val cnt = db.openHelper.readableDatabase.query(
                    "SELECT COUNT(1) FROM evidence WHERE questInstanceId=? AND kind IN ($placeholders)",
                    args
                ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
                cnt > 0
            }
        } else true
        return award(abilityId, amount, evidenceOk = hasRequiredEvidence)
    }

    suspend fun completeQuest(qiId: String, amount: Double): Int {
        val qi = db.questInstanceDao().byId(qiId) ?: return 0
        
        android.util.Log.d("XpEngine", "completeQuest called for quest ${qi.id}, templateId: ${qi.templateId}, abilityId: ${qi.abilityId}")
        
        // Check if this is a level task quest (has templateId) or ability quest
        if (qi.templateId != null) {
            // This is a level task quest - use the task's XP reward
            android.util.Log.d("XpEngine", "Quest has templateId, treating as level task quest")
            return completeLevelTaskQuest(qiId, amount)
        } else {
            // This is an ability quest - use the ability's XP rules
            android.util.Log.d("XpEngine", "Quest has no templateId, treating as ability quest")
            val abilityId = qi.abilityId ?: return 0
            val ability = db.abilityDao().byId(abilityId) ?: return 0

            val hasRequiredEvidence = if (ability.seriousness != null) {
                val kinds = ability.evidenceKinds
                val placeholders = kinds.joinToString(",") { "?" }
                val args = arrayOf(qiId) + kinds.map { it.name }
                db.openHelper.readableDatabase.query(
                    "SELECT COUNT(1) FROM evidence WHERE questInstanceId=? AND kind IN ($placeholders)", args
                ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 } > 0
            } else true

            var base = when (val rule = ability.xpRule) {
                is XpRule.PerUnit   -> (rule.xpPerUnit * normalizedUnitsForPerUnit(ability, amount)).toInt()
                is XpRule.PerMinutes-> {
                    val blocks = (amount / 10.0)
                    (rule.xpPer10Min * blocks).toInt()
                }
                is XpRule.Flat      -> rule.xp
            }
            if (ability.seriousness != null && !hasRequiredEvidence) {
                base = if (ability.xpRule is XpRule.Flat) (base / 2) else 0
            }
            ability.dailyCapXp?.let { cap -> if (base > cap) base = cap }

            val updated = qi.copy(
                status = QuestStatus.DONE,
                xpAwarded = base,
                updatedAt = System.currentTimeMillis()
            )
            db.questInstanceDao().upsert(updated)

            // Use XpService for idempotent awarding
            val xpService = com.example.vampire_system.domain.xp.XpService(db)
            xpService.awardForQuest(
                date = qi.date,
                type = LedgerType.AWARD,
                delta = base,
                qiId = qi.id,
                abilityId = abilityId,
                note = "award ${abilityId}"
            )
            return base
        }
    }

    private suspend fun completeLevelTaskQuest(qiId: String, amount: Double): Int {
        val qi = db.questInstanceDao().byId(qiId) ?: return 0
        val templateId = qi.templateId ?: return 0
        
        // Get the level task to get its XP reward and category
        val task = db.levelTaskDao().byId(templateId) ?: return 0
        
        android.util.Log.d("XpEngine", "Completing level task quest: ${qi.id}, templateId: ${templateId}, task: ${task.id}, category: ${task.category}, xpReward: ${task.xpReward}")
        
        // Use the task's XP reward
        val base = task.xpReward.toInt()
        
        android.util.Log.d("XpEngine", "Awarding ${base} XP for level task quest completion")
        
        val updated = qi.copy(
            status = QuestStatus.DONE,
            xpAwarded = base,
            updatedAt = System.currentTimeMillis()
        )
        db.questInstanceDao().upsert(updated)

        // Use XpService for idempotent awarding
        val xpService = com.example.vampire_system.domain.xp.XpService(db)
        xpService.awardForQuest(
            date = qi.date,
            type = LedgerType.AWARD,
            delta = base,
            qiId = qi.id,
            abilityId = qi.abilityId,
            note = "award task ${templateId}"
        )
        
        android.util.Log.d("XpEngine", "Successfully awarded ${base} XP for level task quest ${qi.id}")
        return base
    }

    suspend fun revertQuestToPending(qiId: String) {
        val qi = db.questInstanceDao().byId(qiId) ?: return
        if (qi.status == QuestStatus.DONE && qi.xpAwarded > 0) {
            // Use XpService for idempotent reversal
            val xpService = com.example.vampire_system.domain.xp.XpService(db)
            xpService.awardForQuest(
                date = qi.date,
                type = LedgerType.ADJUSTMENT,
                delta = -qi.xpAwarded,
                qiId = qi.id,
                abilityId = qi.abilityId,
                note = "revert"
            )
        }
        db.questInstanceDao().upsert(qi.copy(status = QuestStatus.PENDING, xpAwarded = 0, updatedAt = System.currentTimeMillis()))
    }

    suspend fun finalizeDay(date: String = Dates.todayLocal()): DaySummaryEntity {
        val level = levelRepo.getCurrent().levelId

        // Gather today's quest instances
        val foundationIds = Xp.FOUNDATIONS.toSet()
        val placeholders = foundationIds.joinToString(",") { "?" }
        val args = arrayOf(date) + foundationIds.toList()
        val xpRaw = db.openHelper.readableDatabase.query(
            "SELECT SUM(xpAwarded) FROM quest_instances WHERE date = ?",
            arrayOf(date)
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
        val foundationsHit = db.openHelper.readableDatabase.query(
            "SELECT COUNT(DISTINCT abilityId) FROM quest_instances WHERE date = ? AND abilityId IN ($placeholders)",
            args
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

        // Streak bonus
        val streakRepo = com.example.vampire_system.domain.repo.StreakRepo(db)
        val streakState = streakRepo.get() ?: StreakStateEntity(
            consecutiveDays = 0, tier = StreakTier.NONE, mulligansLeft = 1, updatedAt = System.currentTimeMillis()
        )
        val bonusPct = when (streakState.tier) {
            StreakTier.PLUS_10 -> 0.10
            StreakTier.PLUS_20 -> 0.20
            else -> 0.0
        }
        val xpAfterBonus = (xpRaw + (xpRaw * bonusPct)).toInt()
        // Bonuses (idempotent)
        val bonusDelta = (xpAfterBonus - xpRaw)
        if (bonusDelta > 0) {
            val xpService = com.example.vampire_system.domain.xp.XpService(db)
            xpService.awardForNote(date, LedgerType.BONUS, bonusDelta, "streak/foundations bonus")
        }

        // Foundations penalty (Level ≥11 and <3 foundations)
        val applyPenalty = (level >= 11 && foundationsHit < 3)
        val penalty = if (applyPenalty) (xpAfterBonus * 0.20).toInt() else 0
        if (penalty > 0) {
            val xpService = com.example.vampire_system.domain.xp.XpService(db)
            xpService.awardForNote(date, LedgerType.PENALTY, -penalty, "foundations <3")
        }
        var xpNet = xpAfterBonus - penalty

        // All-5 +10 bonus
        if (foundationsHit == 5) xpNet += 10

        // Persist DaySummary
        val summary = DaySummaryEntity(
            date = date,
            levelId = level,
            xpRaw = xpRaw,
            xpBonus = (xpAfterBonus - xpRaw),
            xpPenalty = penalty,
            xpNet = xpNet,
            foundationsHit = foundationsHit,
            streakTier = streakState.tier
        )
        db.dayDao().upsert(summary)

        // Update streak
        StreakEngine(db).onDayClosed(workedToday = xpRaw > 0)

        // Do NOT add XP or level up here; awards already updated level in XpService

        return summary
    }
}


