package com.example.vampire_system

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.PlanSeeder

class VampireApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Apply dynamic colors
        com.google.android.material.color.DynamicColors.applyToActivitiesIfAvailable(this)
        
        // Setup Coil with video frame support
        val loader = coil.ImageLoader.Builder(this)
            .components { add(coil.decode.VideoFrameDecoder.Factory()) }
            .crossfade(true)
            .bitmapFactoryMaxParallelism(2) // avoid CPU spikes
            .respectCacheHeaders(false)
            .build()
        coil.Coil.setImageLoader(loader)
        
        // Setup logging
        val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            timber.log.Timber.plant(timber.log.Timber.DebugTree())
            android.os.StrictMode.setThreadPolicy(
                android.os.StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            )
            android.os.StrictMode.setVmPolicy(
                android.os.StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build()
            )
        } else {
            timber.log.Timber.plant(timber.log.Timber.DebugTree()) // replace with prod tree when you add crashlytics
        }
        
        val db = AppDatabase.get(this)
        CoroutineScope(Dispatchers.IO).launch {
            PlanSeeder.seedIfEmpty(db)
            com.example.vampire_system.data.seed.MilestoneSeeder.seedIfEmpty(db)
            
            // Rebuild search index if empty (lazy initialization)
            try {
                // Just rebuild the index on app start for now
                com.example.vampire_system.domain.search.SearchIndexer(db).rebuild()
            } catch (e: Exception) {
                // Ignore indexing errors on app start
            }
        }

        // Notification channels
        com.example.vampire_system.util.Notifier.ensureChannels(this)

        // Schedule workers
        CoroutineScope(Dispatchers.IO).launch {
            val settings = com.example.vampire_system.domain.repo.SettingsRepo(db).get()
            val resetHour = settings?.resetHour ?: 5
            val zoneId = settings?.timezone ?: "Asia/Kolkata"

            com.example.vampire_system.features.today.RolloverWorker.ensureScheduled(this@VampireApp, resetHour, zoneId)
            com.example.vampire_system.features.backup.IncrementalBackupWorker.scheduleNext(this@VampireApp, resetHour, zoneId)
            com.example.vampire_system.features.backup.WeeklyFullBackupWorker.scheduleNext(this@VampireApp, zoneId)
        }
    }
}


