package com.naufal.tbhmonitor.ui.screens.runes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.tbhmonitor.data.local.ConnectionPreferences
import com.naufal.tbhmonitor.data.model.Pet
import com.naufal.tbhmonitor.data.model.Rune
import com.naufal.tbhmonitor.data.repository.TbhRepository
import com.naufal.tbhmonitor.data.repository.TbhResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Rune & Pet sama-sama datang dari satu response /api/player (lihat Player.kt step 3),
 *  jadi satu state ini nampung keduanya sekaligus - gak perlu 2 request terpisah. */
sealed interface RunesUiState {
    data object Loading : RunesUiState

    data class Success(
        val runes: List<Rune>,
        val pets: List<Pet>,
        val isRefreshing: Boolean = false,
        val refreshErrorMessage: String? = null
    ) : RunesUiState

    data class Error(val message: String) : RunesUiState

    data object NotConnected : RunesUiState
}

class RunesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TbhRepository(ConnectionPreferences(application))

    private val _uiState = MutableStateFlow<RunesUiState>(RunesUiState.Loading)
    val uiState: StateFlow<RunesUiState> = _uiState.asStateFlow()

    suspend fun autoRefreshLoop() {
        while (true) {
            loadData(isManualRefresh = false)
            delay(AUTO_REFRESH_INTERVAL_MS)
        }
    }

    fun refresh() {
        viewModelScope.launch { loadData(isManualRefresh = true) }
    }

    private suspend fun loadData(isManualRefresh: Boolean) {
        val current = _uiState.value
        if (isManualRefresh && current is RunesUiState.Success) {
            _uiState.value = current.copy(isRefreshing = true, refreshErrorMessage = null)
        }

        when (val result = repository.getPlayer()) {
            is TbhResult.Success -> {
                _uiState.value = RunesUiState.Success(runes = result.data.runes, pets = result.data.pets)
            }

            is TbhResult.Error -> {
                val stillCurrent = _uiState.value
                _uiState.value = if (stillCurrent is RunesUiState.Success) {
                    stillCurrent.copy(isRefreshing = false, refreshErrorMessage = result.message)
                } else {
                    RunesUiState.Error(result.message)
                }
            }

            TbhResult.NotConnected -> {
                _uiState.value = RunesUiState.NotConnected
            }
        }
    }

    private companion object {
        const val AUTO_REFRESH_INTERVAL_MS = 10_000L
    }
}
