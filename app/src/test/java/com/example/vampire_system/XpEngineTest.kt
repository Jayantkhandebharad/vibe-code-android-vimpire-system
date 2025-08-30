package com.example.vampire_system

import com.example.vampire_system.data.db.*
import com.example.vampire_system.data.model.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class XpEngineTest {
    @Test fun award_flat_and_ledger() = runBlocking {
        val db = TestDb.create()
        // seed ability
        db.abilityDao().upsert(AbilityEntity(
            id = "gym", name = "Gym", group = AbilityGroup.FITNESS,
            unlockLevel = 1, unit = null, xpRule = XpRule.Flat(50), dailyCapXp = 100,
            seriousness = null, evidenceKinds = setOf(EvidenceKind.PHOTO)
        ))
        // seed quest
        val qi = QuestInstanceEntity(
            id = "QI_2025-08-27_gym", date = "2025-08-27", levelId = 1,
            templateId = null, abilityId = "gym",
            status = QuestStatus.PENDING, xpAwarded = 0,
            createdAt = 0, updatedAt = 0
        )
        db.questInstanceDao().upsert(qi)
        val xp = com.example.vampire_system.domain.engine.XpEngine(db, com.example.vampire_system.domain.repo.LevelRepo(db))
        // no evidence -> half XP for flat, per spec
        val got = xp.completeQuest(qi.id, 1.0)
        assertThat(got).isEqualTo(25)
        val ledger = db.xpLedgerDao().byDate("2025-08-27")
        assertThat(ledger).isNotEmpty()
        assertThat(ledger.first().type).isEqualTo(LedgerType.AWARD)
    }

    @Test fun revert_writes_adjustment() = runBlocking {
        val db = TestDb.create()
        // simple done row
        db.questInstanceDao().upsert(QuestInstanceEntity(
            id="q1", date="2025-08-27", levelId=1, templateId=null, abilityId="ship",
            status=QuestStatus.DONE, xpAwarded=40, createdAt=0, updatedAt=0))
        val xp = com.example.vampire_system.domain.engine.XpEngine(db, com.example.vampire_system.domain.repo.LevelRepo(db))
        xp.revertQuestToPending("q1")
        val adj = db.xpLedgerDao().byDate("2025-08-27").first()
        assertThat(adj.type).isEqualTo(LedgerType.ADJUSTMENT)
        assertThat(adj.deltaXp).isEqualTo(-40)
    }
}
