package com.naufal.tbhmonitor.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.tbhmonitor.data.local.ConnectionPreferences
import com.naufal.tbhmonitor.data.model.Player
import com.naufal.tbhmonitor.data.repository.TbhRepository
import com.naufal.tbhmonitor.data.repository.TbhResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State UI Dashboard. [Success.refreshErrorMessage] SENGAJA dipisah dari state [Error]
 * penuh - kalau refresh (auto atau manual) gagal padahal udah ada data sebelumnya, data
 * lama tetap ditampilin (gak nge-blank-in seluruh screen cuma gara-gara satu kali auto-refresh
 * gagal/timeout) sambil nampilin pesan kecil. State [Error] penuh cuma kepakai kalau
 * cold-start load-nya sendiri yang gagal (belum ada data sama sekali buat ditampilin).
 */
sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val player: Player,
        val isRefreshing: Boolean = false,
        val refreshErrorMessage: String? = null
    ) : DashboardUiState

    data class Error(val message: String) : DashboardUiState

    /** Seharusnya jarang kejadian di tengah sesi (cuma abis disconnect()), tapi tetap dihandle. */
    data object NotConnected : DashboardUiState
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TbhRepository(ConnectionPreferences(application))

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /**
     * Loop auto-refresh (fitur "Auto-refresh data secara berkala" dari desktop app).
     * Sengaja dijalanin dari LaunchedEffect di DashboardScreen (bukan viewModelScope.launch
     * di init{}), biar otomatis berhenti pas user pindah tab (LaunchedEffect di-cancel pas
     * composable-nya keluar dari komposisi) dan lanjut lagi begitu balik ke tab Dashboard -
     * gak buang-buang request polling API pas user lagi liat tab lain.
     */
    suspend fun autoRefreshLoop() {
        while (true) {
            loadPlayer(isManualRefresh = false)
            delay(AUTO_REFRESH_INTERVAL_MS)
        }
    }

    /** Dipanggil dari gesture pull-to-refresh. */
    fun refresh() {
        viewModelScope.launch { loadPlayer(isManualRefresh = true) }
    }

    /** Dipanggil dari tombol "Ganti Koneksi" - putus koneksi lalu balik ke ConnectScreen. */
    fun disconnect(onDisconnected: () -> Unit) {
        viewModelScope.launch {
            repository.disconnect()
            onDisconnected()
        }
    }

    private suspend fun loadPlayer(isManualRefresh: Boolean) {
        val current = _uiState.value
        if (isManualRefresh && current is DashboardUiState.Success) {
            _uiState.value = current.copy(isRefreshing = true, refreshErrorMessage = null)
        }

        when (val result = repository.getPlayer()) {
            is TbhResult.Success -> {
                _uiState.value = DashboardUiState.Success(player = result.data)
            }

            is TbhResult.Error -> {
                val stillCurrent = _uiState.value
                _uiState.value = if (stillCurrent is DashboardUiState.Success) {
                    stillCurrent.copy(isRefreshing = false, refreshErrorMessage = result.message)
                } else {
                    DashboardUiState.Error(result.message)
                }
            }

            TbhResult.NotConnected -> {
                _uiState.value = DashboardUiState.NotConnected
            }
        }
    }

    private companion object {
        const val AUTO_REFRESH_INTERVAL_MS = 10_000L
    }
}
