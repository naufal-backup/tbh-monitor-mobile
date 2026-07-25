package com.naufal.tbhmonitor.data.remote

import com.google.gson.JsonObject
import com.naufal.tbhmonitor.data.model.Item
import com.naufal.tbhmonitor.data.model.Player
import retrofit2.Response
import retrofit2.http.GET

/**
 * Endpoint REST yang di-expose local server desktop app tbh-monitor (lihat src/server.rs
 * di repo backend). Base URL diisi dinamis - hasil scan QR atau input manual - lihat
 * RetrofitClient & ConnectionPreferences.
 *
 * Semua fungsi pakai suspend + Response<T> (bukan langsung return body) supaya layer
 * repository bisa cek response.isSuccessful / error code sebelum diteruskan ke ViewModel,
 * mengingat server ini cuma jalan opsional dari desktop app - bisa saja belum aktif,
 * port beda, atau save data belum ke-load (backend balikin 404 kalau begitu).
 */
interface ApiService {

    /**
     * GET /api/data - seluruh save data mentah (AccountSaveData + PlayerSaveData).
     * Tiap field "value" di response ini masih berupa JSON string yang di-encode dua kali
     * (lihat backend/src/es3.rs: RawEs3Field), beda dari /api/player yang sudah di-parse
     * duluan oleh backend. Belum ada screen yang butuh raw data ini, jadi sengaja
     * dikembalikan sebagai JsonObject mentah alih-alih model strongly-typed.
     */
    @GET("api/data")
    suspend fun getRawData(): Response<JsonObject>

    /** GET /api/player - data player yang sudah di-parse backend: hero, item, rune, pet, gold. */
    @GET("api/player")
    suspend fun getPlayer(): Response<Player>

    /** GET /api/inventory - daftar item inventory saja (subset dari Player.items). */
    @GET("api/inventory")
    suspend fun getInventory(): Response<List<Item>>
}
