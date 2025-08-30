package com.example.vampire_system.features.today

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.domain.engine.QuestEngine
import com.example.vampire_system.domain.engine.XpEngine
import com.example.vampire_system.domain.repo.LevelRepo
import com.example.vampire_system.domain.repo.SettingsRepo
import com.example.vampire_system.util.Notifier
import com.example.vampire_system.util.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RolloverWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = AppDatabase.get(applicationContext)
        val settings = SettingsRepo(db).get()
        val zone = Schedule.zone(settings?.timezone)
        val resetHour = settings?.resetHour ?: 5

        val closedDate = Schedule.yesterday(zone) // day that just ended at ~05:00
        val xp = XpEngine(db, LevelRepo(db))
        xp.finalizeDay(closedDate)

        // Prepare today’s quests
        val quest = QuestEngine(db, LevelRepo(db))
        quest.generateDaily(Schedule.today(zone))

        Notifier.dailyReady(applicationContext, "Core gate checked; quests generated.")

        // schedule next day's rollover
        scheduleNext(applicationContext, resetHour, zone.id)

        Result.success()
    }

    companion object {
        private const val UNIQUE = "daily_rollover"

        fun scheduleNext(context: Context, resetHour: Int, zoneId: String) {
            val delayMs = Schedule.delayToNext(resetHour, 0, Schedule.zone(zoneId))
            val (delay, unit) = Schedule.millisToDuration(delayMs)
            val req = OneTimeWorkRequestBuilder<RolloverWorker>()
                .setInitialDelay(delay, unit)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE, ExistingWorkPolicy.REPLACE, req)
        }

        fun ensureScheduled(context: Context, resetHour: Int, zoneId: String) {
            scheduleNext(context, resetHour, zoneId)
        }
    }
}


