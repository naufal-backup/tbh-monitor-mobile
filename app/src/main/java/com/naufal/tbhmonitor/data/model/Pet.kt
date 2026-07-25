package com.naufal.tbhmonitor.data.model

import com.google.gson.annotations.SerializedName

/**
 * Satu elemen array `PetSaveData` di dalam response /api/player.
 *
 * Save file punya 2 varian nama field buat status yang sama (beda versi game) -
 * desktop app cross-check keduanya (get("A").or_else(get("B"))), di sini dipetakan
 * pakai `alternate` biar Gson otomatis coba nama keduanya.
 */
data class Pet(
    @SerializedName("PetKey") val petKey: Long = 0,
    @SerializedName(value = "IsUnlock", alternate = ["IsUnLock"]) val isUnlocked: Boolean = false,
    @SerializedName(value = "IsEquipped", alternate = ["IsViewed"]) val isEquipped: Boolean = false
)
