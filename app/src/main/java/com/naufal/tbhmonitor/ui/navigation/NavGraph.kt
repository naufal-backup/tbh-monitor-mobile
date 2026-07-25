package com.naufal.tbhmonitor.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.naufal.tbhmonitor.qr.QrScannerScreen

/**
 * Root NavHost buat semua screen. Screen yang belum dibikin (Connect, Dashboard, Heroes,
 * HeroDetail, Inventory, Runes - step 11-15) sementara diisi [PlaceholderScreen] biar
 * project ini tetap compile & bisa dijalanin buat testing QrScannerScreen lebih dulu.
 * Ganti composable-nya satu-satu begitu step terkait selesai.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Connect.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Connect.route) {
            PlaceholderScreen("ConnectScreen - step 11")
        }

        composable(Screen.QrScanner.route) {
            QrScannerScreen(
                onQrCodeScanned = { url ->
                    // TODO (step 11): repository.connect(url) lewat ConnectViewModel dulu,
                    // baru navigate ke Dashboard + popBackStack sampai start destination.
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            PlaceholderScreen("DashboardScreen - step 12")
        }

        composable(Screen.Heroes.route) {
            PlaceholderScreen("HeroesScreen - step 13")
        }

        composable(
            route = Screen.HeroDetail.route,
            arguments = listOf(
                navArgument(Screen.HeroDetail.ARG_HERO_KEY) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val heroKey = backStackEntry.arguments?.getLong(Screen.HeroDetail.ARG_HERO_KEY) ?: 0L
            PlaceholderScreen("HeroDetailScreen - step 13 (heroKey=$heroKey)")
        }

        composable(Screen.Inventory.route) {
            PlaceholderScreen("InventoryScreen - step 14")
        }

        composable(Screen.Runes.route) {
            PlaceholderScreen("RunesScreen - step 15")
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
