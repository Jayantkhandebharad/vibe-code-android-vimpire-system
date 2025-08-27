package com.example.vampire_system.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class QuestKind { DAILY, WEEKLY, BOSS }

@Serializable
data class QuestTemplate(
    val id: String,
    val abilityId: String,
    val title: String,
    val kind: QuestKind
)

@Serializable
enum class QuestStatus { PENDING, DONE }

@Serializable
data class QuestInstance(
    val id: String,
    val date: String,                 // yyyy-MM-dd local
    val levelId: Int,
    val templateId: String? = null,
    val abilityId: String? = null,    // optional, for ad-hoc quests
    val status: QuestStatus = QuestStatus.PENDING,
    val xpAwarded: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


