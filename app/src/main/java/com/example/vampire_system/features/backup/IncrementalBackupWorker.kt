package com.example.vampire_system.features.backup

import android.content.Context
import androidx.work.*
import com.example.vampire_system.domain.repo.SettingsRepo
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.util.Notifier
import com.example.vampire_system.util.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IncrementalBackupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = AppDatabase.get(applicationContext)
        val uriStr = BackupPrefs.getFolder(applicationContext)
        if (uriStr == null) return@withContext Result.success()
        val ok = BackupManager(applicationContext, db).backupIncremental(android.net.Uri.parse(uriStr))
        if (ok) Notifier.backupOk(applicationContext, "Incremental backup completed")
        else Notifier.backupFail(applicationContext, "Incremental backup failed")
        val settings = SettingsRepo(db).get()
        scheduleNext(applicationContext, (settings?.resetHour ?: 5), settings?.timezone ?: "Asia/Kolkata")
        Result.success()
    }

    companion object {
        private const val UNIQUE = "incremental_backup"
        fun scheduleNext(context: Context, resetHour: Int, zoneId: String) {
            val delayMs = Schedule.delayToNext(resetHour, 15, Schedule.zone(zoneId))
            val (delay, unit) = Schedule.millisToDuration(delayMs)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val req = OneTimeWorkRequestBuilder<IncrementalBackupWorker>()
                .setInitialDelay(delay, unit)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE, ExistingWorkPolicy.REPLACE, req)
        }
    }
}


