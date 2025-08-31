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

        // 1) Comprehensive Abilities across all categories
        val abilities = listOf(
            // FOUNDATIONS - Core daily habits
            AbilityEntity("pushups","Push-ups",AbilityGroup.FOUNDATIONS,"rep", XpRule.PerUnit(0.25), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO), unlockLevel = 1),
            AbilityEntity("reading","Reading (non-fiction/tech)",AbilityGroup.FOUNDATIONS,"page", XpRule.PerUnit(1.0), 40,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 2),
            AbilityEntity("notes","Notes / Writing",AbilityGroup.FOUNDATIONS,"250w", XpRule.PerUnit(3.0), null,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 4),
            AbilityEntity("meditation","Meditation",AbilityGroup.FOUNDATIONS,"10min", XpRule.PerMinutes(3), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 5),
            AbilityEntity("planning","Daily Planning",AbilityGroup.FOUNDATIONS,"day", XpRule.Flat(5), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.CHECKLIST), unlockLevel = 6),
            AbilityEntity("learning","Learning New Skill",AbilityGroup.FOUNDATIONS,"30min", XpRule.PerMinutes(2), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 7),
            AbilityEntity("hydration","Hydration",AbilityGroup.FOUNDATIONS,"500ml", XpRule.PerUnit(1.0), 20,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 8),
            AbilityEntity("protein","Protein Tracking",AbilityGroup.FOUNDATIONS,"20g", XpRule.PerUnit(2.0), 30,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 9),
            
            // FITNESS - Physical activities
            AbilityEntity("walk_run","Walk / Run",AbilityGroup.FITNESS,"10min", XpRule.PerMinutes(5), null,null,
                setOf(EvidenceKind.TIMER), unlockLevel = 3),
            AbilityEntity("gym","Gym Workout",AbilityGroup.FITNESS,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 10),
            AbilityEntity("swimming","Swimming",AbilityGroup.FITNESS,"20min", XpRule.PerMinutes(4), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 12),
            AbilityEntity("badminton","Badminton",AbilityGroup.FITNESS,"match", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 14),
            AbilityEntity("yoga","Yoga",AbilityGroup.FITNESS,"30min", XpRule.PerMinutes(3), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 16),
            AbilityEntity("cycling","Cycling",AbilityGroup.FITNESS,"15min", XpRule.PerMinutes(4), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 18),
            AbilityEntity("strength","Strength Training",AbilityGroup.FITNESS,"exercise", XpRule.PerUnit(2.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO), unlockLevel = 20),
            AbilityEntity("cardio","Cardio Session",AbilityGroup.FITNESS,"20min", XpRule.PerMinutes(3), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 22),
            
            // GENAI - AI/ML focused activities
            AbilityEntity("prompt_eng","Prompt Engineering",AbilityGroup.GENAI,"prompt", XpRule.PerUnit(5.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 25),
            AbilityEntity("fine_tune","Model Fine-tuning",AbilityGroup.GENAI,"model", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 28),
            AbilityEntity("eval","Model Evaluation",AbilityGroup.GENAI,"eval", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 30),
            AbilityEntity("rag","RAG Implementation",AbilityGroup.GENAI,"system", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 32),
            AbilityEntity("ai_research","AI Research Reading",AbilityGroup.GENAI,"paper", XpRule.PerUnit(15.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 35),
            AbilityEntity("ai_coding","AI-Assisted Coding",AbilityGroup.GENAI,"feature", XpRule.Flat(20), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 38),
            
            // NUTRITION - Food and health tracking
            AbilityEntity("meal_plan","Meal Planning",AbilityGroup.NUTRITION,"day", XpRule.Flat(8), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.CHECKLIST), unlockLevel = 15),
            AbilityEntity("cooking","Cooking New Recipe",AbilityGroup.NUTRITION,"recipe", XpRule.Flat(15), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO), unlockLevel = 17),
            AbilityEntity("supplements","Supplement Tracking",AbilityGroup.NUTRITION,"day", XpRule.Flat(3), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.CHECKLIST), unlockLevel = 19),
            AbilityEntity("sleep_track","Sleep Tracking",AbilityGroup.NUTRITION,"night", XpRule.Flat(5), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.TIMER), unlockLevel = 21),
            
            // COMMUNICATION - Social and presentation skills
            AbilityEntity("public_speak","Public Speaking",AbilityGroup.COMMUNICATION,"minute", XpRule.PerUnit(2.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.VIDEO), unlockLevel = 24),
            AbilityEntity("networking","Networking",AbilityGroup.COMMUNICATION,"contact", XpRule.PerUnit(8.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 26),
            AbilityEntity("mentoring","Mentoring",AbilityGroup.COMMUNICATION,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.TIMER), unlockLevel = 29),
            AbilityEntity("code_review","Code Review",AbilityGroup.COMMUNICATION,"review", XpRule.Flat(15), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 31),
            AbilityEntity("teaching","Teaching",AbilityGroup.COMMUNICATION,"lesson", XpRule.Flat(20), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.VIDEO), unlockLevel = 33),
            AbilityEntity("writing","Technical Writing",AbilityGroup.COMMUNICATION,"500w", XpRule.PerUnit(4.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 36),
            
            // ADVANCED - High-level skills
            AbilityEntity("shipping","Product Shipping",AbilityGroup.FOUNDATIONS,"feature", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 40),
            AbilityEntity("research","Research & Development",AbilityGroup.FOUNDATIONS,"project", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 42),
            AbilityEntity("optimization","Performance Optimization",AbilityGroup.FOUNDATIONS,"improvement", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 44),
            AbilityEntity("architecture","System Architecture",AbilityGroup.FOUNDATIONS,"design", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 46),
            AbilityEntity("leadership","Leadership",AbilityGroup.COMMUNICATION,"initiative", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 48),
            AbilityEntity("innovation","Innovation",AbilityGroup.FOUNDATIONS,"idea", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 50)
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
                    LevelTaskEntity("L10_SHIP", 10, "ship", "1-file CLI/API; provide usage example", listOf("repo_link","usage_example"), TaskCategory.KNOWLEDGE, 10.0),
        LevelTaskEntity("L11_CURATE", 11, "data_curation", "Curate >=200 instruction pairs; dedup; tag; stats", listOf("dataset_stats","sample_rows"), TaskCategory.KNOWLEDGE, 10.0)
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
    
    // Force add all abilities even if database already exists
    suspend fun forceAddAllAbilities(db: AppDatabase) = withContext(Dispatchers.IO) {
        val abilityDao = db.abilityDao()
        
        // Comprehensive Abilities across all categories
        val abilities = listOf(
            // FOUNDATIONS - Core daily habits
            AbilityEntity("pushups","Push-ups",AbilityGroup.FOUNDATIONS,"rep", XpRule.PerUnit(0.25), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO), unlockLevel = 1),
            AbilityEntity("reading","Reading (non-fiction/tech)",AbilityGroup.FOUNDATIONS,"page", XpRule.PerUnit(1.0), 40,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 2),
            AbilityEntity("notes","Notes / Writing",AbilityGroup.FOUNDATIONS,"250w", XpRule.PerUnit(3.0), null,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 4),
            AbilityEntity("meditation","Meditation",AbilityGroup.FOUNDATIONS,"10min", XpRule.PerMinutes(3), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.TIMER), unlockLevel = 5),
            AbilityEntity("planning","Daily Planning",AbilityGroup.FOUNDATIONS,"day", XpRule.Flat(5), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.CHECKLIST), unlockLevel = 6),
            AbilityEntity("learning","Learning New Skill",AbilityGroup.FOUNDATIONS,"30min", XpRule.PerMinutes(2), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 7),
            AbilityEntity("hydration","Hydration",AbilityGroup.FOUNDATIONS,"500ml", XpRule.PerUnit(1.0), 20,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 8),
            AbilityEntity("protein","Protein Tracking",AbilityGroup.FOUNDATIONS,"20g", XpRule.PerUnit(2.0), 30,null,
                setOf(EvidenceKind.NOTE), unlockLevel = 9),
            
            // FITNESS - Physical activities
            AbilityEntity("walk_run","Walk / Run",AbilityGroup.FITNESS,"10min", XpRule.PerMinutes(5), null,null,
                setOf(EvidenceKind.TIMER), unlockLevel = 3),
            AbilityEntity("gym","Gym Workout",AbilityGroup.FITNESS,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 10),
            AbilityEntity("swimming","Swimming",AbilityGroup.FITNESS,"20min", XpRule.PerMinutes(4), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 12),
            AbilityEntity("badminton","Badminton",AbilityGroup.FITNESS,"match", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 14),
            AbilityEntity("yoga","Yoga",AbilityGroup.FITNESS,"30min", XpRule.PerMinutes(3), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 16),
            AbilityEntity("cycling","Cycling",AbilityGroup.FITNESS,"15min", XpRule.PerMinutes(4), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 18),
            AbilityEntity("strength","Strength Training",AbilityGroup.FITNESS,"exercise", XpRule.PerUnit(2.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO), unlockLevel = 20),
            AbilityEntity("cardio","Cardio Session",AbilityGroup.FITNESS,"20min", XpRule.PerMinutes(3), null,null,
                setOf(EvidenceKind.TIMER, EvidenceKind.NOTE), unlockLevel = 22),
            
            // GENAI - AI/ML focused activities
            AbilityEntity("prompt_eng","Prompt Engineering",AbilityGroup.GENAI,"prompt", XpRule.PerUnit(5.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 25),
            AbilityEntity("fine_tune","Model Fine-tuning",AbilityGroup.GENAI,"model", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 28),
            AbilityEntity("eval","Model Evaluation",AbilityGroup.GENAI,"eval", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 30),
            AbilityEntity("rag","RAG Implementation",AbilityGroup.GENAI,"system", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 32),
            AbilityEntity("ai_research","AI Research Reading",AbilityGroup.GENAI,"paper", XpRule.PerUnit(15.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 35),
            AbilityEntity("ai_coding","AI-Assisted Coding",AbilityGroup.GENAI,"feature", XpRule.Flat(20), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 38),
            
            // NUTRITION - Food and health tracking
            AbilityEntity("meal_plan","Meal Planning",AbilityGroup.NUTRITION,"day", XpRule.Flat(8), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.CHECKLIST), unlockLevel = 15),
            AbilityEntity("cooking","Cooking New Recipe",AbilityGroup.NUTRITION,"recipe", XpRule.Flat(15), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.PHOTO), unlockLevel = 17),
            AbilityEntity("supplements","Supplement Tracking",AbilityGroup.NUTRITION,"day", XpRule.Flat(3), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.CHECKLIST), unlockLevel = 19),
            AbilityEntity("sleep_track","Sleep Tracking",AbilityGroup.NUTRITION,"night", XpRule.Flat(5), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.TIMER), unlockLevel = 21),
            
            // COMMUNICATION - Social and presentation skills
            AbilityEntity("public_speak","Public Speaking",AbilityGroup.COMMUNICATION,"minute", XpRule.PerUnit(2.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.VIDEO), unlockLevel = 24),
            AbilityEntity("networking","Networking",AbilityGroup.COMMUNICATION,"contact", XpRule.PerUnit(8.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 26),
            AbilityEntity("mentoring","Mentoring",AbilityGroup.COMMUNICATION,"session", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.TIMER), unlockLevel = 29),
            AbilityEntity("code_review","Code Review",AbilityGroup.COMMUNICATION,"review", XpRule.Flat(15), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 31),
            AbilityEntity("teaching","Teaching",AbilityGroup.COMMUNICATION,"lesson", XpRule.Flat(20), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.VIDEO), unlockLevel = 33),
            AbilityEntity("writing","Technical Writing",AbilityGroup.COMMUNICATION,"500w", XpRule.PerUnit(4.0), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 36),
            
            // ADVANCED - High-level skills
            AbilityEntity("shipping","Product Shipping",AbilityGroup.FOUNDATIONS,"feature", XpRule.Flat(50), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 40),
            AbilityEntity("research","Research & Development",AbilityGroup.FOUNDATIONS,"project", XpRule.Flat(40), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 42),
            AbilityEntity("optimization","Performance Optimization",AbilityGroup.FOUNDATIONS,"improvement", XpRule.Flat(35), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 44),
            AbilityEntity("architecture","System Architecture",AbilityGroup.FOUNDATIONS,"design", XpRule.Flat(45), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 46),
            AbilityEntity("leadership","Leadership",AbilityGroup.COMMUNICATION,"initiative", XpRule.Flat(30), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 48),
            AbilityEntity("innovation","Innovation",AbilityGroup.FOUNDATIONS,"idea", XpRule.Flat(25), null,null,
                setOf(EvidenceKind.NOTE, EvidenceKind.LINK), unlockLevel = 50)
        )
        
        // Force add all abilities (this will update existing ones and add new ones)
        abilityDao.upsertAll(abilities)
        
        println("✅ Added ${abilities.size} abilities to database")
    }
}


