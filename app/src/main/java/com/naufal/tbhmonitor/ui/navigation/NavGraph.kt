package com.naufal.tbhmonitor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.naufal.tbhmonitor.qr.QrScannerScreen
import com.naufal.tbhmonitor.ui.screens.connect.ConnectScreen
import com.naufal.tbhmonitor.ui.screens.connect.RescanQrScreen
import com.naufal.tbhmonitor.ui.screens.dashboard.DashboardScreen
import com.naufal.tbhmonitor.ui.screens.heroes.HeroDetailScreen
import com.naufal.tbhmonitor.ui.screens.heroes.HeroesScreen
import com.naufal.tbhmonitor.ui.screens.inventory.InventoryScreen
import com.naufal.tbhmonitor.ui.screens.runes.RunesScreen

/**
 * Root NavHost buat semua screen.
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
            HeroesScreen(
                onHeroClick = { heroKey -> navController.navigate(Screen.HeroDetail.createRoute(heroKey)) }
            )
        }

        composable(
            route = Screen.HeroDetail.route,
            arguments = listOf(
                navArgument(Screen.HeroDetail.ARG_HERO_KEY) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val heroKey = backStackEntry.arguments?.getLong(Screen.HeroDetail.ARG_HERO_KEY) ?: 0L
            HeroDetailScreen(
                heroKey = heroKey,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Inventory.route) {
            InventoryScreen()
        }

        composable(Screen.Runes.route) {
            RunesScreen()
        }

        composable(Screen.RescanQr.route) {
            RescanQrScreen(
                onConnected = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.RescanQr.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
