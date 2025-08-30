package com.example.vampire_system.ui.home

import androidx.lifecycle.*
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.QuestInstanceEntity
import com.example.vampire_system.data.model.QuestStatus
import com.example.vampire_system.domain.engine.QuestEngine
import com.example.vampire_system.domain.engine.XpEngine
import com.example.vampire_system.domain.repo.LevelRepo
import com.example.vampire_system.util.Dates
import kotlinx.coroutines.launch

class HomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val date = Dates.todayLocal()
    val quests: LiveData<List<QuestInstanceEntity>> =
        db.questInstanceDao().pendingForDate(date).asLiveData()

    val levelId = MutableLiveData<Int>()

    init {
        viewModelScope.launch {
            val lr = LevelRepo(db)
            levelId.postValue(lr.getCurrent().levelId)
            // ensure today’s quests exist
            QuestEngine(db, lr).generateDaily(date)
        }
    }

    fun toggleDone(item: QuestInstanceEntity) = viewModelScope.launch {
        val updated = item.copy(
            status = if (item.status == QuestStatus.DONE) QuestStatus.PENDING else QuestStatus.DONE,
            updatedAt = System.currentTimeMillis()
        )
        db.questInstanceDao().upsert(updated)
    }

    fun quickFoundationXp(abilityId: String) = viewModelScope.launch {
        val today = com.example.vampire_system.util.Dates.todayLocal()
        val lr = com.example.vampire_system.domain.repo.LevelRepo(db)
        val qe = com.example.vampire_system.domain.engine.QuestEngine(db, lr)
        val xpEngine = com.example.vampire_system.domain.engine.XpEngine(db, lr)

        // 1) Make sure today's foundation quests exist
        qe.ensureDailyFoundations(today)

        // 2) Resolve deterministic quest id
        val qiId = "QI_${today}_${abilityId}"
        val qi = db.questInstanceDao().byId(qiId) ?: return@launch

        // 3) If already not pending, no-op (prevents double logging)
        if (qi.status != com.example.vampire_system.data.model.QuestStatus.PENDING) return@launch

        // 4) Use the SAME engine as "Mark done" (single source of truth)
        val amount = when (abilityId) {
            "pushups"    -> 20.0   // reps
            "reading"    -> 10.0   // pages
            "notes"      -> 250.0  // words
            "meditation" -> 10.0   // minutes
            "mobility"   -> 10.0   // minutes
            else         -> 1.0
        }
        xpEngine.completeQuest(qi.id, amount)

        // 5) Refresh level label
        levelId.postValue(lr.getCurrent().levelId)
    }

    fun runRolloverNow() = viewModelScope.launch {
        val lr = LevelRepo(db)
        // finalize yesterday and regenerate today
        val today = Dates.todayLocal()
        QuestEngine(db, lr).generateDaily(today)
    }

    class Factory(private val db: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(db) as T
    }
}