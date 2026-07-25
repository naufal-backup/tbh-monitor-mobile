package com.naufal.tbhmonitor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

/** Satu tab bottom nav: route tujuan + label + ikon. */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

/**
 * 4 tab utama app - urutan list ini juga nentuin urutan tampil di bottom nav.
 * ConnectScreen, QrScannerScreen, dan HeroDetailScreen SENGAJA gak masuk sini,
 * lihat [shouldShowBottomBar].
 */
val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Dashboard", Icons.Filled.Dashboard),
    BottomNavItem(Screen.Heroes, "Heroes", Icons.Filled.Groups),
    BottomNavItem(Screen.Inventory, "Inventory", Icons.Filled.Inventory2),
    BottomNavItem(Screen.Runes, "Runes", Icons.Filled.AutoAwesome)
)

/**
 * True kalau [route] termasuk salah satu dari 4 tab utama. Dipakai root Scaffold (step 16)
 * buat nentuin kapan bottom bar dirender - disembunyikan otomatis di Connect/QrScanner/
 * HeroDetail biar screen-screen itu kerasa fokus/immersive.
 */
fun shouldShowBottomBar(route: String?): Boolean =
    bottomNavItems.any { it.screen.route == route }

/**
 * Bottom navigation bar Material3. 4 item pertama ([bottomNavItems]) klik pakai pola
 * standar Navigation Compose: popUpTo(startDestination){saveState=true} + launchSingleTop
 * + restoreState, biar state tiap tab (misal posisi scroll Inventory) gak ke-reset pas
 * pindah-pindah tab.
 *
 * Item ke-5 ("Scan") SENGAJA beda - itu tombol aksi buka [Screen.RescanQr] (halaman kamera
 * buat scan ulang QR & ganti/reconnect server), BUKAN tab konten yang persistent, jadi
 * `selected` di-hardcode false (gak pernah "aktif" kayak 4 tab lainnya) dan navigate-nya gak
 * pakai popUpTo/saveState - cukup di-push biasa, balik lewat tombol back/batal di halamannya.
 */
@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.RescanQr.route) },
            icon = { Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan QR") },
            label = { Text("Scan") }
        )
    }
}
