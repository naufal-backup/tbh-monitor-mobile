package com.naufal.tbhmonitor.data.model

import com.google.gson.annotations.SerializedName

/** Satu elemen array `RuneSaveData` di dalam response /api/player. */
data class Rune(
    @SerializedName("RuneKey") val runeKey: Long = 0,
    @SerializedName("Level") val level: Long = 0
) {
    /** Node dianggap unlocked kalau Level > 0, sesuai logika desktop app. */
    val isUnlocked: Boolean get() = level > 0
}
