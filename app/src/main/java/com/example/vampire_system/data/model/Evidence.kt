package com.example.vampire_system.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Evidence(
    val id: String,
    val questInstanceId: String,
    val kind: EvidenceKind,
    val uriOrText: String,            // SAF URI, http link, or text note
    val meta: Map<String, String> = emptyMap(), // e.g., rpe, minutes, hr zone
    val sha256: String? = null,       // for media files
    val createdAt: Long = System.currentTimeMillis()
)


