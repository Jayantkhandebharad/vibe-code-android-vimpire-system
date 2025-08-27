package com.example.vampire_system.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Level(
    val id: Int,                      // 1..100
    val xpRequired: Int,              // from decade sequence
    val title: String? = null,
    val boss: Boolean = false,
    val acceptance: List<String> = emptyList(),   // textual acceptance criteria
    val unlocks: List<String> = emptyList()       // ability ids unlocked when cleared
)

@Serializable
data class LevelTask(
    val id: String,                   // stable id like "L12_SFT_V1"
    val levelId: Int,
    val abilityId: String? = null,    // optional: link to an ability
    val spec: String,                 // “Do exactly: …”
    val acceptance: List<String>      // “Provide: …”
)


