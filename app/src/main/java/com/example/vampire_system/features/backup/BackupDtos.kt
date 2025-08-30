package com.example.vampire_system.features.backup

import kotlinx.serialization.Serializable

@Serializable
data class Manifest(
    val kind: String,
    val createdAt: Long,
    val schemaVersion: Int,
    val appVersion: String,
    val lastFullAt: Long? = null,
    val encrypt: Boolean = true,
    val counts: Map<String, Int> = emptyMap()
)

@Serializable
data class PlanDump(
    val abilities: List<com.example.vampire_system.data.db.AbilityEntity>,
    val levels: List<com.example.vampire_system.data.db.LevelEntity>,
    val tasks: List<com.example.vampire_system.data.db.LevelTaskEntity>,
    val milestones: List<com.example.vampire_system.data.db.LevelMilestoneEntity> = emptyList()
)

@Serializable
data class ProgressDump(
    val levelProgress: com.example.vampire_system.data.db.LevelProgressEntity?,
    val daySummaries: List<com.example.vampire_system.data.db.DaySummaryEntity>,
    val quests: List<com.example.vampire_system.data.db.QuestInstanceEntity>,
    val evidence: List<com.example.vampire_system.data.db.EvidenceEntity>,
    val ledger: List<com.example.vampire_system.data.db.XpLedgerEntity>,
    val streak: com.example.vampire_system.data.db.StreakStateEntity?,
    val settings: com.example.vampire_system.data.db.SettingsEntity?,
    val profile: com.example.vampire_system.data.db.UserProfileEntity?,
    val stages: List<com.example.vampire_system.data.db.StageItemEntity>
)


