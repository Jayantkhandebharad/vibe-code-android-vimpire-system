package com.example.vampire_system.domain.engine

import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.model.Xp
import com.example.vampire_system.util.Dates

class CoreGateEngine(private val db: AppDatabase) {

    // Rule: At the START of every level, only Foundations can earn XP.
    // Unlock for the level if:
    //  - CoreChain: >=3 foundations on 2 consecutive days, or
    //  - CoreBurst: all 5 foundations in one day.
    //
    // We enforce the lock only on days >= startedAtDate of that level.
    // Simpler: level unlock applies after next-day rollover when you reach a new level.

    suspend fun isUnlockedForToday(levelId: Int, date: String = Dates.todayLocal()): Boolean {
        // Fetch yesterday and today foundation counts from quest_instances
        val foundations = Xp.FOUNDATIONS.toSet()

        // CoreBurst: all 5 today
        val todayCount = countFoundations(date, foundations)
        if (todayCount >= 5) return true

        // CoreChain: >=3 on two consecutive days (yesterday+today)
        val yesterday = java.time.LocalDate.parse(date).minusDays(1).toString()
        val yCount = countFoundations(yesterday, foundations)
        if (todayCount >= 3 && yCount >= 3) return true

        return false
    }

    private fun countFoundations(date: String, foundations: Set<String>): Int {
        // Using raw query for simplicity (replace with DAO in production)
        val placeholders = foundations.joinToString(",") { "?" }
        val args = arrayOf(date) + foundations.toList()
        return db.openHelper.readableDatabase.query(
            "SELECT COUNT(DISTINCT abilityId) FROM quest_instances WHERE date=? AND abilityId IN ($placeholders)",
            args
        ).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }
}


