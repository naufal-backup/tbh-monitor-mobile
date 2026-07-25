package com.naufal.tbhmonitor.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palet warna TBH Monitor - dicontek 1:1 dari konstanta warna desktop app (tbh-monitor,
 * lihat src/main.rs baris 13-23) biar mobile app ini kerasa sambungan dari desktop app,
 * bukan aplikasi terpisah dengan identitas visual sendiri. Nilai hex di komentar dijaga
 * sama persis biar gampang di-cross-check ke source aslinya kalau ada perubahan di sana.
 */

// --- Base UI chrome (main.rs: BG_DARK, CARD_BG, CARD_BORDER) ---
val BgDark = Color(0xFF0F0F15)
val CardBg = Color(0xFF181922)
val CardBorder = Color(0xFF282A3A)

// --- Teks (main.rs: TEXT_PRIMARY, TEXT_SECONDARY, TEXT_MUTED) ---
val TextPrimary = Color(0xFFE5E7EB)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)

// --- Accent & semantic (main.rs: ACCENT, GREEN, YELLOW, BOSS_BLUE, BOSS_BG) ---
val Accent = Color(0xFF6366F1)     // primary umum, dipakai desktop app buat highlight umum
val Green = Color(0xFF22C55E)      // status positif (misal: koneksi aktif)
val Yellow = Color(0xFFEAB308)     // gold / currency - warna khusus angka gold player
val BossBlue = Color(0xFF2F8BFC)   // highlight elemen spesial (boss, dsb)
val BossBg = Color(0xFF0C142D)

/** Warna khusus tab Runes (node unlocked, progress bar) - main.rs pakai ini inline, bukan named const. */
val RunePurple = Color(0xFFA855F7)

/**
 * Warna error UI (state gagal connect, teks error). TIDAK ada padanannya di desktop app
 * (desktop app baca file lokal langsung, gak punya konsep "gagal koneksi"). Dipilih senada
 * sama grade Immortal (#FC2424) tapi diredupkan, biar gak "berantem" secara visual sama
 * badge rarity Immortal beneran di InventoryScreen.
 */
val ErrorRed = Color(0xFFF87171)

/**
 * Warna rarity/grade item, index 0-9 PERSIS sama urutan grade_color() di src/main.rs
 * (Common -> Cosmic). Dipakai buat badge grade item di InventoryScreen (step 14).
 */
val ItemGradeColors = listOf(
    Color(0xFFE4E4E4), // 0 Common
    Color(0xFF54FC0C), // 1 Uncommon
    Color(0xFF2F8BFC), // 2 Rare
    Color(0xFFFC9C0C), // 3 Legendary
    Color(0xFFFC2424), // 4 Immortal
    Color(0xFFB40CFC), // 5 Arcana
    Color(0xFFFC246C), // 6 Beyond
    Color(0xFF6CCCE4), // 7 Celestial
    Color(0xFFFCE454), // 8 Divine
    Color(0xFFFCFCFC)  // 9 Cosmic
)

/**
 * Background gelap per grade (dari item_grade_bg() di src/main.rs), buat badge grade
 * punya kontras yang cukup nampilin ItemGradeColors di atasnya.
 */
val ItemGradeBackgrounds = listOf(
    Color(0xFF1E1E20), // 0 Common: dark gray
    Color(0xFF0F230F), // 1 Uncommon: dark green
    Color(0xFF0C142D), // 2 Rare: dark blue
    Color(0xFF2A1C08), // 3 Legendary: dark orange
    Color(0xFF2D0A0A), // 4 Immortal: dark red
    Color(0xFF200A30), // 5 Arcana: dark purple
    Color(0xFF300A16), // 6 Beyond: dark pink
    Color(0xFF0F232A), // 7 Celestial: dark cyan
    Color(0xFF2D280C), // 8 Divine: dark gold
    Color(0xFF2A2A30)  // 9 Cosmic: dark white
)
