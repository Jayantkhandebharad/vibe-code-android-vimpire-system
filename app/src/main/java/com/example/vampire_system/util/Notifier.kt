package com.example.vampire_system.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.vampire_system.R

object Notifier {
    const val CH_DAILY = "v_daily"
    const val CH_BACKUP = "v_backup"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val daily = NotificationChannel(CH_DAILY, "Daily", NotificationManager.IMPORTANCE_DEFAULT)
            val backup = NotificationChannel(CH_BACKUP, "Backups", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(daily)
            nm.createNotificationChannel(backup)
        }
    }

    fun dailyReady(context: Context, text: String = "Today’s quests are ready") {
        val n = NotificationCompat.Builder(context, CH_DAILY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Vampire Mode+")
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1001, n)
    }

    fun backupOk(context: Context, text: String = "Backup completed") {
        val n = NotificationCompat.Builder(context, CH_BACKUP)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Vampire Mode+")
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(2001, n)
    }

    fun backupFail(context: Context, text: String = "Backup failed") {
        val n = NotificationCompat.Builder(context, CH_BACKUP)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Vampire Mode+")
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(2002, n)
    }
}


