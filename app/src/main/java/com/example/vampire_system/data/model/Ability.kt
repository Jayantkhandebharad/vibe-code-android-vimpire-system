package com.example.vampire_system.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Ability(
    val id: String,                 // stable key, e.g., "pushups", "reading"
    val name: String,
    val group: AbilityGroup,
    val unit: String?,              // "rep", "page", "min", "block", "day", or null
    val xpRule: XpRule,
    val dailyCapXp: Int? = null,    // e.g., 40 for reading
    val seriousness: Seriousness? = null,
    val evidenceKinds: Set<EvidenceKind> = emptySet(),
    val unlockLevel: Int            // first level where it can be used (after Core Gate)
)

@Serializable
enum class AbilityGroup { FOUNDATIONS, FITNESS, GENAI, NUTRITION, COMMUNICATION }

@Serializable
sealed class XpRule {
    @Serializable data class PerUnit(val xpPerUnit: Double): XpRule()      // e.g., 0.25 XP per rep
    @Serializable data class PerMinutes(val xpPer10Min: Int): XpRule()     // e.g., 5 XP per 10 min
    @Serializable data class Flat(val xp: Int): XpRule()                   // e.g., 8 XP for a day target
}

@Serializable
data class Seriousness(
    val minMinutes: Int? = null,
    val minRpe: Int? = null,
    val hrZone2Minutes: Int? = null,
    val requiresLog: Boolean = false,
    val structured: Boolean = false,              // for drills/matches
    val scoreOrDrillLog: Boolean = false,         // for badminton
    val focusNoteRequired: Boolean = false        // for swim drills
)

@Serializable
enum class EvidenceKind { NOTE, PHOTO, VIDEO, AUDIO, TIMER, LINK, FILE, CHECKLIST }


