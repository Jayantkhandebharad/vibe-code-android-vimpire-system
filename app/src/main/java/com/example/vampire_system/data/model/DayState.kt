package com.example.vampire_system.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class StreakTier { NONE, PLUS_10, PLUS_20 }

@Serializable
data class DaySummary(
    val date: String,          // yyyy-MM-dd
    val levelId: Int,
    val xpRaw: Int,
    val xpBonus: Int,
    val xpPenalty: Int,
    val xpNet: Int,
    val foundationsHit: Int,
    val streakTier: StreakTier
)

@Serializable
data class StreakState(
    val consecutiveDays: Int = 0,
    val tier: StreakTier = StreakTier.NONE,
    val mulligansLeft: Int = 1
)

@Serializable
data class Settings(
    val resetHour: Int = 5,          // 05:00 local
    val timezone: String = "Asia/Kolkata",
    val encryptBackups: Boolean = true,
    val wifiOnlyBackups: Boolean = true
)

@Serializable
data class UserProfile(
    val weightKg: Double? = null,
    val proteinGPerKg: Double = 2.0
)


