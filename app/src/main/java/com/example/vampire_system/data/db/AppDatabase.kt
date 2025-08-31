package com.example.vampire_system.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        AbilityEntity::class, LevelEntity::class, LevelTaskEntity::class,
        QuestTemplateEntity::class, QuestInstanceEntity::class,
        EvidenceEntity::class, DaySummaryEntity::class,
        StreakStateEntity::class, SettingsEntity::class, UserProfileEntity::class,
        LevelProgressEntity::class,
        XpLedgerEntity::class, StageItemEntity::class,
        LevelMilestoneEntity::class, SearchIndexEntity::class, SavedSearchEntity::class
    ],
    version = 12,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun abilityDao(): AbilityDao
    abstract fun levelDao(): LevelDao
    abstract fun levelTaskDao(): LevelTaskDao
    abstract fun questTemplateDao(): QuestTemplateDao
    abstract fun questInstanceDao(): QuestInstanceDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun dayDao(): DayDao
    abstract fun streakDao(): StreakDao
    abstract fun settingsDao(): SettingsDao
    abstract fun profileDao(): ProfileDao
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun xpLedgerDao(): XpLedgerDao
    abstract fun stageDao(): StageDao
    abstract fun levelMilestoneDao(): LevelMilestoneDao
    abstract fun searchIndexDao(): SearchIndexDao
    abstract fun savedSearchDao(): SavedSearchDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS level_progress (
                        id INTEGER NOT NULL PRIMARY KEY,
                        levelId INTEGER NOT NULL,
                        xpInLevel INTEGER NOT NULL,
                        startedAtDate TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS xp_ledger (
                id TEXT NOT NULL PRIMARY KEY,
                date TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                type TEXT NOT NULL,
                deltaXp INTEGER NOT NULL,
                abilityId TEXT,
                questInstanceId TEXT,
                note TEXT
            )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_ledger_date ON xp_ledger(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_ledger_createdAt ON xp_ledger(createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_ledger_ability ON xp_ledger(abilityId)")

                db.execSQL("""
            CREATE TABLE IF NOT EXISTS stage_items (
                id TEXT NOT NULL PRIMARY KEY,
                questInstanceId TEXT NOT NULL,
                label TEXT NOT NULL,
                done INTEGER NOT NULL,
                completedAt INTEGER
            )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_stage_qi ON stage_items(questInstanceId)")
            }
        }
        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS level_milestones (
                levelId INTEGER NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                subtitle TEXT,
                isBoss INTEGER NOT NULL,
                colorHex TEXT
            )
                """.trimIndent())
            }
        }
        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_evidence_qi_created ON evidence(questInstanceId, createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_days_date ON day_summaries(date)")
            }
        }
        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS search_index (
                        sid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT,
                        kind TEXT NOT NULL,
                        abilityId TEXT,
                        questInstanceId TEXT,
                        title TEXT NOT NULL,
                        snippet TEXT,
                        text TEXT NOT NULL
                    )
                """.trimIndent())

                // FTS4 (unicode61 tokenizer); content = search_index; link by sid
                db.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS search_index_fts
                    USING fts4(text, tokenize=unicode61, content=search_index, content_rowid=sid)
                """.trimIndent())

                // Triggers to keep FTS in sync
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS search_index_ai AFTER INSERT ON search_index BEGIN
                      INSERT INTO search_index_fts(rowid, text) VALUES (new.sid, new.text);
                    END;
                """.trimIndent())
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS search_index_ad AFTER DELETE ON search_index BEGIN
                      INSERT INTO search_index_fts(search_index_fts, rowid, text) VALUES('delete', old.sid, old.text);
                    END;
                """.trimIndent())
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS search_index_au AFTER UPDATE ON search_index BEGIN
                      INSERT INTO search_index_fts(search_index_fts, rowid, text) VALUES('delete', old.sid, old.text);
                      INSERT INTO search_index_fts(rowid, text) VALUES (new.sid, new.text);
                    END;
                """.trimIndent())

                // Helpful indexes
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_search_kind ON search_index(kind)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_search_date ON search_index(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_search_ability ON search_index(abilityId)")
            }
        }
        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS saved_searches(
                      id TEXT NOT NULL PRIMARY KEY,
                      title TEXT NOT NULL,
                      query TEXT,
                      kind TEXT,
                      ability TEXT,
                      from TEXT,
                      to TEXT,
                      doneOnly INTEGER NOT NULL,
                      hasEvidence INTEGER NOT NULL,
                      orderIdx INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_saved_order ON saved_searches(orderIdx)")
            }
        }

        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Step 1: Add eventKey column as nullable first
                db.execSQL("ALTER TABLE xp_ledger ADD COLUMN eventKey TEXT")

                // Step 2: Backfill a deterministic key for all existing rows
                //  - if questInstanceId present → qi:<qi>:d:<date>:t:<type>
                //  - else if note present → note:<hash(note)>:d:<date>:t:<type>
                //  - else → legacy:<id>
                db.execSQL("""
                    UPDATE xp_ledger
                    SET eventKey = CASE
                        WHEN questInstanceId IS NOT NULL THEN 'qi:' || questInstanceId || ':d:' || date || ':t:' || type
                        WHEN note IS NOT NULL THEN 'note:' || substr(lower(trim(note)),1,80) || ':d:' || date || ':t:' || type
                        ELSE 'legacy:' || id
                    END
                """.trimIndent())

                // Step 3: Delete duplicates keeping the oldest row per eventKey
                db.execSQL("""
                    DELETE FROM xp_ledger
                    WHERE rowid NOT IN (SELECT MIN(rowid) FROM xp_ledger GROUP BY eventKey)
                """.trimIndent())

                // Step 4: Create new table with NOT NULL constraint for eventKey
                db.execSQL("""
                    CREATE TABLE xp_ledger_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        date TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        deltaXp INTEGER NOT NULL,
                        abilityId TEXT,
                        questInstanceId TEXT,
                        note TEXT,
                        eventKey TEXT NOT NULL
                    )
                """.trimIndent())

                // Step 5: Copy data from old table to new table
                db.execSQL("""
                    INSERT INTO xp_ledger_new (id, date, createdAt, type, deltaXp, abilityId, questInstanceId, note, eventKey)
                    SELECT id, date, createdAt, type, deltaXp, abilityId, questInstanceId, note, eventKey
                    FROM xp_ledger
                """.trimIndent())

                // Step 6: Drop old table and rename new table
                db.execSQL("DROP TABLE xp_ledger")
                db.execSQL("ALTER TABLE xp_ledger_new RENAME TO xp_ledger")

                // Step 7: Recreate all indices with correct names to match entity definition
                db.execSQL("CREATE INDEX IF NOT EXISTS index_xp_ledger_date ON xp_ledger(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_xp_ledger_createdAt ON xp_ledger(createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_xp_ledger_abilityId ON xp_ledger(abilityId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_xp_ledger_eventKey ON xp_ledger(eventKey)")
            }
        }

        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Fix databases that already have version 8 with nullable eventKey
                // Check if eventKey column exists and has correct constraints
                
                // First, ensure all eventKey values are populated (in case some are NULL)
                db.execSQL("""
                    UPDATE xp_ledger
                    SET eventKey = CASE
                        WHEN eventKey IS NULL AND questInstanceId IS NOT NULL THEN 'qi:' || questInstanceId || ':d:' || date || ':t:' || type
                        WHEN eventKey IS NULL AND note IS NOT NULL THEN 'note:' || substr(lower(trim(note)),1,80) || ':d:' || date || ':t:' || type
                        WHEN eventKey IS NULL THEN 'legacy:' || id
                        ELSE eventKey
                    END
                    WHERE eventKey IS NULL OR eventKey = ''
                """.trimIndent())

                // Create new table with correct schema
                db.execSQL("""
                    CREATE TABLE xp_ledger_fixed (
                        id TEXT NOT NULL PRIMARY KEY,
                        date TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        deltaXp INTEGER NOT NULL,
                        abilityId TEXT,
                        questInstanceId TEXT,
                        note TEXT,
                        eventKey TEXT NOT NULL
                    )
                """.trimIndent())

                // Copy data from old table to new table
                db.execSQL("""
                    INSERT INTO xp_ledger_fixed (id, date, createdAt, type, deltaXp, abilityId, questInstanceId, note, eventKey)
                    SELECT id, date, createdAt, type, deltaXp, abilityId, questInstanceId, note, eventKey
                    FROM xp_ledger
                """.trimIndent())

                // Drop old table and rename new table
                db.execSQL("DROP TABLE xp_ledger")
                db.execSQL("ALTER TABLE xp_ledger_fixed RENAME TO xp_ledger")

                // Recreate all indices with correct names to match entity definition
                db.execSQL("CREATE INDEX IF NOT EXISTS index_xp_ledger_date ON xp_ledger(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_xp_ledger_createdAt ON xp_ledger(createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_xp_ledger_abilityId ON xp_ledger(abilityId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_xp_ledger_eventKey ON xp_ledger(eventKey)")
            }
        }

        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Fix multiple table indices to match entity definitions
                
                // Fix search_index table indices
                db.execSQL("DROP INDEX IF EXISTS idx_search_date")
                db.execSQL("DROP INDEX IF EXISTS idx_search_kind") 
                db.execSQL("DROP INDEX IF EXISTS idx_search_ability")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_date ON search_index(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_kind ON search_index(kind)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_abilityId ON search_index(abilityId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_questInstanceId ON search_index(questInstanceId)")
                
                // Fix stage_items table index
                db.execSQL("DROP INDEX IF EXISTS idx_stage_qi")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stage_items_questInstanceId ON stage_items(questInstanceId)")
                
                // Fix saved_searches table index
                db.execSQL("DROP INDEX IF EXISTS idx_saved_order")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_searches_orderIdx ON saved_searches(orderIdx)")
            }
        }

        private val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add category column to level_tasks table with default value 'KNOWLEDGE'
                db.execSQL("ALTER TABLE level_tasks ADD COLUMN category TEXT NOT NULL DEFAULT 'KNOWLEDGE'")
                
                // Ensure any remaining old indices are cleaned up and correct ones exist
                // This handles cases where users might have skipped some migrations
                db.execSQL("DROP INDEX IF EXISTS idx_search_date")
                db.execSQL("DROP INDEX IF EXISTS idx_search_kind") 
                db.execSQL("DROP INDEX IF EXISTS idx_search_ability")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_date ON search_index(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_kind ON search_index(kind)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_abilityId ON search_index(abilityId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_questInstanceId ON search_index(questInstanceId)")
            }
        }

        private val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add xpReward column to level_tasks table with default value 10.0
                db.execSQL("ALTER TABLE level_tasks ADD COLUMN xpReward REAL NOT NULL DEFAULT 10.0")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vampire.db"
                )
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Ensure FTS virtual table and triggers exist for fresh installs (not just migrations)
                        // Some SQLite builds may not support content_rowid param; create a simple FTS table on 'text'
                        db.execSQL(
                            """
                                CREATE VIRTUAL TABLE IF NOT EXISTS search_index_fts
                                USING fts4(text, tokenize=unicode61)
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                                CREATE TRIGGER IF NOT EXISTS search_index_ai AFTER INSERT ON search_index BEGIN
                                  INSERT INTO search_index_fts(text) VALUES (new.text);
                                END;
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                                CREATE TRIGGER IF NOT EXISTS search_index_ad AFTER DELETE ON search_index BEGIN
                                  DELETE FROM search_index_fts WHERE text = old.text;
                                END;
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                                CREATE TRIGGER IF NOT EXISTS search_index_au AFTER UPDATE ON search_index BEGIN
                                  DELETE FROM search_index_fts WHERE text = old.text;
                                  INSERT INTO search_index_fts(text) VALUES (new.text);
                                END;
                            """.trimIndent()
                        )
                        // Helpful indexes - use correct naming convention to match entity definitions
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_kind ON search_index(kind)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_date ON search_index(date)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_abilityId ON search_index(abilityId)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_search_index_questInstanceId ON search_index(questInstanceId)")
                    }
                })
                .build().also { INSTANCE = it }
            }
    }
}


