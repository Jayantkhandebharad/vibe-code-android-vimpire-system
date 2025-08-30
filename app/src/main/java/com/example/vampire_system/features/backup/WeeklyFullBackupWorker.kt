package com.example.vampire_system.features.backup

import android.content.Context
import androidx.work.*
import com.example.vampire_system.domain.repo.SettingsRepo
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.util.Notifier
import com.example.vampire_system.util.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeeklyFullBackupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = AppDatabase.get(applicationContext)
        val uriStr = BackupPrefs.getFolder(applicationContext)
        if (uriStr != null) {
            val encrypt = BackupPrefs.getEncrypt(applicationContext)
            val ok = BackupManager(applicationContext, db).backupFull(android.net.Uri.parse(uriStr), passphrase = null)
            if (ok) Notifier.backupOk(applicationContext, "Weekly full backup completed")
            else Notifier.backupFail(applicationContext, "Weekly full backup failed")
        }
        scheduleNext(applicationContext, com.example.vampire_system.util.Schedule.zone(
            SettingsRepo(db).get()?.timezone ?: "Asia/Kolkata"
        ).id)
        Result.success()
    }

    companion object {
        private const val UNIQUE = "weekly_full_backup"
        fun scheduleNext(context: Context, zoneId: String) {
            val delayMs = Schedule.delayToNextMonday(5, 30, Schedule.zone(zoneId))
            val (delay, unit) = Schedule.millisToDuration(delayMs)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val req = OneTimeWorkRequestBuilder<WeeklyFullBackupWorker>()
                .setInitialDelay(delay, unit)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE, ExistingWorkPolicy.REPLACE, req)
        }
    }
}


