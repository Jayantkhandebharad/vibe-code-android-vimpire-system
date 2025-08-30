package com.example.vampire_system.features.evidence

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.example.vampire_system.BuildConfig
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.EvidenceEntity
import com.example.vampire_system.data.model.EvidenceKind
import com.example.vampire_system.util.Files
import com.example.vampire_system.util.Hash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class EvidenceManager(private val context: Context, private val db: AppDatabase) {

    // ---- TEXT / LINK / TIMER ----
    suspend fun addText(questId: String, text: String) = createEvidence(questId, EvidenceKind.NOTE, text)
    suspend fun addLink(questId: String, url: String) = createEvidence(questId, EvidenceKind.LINK, url)
    suspend fun addTimer(questId: String, minutes: Int) =
        createEvidence(questId, EvidenceKind.TIMER, "$minutes", mapOf("minutes" to minutes.toString()))

    // ---- FILE PICK (copy into app private for backup hashing) ----
    suspend fun importFromUri(questId: String, source: Uri, kind: EvidenceKind = EvidenceKind.FILE): EvidenceEntity =
        withContext(Dispatchers.IO) {
            val ext = guessExt(context.contentResolver, source) ?: "bin"
            val dst = Files.newFile(context, ext)
            context.contentResolver.openInputStream(source)!!.use { it.copyTo(dst.outputStream()) }
            val hash = Hash.sha256(dst)
            createEvidence(questId, kind, dst.absolutePath, mapOf("ext" to ext), sha = hash)
        }

    // ---- PHOTO capture intent ----
    fun preparePhotoCapture(): Pair<File, Uri> {
        val file = Files.newFile(context, "jpg")
        val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        return file to uri
    }
    suspend fun savePhoto(questId: String, file: File): EvidenceEntity =
        withContext(Dispatchers.IO) {
            val hash = Hash.sha256(file)
            createEvidence(questId, EvidenceKind.PHOTO, file.absolutePath, sha = hash)
        }

    // ---- VIDEO capture intent ----
    fun prepareVideoCapture(): Pair<File, Uri> {
        val file = Files.newFile(context, "mp4")
        val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        return file to uri
    }
    suspend fun saveVideo(questId: String, file: File): EvidenceEntity =
        withContext(Dispatchers.IO) {
            val hash = Hash.sha256(file)
            createEvidence(questId, EvidenceKind.VIDEO, file.absolutePath, sha = hash)
        }

    // ---- AUDIO record (MediaRecorder wrapper) ----
    fun newAudioFile(): File = Files.newFile(context, "m4a")

    fun startAudioRecorder(dst: File): MediaRecorder {
        val mr = MediaRecorder()
        mr.setAudioSource(MediaRecorder.AudioSource.MIC)
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mr.setAudioEncodingBitRate(128_000)
        mr.setAudioSamplingRate(44_100)
        mr.setOutputFile(dst.absolutePath)
        mr.prepare()
        mr.start()
        return mr
    }

    suspend fun saveAudio(questId: String, file: File): EvidenceEntity =
        withContext(Dispatchers.IO) {
            val hash = Hash.sha256(file)
            createEvidence(questId, EvidenceKind.AUDIO, file.absolutePath, sha = hash)
        }

    // ---- internals ----
    private suspend fun createEvidence(
        questId: String,
        kind: EvidenceKind,
        uriOrText: String,
        meta: Map<String,String> = emptyMap(),
        sha: String? = null
    ): EvidenceEntity = withContext(Dispatchers.IO) {
        val e = EvidenceEntity(
            id = java.util.UUID.randomUUID().toString(),
            questInstanceId = questId,
            kind = kind,
            uriOrText = uriOrText,
            meta = meta,
            sha256 = sha,
            createdAt = System.currentTimeMillis()
        )
        db.evidenceDao().insert(e)
        
        e
    }

    private fun guessExt(cr: ContentResolver, uri: Uri): String? {
        cr.getType(uri)?.let { mime ->
            return when {
                mime.endsWith("jpeg") -> "jpg"
                mime.endsWith("png") -> "png"
                mime.endsWith("mp4") -> "mp4"
                mime.endsWith("mpeg") -> "mp3"
                mime.endsWith("aac") -> "m4a"
                else -> null
            }
        }
        cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val name = c.getString(0)
                val dot = name.lastIndexOf('.')
                if (dot > 0) return name.substring(dot + 1)
            }
        }
        return null
    }
}


