package com.example.vampire_system.util

import android.graphics.*
import android.media.MediaMetadataRetriever
import java.io.File
import kotlin.math.max
import kotlin.math.min

object Thumbs {
    fun image(path: String, maxW: Int = 320, maxH: Int = 320): Bitmap? {
        val f = File(path); if (!f.exists()) return null
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)
        val w = opts.outWidth; val h = opts.outHeight
        if (w <= 0 || h <= 0) return null
        val scale = max(1, min(w / maxW, h / maxH))
        val opts2 = BitmapFactory.Options().apply { inSampleSize = scale }
        return BitmapFactory.decodeFile(path, opts2)
    }

    fun video(path: String, maxW: Int = 320, maxH: Int = 320): Bitmap? {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(path)
            val bmp = mmr.frameAtTime ?: return null
            Bitmap.createScaledBitmap(bmp,
                maxW.coerceAtMost(bmp.width), maxH.coerceAtMost(bmp.height), true)
        } catch (_: Exception) { null } finally { try { mmr.release() } catch (_: Exception) {} }
    }
}


