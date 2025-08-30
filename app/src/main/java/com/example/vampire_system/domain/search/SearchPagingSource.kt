package com.example.vampire_system.domain.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.SearchIndexEntity
import com.example.vampire_system.data.db.SearchKind

class SearchPagingSource(
    private val db: AppDatabase,
    private val query: String,
    private val kind: SearchKind?,
    private val abilities: List<String>,
    private val from: String?,
    private val to: String?,
    private val doneOnly: Boolean,
    private val hasEvidence: Boolean,
    private val minXp: Int?,
    private val maxXp: Int?
) : PagingSource<Int, SearchIndexEntity>() {

    override fun getRefreshKey(state: PagingState<Int, SearchIndexEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchIndexEntity> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            val offset = page * pageSize

            val results = if (query.isNotBlank()) {
                // FTS search with filters
                val abilClause = if (abilities.isEmpty()) "1" else (
                    "s.abilityId IN (" + List(abilities.size) { "?" }.joinToString(",") + ")"
                )
                val sql = """
                    SELECT s.* FROM search_index s
                    JOIN search_index_fts fts ON fts.rowid = s.sid
                    LEFT JOIN quest_instances q ON q.id = s.questInstanceId
                    WHERE fts MATCH ?
                      AND (? IS NULL OR s.kind = ?)
                      AND (CASE WHEN ? = 0 THEN 1 ELSE ($abilClause) END)
                      AND (? IS NULL OR s.date >= ?)
                      AND (? IS NULL OR s.date <= ?)
                      AND (CASE WHEN ?=1 THEN (q.status = 'DONE') ELSE 1 END)
                      AND (CASE WHEN ?=1 THEN EXISTS (
                            SELECT 1 FROM evidence e WHERE e.questInstanceId = s.questInstanceId
                          ) ELSE 1 END)
                      AND (CASE WHEN ? IS NULL THEN 1 ELSE (q.xpAwarded >= ?) END)
                      AND (CASE WHEN ? IS NULL THEN 1 ELSE (q.xpAwarded <= ?) END)
                    GROUP BY s.sid
                    ORDER BY s.date DESC, s.sid DESC
                    LIMIT ? OFFSET ?
                """.trimIndent()

                val args = mutableListOf<Any?>()
                args += query
                args += kind?.name; args += kind?.name
                args += if (abilities.isEmpty()) 0 else 1
                if (abilities.isNotEmpty()) abilities.forEach { args += it }
                args += from; args += from
                args += to; args += to
                args += if (doneOnly) 1 else 0
                args += if (hasEvidence) 1 else 0
                args += minXp; args += minXp
                args += maxXp; args += maxXp
                args += pageSize; args += offset

                db.openHelper.readableDatabase.query(sql, args.toTypedArray()).use { cursor ->
                    val items = mutableListOf<SearchIndexEntity>()
                    while (cursor.moveToNext()) items.add(cursor.toSearchIndexEntity())
                    items
                }
            } else {
                // Non-FTS with filters
                val abilClause = if (abilities.isEmpty()) "1" else (
                    "s.abilityId IN (" + List(abilities.size) { "?" }.joinToString(",") + ")"
                )
                val sql = """
                    SELECT s.* FROM search_index s
                    LEFT JOIN quest_instances q ON q.id = s.questInstanceId
                    WHERE (? IS NULL OR s.kind = ?)
                      AND (CASE WHEN ? = 0 THEN 1 ELSE ($abilClause) END)
                      AND (? IS NULL OR s.date >= ?)
                      AND (? IS NULL OR s.date <= ?)
                      AND (CASE WHEN ?=1 THEN (q.status = 'DONE') ELSE 1 END)
                      AND (CASE WHEN ?=1 THEN EXISTS (
                            SELECT 1 FROM evidence e WHERE e.questInstanceId = s.questInstanceId
                          ) ELSE 1 END)
                      AND (CASE WHEN ? IS NULL THEN 1 ELSE (q.xpAwarded >= ?) END)
                      AND (CASE WHEN ? IS NULL THEN 1 ELSE (q.xpAwarded <= ?) END)
                    GROUP BY s.sid
                    ORDER BY s.date DESC, s.sid DESC
                    LIMIT ? OFFSET ?
                """.trimIndent()

                val args = mutableListOf<Any?>()
                args += kind?.name; args += kind?.name
                args += if (abilities.isEmpty()) 0 else 1
                if (abilities.isNotEmpty()) abilities.forEach { args += it }
                args += from; args += from
                args += to; args += to
                args += if (doneOnly) 1 else 0
                args += if (hasEvidence) 1 else 0
                args += minXp; args += minXp
                args += maxXp; args += maxXp
                args += pageSize; args += offset

                db.openHelper.readableDatabase.query(sql, args.toTypedArray()).use { cursor ->
                    val items = mutableListOf<SearchIndexEntity>()
                    while (cursor.moveToNext()) items.add(cursor.toSearchIndexEntity())
                    items
                }
            }

            LoadResult.Page(
                data = results,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (results.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun android.database.Cursor.toSearchIndexEntity(): SearchIndexEntity {
        return SearchIndexEntity(
            sid = getLong(getColumnIndexOrThrow("sid")),
            date = getString(getColumnIndexOrThrow("date")),
            kind = SearchKind.valueOf(getString(getColumnIndexOrThrow("kind"))),
            abilityId = getString(getColumnIndexOrThrow("abilityId")),
            questInstanceId = getString(getColumnIndexOrThrow("questInstanceId")),
            title = getString(getColumnIndexOrThrow("title")),
            snippet = getString(getColumnIndexOrThrow("snippet")),
            text = getString(getColumnIndexOrThrow("text"))
        )
    }
}
