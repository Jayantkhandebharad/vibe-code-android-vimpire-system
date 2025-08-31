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
    val completedTasks: Int,
    val currentXp: Int?, // null for non-current levels
    val milestone: LevelMilestoneEntity?
)

class RoadmapRepo(private val db: AppDatabase) {
    suspend fun build(): List<LevelCard> {
        val abilities = db.abilityDao().getAllOnce()
        val milestones = db.levelMilestoneDao().all().associateBy { it.levelId }
        val levelRepo = LevelRepo(db)
        val currentProgress = levelRepo.getCurrent()
        val currentLevel = currentProgress.levelId
        
        // DEBUG: Test XP calculation directly
        Xp.debugXpProgression()
        
        return (1..100).map { l ->
            val unlocks = abilities.filter { it.unlockLevel == l }.map { it.name }
            val levelTasks = db.levelTaskDao().forLevelOnce(l)
            val taskCount = levelTasks.size
            
            // Count completed tasks for this level
            val completedTasks = if (taskCount > 0) {
                // Get completed quests that are based on level tasks
                val completedQuests = db.questInstanceDao().getCompleted()
                levelTasks.count { task ->
                    completedQuests.any { quest ->
                        // Check if this quest is based on this level task
                        quest.templateId == task.id
                    }
                }
            } else 0
            
            val xpNeeded = Xp.xpForLevel(l)
            val cumulativeXp = Xp.totalXpToReach(l)
            
            // DEBUG: Log first few levels to verify XP progression
            if (l <= 5) {
                println("DEBUG: Level $l needs $xpNeeded XP, cumulative: $cumulativeXp")
            }
            
            LevelCard(
                level = l,
                xpNeeded = xpNeeded,
                cumulativeXp = cumulativeXp,
                unlockedAbilities = unlocks,
                taskCount = taskCount,
                completedTasks = completedTasks,
                currentXp = if (l == currentLevel) currentProgress.xpInLevel else null,
                milestone = milestones[l]
            )
        }
    }
}


