package com.example.vampire_system.ui.dashboard

import androidx.lifecycle.*
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.model.Xp
import com.example.vampire_system.domain.repo.LevelRepo
import com.example.vampire_system.util.Dates
import kotlinx.coroutines.launch

data class DashboardState(
    val level: Int = 1,
    val xpInLevel: Int = 0,
    val xpRequired: Int = Xp.xpForLevel(1),
    val pendingQuests: Int = 0,
    val doneQuests: Int = 0
)

class DashboardViewModel(private val db: AppDatabase) : ViewModel() {
    private val _state = MutableLiveData(DashboardState())
    val state: LiveData<DashboardState> = _state

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        val lr = LevelRepo(db).getCurrent()
        val date = Dates.todayLocal()
        
        // DEBUG: Test XP calculation
        val currentLevel = lr.levelId
        val xpNeeded = com.example.vampire_system.data.model.Xp.xpForLevel(currentLevel)
        println("DEBUG Dashboard: Level $currentLevel needs $xpNeeded XP")

        val cursor = db.openHelper.readableDatabase.query(
            "SELECT SUM(CASE WHEN status='PENDING' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN status='DONE' THEN 1 ELSE 0 END) " +
            "FROM quest_instances WHERE date=?",
            arrayOf(date)
        )
        var pending = 0; var done = 0
        cursor.use { if (it.moveToFirst()) { pending = it.getInt(0); done = it.getInt(1) } }

        // Use ONLY LevelProgress (already updated by XpService)
        val xpDisplay = lr.xpInLevel

        _state.postValue(
            DashboardState(
                level = lr.levelId,
                xpInLevel = xpDisplay,
                xpRequired = com.example.vampire_system.data.model.Xp.xpForLevel(lr.levelId),
                pendingQuests = pending,
                doneQuests = done
            )
        )
    }

    class Factory(private val db: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = DashboardViewModel(db) as T
    }
}