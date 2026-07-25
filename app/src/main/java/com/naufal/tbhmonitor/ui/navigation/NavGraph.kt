package com.naufal.tbhmonitor.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.naufal.tbhmonitor.qr.QrScannerScreen
import com.naufal.tbhmonitor.ui.screens.connect.ConnectScreen
import com.naufal.tbhmonitor.ui.screens.dashboard.DashboardScreen

/**
 * Root NavHost buat semua screen. Screen yang belum dibikin (Heroes, HeroDetail,
 * Inventory, Runes - step 13-15) sementara diisi [PlaceholderScreen] biar project ini
 * tetap compile & bisa dijalanin buat testing Connect + Dashboard lebih dulu. Ganti
 * composable-nya satu-satu begitu step terkait selesai.
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
        composable(Screen.Connect.route) { backStackEntry ->
            val scannedUrl by backStackEntry.savedStateHandle
                .getStateFlow<String?>(Screen.QrScanner.RESULT_KEY_SCANNED_URL, null)
                .collectAsStateWithLifecycle()

            ConnectScreen(
                scannedUrl = scannedUrl,
                onConsumedScannedUrl = {
                    backStackEntry.savedStateHandle[Screen.QrScanner.RESULT_KEY_SCANNED_URL] = null
                },
                onNavigateToQrScanner = { navController.navigate(Screen.QrScanner.route) },
                onConnected = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Connect.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.QrScanner.route) {
            QrScannerScreen(
                onQrCodeScanned = { url ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(Screen.QrScanner.RESULT_KEY_SCANNED_URL, url)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onViewAllHeroes = { navController.navigate(Screen.Heroes.route) },
                onDisconnected = {
                    navController.navigate(Screen.Connect.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
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
