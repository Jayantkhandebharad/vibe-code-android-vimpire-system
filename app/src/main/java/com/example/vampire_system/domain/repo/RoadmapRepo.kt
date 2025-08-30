package com.example.vampire_system.domain.repo

import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.LevelMilestoneEntity
import com.example.vampire_system.data.model.Xp

data class LevelCard(
    val level: Int,
    val xpNeeded: Int,
    val cumulativeXp: Int,
    val unlockedAbilities: List<String>,
    val taskCount: Int,
    val milestone: LevelMilestoneEntity?
)

class RoadmapRepo(private val db: AppDatabase) {
    suspend fun build(): List<LevelCard> {
        val abilities = db.abilityDao().getAllOnce()
        val milestones = db.levelMilestoneDao().all().associateBy { it.levelId }
        return (1..100).map { l ->
            val unlocks = abilities.filter { it.unlockLevel == l }.map { it.name }
            val tasks = db.levelTaskDao().forLevelOnce(l).size
            LevelCard(
                level = l,
                xpNeeded = Xp.xpForLevel(l),
                cumulativeXp = Xp.totalXpToReach(l),
                unlockedAbilities = unlocks,
                taskCount = tasks,
                milestone = milestones[l]
            )
        }
    }
}


