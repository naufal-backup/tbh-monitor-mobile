package com.naufal.tbhmonitor.ui.screens.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.tbhmonitor.data.local.ConnectionPreferences
import com.naufal.tbhmonitor.data.model.Item
import com.naufal.tbhmonitor.data.repository.TbhRepository
import com.naufal.tbhmonitor.data.repository.TbhResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** State UI Inventory - pola sama dengan HeroesUiState/DashboardUiState, dipakai endpoint
 *  /api/inventory (bukan /api/player) sesuai ApiService step 4. */
sealed interface InventoryUiState {
    data object Loading : InventoryUiState

    data class Success(
        val items: List<Item>,
        val isRefreshing: Boolean = false,
        val refreshErrorMessage: String? = null
    ) : InventoryUiState

    data class Error(val message: String) : InventoryUiState

    data object NotConnected : InventoryUiState
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TbhRepository(ConnectionPreferences(application))

    private val _uiState = MutableStateFlow<InventoryUiState>(InventoryUiState.Loading)
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    suspend fun autoRefreshLoop() {
        while (true) {
            loadItems(isManualRefresh = false)
            delay(AUTO_REFRESH_INTERVAL_MS)
        }
    }

    fun refresh() {
        viewModelScope.launch { loadItems(isManualRefresh = true) }
    }

    private suspend fun loadItems(isManualRefresh: Boolean) {
        val current = _uiState.value
        if (isManualRefresh && current is InventoryUiState.Success) {
            _uiState.value = current.copy(isRefreshing = true, refreshErrorMessage = null)
        }

        when (val result = repository.getInventory()) {
            is TbhResult.Success -> {
                _uiState.value = InventoryUiState.Success(items = result.data)
            }

            is TbhResult.Error -> {
                val stillCurrent = _uiState.value
                _uiState.value = if (stillCurrent is InventoryUiState.Success) {
                    stillCurrent.copy(isRefreshing = false, refreshErrorMessage = result.message)
                } else {
                    InventoryUiState.Error(result.message)
                }
            }

            TbhResult.NotConnected -> {
                _uiState.value = InventoryUiState.NotConnected
            }
        }
    }

    private companion object {
        const val AUTO_REFRESH_INTERVAL_MS = 10_000L
    }
}
