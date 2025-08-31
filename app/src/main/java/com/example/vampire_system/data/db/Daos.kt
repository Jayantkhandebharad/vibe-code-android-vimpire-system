package com.example.vampire_system.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AbilityDao {
    @Query("SELECT * FROM abilities")
    fun getAll(): Flow<List<AbilityEntity>>
    @Query("SELECT * FROM abilities")
    suspend fun getAllOnce(): List<AbilityEntity>
    @Query("SELECT * FROM abilities WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): AbilityEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AbilityEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AbilityEntity>)
    @Query("DELETE FROM abilities")
    suspend fun deleteAll()
}

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels ORDER BY id")
    fun getAll(): Flow<List<LevelEntity>>
    @Query("SELECT * FROM levels WHERE id = :id")
    suspend fun byId(id: Int): LevelEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<LevelEntity>)
    @Query("DELETE FROM levels")
    suspend fun deleteAll()
}

@Dao
interface LevelTaskDao {
    @Query("SELECT * FROM level_tasks WHERE levelId = :levelId")
    suspend fun forLevel(levelId: Int): List<LevelTaskEntity>
    @Query("SELECT * FROM level_tasks WHERE levelId = :level ORDER BY id")
    suspend fun forLevelOnce(level: Int): List<LevelTaskEntity>
    @Query("SELECT * FROM level_tasks")
    suspend fun getAllOnce(): List<LevelTaskEntity>
    @Query("SELECT * FROM level_tasks WHERE id = :id")
    suspend fun byId(id: String): LevelTaskEntity?
    @Query("DELETE FROM level_tasks WHERE id = :id")
    suspend fun deleteById(id: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: LevelTaskEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<LevelTaskEntity>)
    @Query("DELETE FROM level_tasks")
    suspend fun deleteAll()
}

@Dao
interface QuestTemplateDao {
    @Query("SELECT * FROM quest_templates")
    suspend fun all(): List<QuestTemplateEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<QuestTemplateEntity>)
}

@Dao
interface QuestInstanceDao {
    @Query("SELECT * FROM quest_instances WHERE date = :date ORDER BY createdAt")
    fun forDate(date: String): Flow<List<QuestInstanceEntity>>
    @Query("SELECT * FROM quest_instances WHERE date = :date AND status = 'PENDING' ORDER BY createdAt")
    fun pendingForDate(date: String): Flow<List<QuestInstanceEntity>>
    @Query("SELECT * FROM quest_instances WHERE date = :date")
    suspend fun listForDate(date: String): List<QuestInstanceEntity>
    @Query("DELETE FROM quest_instances WHERE date = :date AND status = 'PENDING' AND id NOT IN (:keepIds)")
    suspend fun deletePendingNotIn(date: String, keepIds: List<String>)
    @Query("SELECT * FROM quest_instances WHERE id = :id")
    suspend fun byId(id: String): QuestInstanceEntity?
    @Query("SELECT * FROM quest_instances WHERE id = :id")
    suspend fun byIdOnce(id: String): QuestInstanceEntity?
    @Query("SELECT * FROM quest_instances")
    suspend fun rawAll(): List<QuestInstanceEntity>
    @Query("SELECT * FROM quest_instances WHERE status = 'DONE'")
    suspend fun getCompleted(): List<QuestInstanceEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: QuestInstanceEntity)
    @Update suspend fun update(item: QuestInstanceEntity)
}

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence WHERE questInstanceId = :qi ORDER BY createdAt")
    fun forQuest(qi: String): Flow<List<EvidenceEntity>>
    @Query("SELECT * FROM evidence WHERE questInstanceId = :qi ORDER BY createdAt DESC")
    suspend fun forQuestOnce(qi: String): List<EvidenceEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: EvidenceEntity)
}

@Dao
interface DayDao {
    @Query("SELECT * FROM day_summaries WHERE date = :date")
    suspend fun byDate(date: String): DaySummaryEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DaySummaryEntity)
}

@Dao
interface StreakDao {
    @Query("SELECT * FROM streak_state WHERE id = 1")
    suspend fun get(): StreakStateEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: StreakStateEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun get(): SettingsEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SettingsEntity)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun get(): UserProfileEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: UserProfileEntity)
}


@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress WHERE id = 1")
    suspend fun get(): LevelProgressEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: LevelProgressEntity)
}


@Dao
interface XpLedgerDao {
    @Query("SELECT * FROM xp_ledger WHERE date BETWEEN :from AND :to ORDER BY date DESC, createdAt DESC")
    suspend fun range(from: String, to: String): List<XpLedgerEntity>

    @Query("SELECT * FROM xp_ledger WHERE date = :date ORDER BY createdAt DESC")
    suspend fun byDate(date: String): List<XpLedgerEntity>

    @Query("SELECT * FROM xp_ledger ORDER BY date DESC, createdAt DESC")
    fun pagingAll(): androidx.paging.PagingSource<Int, XpLedgerEntity>

    @Query("SELECT * FROM xp_ledger ORDER BY date DESC, createdAt DESC")
    suspend fun getAllOnce(): List<XpLedgerEntity>

    @Query("SELECT * FROM xp_ledger WHERE type = :type ORDER BY date DESC, createdAt DESC")
    fun pagingByType(type: LedgerType): androidx.paging.PagingSource<Int, XpLedgerEntity>

    @Query("SELECT * FROM xp_ledger WHERE date BETWEEN :from AND :to ORDER BY date DESC, createdAt DESC")
    fun pagingRange(from: String, to: String): androidx.paging.PagingSource<Int, XpLedgerEntity>

    // keep existing REPLACE for bulk imports if you use it
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: XpLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<XpLedgerEntity>)

    // NEW: use this for live awards so dup keys are ignored instead of replaced
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(item: XpLedgerEntity): Long
}

@Dao
interface StageDao {
    @Query("SELECT * FROM stage_items WHERE questInstanceId = :qi ORDER BY id")
    suspend fun forQuest(qi: String): List<StageItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StageItemEntity>)

    @Query("UPDATE stage_items SET done = :done, completedAt = :ts WHERE id = :id")
    suspend fun setDone(id: String, done: Boolean, ts: Long?)
}

@Dao
interface LevelMilestoneDao {
    @Query("SELECT * FROM level_milestones ORDER BY levelId")
    suspend fun all(): List<LevelMilestoneEntity>

    @Query("SELECT * FROM level_milestones WHERE levelId = :level LIMIT 1")
    suspend fun byLevel(level: Int): LevelMilestoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: LevelMilestoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<LevelMilestoneEntity>)
}

@Dao
interface SearchIndexDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SearchIndexEntity>)

    @Query("DELETE FROM search_index")
    suspend fun clear()
}

@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_searches ORDER BY orderIdx ASC")
    suspend fun all(): List<SavedSearchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SavedSearchEntity)

    @Query("DELETE FROM saved_searches WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE saved_searches SET orderIdx = :orderIdx WHERE id = :id")
    suspend fun setOrder(id: String, orderIdx: Int)
}

