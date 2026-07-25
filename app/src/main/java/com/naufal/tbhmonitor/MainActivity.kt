package com.naufal.tbhmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.naufal.tbhmonitor.data.local.ConnectionPreferences
import com.naufal.tbhmonitor.data.repository.TbhRepository
import com.naufal.tbhmonitor.ui.navigation.BottomNavBar
import com.naufal.tbhmonitor.ui.navigation.NavGraph
import com.naufal.tbhmonitor.ui.navigation.Screen
import com.naufal.tbhmonitor.ui.navigation.shouldShowBottomBar
import com.naufal.tbhmonitor.ui.theme.TbhMonitorTheme

/**
 * Satu-satunya Activity di app ini (single-activity architecture, semua screen adalah
 * composable lewat [NavGraph]). Tugasnya: pasang tema, cek koneksi tersimpan buat nentuin
 * start destination (Dashboard kalau udah pernah connect, Connect kalau belum - lihat
 * [TbhRepository.restoreSavedConnection], step 7), lalu render Scaffold berisi [NavGraph]
 * + [BottomNavBar] (disembunyikan otomatis di layar yang bukan salah satu dari 4 tab utama).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TbhMonitorTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    // null = masih ngecek DataStore, belum tau start destination-nya apa.
    var startDestination by remember { mutableStateOf<String?>(null) }
    val appContext = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
        val repository = TbhRepository(ConnectionPreferences(appContext))
        val hasSavedConnection = repository.restoreSavedConnection()
        startDestination = if (hasSavedConnection) Screen.Dashboard.route else Screen.Connect.route
    }

    val destination = startDestination
    if (destination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                BottomNavBar(navController = navController)
            }
        }
    ) { padding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(padding),
            startDestination = destination
        )
    }
}
