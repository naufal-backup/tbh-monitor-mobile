package com.naufal.tbhmonitor.data.model

/**
 * Derivasi grade & tipe item dari [Item.itemKey] - PORT LANGSUNG dari `item_grade()`,
 * `item_type()`, dan `grade_name()` di desktop app (tbh-monitor/src/main.rs baris 226-253).
 *
 * Save file gak nyimpen grade/tipe sebagai field terpisah - keduanya "encode" langsung di
 * angka itemKey (grade = digit ke-3 kalau itemKey 6 digit, tipe = itemKey / 10000), jadi bisa
 * didapat tanpa perlu data tabel item terpisah. Beda dari NAMA item/hero yang butuh
 * names_en.json - itu belum ada padanannya di mobile app ini, makanya masih ditampilin
 * sebagai "Item #key" (lihat InventoryScreen, HeroesScreen).
 */
object ItemMeta {

    /** 0-9, index ke [com.naufal.tbhmonitor.ui.theme.ItemGradeColors]. 0 kalau gak dikenali. */
    fun grade(itemKey: Long): Int {
        val s = itemKey.toString()
        return if (s.length == 6) s[2].digitToIntOrNull() ?: 0 else 0
    }

    fun gradeName(grade: Int): String = when (grade) {
        0 -> "Common"
        1 -> "Uncommon"
        2 -> "Rare"
        3 -> "Legendary"
        4 -> "Immortal"
        5 -> "Arcana"
        6 -> "Beyond"
        7 -> "Celestial"
        8 -> "Divine"
        9 -> "Cosmic"
        else -> "Unknown"
    }

    fun type(itemKey: Long): String = when (itemKey / 10000) {
        11L -> "Gem"
        12L -> "Material"
        13L -> "Scroll"
        14L -> "Ingot"
        16L -> "Coin"
        19L -> "Soulstone"
        30L -> "Sword"
        31L -> "Bow"
        32L -> "Staff"
        33L -> "Scepter"
        34L -> "Crossbow"
        35L -> "Axe"
        40L -> "Shield"
        41L -> "Arrow"
        42L -> "Orb"
        43L -> "Tome"
        44L -> "Bolt"
        45L -> "Hatchet"
        50L -> "Helmet"
        51L -> "Armor"
        52L -> "Gloves"
        53L -> "Boots"
        60L -> "Amulet"
        61L -> "Earring"
        62L -> "Ring"
        63L -> "Bracer"
        91L -> "Stage Box"
        92L -> "Boss Box"
        else -> "Item"
    }
}

val Item.grade: Int get() = ItemMeta.grade(itemKey)
val Item.gradeName: String get() = ItemMeta.gradeName(grade)
val Item.itemType: String get() = ItemMeta.type(itemKey)
