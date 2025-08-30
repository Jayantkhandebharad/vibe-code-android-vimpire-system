package com.example.vampire_system.data.db

import androidx.room.*
import com.example.vampire_system.data.model.*
import kotlinx.serialization.Serializable

@Entity(tableName = "abilities")
@Serializable
data class AbilityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val group: AbilityGroup,
    val unit: String?,
    val xpRule: XpRule,
    val dailyCapXp: Int?,
    val seriousness: Seriousness?,
    val evidenceKinds: Set<EvidenceKind>,
    val unlockLevel: Int
)

@Entity(tableName = "levels")
@Serializable
data class LevelEntity(
    @PrimaryKey val id: Int,
    val xpRequired: Int,
    val title: String?,
    val boss: Boolean,
    val acceptance: List<String>,
    val unlocks: List<String>
)

@Entity(
    tableName = "level_tasks",
    indices = [Index("levelId")]
)
@Serializable
data class LevelTaskEntity(
    @PrimaryKey val id: String,
    val levelId: Int,
    val abilityId: String?,
    val spec: String,
    val acceptance: List<String>
)

@Entity(tableName = "quest_templates")
@Serializable
data class QuestTemplateEntity(
    @PrimaryKey val id: String,
    val abilityId: String,
    val title: String,
    val kind: QuestKind
)

@Entity(
    tableName = "quest_instances",
    indices = [Index("date"), Index("levelId")]
)
@Serializable
data class QuestInstanceEntity(
    @PrimaryKey val id: String,
    val date: String,
    val levelId: Int,
    val templateId: String?,
    val abilityId: String?,
    val status: QuestStatus,
    val xpAwarded: Int,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "evidence",
    indices = [Index("questInstanceId")]
)
@Serializable
data class EvidenceEntity(
    @PrimaryKey val id: String,
    val questInstanceId: String,
    val kind: EvidenceKind,
    val uriOrText: String,
    val meta: Map<String,String>,
    val sha256: String?,
    val createdAt: Long
)

@Entity(tableName = "day_summaries")
@Serializable
data class DaySummaryEntity(
    @PrimaryKey val date: String,
    val levelId: Int,
    val xpRaw: Int,
    val xpBonus: Int,
    val xpPenalty: Int,
    val xpNet: Int,
    val foundationsHit: Int,
    val streakTier: StreakTier
)

@Entity(tableName = "streak_state")
@Serializable
data class StreakStateEntity(
    @PrimaryKey val id: Int = 1,
    val consecutiveDays: Int,
    val tier: StreakTier,
    val mulligansLeft: Int,
    val updatedAt: Long
)

@Entity(tableName = "settings")
@Serializable
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val resetHour: Int,
    val timezone: String,
    val encryptBackups: Boolean,
    val wifiOnlyBackups: Boolean
)

@Entity(tableName = "user_profile")
@Serializable
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val weightKg: Double?,
    val proteinGPerKg: Double
)


@Entity(tableName = "level_progress")
@Serializable
data class LevelProgressEntity(
    @PrimaryKey val id: Int = 1,
    val levelId: Int,
    val xpInLevel: Int,
    val startedAtDate: String, // yyyy-MM-dd (for core-gate: “new level day” marker)
    val updatedAt: Long
)


@Entity(
    tableName = "xp_ledger",
    indices = [
        Index("date"),
        Index("createdAt"),
        Index("abilityId"),
        Index(value = ["eventKey"], unique = true) // NEW: one row per logical event
    ]
)
@Serializable
data class XpLedgerEntity(
    @PrimaryKey val id: String,               // UUID
    val date: String,                         // yyyy-MM-dd (local)
    val createdAt: Long,                      // epoch millis
    val type: LedgerType,                     // AWARD, BONUS, PENALTY, ADJUSTMENT, LEVEL_UP
    val deltaXp: Int,                         // signed (+/-)
    val abilityId: String?,                   // null for global bonus/penalty/level_up
    val questInstanceId: String?,             // optional linkage
    val note: String? = null,                 // e.g., "reading 10 pages"
    val eventKey: String                      // NEW: idempotency key
)

@Serializable
enum class LedgerType { AWARD, BONUS, PENALTY, ADJUSTMENT, LEVEL_UP }

@Entity(
    tableName = "stage_items",
    indices = [Index("questInstanceId")]
)
@Serializable
data class StageItemEntity(
    @PrimaryKey val id: String,
    val questInstanceId: String,
    val label: String,                        // from LevelTask.acceptance item
    val done: Boolean = false,
    val completedAt: Long? = null
)


@Serializable
@Entity(tableName = "level_milestones")
data class LevelMilestoneEntity(
    @PrimaryKey val levelId: Int,
    val title: String,
    val subtitle: String? = null,
    val isBoss: Boolean = false,
    val colorHex: String? = null        // e.g. "#FF6D00", null = theme default
)

@Serializable
enum class SearchKind { NOTE, LINK, FILE, QUEST, TASK, ABILITY }

@Entity(tableName = "search_index", indices = [
    Index("date"), Index("kind"), Index("abilityId"), Index("questInstanceId")
])
data class SearchIndexEntity(
    @PrimaryKey(autoGenerate = true) val sid: Long = 0L,
    val date: String?,                 // yyyy-MM-dd if applicable
    val kind: SearchKind,              // NOTE/LINK/FILE/QUEST/TASK/ABILITY
    val abilityId: String?,            // for ability-scoped items
    val questInstanceId: String?,      // for evidence/quests
    val title: String,                 // main label in results
    val snippet: String?,              // short preview (e.g., first line of note/spec)
    val text: String                   // full searchable text blob
)

@Entity(tableName = "saved_searches", indices = [Index("orderIdx")])
data class SavedSearchEntity(
    @PrimaryKey val id: String,          // UUID
    val title: String,                   // Chip label
    val query: String?,                  // Search text
    val kind: SearchKind?,               // null=All
    val ability: String?,                // null=All
    val from: String?,                   // yyyy-MM-dd or null
    val to: String?,                     // yyyy-MM-dd or null
    val doneOnly: Boolean,               // extra filter
    val hasEvidence: Boolean,            // extra filter
    val orderIdx: Int                    // display order
)
