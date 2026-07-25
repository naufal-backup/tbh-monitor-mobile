package com.naufal.tbhmonitor.ui.screens.connect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naufal.tbhmonitor.ui.theme.TbhMonitorTheme

/**
 * Layar awal: user pilih scan QR (buka [com.naufal.tbhmonitor.qr.QrScannerScreen] lewat
 * NavGraph) atau ketik manual URL server desktop app, lalu tersambung ke Dashboard.
 *
 * [scannedUrl] & [onConsumedScannedUrl] itu jembatan hasil scan QR balik kesini - QR
 * scanner ada di route terpisah (bukan dialog di atas screen ini), jadi hasilnya dikirim
 * lewat SavedStateHandle punya back stack entry Connect (pola standar Navigation Compose
 * buat "return a result antar screen"). Lihat NavGraph.kt buat wiring lengkapnya.
 */
@Composable
fun ConnectScreen(
    scannedUrl: String?,
    onConsumedScannedUrl: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onConnected: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConnectViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(scannedUrl) {
        if (!scannedUrl.isNullOrBlank()) {
            onConsumedScannedUrl()
            viewModel.connectWithUrl(scannedUrl, onConnected)
        }
    }

    ConnectContent(
        uiState = uiState,
        onScanQrClick = onNavigateToQrScanner,
        onManualUrlChange = viewModel::onManualUrlChange,
        onConnectClick = { viewModel.connectWithUrl(uiState.manualUrlInput, onConnected) },
        modifier = modifier
    )
}

/** Bagian stateless dari ConnectScreen - dipisah biar gampang di-preview & di-test. */
@Composable
private fun ConnectContent(
    uiState: ConnectUiState,
    onScanQrClick: () -> Unit,
    onManualUrlChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TBH Monitor",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pantau gold, hero, item, dan progres rune TaskbarHero langsung dari HP. " +
                "Jalankan local server-nya lewat tab Settings di desktop app, lalu hubungkan di sini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onScanQrClick,
            enabled = !uiState.isConnecting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan QR Code")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  atau input manual  ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.manualUrlInput,
            onValueChange = onManualUrlChange,
            enabled = !uiState.isConnecting,
            label = { Text("URL server, contoh: 192.168.1.5:8080") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            isError = uiState.errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onConnectClick,
            enabled = !uiState.isConnecting && uiState.manualUrlInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hubungkan")
        }

        if (uiState.isConnecting) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Menghubungkan...", style = MaterialTheme.typography.bodySmall)
            }
        }

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectContentPreview() {
    TbhMonitorTheme {
        ConnectContent(
            uiState = ConnectUiState(manualUrlInput = "192.168.1.5:8080"),
            onScanQrClick = {},
            onManualUrlChange = {},
            onConnectClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectContentErrorPreview() {
    TbhMonitorTheme {
        ConnectContent(
            uiState = ConnectUiState(
                manualUrlInput = "192.168.1.5:8080",
                errorMessage = "Gagal terhubung ke server - pastikan HP & PC ada di jaringan yang sama"
            ),
            onScanQrClick = {},
            onManualUrlChange = {},
            onConnectClick = {}
        )
    }
}
