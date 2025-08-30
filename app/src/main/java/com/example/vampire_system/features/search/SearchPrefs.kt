package com.example.vampire_system.features.search

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.ds by preferencesDataStore("search_prefs")
private val KEY_RECENTS = stringPreferencesKey("recents")

object SearchPrefs {
    suspend fun pushRecent(context: Context, q: String) {
        if (q.isBlank()) return
        context.ds.edit { prefs ->
            val current = prefs[KEY_RECENTS] ?: ""
            val list = current.split(",").filter { it.isNotBlank() }.toMutableList()
            list.remove(q); list.add(0, q)
            while (list.size > 10) list.removeLast()
            prefs[KEY_RECENTS] = list.joinToString(",")
        }
    }
    
    suspend fun getRecents(context: Context): List<String> {
        val prefs = context.ds.data.first()
        val recents = prefs[KEY_RECENTS] ?: return emptyList()
        return recents.split(",").filter { it.isNotBlank() }
    }
}

