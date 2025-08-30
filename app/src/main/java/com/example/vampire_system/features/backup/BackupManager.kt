package com.example.vampire_system.features.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.vampire_system.BuildConfig
import com.example.vampire_system.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BackupManager(private val context: Context, private val db: AppDatabase) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun backupFull(folderTreeUri: Uri, passphrase: CharArray?): Boolean = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, folderTreeUri) ?: return@withContext false
        val today = LocalDate.now().format(dateFmt)
        val outDir = root.createDirectory("VampireModePlusBackups/$today-full") ?: return@withContext false

        val encrypt = BackupPrefs.getEncrypt(context)
        val softKey = if (encrypt) Crypto.getOrCreateSoftKey(context) else null
        if (encrypt && passphrase != null) {
            val keyEncBytes = Crypto.wrapSoftKeyWithPassphrase(softKey!!, passphrase)
            writeBytes(root, "VampireModePlusBackups/key.enc", keyEncBytes)
        }

        val plan = PlanDump(
            abilities = db.abilityDao().getAllNow(),
            levels = db.levelDao().getAllNow(),
            tasks = db.levelTaskDao().forAllLevels(),
            milestones = db.levelMilestoneDao().all()
        )
        writeJson(outDir, "plan.json", plan, encrypt, softKey)

        val progress = ProgressDump(
            levelProgress = db.levelProgressDao().get(),
            daySummaries = db.openHelper.readableDatabase.query("SELECT * FROM day_summaries", emptyArray()).useRows { it.toDaySummaries() },
            quests = db.openHelper.readableDatabase.query("SELECT * FROM quest_instances", emptyArray()).useRows { it.toQuestInstances() },
            evidence = db.openHelper.readableDatabase.query("SELECT * FROM evidence", emptyArray()).useRows { it.toEvidence() },
            ledger = db.openHelper.readableDatabase.query("SELECT * FROM xp_ledger", emptyArray()).useRows { it.toLedger() },
            streak = db.streakDao().get(),
            settings = db.settingsDao().get(),
            profile = db.profileDao().get(),
            stages = db.openHelper.readableDatabase.query("SELECT * FROM stage_items", emptyArray()).useRows { it.toStages() }
        )
        writeJson(outDir, "progress.json", progress, encrypt, softKey)

        val evidenceDir = outDir.createDirectory("evidence")
        progress.evidence.filter { it.sha256 != null && it.uriOrText.endsWith("/files/") == false }.forEach { e ->
            val srcPath = e.uriOrText
            val sha = e.sha256!!
            val ext = (e.meta["ext"] ?: guessExtFromPath(srcPath)) ?: "bin"
            val name = "$sha.$ext" + if (encrypt) ".enc" else ""
            val already = evidenceDir?.findFile(name) != null
            if (!already) {
                val bytes = java.io.File(srcPath).takeIf { it.exists() }?.readBytes() ?: return@forEach
                val output = if (encrypt) {
                    val key = Crypto.softKeyToSecretKey(softKey!!)
                    val box = Crypto.aesEncrypt(key, bytes)
                    box.iv + box.ciphertext
                } else bytes
                writeBytes(evidenceDir!!, name, output)
            }
        }

        val manifest = Manifest(
            kind = "full",
            createdAt = System.currentTimeMillis(),
            schemaVersion = 3,
            appVersion = BuildConfig.VERSION_NAME,
            lastFullAt = System.currentTimeMillis(),
            encrypt = encrypt,
            counts = mapOf(
                "quests" to progress.quests.size,
                "evidence" to progress.evidence.size,
                "ledger" to progress.ledger.size,
                "daySummaries" to progress.daySummaries.size
            )
        )
        writeJson(outDir, "manifest.json", manifest, encrypt = false, softKey = null)

        true
    }

    suspend fun backupIncremental(folderTreeUri: Uri): Boolean = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, folderTreeUri) ?: return@withContext false
        val today = LocalDate.now().format(dateFmt)
        val outDir = root.createDirectory("VampireModePlusBackups/$today-inc") ?: return@withContext false

        val encrypt = BackupPrefs.getEncrypt(context)
        val softKey = if (encrypt) Crypto.getOrCreateSoftKey(context) else null

        val backups = root.findFile("VampireModePlusBackups")
        val lastFullDate = backups?.listFiles()?.map { it.name.orEmpty() }
            ?.filter { it.endsWith("-full") }?.maxOrNull()?.substringBefore("-full")
        val since = lastFullDate ?: LocalDate.now().minusDays(7).format(dateFmt)

        val quests = db.openHelper.readableDatabase.query(
            "SELECT * FROM quest_instances WHERE date >= ?", arrayOf(since)
        ).useRows { it.toQuestInstances() }

        val evidence = db.openHelper.readableDatabase.query(
            "SELECT * FROM evidence WHERE createdAt >= ?", arrayOf(dateToEpoch(since).toString())
        ).useRows { it.toEvidence() }

        val ledger = db.openHelper.readableDatabase.query(
            "SELECT * FROM xp_ledger WHERE date >= ?", arrayOf(since)
        ).useRows { it.toLedger() }

        val daySummaries = db.openHelper.readableDatabase.query(
            "SELECT * FROM day_summaries WHERE date >= ?", arrayOf(since)
        ).useRows { it.toDaySummaries() }

        val stages = db.openHelper.readableDatabase.query(
            "SELECT s.* FROM stage_items s JOIN quest_instances q ON s.questInstanceId=q.id WHERE q.date >= ?",
            arrayOf(since)
        ).useRows { it.toStages() }

        val delta = ProgressDump(
            levelProgress = db.levelProgressDao().get(),
            daySummaries = daySummaries,
            quests = quests,
            evidence = evidence,
            ledger = ledger,
            streak = db.streakDao().get(),
            settings = db.settingsDao().get(),
            profile = db.profileDao().get(),
            stages = stages
        )
        writeJson(outDir, "progress_delta.json", delta, encrypt, softKey)

        val evidenceDir = outDir.createDirectory("evidence")
        evidence.filter { it.sha256 != null }.forEach { e ->
            val srcPath = e.uriOrText
            val sha = e.sha256!!
            val ext = (e.meta["ext"] ?: guessExtFromPath(srcPath)) ?: "bin"
            val name = "$sha.$ext" + if (encrypt) ".enc" else ""
            if (evidenceDir?.findFile(name) == null) {
                val bytes = java.io.File(srcPath).takeIf { it.exists() }?.readBytes() ?: return@forEach
                val output = if (encrypt) {
                    val key = Crypto.softKeyToSecretKey(softKey!!)
                    val box = Crypto.aesEncrypt(key, bytes)
                    box.iv + box.ciphertext
                } else bytes
                writeBytes(evidenceDir!!, name, output)
            }
        }

        val manifest = Manifest(
            kind = "inc",
            createdAt = System.currentTimeMillis(),
            schemaVersion = 3,
            appVersion = BuildConfig.VERSION_NAME,
            lastFullAt = null,
            encrypt = encrypt,
            counts = mapOf("quests" to quests.size, "evidence" to evidence.size, "ledger" to ledger.size)
        )
        writeJson(outDir, "manifest.json", manifest, encrypt = false, softKey = null)

        true
    }

    suspend fun restoreFromFolder(folderTreeUri: Uri, passphrase: CharArray?): Boolean = withContext(Dispatchers.IO) {
        val folder = DocumentFile.fromTreeUri(context, folderTreeUri) ?: return@withContext false
        val manifest = readJson<Manifest>(folder, "manifest.json", encrypted = false, softKey = null) ?: return@withContext false
        val encrypted = manifest.encrypt

        val softKey: javax.crypto.SecretKey? = if (encrypted) {
            val root = folder.parentFile?.parentFile ?: folder.parentFile ?: return@withContext false
            val keyEncFile = root.findFile("key.enc") ?: root.findFile("VampireModePlusBackups")?.findFile("key.enc")
            val keyBytes = keyEncFile?.let { context.contentResolver.openInputStream(it.uri)?.readBytes() }
            requireNotNull(keyBytes) { "Missing key.enc at root. Provide passphrase-backed key.enc." }
            val soft = Crypto.unwrapSoftKeyWithPassphrase(keyBytes, passphrase ?: CharArray(0))
            Crypto.softKeyToSecretKey(soft)
        } else null

        val progress: ProgressDump? = when {
            folder.findFile("progress.json") != null ->
                readJson(folder, "progress.json", encrypted, softKey)
            folder.findFile("progress_delta.json") != null ->
                readJson(folder, "progress_delta.json", encrypted, softKey)
            else -> null
        }
        progress ?: return@withContext false

        // Optional: restore plan.json milestones if present
        folder.findFile("plan.json")?.let {
            val plan: PlanDump? = readJson(folder, "plan.json", encrypted = false, softKey = null)
            plan?.milestones?.forEach { m -> db.levelMilestoneDao().upsert(m) }
        }

        val dbe = db.openHelper.writableDatabase
        dbe.beginTransaction()
        try {
            progress.daySummaries.forEach { db.dayDao().upsert(it) }
            progress.quests.forEach { db.questInstanceDao().upsert(it) }
            progress.evidence.forEach { db.evidenceDao().insert(it) }
            progress.ledger.forEach { db.xpLedgerDao().insert(it) }
            progress.streak?.let { db.streakDao().upsert(it) }
            progress.settings?.let { db.settingsDao().upsert(it) }
            progress.profile?.let { db.profileDao().upsert(it) }
            progress.levelProgress?.let { db.levelProgressDao().upsert(it) }
            db.stageDao().upsertAll(progress.stages)
            dbe.setTransactionSuccessful()
        } finally {
            dbe.endTransaction()
        }
        true
    }

    private inline fun <reified T> writeJson(dir: DocumentFile, name: String, obj: T, encrypt: Boolean, softKey: ByteArray?) {
        val bytes = json.encodeToString(obj).toByteArray()
        if (encrypt) {
            val key = Crypto.softKeyToSecretKey(softKey!!)
            val box = Crypto.aesEncrypt(key, bytes)
            writeBytes(dir, name + ".enc", box.iv + box.ciphertext)
        } else {
            writeBytes(dir, name, bytes)
        }
    }

    private fun writeBytes(dir: DocumentFile, name: String, bytes: ByteArray) {
        val file = dir.findFile(name) ?: dir.createFile("application/octet-stream", name) ?: return
        context.contentResolver.openOutputStream(file.uri)?.use { it.write(bytes) }
    }

    private inline fun <reified T> readJson(dir: DocumentFile, name: String, encrypted: Boolean, softKey: javax.crypto.SecretKey?): T? {
        val file = dir.findFile(if (encrypted) "$name.enc" else name) ?: return null
        val raw = context.contentResolver.openInputStream(file.uri)?.readBytes() ?: return null
        val data = if (encrypted) {
            val iv = raw.copyOfRange(0,12); val ct = raw.copyOfRange(12, raw.size)
            Crypto.aesDecrypt(softKey!!, iv, ct)
        } else raw
        return json.decodeFromString<T>(data.decodeToString())
    }

    private fun guessExtFromPath(path: String) = path.substringAfterLast('.', missingDelimiterValue = "bin")
    private fun dateToEpoch(ymd: String): Long = java.time.LocalDate.parse(ymd).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private inline fun <T> android.database.Cursor.useRows(block: (android.database.Cursor)->T): T = use { block(it) }

