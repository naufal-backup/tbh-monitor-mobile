package com.naufal.tbhmonitor.data.model

import com.google.gson.annotations.SerializedName

/** Satu elemen array `heroSaveDatas` di dalam response /api/player. */
data class Hero(
    @SerializedName("heroKey") val heroKey: Long = 0,
    @SerializedName("HeroLevel") val level: Long = 0,
    @SerializedName("HeroExp") val exp: Double = 0.0,
    @SerializedName("IsUnLock") val isUnlocked: Boolean = false,
    @SerializedName("AbilityPoint") val abilityPoint: Long = 0,
    @SerializedName("AllocatedHeroAbilityPoint") val allocatedAbilityPoint: Long = 0,
    @SerializedName("equippedItemIds") val equippedItemIds: List<Long> = emptyList(),
    // Typo "SKill" sengaja dipertahankan - persis nama field aslinya di save file.
    @SerializedName("equippedSKillKey") val equippedSkillKeys: List<Long> = emptyList()
) {
    val skillCount: Int get() = equippedSkillKeys.size
    val remainingAbilityPoint: Long get() = abilityPoint - allocatedAbilityPoint
}
