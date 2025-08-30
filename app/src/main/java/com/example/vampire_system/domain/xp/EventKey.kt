package com.example.vampire_system.domain.xp

import java.security.MessageDigest

object EventKey {
    private fun sha1(s: String): String {
        val d = MessageDigest.getInstance("SHA-1").digest(s.toByteArray())
        return d.joinToString("") { "%02x".format(it) }
    }
    
    fun forQuest(date: String, type: String, qi: String) =
        "qi:$qi:d:$date:t:$type"

    fun forNote(date: String, type: String, note: String) =
        "note:${sha1(note.trim().lowercase())}:d:$date:t:$type"
}
