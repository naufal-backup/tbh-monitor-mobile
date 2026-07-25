package com.naufal.tbhmonitor.data.repository

import com.google.gson.JsonObject
import com.naufal.tbhmonitor.data.local.ConnectionPreferences
import com.naufal.tbhmonitor.data.model.Item
import com.naufal.tbhmonitor.data.model.Player
import com.naufal.tbhmonitor.data.remote.ApiService
import com.naufal.tbhmonitor.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.Response
import java.io.IOException

/**
 * Hasil generik buat tiap pemanggilan API - dipakai ViewModel buat nentuin UI state
 * (loading/success/error/belum-connect) tanpa perlu tau detail Retrofit atau exception
 * jaringan di layer atas.
 */
sealed interface TbhResult<out T> {
    data class Success<T>(val data: T) : TbhResult<T>

    /** Belum ada base URL tersimpan sama sekali - user harus scan QR / input manual dulu. */
    data object NotConnected : TbhResult<Nothing>

    /** Request gagal: network error, timeout, atau response non-2xx dari server. */
    data class Error(val message: String) : TbhResult<Nothing>
}

/**
 * Satu titik akses data buat semua ViewModel - gabungin RetrofitClient (remote, step 5)
 * dan ConnectionPreferences (local/DataStore, step 6). ViewModel gak perlu tau soal
 * Retrofit atau DataStore sama sekali, cukup panggil fungsi-fungsi di sini.
 */
class TbhRepository(
    private val connectionPreferences: ConnectionPreferences
) {

    /** Base URL tersimpan, buat ConnectScreen (step 11) nampilin status koneksi terakhir. */
    val baseUrlFlow: Flow<String?> = connectionPreferences.baseUrlFlow

    /**
     * Dipanggil sekali pas app start (MainActivity, step 16) - restore koneksi terakhir
     * kalau ada, biar RetrofitClient siap dipakai tanpa user harus scan ulang tiap buka app.
     *
     * @return true kalau ada base URL tersimpan (langsung bisa lanjut ke Dashboard),
     *         false kalau belum pernah connect (harus diarahkan ke ConnectScreen).
     */
    suspend fun restoreSavedConnection(): Boolean {
        val savedUrl = connectionPreferences.baseUrlFlow.first()
        if (savedUrl.isNullOrBlank()) return false
        RetrofitClient.setBaseUrl(savedUrl)
        return true
    }

    /**
     * Dipanggil dari ConnectScreen sesudah scan QR / input manual - set base URL ke
     * RetrofitClient sekaligus simpan ke DataStore biar kepakai lagi pas app dibuka ulang.
     */
    suspend fun connect(baseUrl: String) {
        RetrofitClient.setBaseUrl(baseUrl)
        connectionPreferences.saveBaseUrl(baseUrl)
    }

    /** Dipanggil dari tombol "Ganti koneksi" / "Scan ulang" - hapus base URL tersimpan. */
    suspend fun disconnect() {
        connectionPreferences.clearBaseUrl()
        RetrofitClient.clear()
    }

    suspend fun getPlayer(): TbhResult<Player> = safeApiCall { it.getPlayer() }

    suspend fun getInventory(): TbhResult<List<Item>> = safeApiCall { it.getInventory() }

    suspend fun getRawData(): TbhResult<JsonObject> = safeApiCall { it.getRawData() }

    /**
     * Wrapper buat semua pemanggilan endpoint: cek dulu ApiService udah ada (udah pernah
     * connect), lempar [TbhResult.NotConnected] kalau belum. Kalau udah ada, eksekusi
     * request dan tangkap exception jaringan (timeout, host gak ketemu, dll) jadi
     * [TbhResult.Error] alih-alih nge-crash ViewModel - server desktop app ini sifatnya
     * opsional & bisa mati/ganti port kapan aja, jadi error di sini wajar dan harus
     * bisa ditampilkan dengan tenang ke user, bukan crash.
     */
    private suspend fun <T> safeApiCall(
        call: suspend (ApiService) -> Response<T>
    ): TbhResult<T> {
        val api = RetrofitClient.getApiServiceOrNull() ?: return TbhResult.NotConnected

        return try {
            val response = call(api)
            val body = response.body()
            when {
                !response.isSuccessful -> TbhResult.Error("Server error: HTTP ${response.code()}")
                body != null -> TbhResult.Success(body)
                else -> TbhResult.Error("Response kosong dari server")
            }
        } catch (e: IOException) {
            TbhResult.Error("Gagal terhubung ke server - pastikan HP & PC ada di jaringan yang sama")
        } catch (e: Exception) {
            TbhResult.Error(e.message ?: "Terjadi error yang tidak diketahui")
        }
    }
}
