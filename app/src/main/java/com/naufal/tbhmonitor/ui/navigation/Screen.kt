package com.naufal.tbhmonitor.ui.navigation

/**
 * Semua route yang ada di app. [NavGraph] pakai [route] buat daftarin composable-nya,
 * [BottomNavBar] pakai subset-nya (lihat bottomNavItems) buat 4 tab utama.
 */
sealed class Screen(val route: String) {

    /** Layar awal - pilih scan QR atau input manual URL server. */
    data object Connect : Screen("connect")

    /** Kamera scan QR (CameraX + ML Kit), dibuka dari ConnectScreen. */
    data object QrScanner : Screen("qr_scanner") {
        /**
         * Key SavedStateHandle buat kirim hasil scan balik ke ConnectScreen - pola
         * standar Navigation Compose buat "return a result" antar screen tanpa
         * perlu share instance ViewModel. Lihat NavGraph.kt & ConnectScreen.kt.
         */
        const val RESULT_KEY_SCANNED_URL = "scanned_url"
    }

    /** Tab bottom nav 1/4 - ringkasan gold, hero, item, progres rune. */
    data object Dashboard : Screen("dashboard")

    /** Tab bottom nav 2/4 - daftar semua hero. */
    data object Heroes : Screen("heroes")

    /** Sub-halaman dari Heroes, BUKAN tab bottom nav - detail 1 hero. */
    data object HeroDetail : Screen("hero_detail/{heroKey}") {
        const val ARG_HERO_KEY = "heroKey"

        /** Bangun route konkret buat navigasi, misal Screen.HeroDetail.createRoute(120001). */
        fun createRoute(heroKey: Long) = "hero_detail/$heroKey"
    }

    /** Tab bottom nav 3/4 - daftar item, search/filter/sort. */
    data object Inventory : Screen("inventory")

    /** Tab bottom nav 4/4 - rune tree + daftar pet/companion. */
    data object Runes : Screen("runes")
}
