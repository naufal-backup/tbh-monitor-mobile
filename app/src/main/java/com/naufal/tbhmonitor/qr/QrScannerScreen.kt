@file:OptIn(ExperimentalPermissionsApi::class, androidx.camera.core.ExperimentalGetImage::class)

package com.naufal.tbhmonitor.qr

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "QrScannerScreen"

/**
 * Screen scan QR code (CameraX + ML Kit) buat dapetin base URL server desktop app.
 *
 * Komponen ini sengaja "bodoh" - gak tau apa-apa soal ConnectionPreferences atau
 * TbhRepository. [onQrCodeScanned] cuma ngasih tau raw string hasil scan ke caller;
 * ConnectViewModel (step 11) nanti yang manggil repository.connect() buat beneran
 * nyimpennya. Ini biar screen ini reusable & gampang di-test tanpa perlu Context/DataStore.
 */
@Composable
fun QrScannerScreen(
    onQrCodeScanned: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Box(modifier = modifier.fillMaxSize()) {
        when (val status = cameraPermissionState.status) {
            is PermissionStatus.Granted -> {
                CameraPreview(
                    onBarcodeScanned = onQrCodeScanned,
                    modifier = Modifier.fillMaxSize()
                )
                ScannerOverlay(onCancel = onCancel)
            }

            is PermissionStatus.Denied -> {
                CameraPermissionRequest(
                    permissionState = cameraPermissionState,
                    shouldShowRationale = status.shouldShowRationale,
                    onCancel = onCancel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/** Preview kamera live + analisis tiap frame buat nyari QR code. */
@Composable
private fun CameraPreview(
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val hasScanned = remember { AtomicBoolean(false) }

    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    // Kamera & scanner ML Kit itu resource berat - wajib dilepas begitu screen ditinggalkan,
    // kalau enggak kamera bisa "nyangkut" nyala walau user udah pindah screen.
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            analyzeFrame(
                                imageProxy = imageProxy,
                                scanner = barcodeScanner,
                                hasScanned = hasScanned,
                                onScanned = onBarcodeScanned
                            )
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal bind kamera ke lifecycle", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

/**
 * Proses satu frame kamera: convert ke InputImage, cari QR code lewat ML Kit.
 * [hasScanned] jadi guard supaya [onScanned] cuma ke-trigger sekali walau ML Kit
 * ngasih beberapa hasil beruntun buat QR yang sama dalam sepersekian detik - tanpa
 * ini, caller (ConnectViewModel nanti) bisa aja nerima beberapa kali callback dari
 * satu kali scan.
 */
private fun analyzeFrame(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    hasScanned: AtomicBoolean,
    onScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null || hasScanned.get()) {
        imageProxy.close()
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            val rawValue = barcodes.firstOrNull()?.rawValue
            if (!rawValue.isNullOrBlank() && hasScanned.compareAndSet(false, true)) {
                onScanned(rawValue)
            }
        }
        .addOnFailureListener { e -> Log.e(TAG, "Gagal proses frame", e) }
        .addOnCompleteListener { imageProxy.close() }
}

/** Overlay di atas preview kamera: tombol batal, kotak fokus, dan instruksi. */
@Composable
private fun ScannerOverlay(onCancel: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Batal scan",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(240.dp)
                .border(3.dp, Color.White, RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Arahkan kamera ke QR code di aplikasi desktop tbh-monitor",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        )
    }
}

/** Layar minta izin kamera - ditampilin kalau permission belum granted. */
@Composable
private fun CameraPermissionRequest(
    permissionState: PermissionState,
    shouldShowRationale: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var requestedOnce by remember { mutableStateOf(false) }

    // shouldShowRationale balik false baik pas BELUM PERNAH ditanya sama sekali,
    // maupun pas user pilih "jangan tanya lagi" - satu-satunya cara bedain keduanya
    // di sisi app adalah nge-track sendiri apakah request udah pernah di-launch.
    val isPermanentlyDenied = requestedOnce && !shouldShowRationale

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isPermanentlyDenied) {
                "Izin kamera ditolak. Aktifkan manual lewat Settings buat bisa scan QR code."
            } else {
                "Izin kamera dibutuhkan buat scan QR code dari aplikasi desktop tbh-monitor."
            },
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isPermanentlyDenied) {
            Button(onClick = { openAppSettings(context) }) {
                Text("Buka Settings")
            }
        } else {
            Button(
                onClick = {
                    requestedOnce = true
                    permissionState.launchPermissionRequest()
                }
            ) {
                Text("Izinkan Akses Kamera")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onCancel) {
            Text("Kembali")
        }
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
