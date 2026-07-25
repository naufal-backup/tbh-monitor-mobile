# Development Plan

Arsitektur: MVVM, Jetpack Compose untuk UI, Retrofit untuk networking ke backend tbh-monitor.

## Urutan pengerjaan

1. [DONE] build.gradle.kts (project & app level) - setup dependencies
2. [DONE] AndroidManifest.xml - permission INTERNET, CAMERA
3. [DONE] data/model/ - Player.kt, Hero.kt, Item.kt, Rune.kt, Pet.kt, sesuai response /api/data, /api/player, /api/inventory
4. [DONE] data/remote/ApiService.kt - interface Retrofit untuk endpoint /api/data, /api/player, /api/inventory
5. [DONE] data/remote/RetrofitClient.kt - instance Retrofit, base URL dinamis (diisi dari hasil scan QR)
6. [DONE] data/local/ConnectionPreferences.kt - simpan base URL pakai DataStore
7. data/repository/TbhRepository.kt - gabungkan remote + local, dipakai ViewModel
8. qr/QrScannerScreen.kt - scan QR pakai CameraX + ML Kit, hasil scan disimpan ke ConnectionPreferences
9. ui/theme/ - Theme.kt, Color.kt, Type.kt
10. ui/navigation/NavGraph.kt - routing antar screen
11. ui/screens/connect/ConnectScreen.kt + ConnectViewModel.kt - screen awal, scan QR / input manual URL
12. ui/screens/dashboard/DashboardScreen.kt + DashboardViewModel.kt - ringkasan gold, hero, item, progres rune
13. ui/screens/heroes/HeroesScreen.kt, HeroDetailScreen.kt + HeroesViewModel.kt
14. ui/screens/inventory/InventoryScreen.kt + InventoryViewModel.kt - list, search, filter, sorting
15. ui/screens/runes/RunesScreen.kt + RunesViewModel.kt - rune tree & pet
16. MainActivity.kt - entry point, NavHost, cek koneksi yang tersimpan

## Library yang diperlukan

Networking
- retrofit2:retrofit
- retrofit2:converter-gson
- okhttp3:logging-interceptor

Async
- kotlinx-coroutines-android

UI
- androidx.compose (bom, ui, material3, ui-tooling-preview)
- androidx.navigation:navigation-compose
- androidx.lifecycle:lifecycle-viewmodel-compose
- androidx.activity:activity-compose

QR scanner
- androidx.camera: camera-core, camera-camera2, camera-lifecycle, camera-view (CameraX)
- com.google.mlkit:barcode-scanning

Local storage
- androidx.datastore:datastore-preferences

Opsional
- io.coil-kt:coil-compose, kalau nanti ada icon/gambar item
- accompanist-permissions, buat handle izin kamera di Compose
