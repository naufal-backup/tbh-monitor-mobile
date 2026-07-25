package com.naufal.tbhmonitor.ui.screens.connect

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.tbhmonitor.data.local.ConnectionPreferences
import com.naufal.tbhmonitor.data.repository.TbhRepository
import com.naufal.tbhmonitor.data.repository.TbhResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** State UI buat ConnectScreen. */
data class ConnectUiState(
    val manualUrlInput: String = "",
    val isConnecting: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel buat ConnectScreen - satu-satunya tempat yang beneran manggil
 * [TbhRepository.connect], baik dari hasil scan QR maupun input manual. QrScannerScreen
 * (step 8) sendiri gak tau apa-apa soal repository, cuma ngasih raw string hasil scan
 * lewat callback yang diteruskan ke [connectWithUrl] (lihat NavGraph.kt buat alurnya).
 *
 * Pakai AndroidViewModel (bukan ViewModel + custom Factory) biar bisa bikin
 * ConnectionPreferences sendiri dari applicationContext tanpa perlu DI framework -
 * konsisten sama pendekatan minim-dependency di seluruh project ini.
 */
class ConnectViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TbhRepository(ConnectionPreferences(application))

    private val _uiState = MutableStateFlow(ConnectUiState())
    val uiState: StateFlow<ConnectUiState> = _uiState.asStateFlow()

    fun onManualUrlChange(value: String) {
        _uiState.update { it.copy(manualUrlInput = value, errorMessage = null) }
    }

    /** Dipakai RescanQrScreen buat balik nampilin kamera lagi sesudah gagal connect,
     *  tanpa perlu nyentuh manualUrlInput (beda dari onManualUrlChange). */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Simpan [url] sebagai base URL baru, lalu langsung tes beneran nyambung atau
     * enggak (panggil getPlayer()) sebelum navigasi ke Dashboard - biar user gak
     * "kejebak" di Dashboard kosong baru ketauan belakangan URL-nya salah / server
     * desktop app belum dijalanin.
     */
    fun connectWithUrl(url: String, onConnected: () -> Unit) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "URL server belum diisi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true, errorMessage = null) }

            repository.connect(trimmed)

            when (val result = repository.getPlayer()) {
                is TbhResult.Success -> {
                    _uiState.update { it.copy(isConnecting = false) }
                    onConnected()
                }

                is TbhResult.Error -> {
                    _uiState.update { it.copy(isConnecting = false, errorMessage = result.message) }
                }

                TbhResult.NotConnected -> {
                    // Gak seharusnya kejadian - baru aja connect() di atas - tapi tetep
                    // di-handle biar `when` exhaustive & UI gak diem aja kalau ini kejadian.
                    _uiState.update {
                        it.copy(isConnecting = false, errorMessage = "Gagal menyimpan koneksi, coba lagi")
                    }
                }
            }
        }
    }
}
