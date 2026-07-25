package com.naufal.tbhmonitor.ui.screens.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naufal.tbhmonitor.qr.QrScannerScreen

/**
 * Scan QR buat ganti/reconnect ke server lain - dibuka dari tombol aksi "Scan" di
 * BottomNavBar, bisa diakses dari tab manapun (bukan cuma pas awal belum connect kayak
 * [ConnectScreen]). Hasil scan langsung dipakai buat connectWithUrl() di sini sendiri
 * (reuse [ConnectViewModel] yang sama, instance baru scoped ke route rescan_qr ini),
 * gak perlu bounce hasil scan balik lewat SavedStateHandle kayak alur onboarding.
 */
@Composable
fun RescanQrScreen(
    onConnected: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConnectViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            RescanErrorState(
                message = errorMessage,
                onRetry = viewModel::clearError,
                onCancel = onCancel
            )
        } else {
            QrScannerScreen(
                onQrCodeScanned = { url -> viewModel.connectWithUrl(url, onConnected) },
                onCancel = onCancel,
                modifier = Modifier.fillMaxSize()
            )
            if (uiState.isConnecting) {
                ConnectingOverlay()
            }
        }
    }
}

/** Overlay gelap transparan di atas preview kamera pas lagi verifikasi koneksi ke server baru. */
@Composable
private fun ConnectingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Menghubungkan...", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RescanErrorState(
    message: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Gagal Terhubung",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text("Scan Ulang")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Batal")
        }
    }
}
