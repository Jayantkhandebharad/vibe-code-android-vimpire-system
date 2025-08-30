package com.example.vampire_system.features.backup

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("backup_prefs")

object BackupPrefs {
    private val KEY_URI = stringPreferencesKey("backup_uri")
    private val KEY_PASSPHRASE_HINT = stringPreferencesKey("backup_passphrase_hint")
    private val KEY_ENCRYPT = booleanPreferencesKey("backup_encrypt")

    suspend fun setFolder(context: Context, uri: String) {
        context.dataStore.edit { it[KEY_URI] = uri }
    }
    suspend fun getFolder(context: Context): String? =
        context.dataStore.data.map { it[KEY_URI] }.first()

    suspend fun setEncrypt(context: Context, on: Boolean) {
        context.dataStore.edit { it[KEY_ENCRYPT] = on }
    }
    suspend fun getEncrypt(context: Context): Boolean =
        context.dataStore.data.map { it[KEY_ENCRYPT] ?: true }.first()

    suspend fun setPassphraseHint(context: Context, hint: String?) {
        context.dataStore.edit { prefs ->
            if (hint == null) prefs.remove(KEY_PASSPHRASE_HINT) else prefs[KEY_PASSPHRASE_HINT] = hint
        }
    }
    suspend fun getPassphraseHint(context: Context): String? =
        context.dataStore.data.map { it[KEY_PASSPHRASE_HINT] }.first()
}


