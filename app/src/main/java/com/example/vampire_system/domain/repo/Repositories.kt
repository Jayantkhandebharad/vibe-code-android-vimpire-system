package com.example.vampire_system.domain.repo

import com.example.vampire_system.data.db.*
import com.example.vampire_system.data.model.Xp
import com.example.vampire_system.util.Dates

class LevelRepo(private val db: AppDatabase) {
    private val levelDao = db.levelDao()
    private val lpDao = db.levelProgressDao()

    suspend fun getCurrent(): LevelProgressEntity =
        lpDao.get() ?: LevelProgressEntity(levelId = 1, xpInLevel = 0, startedAtDate = Dates.todayLocal(), updatedAt = System.currentTimeMillis()).also {
            lpDao.upsert(it)
        }

    suspend fun addXp(xp: Int): Pair<Int, Boolean> {
        // returns (newLevelId, didLevelUp)
        var lp = getCurrent()
        var remaining = xp
        var level = lp.levelId
        var inLevel = lp.xpInLevel
        var leveled = false

        while (remaining > 0) {
            val req = Xp.xpForLevel(level)
            val need = (req - inLevel).coerceAtLeast(0)
            if (remaining >= need && need > 0) {
                // level up
                remaining -= need
                level += 1
                inLevel = 0
                leveled = true
                // mark new level start date as tomorrow (lock applies next day)
                // Simpler: mark today; we will only enforce at next-day rollover.
            } else {
                inLevel += remaining
                remaining = 0
            }
        }
        lp = LevelProgressEntity(
            id = 1,
            levelId = level,
            xpInLevel = inLevel,
            startedAtDate = Dates.todayLocal(),
            updatedAt = System.currentTimeMillis()
        )
        lpDao.upsert(lp)
        return level to leveled
    }

    suspend fun setLevel(levelId: Int) {
        val lp = LevelProgressEntity(
            id = 1, levelId = levelId, xpInLevel = 0,
            startedAtDate = Dates.todayLocal(), updatedAt = System.currentTimeMillis()
        )
        lpDao.upsert(lp)
    }

    suspend fun updateXpInLevel(xpInLevel: Int) {
        val current = getCurrent()
        val lp = LevelProgressEntity(
            id = 1,
            levelId = current.levelId,
            xpInLevel = xpInLevel,
            startedAtDate = current.startedAtDate,
            updatedAt = System.currentTimeMillis()
        )
        lpDao.upsert(lp)
    }
}

class AbilityRepo(private val db: AppDatabase) {
    private val dao = db.abilityDao()
    fun all() = dao.getAll()
    suspend fun byId(id: String) = dao.byId(id)
}

class QuestRepo(private val db: AppDatabase) {
    private val qi = db.questInstanceDao()
    private val qt = db.questTemplateDao()
    private val tasks = db.levelTaskDao()

    fun today(date: String) = qi.forDate(date)
    suspend fun upsertInstance(entity: QuestInstanceEntity) = qi.upsert(entity)
    suspend fun templateAll() = qt.all()
    suspend fun tasksForLevel(levelId: Int) = tasks.forLevel(levelId)
}

class DayRepo(private val db: AppDatabase) {
    private val day = db.dayDao()
    suspend fun byDate(date: String) = day.byDate(date)
    suspend fun upsert(summary: DaySummaryEntity) = day.upsert(summary)
}

class StreakRepo(private val db: AppDatabase) {
    private val dao = db.streakDao()
    suspend fun get(): StreakStateEntity? = dao.get()
    suspend fun upsert(s: StreakStateEntity) = dao.upsert(s)
}

class SettingsRepo(private val db: AppDatabase) {
    private val dao = db.settingsDao()
    suspend fun get(): SettingsEntity? = dao.get()
    suspend fun upsert(s: SettingsEntity) = dao.upsert(s)
}


