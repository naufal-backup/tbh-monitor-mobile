package com.naufal.tbhmonitor.ui.screens.inventory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import com.naufal.tbhmonitor.data.model.ItemMeta

/**
 * Icon representatif per tipe item (hasil [ItemMeta.type]) - dipakai InventoryScreen.
 *
 * Gak ada icon asli per-item (itu butuh data/asset dari game yang belum ada padanannya di
 * mobile, lihat catatan di ItemMeta.kt), jadi ini dipetakan ke icon Material yang paling
 * mewakili KATEGORI item-nya (senjata, armor, currency, dll) - cukup buat bedain sekilas
 * pas scroll list, warna badge grade yang tetap jadi penanda utama rarity-nya.
 */
fun itemTypeIcon(type: String): ImageVector = when (type) {
    "Gem" -> Icons.Filled.Diamond
    "Material" -> Icons.Filled.Construction
    "Scroll" -> Icons.Filled.Description
    "Ingot" -> Icons.Filled.Construction
    "Coin" -> Icons.Filled.MonetizationOn
    "Soulstone" -> Icons.Filled.Science

    // Senjata (Sword/Bow/Staff/Scepter/Crossbow/Axe/Hatchet) gak ada icon spesifik di
    // Material Icons, jadi disamain ke satu icon "daya serang".
    "Sword", "Bow", "Staff", "Scepter", "Crossbow", "Axe", "Hatchet" -> Icons.Filled.Bolt
    "Arrow" -> Icons.AutoMirrored.Filled.ArrowForward
    "Orb" -> Icons.Filled.Circle
    "Tome" -> Icons.AutoMirrored.Filled.MenuBook
    "Bolt" -> Icons.Filled.Bolt

    // Gear yang dipakai di badan (Helmet/Armor/Gloves/Boots) disamain ke satu icon "pakaian".
    "Helmet", "Armor", "Gloves", "Boots" -> Icons.Filled.Checkroom
    "Shield" -> Icons.Filled.Shield

    // Perhiasan (Amulet/Earring/Ring/Bracer).
    "Amulet", "Earring", "Ring", "Bracer" -> Icons.Filled.Diamond

    "Stage Box", "Boss Box" -> Icons.Filled.CardGiftcard

    else -> Icons.Filled.Inventory2
}
