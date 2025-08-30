package com.example.vampire_system.features.export

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.vampire_system.data.db.AppDatabase
import com.example.vampire_system.data.db.LedgerType
import java.io.File

object DayExporter {

    suspend fun renderBitmap(context: Context, date: String): Bitmap {
        val db = AppDatabase.get(context)
        val summary = db.dayDao().byDate(date)
        val ledger = db.xpLedgerDao().byDate(date)

        val w = 1080; val h = 1600
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.WHITE)

        val title = Paint().apply { color = Color.BLACK; textSize = 52f; isFakeBoldText = true }
        val body = Paint().apply { color = Color.BLACK; textSize = 36f }
        val small = Paint().apply { color = Color.DKGRAY; textSize = 30f }

        var y = 80f
        c.drawText("Vampire Mode+ — $date", 60f, y, title); y += 40f
        if (summary != null) {
            y += 40f
            c.drawText("XP Raw: ${summary.xpRaw}", 60f, y, body); y += 42f
            c.drawText("Bonus: +${summary.xpBonus}", 60f, y, body); y += 42f
            c.drawText("Penalty: −${summary.xpPenalty}", 60f, y, body); y += 42f
            c.drawText("XP Net: ${summary.xpNet}", 60f, y, body); y += 60f
            c.drawText("Foundations hit: ${summary.foundationsHit} • Streak: ${summary.streakTier}", 60f, y, small); y += 60f
        } else {
            y += 60f
            c.drawText("No DaySummary yet.", 60f, y, body); y += 60f
        }

        c.drawText("Top entries", 60f, y, title); y += 50f
        ledger.take(12).forEach { e ->
            val sign = if (e.deltaXp >= 0) "+" else ""
            val label = e.note ?: e.abilityId ?: e.type.name
            c.drawText("${e.type.name.padEnd(8)}  ${sign}${e.deltaXp} XP  —  ${label}", 60f, y, body)
            y += 40f
        }

        return bmp
    }

    suspend fun exportPdf(context: Context, date: String): File {
        val bmp = renderBitmap(context, date)
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, 1).create()
        val page = pdf.startPage(pageInfo)
        page.canvas.drawBitmap(bmp, 0f, 0f, null)
        pdf.finishPage(page)
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val out = File(dir, "day-${date}.pdf")
        out.outputStream().use { pdf.writeTo(it) }
        pdf.close()
        return out
    }

    suspend fun exportPng(context: Context, date: String): File {
        val bmp = renderBitmap(context, date)
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val out = File(dir, "day-${date}.png")
        out.outputStream().use { fos -> bmp.compress(Bitmap.CompressFormat.PNG, 100, fos) }
        return out
    }
    
    suspend fun exportPdfCached(context: Context, date: String): File {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val out = File(dir, "day-$date.pdf")
        if (out.exists()) return out
        return exportPdf(context, date)
    }

    suspend fun exportPngCached(context: Context, date: String): File {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val out = File(dir, "day-$date.png")
        if (out.exists()) return out
        return exportPng(context, date)
    }
}


