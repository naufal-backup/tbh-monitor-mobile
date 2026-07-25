package com.naufal.tbhmonitor.data.model

import com.google.gson.annotations.SerializedName

/**
 * Bentuk root dari response GET /api/player.
 *
 * Backend (tbh-monitor) langsung forward JSON hasil parse save file apa adanya
 * (lihat PlayerData#other di es3.rs, di-flatten dari serde_json::Value), makanya
 * nama-nama field di sini mengikuti persis key yang ada di save file - termasuk
 * yang typo ("currenySaveDatas") dan inkonsistensi kapital ("heroKey" vs "HeroLevel").
 */
data class Player(
    @SerializedName("currenySaveDatas") val currencies: List<Currency> = emptyList(),
    @SerializedName("heroSaveDatas") val heroes: List<Hero> = emptyList(),
    @SerializedName("itemSaveDatas") val items: List<Item> = emptyList(),
    @SerializedName("RuneSaveData") val runes: List<Rune> = emptyList(),
    @SerializedName("PetSaveData") val pets: List<Pet> = emptyList()
) {
    /** Gold diambil dari elemen pertama currenySaveDatas, sama seperti logika desktop app. */
    val gold: Long
        get() = currencies.firstOrNull()?.quantity ?: 0L

    val unlockedRuneCount: Int get() = runes.count { it.isUnlocked }
}

data class Currency(
    @SerializedName("Quantity") val quantity: Long = 0
)
