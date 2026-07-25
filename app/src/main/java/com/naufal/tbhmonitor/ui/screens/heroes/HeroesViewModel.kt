package com.naufal.tbhmonitor.ui.screens.heroes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.tbhmonitor.data.local.ConnectionPreferences
import com.naufal.tbhmonitor.data.model.Hero
import com.naufal.tbhmonitor.data.repository.TbhRepository
import com.naufal.tbhmonitor.data.repository.TbhResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** State UI Heroes - pola sama persis dengan [com.naufal.tbhmonitor.ui.screens.dashboard.DashboardUiState]. */
sealed interface HeroesUiState {
    data object Loading : HeroesUiState

    data class Success(
        val heroes: List<Hero>,
        val isRefreshing: Boolean = false,
        val refreshErrorMessage: String? = null
    ) : HeroesUiState

    data class Error(val message: String) : HeroesUiState

    data object NotConnected : HeroesUiState
}

class HeroesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TbhRepository(ConnectionPreferences(application))

    private val _uiState = MutableStateFlow<HeroesUiState>(HeroesUiState.Loading)
    val uiState: StateFlow<HeroesUiState> = _uiState.asStateFlow()

    /** Sama seperti DashboardViewModel.autoRefreshLoop() - dijalankan dari LaunchedEffect
     *  di HeroesScreen supaya otomatis berhenti begitu tab-nya gak lagi aktif. */
    suspend fun autoRefreshLoop() {
        while (true) {
            loadHeroes(isManualRefresh = false)
            delay(AUTO_REFRESH_INTERVAL_MS)
        }
    }

    fun refresh() {
        viewModelScope.launch { loadHeroes(isManualRefresh = true) }
    }

    private suspend fun loadHeroes(isManualRefresh: Boolean) {
        val current = _uiState.value
        if (isManualRefresh && current is HeroesUiState.Success) {
            _uiState.value = current.copy(isRefreshing = true, refreshErrorMessage = null)
        }

        when (val result = repository.getPlayer()) {
            is TbhResult.Success -> {
                _uiState.value = HeroesUiState.Success(heroes = result.data.heroes)
            }

            is TbhResult.Error -> {
                val stillCurrent = _uiState.value
                _uiState.value = if (stillCurrent is HeroesUiState.Success) {
                    stillCurrent.copy(isRefreshing = false, refreshErrorMessage = result.message)
                } else {
                    HeroesUiState.Error(result.message)
                }
            }

            TbhResult.NotConnected -> {
                _uiState.value = HeroesUiState.NotConnected
            }
        }
    }

    private companion object {
        const val AUTO_REFRESH_INTERVAL_MS = 10_000L
    }
}
