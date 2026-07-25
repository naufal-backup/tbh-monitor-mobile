package com.naufal.tbhmonitor.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/**
 * Satu elemen array `itemSaveDatas` - dipakai baik dari response /api/player
 * (field "itemSaveDatas") maupun response /api/inventory (array-nya langsung ini).
 */
data class Item(
    @SerializedName("UniqueId") val uniqueId: Long = 0,
    @SerializedName("ItemKey") val itemKey: Long = 0,
    @SerializedName("Quantity") val quantity: Long = 0,
    // Beberapa versi save file pakai "Level", yang lain "ItemLevel" - Gson coba keduanya.
    @SerializedName(value = "Level", alternate = ["ItemLevel"]) val level: Long = 0,
    @SerializedName("IsChaotic") val isChaotic: Boolean = false,
    @SerializedName("EnchantData") val enchantData: List<EnchantData> = emptyList(),
    @SerializedName("EnchantCount") val enchantCount: List<Long> = emptyList(),
    @SerializedName("InscriptionAppliedTotalCount") val inscriptionCount: Long = 0,
    @SerializedName("EngravingAppliedTotalCount") val engravingCount: Long = 0,
    @SerializedName("DecorationAppliedTotalCount") val decorationCount: Long = 0,
    // Skema tiap elemen socket belum diketahui pasti dari backend (cuma dicek "object kosong
    // atau bukan" buat status keisi/kosong), jadi disimpan mentah sebagai JsonObject dulu.
    @SerializedName("SocketData") val socketData: List<JsonObject>? = null
) {
    /** Total enchant level, dijumlah dari EnchantCount (sama seperti logika desktop app). */
    val totalEnchantLevel: Long get() = enchantCount.sum()

    val totalSocketCount: Int get() = socketData?.size ?: 0
    val filledSocketCount: Int
        get() = socketData?.count { it.size() > 0 } ?: 0

    val hasExtras: Boolean
        get() = inscriptionCount > 0 || engravingCount > 0 || decorationCount > 0 || totalSocketCount > 0
}

/** Satu elemen array `EnchantData` di dalam sebuah Item. */
data class EnchantData(
    @SerializedName("StatType") val statType: Long = 0,
    @SerializedName("Value") val value: Double = 0.0,
    @SerializedName("Tier") val tier: Long = 0
)
