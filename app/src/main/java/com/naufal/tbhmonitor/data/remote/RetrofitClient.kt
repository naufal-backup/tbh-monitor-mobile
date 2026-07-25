package com.naufal.tbhmonitor.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Wrapper Retrofit dengan base URL yang bisa berubah-ubah saat runtime.
 *
 * Base URL baru diketahui setelah user scan QR / input manual di ConnectScreen
 * (lihat ConnectionPreferences buat penyimpanannya), jadi gak bisa di-hardcode
 * pas app start seperti kebanyakan contoh Retrofit. Karena Retrofit sendiri gak
 * support ganti base URL setelah di-build, instance Retrofit + ApiService
 * di-rebuild ulang tiap kali setBaseUrl() dipanggil dengan URL yang beda dari
 * yang lagi aktif. OkHttpClient (dengan timeout & logging interceptor) dipakai
 * bareng buat semua rebuild, gak perlu dibuat ulang.
 */
object RetrofitClient {

    private const val TIMEOUT_SECONDS = 10L

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private var currentBaseUrl: String? = null
    private var currentApiService: ApiService? = null

    /**
     * Set/ganti base URL server desktop app, lalu rebuild ApiService kalau memang beda
     * dari yang lagi aktif. Aman dipanggil berkali-kali (misal tiap kali
     * ConnectionPreferences emit value baru lewat Flow) - no-op kalau URL-nya sama.
     */
    fun setBaseUrl(baseUrl: String) {
        val normalized = normalizeBaseUrl(baseUrl)
        if (normalized == currentBaseUrl && currentApiService != null) return

        currentBaseUrl = normalized
        currentApiService = Retrofit.Builder()
            .baseUrl(normalized)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * ApiService yang lagi aktif, null kalau setBaseUrl() belum pernah dipanggil
     * (belum ada koneksi tersimpan sama sekali). Repository wajib handle null ini
     * sebagai state "belum connect", beda dari error jaringan biasa.
     */
    fun getApiServiceOrNull(): ApiService? = currentApiService

    fun currentBaseUrlOrNull(): String? = currentBaseUrl

    /**
     * Terima input fleksibel dari hasil scan QR / input manual, contoh:
     * - "192.168.1.5:8080"        -> "http://192.168.1.5:8080/"
     * - "http://192.168.1.5:8080" -> "http://192.168.1.5:8080/"
     * - "http://192.168.1.5:8080/" -> dibiarkan apa adanya
     */
    private fun normalizeBaseUrl(input: String): String {
        var url = input.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        if (!url.endsWith("/")) {
            url += "/"
        }
        return url
    }
}
