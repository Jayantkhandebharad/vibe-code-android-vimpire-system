package com.example.vampire_system.domain.xp

import com.example.vampire_system.data.db.*
import com.example.vampire_system.domain.repo.LevelRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class XpService(private val db: AppDatabase) {
    suspend fun awardForQuest(
        date: String, type: LedgerType, delta: Int,
        qiId: String, abilityId: String?, note: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val key = EventKey.forQuest(date, type.name, qiId)
        val row = XpLedgerEntity(
            id = UUID.randomUUID().toString(),
            date = date, createdAt = System.currentTimeMillis(),
            type = type, deltaXp = delta,
            abilityId = abilityId, questInstanceId = qiId,
            note = note, eventKey = key
        )
        val inserted = db.xpLedgerDao().insertIgnore(row) != -1L
        if (inserted && type == LedgerType.AWARD && delta > 0) {
            val levelRepo = LevelRepo(db)
            val (newLevel, leveled) = levelRepo.addXp(delta)
            if (leveled) {
                val lvlKey = EventKey.forNote(date, "LEVEL_UP", "level_$newLevel")
                db.xpLedgerDao().insertIgnore(
                    XpLedgerEntity(
                        id = UUID.randomUUID().toString(),
                        date = date, createdAt = System.currentTimeMillis(),
                        type = LedgerType.LEVEL_UP, deltaXp = 0,
                        abilityId = null, questInstanceId = null,
                        note = "Level up to $newLevel", eventKey = lvlKey
                    )
                )
            }
        }
        inserted
    }

    suspend fun awardForNote(
        date: String, type: LedgerType, delta: Int, note: String
    ): Boolean = withContext(Dispatchers.IO) {
        val key = EventKey.forNote(date, type.name, note)
        val row = XpLedgerEntity(
            id = UUID.randomUUID().toString(),
            date = date, createdAt = System.currentTimeMillis(),
            type = type, deltaXp = delta,
            abilityId = null, questInstanceId = null,
            note = note, eventKey = key
        )
        val inserted = db.xpLedgerDao().insertIgnore(row) != -1L
        if (inserted && type == LedgerType.AWARD && delta > 0) {
            val levelRepo = LevelRepo(db)
            val (newLevel, leveled) = levelRepo.addXp(delta)
            if (leveled) {
                val lvlKey = EventKey.forNote(date, "LEVEL_UP", "level_$newLevel")
                db.xpLedgerDao().insertIgnore(
                    XpLedgerEntity(
                        id = UUID.randomUUID().toString(),
                        date = date, createdAt = System.currentTimeMillis(),
                        type = LedgerType.LEVEL_UP, deltaXp = 0,
                        abilityId = null, questInstanceId = null,
                        note = "Level up to $newLevel", eventKey = lvlKey
                    )
                )
            }
        }
        inserted
    }
}
