package com.example.vampire_system.domain.engine

import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.StreakStateEntity
import com.example.vampire_system.data.model.StreakTier

class StreakEngine(private val db: AppDatabase) {
    suspend fun onDayClosed(workedToday: Boolean) {
        val repo = com.example.vampire_system.domain.repo.StreakRepo(db)
        val prev = repo.get() ?: StreakStateEntity(1, 0, StreakTier.NONE, 1, System.currentTimeMillis())

        val next = if (workedToday) {
            val newCount = prev.consecutiveDays + 1
            val tier = when {
                newCount >= 7 -> StreakTier.PLUS_20
                newCount >= 3 -> StreakTier.PLUS_10
                else -> StreakTier.NONE
            }
            prev.copy(
                consecutiveDays = newCount,
                tier = tier,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            if (prev.mulligansLeft > 0) {
                prev.copy(
                    mulligansLeft = prev.mulligansLeft - 1,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                val tier = when (prev.tier) {
                    StreakTier.PLUS_20 -> StreakTier.PLUS_10
                    StreakTier.PLUS_10 -> StreakTier.NONE
                    else -> StreakTier.NONE
                }
                prev.copy(
                    consecutiveDays = 0,
                    tier = tier,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }

        repo.upsert(next)
    }
}


