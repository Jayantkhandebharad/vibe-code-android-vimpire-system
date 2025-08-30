package com.example.vampire_system.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

object Schedule {
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun zone(tz: String?): ZoneId = try { ZoneId.of(tz ?: "Asia/Kolkata") } catch (_: Exception) { ZoneId.of("Asia/Kolkata") }

    fun delayToNext(hour: Int, minute: Int, zone: ZoneId): Long {
        val now = ZonedDateTime.now(zone)
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return java.time.Duration.between(now, next).toMillis()
    }

    fun delayToNextMonday(hour: Int, minute: Int, zone: ZoneId): Long {
        val now = ZonedDateTime.now(zone)
        var next = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
            .withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusWeeks(1)
        return java.time.Duration.between(now, next).toMillis()
    }

    fun yesterday(zone: ZoneId): String = LocalDate.now(zone).minusDays(1).format(fmt)
    fun today(zone: ZoneId): String = LocalDate.now(zone).format(fmt)

    fun millisToDuration(ms: Long): Pair<Long, TimeUnit> = ms to TimeUnit.MILLISECONDS
}