private fun android.database.Cursor.toDaySummaries() = buildList {
    while (moveToNext()) add(
        com.example.vampire_system.data.db.DaySummaryEntity(
            date = getString(getColumnIndexOrThrow("date")),
            levelId = getInt(getColumnIndexOrThrow("levelId")),
            xpRaw = getInt(getColumnIndexOrThrow("xpRaw")),
            xpBonus = getInt(getColumnIndexOrThrow("xpBonus")),
            xpPenalty = getInt(getColumnIndexOrThrow("xpPenalty")),
            xpNet = getInt(getColumnIndexOrThrow("xpNet")),
            foundationsHit = getInt(getColumnIndexOrThrow("foundationsHit")),
            streakTier = com.example.vampire_system.data.model.StreakTier.valueOf(getString(getColumnIndexOrThrow("streakTier")))
        )
    )
}

private fun android.database.Cursor.toQuestInstances() = buildList {
    while (moveToNext()) add(
        com.example.vampire_system.data.db.QuestInstanceEntity(
            id = getString(getColumnIndexOrThrow("id")),
            date = getString(getColumnIndexOrThrow("date")),
            levelId = getInt(getColumnIndexOrThrow("levelId")),
            templateId = getString(getColumnIndexOrThrow("templateId")),
            abilityId = getString(getColumnIndexOrThrow("abilityId")),
            status = com.example.vampire_system.data.model.QuestStatus.valueOf(getString(getColumnIndexOrThrow("status"))),
            xpAwarded = getInt(getColumnIndexOrThrow("xpAwarded")),
            createdAt = getLong(getColumnIndexOrThrow("createdAt")),
            updatedAt = getLong(getColumnIndexOrThrow("updatedAt"))
        )
    )
}

