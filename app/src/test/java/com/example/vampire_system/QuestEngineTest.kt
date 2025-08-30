package com.example.vampire_system
import com.example.vampire_system.data.db.*
import com.example.vampire_system.data.model.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class QuestEngineTest {
    @Test fun deterministic_daily_no_duplicates() = runBlocking {
        val db = TestDb.create()
        // seed minimal abilities (foundations ids used by engine)
        listOf("hydrate","eat_protein","steps").forEachIndexed { i, id ->
            db.abilityDao().upsert(AbilityEntity(id, id, AbilityGroup.FOUNDATIONS, null, XpRule.Flat(5+i), null, null, emptySet(), 1))
        }
        // seed one unlockable
        db.abilityDao().upsert(AbilityEntity("gym","Gym",AbilityGroup.FITNESS,null,XpRule.Flat(50),null,null, emptySet(), 1))
        val qe = com.example.vampire_system.domain.engine.QuestEngine(db, com.example.vampire_system.domain.repo.LevelRepo(db))
        qe.generateDaily("2025-08-27")
        val first = db.questInstanceDao().listForDate("2025-08-27")
        qe.generateDaily("2025-08-27")
        val second = db.questInstanceDao().listForDate("2025-08-27")
        assertThat(second.map { it.id }.toSet()).isEqualTo(first.map { it.id }.toSet())
    }
}
