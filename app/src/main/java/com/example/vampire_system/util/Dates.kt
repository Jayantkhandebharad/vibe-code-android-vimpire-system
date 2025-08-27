package com.example.vampire_system.util

import java.time.*
import java.time.format.DateTimeFormatter

object Dates {
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun todayLocal(zoneId: ZoneId = ZoneId.systemDefault()): String =
        LocalDate.now(zoneId).format(fmt)

    fun ymd(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate().format(fmt)

    fun nowEpoch(): Long = System.currentTimeMillis()
}