private fun android.database.Cursor.toEvidence() = buildList {
    while (moveToNext()) add(
        com.example.vampire_system.data.db.EvidenceEntity(
            id = getString(getColumnIndexOrThrow("id")),
            questInstanceId = getString(getColumnIndexOrThrow("questInstanceId")),
            kind = com.example.vampire_system.data.model.EvidenceKind.valueOf(getString(getColumnIndexOrThrow("kind"))),
            uriOrText = getString(getColumnIndexOrThrow("uriOrText")),
            meta = emptyMap(),
            sha256 = getString(getColumnIndexOrThrow("sha256")),
            createdAt = getLong(getColumnIndexOrThrow("createdAt"))
        )
    )
}

private fun android.database.Cursor.toLedger() = buildList {
    while (moveToNext()) add(
        com.example.vampire_system.data.db.XpLedgerEntity(
            id = getString(getColumnIndexOrThrow("id")),
            date = getString(getColumnIndexOrThrow("date")),
            createdAt = getLong(getColumnIndexOrThrow("createdAt")),
            type = com.example.vampire_system.data.db.LedgerType.valueOf(getString(getColumnIndexOrThrow("type"))),
            deltaXp = getInt(getColumnIndexOrThrow("deltaXp")),
            abilityId = getString(getColumnIndexOrThrow("abilityId")),
            questInstanceId = getString(getColumnIndexOrThrow("questInstanceId")),
            note = getString(getColumnIndexOrThrow("note")),
            eventKey = run {
                val idx = getColumnIndex("eventKey")
                if (idx >= 0) getString(idx) ?: ("legacy:" + getString(getColumnIndexOrThrow("id")))
                else ("legacy:" + getString(getColumnIndexOrThrow("id")))
            }
        )
    )
}

private fun android.database.Cursor.toStages() = buildList {
    while (moveToNext()) add(
        com.example.vampire_system.data.db.StageItemEntity(
            id = getString(getColumnIndexOrThrow("id")),
            questInstanceId = getString(getColumnIndexOrThrow("questInstanceId")),
            label = getString(getColumnIndexOrThrow("label")),
            done = getInt(getColumnIndexOrThrow("done")) != 0,
            completedAt = getLong(getColumnIndexOrThrow("completedAt")).let { if (it == 0L) null else it }
        )
    )
}

private suspend fun com.example.vampire_system.data.db.AbilityDao.getAllNow() =
    this.getAll().firstOrNull() ?: emptyList()
private suspend fun com.example.vampire_system.data.db.LevelDao.getAllNow() =
    this.getAll().firstOrNull() ?: emptyList()
private suspend fun com.example.vampire_system.data.db.LevelTaskDao.forAllLevels(): List<com.example.vampire_system.data.db.LevelTaskEntity> =
    (1..100).flatMap { this.forLevel(it) }


