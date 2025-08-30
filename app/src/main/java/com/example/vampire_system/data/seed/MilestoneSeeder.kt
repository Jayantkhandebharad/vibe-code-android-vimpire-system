package com.example.vampire_system.data.seed

import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.LevelMilestoneEntity

object MilestoneSeeder {
    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.levelMilestoneDao().all().isNotEmpty()) return
        val defaults = listOf(
            LevelMilestoneEntity(1, "Entry", "Start the run"),
            LevelMilestoneEntity(5, "Consistency Check", "Foundations stable?", isBoss = false),
            LevelMilestoneEntity(10, "Seriousness Gate I", "Unlock Gym/Swim/Badminton", isBoss = true, colorHex = "#2962FF"),
            LevelMilestoneEntity(15, "Shipping Habit", "Weekly ship cadence"),
            LevelMilestoneEntity(20, "Seriousness Gate II", "Hard-mode fitness & comms", isBoss = true, colorHex = "#AA00FF"),
            LevelMilestoneEntity(30, "GenAI Depth I", "Fine-tune & evals focus"),
            LevelMilestoneEntity(40, "Infra Mastery", "Pipelines, tracing, cost"),
            LevelMilestoneEntity(50, "Boss: Systems", "End-to-end GenAI system", isBoss = true, colorHex = "#D50000"),
            LevelMilestoneEntity(60, "Teaching", "Mentor & code reviews"),
            LevelMilestoneEntity(70, "Research Taste", "Novel ideas & writeups"),
            LevelMilestoneEntity(80, "Optimization", "Latency/quality frontier"),
            LevelMilestoneEntity(90, "Boss: Product", "Ship lovable product", isBoss = true, colorHex = "#00C853"),
            LevelMilestoneEntity(100, "God Mode", "Sustain & balance", isBoss = true, colorHex = "#FF6D00")
        )
        db.levelMilestoneDao().upsertAll(defaults)
    }
}


