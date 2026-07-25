package com.naufal.tbhmonitor.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** DataStore Preferences buat nyimpen base URL server desktop app (hasil scan QR / input manual). */
private val Context.connectionDataStore by preferencesDataStore(name = "connection_prefs")

/**
 * Nyimpen base URL yang terakhir dipakai supaya user gak perlu scan ulang QR tiap kali
 * buka app. Alur pemakaiannya: ConnectScreen (step 11) baca [baseUrlFlow] - kalau ada
 * value tersimpan, langsung dipakai buat RetrofitClient.setBaseUrl() dan lanjut ke
 * DashboardScreen tanpa perlu scan ulang; kalau null, tampilkan opsi scan QR / input manual.
 */
class ConnectionPreferences(private val context: Context) {

    private object Keys {
        val BASE_URL = stringPreferencesKey("base_url")
    }

    /** Null kalau belum pernah connect sama sekali (fresh install, atau abis clearBaseUrl()). */
    val baseUrlFlow: Flow<String?> =
        context.connectionDataStore.data.map { prefs -> prefs[Keys.BASE_URL] }

    suspend fun saveBaseUrl(baseUrl: String) {
        context.connectionDataStore.edit { prefs ->
            prefs[Keys.BASE_URL] = baseUrl
        }
    }

    /** Dipanggil dari tombol "Ganti koneksi" / "Scan ulang" di ConnectScreen. */
    suspend fun clearBaseUrl() {
        context.connectionDataStore.edit { prefs ->
            prefs.remove(Keys.BASE_URL)
        }
    }
}
