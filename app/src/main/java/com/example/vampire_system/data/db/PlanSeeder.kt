package com.example.vampire_system.data.db

import com.example.vampire_system.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PlanSeeder {

    suspend fun seedIfEmpty(db: AppDatabase) = withContext(Dispatchers.IO) {
        val abilityDao = db.abilityDao()
        val levelDao = db.levelDao()
        val taskDao = db.levelTaskDao()

        // Probe: if level 1 exists we assume seeded
        val probe = levelDao.byId(1)
        if (probe != null) return@withContext

        // 1) Abilities (subset representative; extend later)
        val abilities = listOf(
            AbilityEntity("pushups","Push-ups",AbilityGroup.FOUNDATIONS,"rep", XpRule.PerUnit(0.25), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO), unlockLevel = 1),
            AbilityEntity("reading","Reading (non-fiction/tech)",AbilityGroup.FOUNDATIONS,"page", XpRule.PerUnit(1.0), 40,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 2),
            AbilityEntity("walk_run","Walk / Run",AbilityGroup.FITNESS,"10min", XpRule.PerMinutes(5), null,null,
                setOf(EvidenceKind.TIMER), unlockLevel = 3),
            AbilityEntity("notes","Notes / Writing",AbilityGroup.FOUNDATIONS,"250w", XpRule.PerUnit(3.0), null,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 4)
        )
        abilityDao.upsertAll(abilities)

        // 2) Levels 1..100 with XP requirements and bosses each decade
        val levels = (1..100).map { lvl ->
            LevelEntity(
                id = lvl,
                xpRequired = Xp.xpForLevel(lvl),
                title = if (lvl % 10 == 0) "Boss" else null,
                boss = (lvl % 10 == 0),
                acceptance = emptyList(),
                unlocks = emptyList()
            )
        }
        levelDao.upsertAll(levels)

        // 3) Minimal tasks for sample levels
        val tasks = listOf(
            LevelTaskEntity("L10_SHIP", 10, "ship", "1-file CLI/API; provide usage example", listOf("repo_link","usage_example")),
            LevelTaskEntity("L11_CURATE", 11, "data_curation", "Curate >=200 instruction pairs; dedup; tag; stats", listOf("dataset_stats","sample_rows"))
        )
        taskDao.upsertAll(tasks)

        val lpDao = db.levelProgressDao()
        if (lpDao.get() == null) {
            lpDao.upsert(
                LevelProgressEntity(
                    levelId = 1,
                    xpInLevel = 0,
                    startedAtDate = com.example.vampire_system.util.Dates.todayLocal(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}


