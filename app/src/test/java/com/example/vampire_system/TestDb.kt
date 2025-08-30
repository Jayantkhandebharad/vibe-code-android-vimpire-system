package com.example.vampire_system
import androidx.room.Room
import org.robolectric.RuntimeEnvironment
import com.example.vampire_system.data.db.AppDatabase

object TestDb {
    fun create(): AppDatabase {
        val ctx = RuntimeEnvironment.getApplication()
        return Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}
