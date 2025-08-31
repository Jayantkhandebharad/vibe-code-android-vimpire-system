package com.example.vampire_system.domain.search

import com.example.vampire_system.data.db.*
import com.example.vampire_system.data.model.EvidenceKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchIndexer(private val db: AppDatabase) {

    // Full rebuild (fast: few ms to a couple hundred ms depending on data)
    suspend fun rebuild() = withContext(Dispatchers.IO) {
        val sdao = db.searchIndexDao()
        sdao.clear()
        val items = mutableListOf<SearchIndexEntity>()

        // Abilities
        db.abilityDao().getAllOnce().forEach { a ->
            items += SearchIndexEntity(
                date = null, kind = SearchKind.ABILITY,
                abilityId = a.id, questInstanceId = null,
                title = "Ability: ${a.name}",
                snippet = a.unit ?: "",
                text = listOfNotNull(a.name, a.unit).joinToString(" ")
            )
        }

        // Level Tasks (spec + acceptance)
        (1..100).forEach { lvl ->
            db.levelTaskDao().forLevelOnce(lvl).forEach { t ->
                val lines = t.acceptance.joinToString(" • ")
                items += SearchIndexEntity(
                    date = null, kind = SearchKind.TASK,
                    abilityId = t.abilityId, questInstanceId = null,
                    title = "Task L$lvl: ${t.spec.take(60)}",
                    snippet = lines.take(120),
                    text = (t.spec + " " + lines)
                )
            }
        }

        // Quests (by day) — index ability name + task spec if any
        val abilityMap = db.abilityDao().getAllOnce().associateBy { it.id }
        val taskMap = (1..100).flatMap { db.levelTaskDao().forLevelOnce(it) }.associateBy { it.id }
        db.questInstanceDao().rawAll().forEach { q ->
            val aname = q.abilityId?.let { abilityMap[it]?.name } ?: ""
            val spec = q.templateId?.let { taskMap[it]?.spec } ?: ""
            items += SearchIndexEntity(
                date = q.date, kind = SearchKind.QUEST,
                abilityId = q.abilityId, questInstanceId = q.id,
                title = "Quest: ${if (aname.isNotBlank()) aname else spec.take(40)}",
                snippet = if (spec.isNotBlank()) spec.take(120) else aname,
                text = listOf(aname, spec).joinToString(" ").trim()
            )
        }

        // Evidence (NOTES, LINKS, FILE names)
        db.openHelper.readableDatabase.query(
            "SELECT id, questInstanceId, kind, uriOrText, createdAt FROM evidence",
            emptyArray()
        ).use { c ->
            val kindIdx = c.getColumnIndexOrThrow("kind")
            val textIdx = c.getColumnIndexOrThrow("uriOrText")
            val qiIdx = c.getColumnIndexOrThrow("questInstanceId")
            val createdIdx = c.getColumnIndexOrThrow("createdAt")
            while (c.moveToNext()) {
                val kind = EvidenceKind.valueOf(c.getString(kindIdx))
                val qi = c.getString(qiIdx)
                val txt = c.getString(textIdx)
                val date = java.time.Instant.ofEpochMilli(c.getLong(createdIdx))
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()

                when (kind) {
                    EvidenceKind.NOTE -> items += SearchIndexEntity(
                        date = date, kind = SearchKind.NOTE,
                        abilityId = null, questInstanceId = qi,
                        title = "Note",
                        snippet = txt.take(140),
                        text = txt
                    )
                    EvidenceKind.LINK -> items += SearchIndexEntity(
                        date = date, kind = SearchKind.LINK,
                        abilityId = null, questInstanceId = qi,
                        title = "Link",
                        snippet = txt.take(140),
                        text = txt
                    )
                    EvidenceKind.FILE, EvidenceKind.PHOTO, EvidenceKind.VIDEO, EvidenceKind.AUDIO -> {
                        // index filename path segment
                        val name = txt.substringAfterLast('/')
                        items += SearchIndexEntity(
                            date = date, kind = SearchKind.FILE,
                            abilityId = null, questInstanceId = qi,
                            title = "File: $name",
                            snippet = name,
                            text = name
                        )
                    }
                    EvidenceKind.TIMER, EvidenceKind.CHECKLIST -> { /* skip */ }
                    // Handle all other evidence types as general content
                    else -> items += SearchIndexEntity(
                        date = date, kind = SearchKind.NOTE, // Treat as note for search purposes
                        abilityId = null, questInstanceId = qi,
                        title = "Evidence: ${kind.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        snippet = txt.take(140),
                        text = txt
                    )
                }
            }
        }

        // Bulk insert (triggers fill the FTS shadow table)
        sdao.insertAll(items)
    }

    // Light, incremental index for a single evidence row (call when adding notes)
    suspend fun indexEvidence(e: EvidenceEntity) = withContext(Dispatchers.IO) {
        val kind = when (e.kind) {
            EvidenceKind.NOTE -> SearchKind.NOTE
            EvidenceKind.LINK -> SearchKind.LINK
            EvidenceKind.FILE, EvidenceKind.PHOTO, EvidenceKind.VIDEO, EvidenceKind.AUDIO -> SearchKind.FILE
            EvidenceKind.TIMER, EvidenceKind.CHECKLIST -> return@withContext
            // Handle all other evidence types as notes for search purposes
            else -> SearchKind.NOTE
        }
        val date = java.time.Instant.ofEpochMilli(e.createdAt)
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
        val name = if (kind == SearchKind.FILE) e.uriOrText.substringAfterLast('/') else null

        db.searchIndexDao().insertAll(listOf(
            SearchIndexEntity(
                date = date, kind = kind,
                abilityId = null, questInstanceId = e.questInstanceId,
                title = when (kind) { 
                    SearchKind.NOTE -> "Note"; 
                    SearchKind.LINK -> "Link"; 
                    else -> "File: ${name ?: "blob"}" 
                },
                snippet = when (kind) { 
                    SearchKind.NOTE, SearchKind.LINK -> e.uriOrText.take(140)
                    else -> name 
                },
                text = when (kind) { 
                    SearchKind.NOTE, SearchKind.LINK -> e.uriOrText
                    else -> (name ?: "") 
                }
            )
        ))
    }
}
