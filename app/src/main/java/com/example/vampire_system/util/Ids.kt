package com.example.vampire_system.util

import java.util.UUID

object Ids {
    fun newId(prefix: String? = null): String =
        (prefix?.let { "${it}_" } ?: "") + UUID.randomUUID().toString()
}


