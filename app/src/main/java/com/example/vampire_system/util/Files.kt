package com.example.vampire_system.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Files {
    private val y = SimpleDateFormat("yyyy", Locale.US)
    private val m = SimpleDateFormat("MM", Locale.US)
    private val d = SimpleDateFormat("dd", Locale.US)

    fun evidenceDir(context: Context, time: Long = System.currentTimeMillis()): File {
        val base = File(context.filesDir, "evidence")
        val now = Date(time)
        val dir = File(base, "${y.format(now)}/${m.format(now)}/${d.format(now)}")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun newFile(context: Context, ext: String, time: Long = System.currentTimeMillis()): File {
        val name = "${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.$ext"
        return File(evidenceDir(context, time), name)
    }
}


