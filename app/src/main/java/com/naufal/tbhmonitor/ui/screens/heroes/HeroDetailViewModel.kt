package com.naufal.tbhmonitor.ui.screens.heroes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.tbhmonitor.data.local.ConnectionPreferences
import com.naufal.tbhmonitor.data.model.Hero
import com.naufal.tbhmonitor.data.repository.TbhRepository
import com.naufal.tbhmonitor.data.repository.TbhResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HeroDetailUiState {
    data object Loading : HeroDetailUiState
    data class Success(val hero: Hero) : HeroDetailUiState

    /** Player berhasil di-fetch tapi gak ada hero dengan heroKey yang diminta - seharusnya
     *  gak kejadian lewat navigasi normal (heroKey selalu dari list Heroes), tapi tetap
     *  dihandle biar gak nge-crash kalau ada race condition (mis. save data berubah). */
    data object NotFound : HeroDetailUiState

    data class Error(val message: String) : HeroDetailUiState
    data object NotConnected : HeroDetailUiState
}

/**
 * Gak ada endpoint /api/player/{heroKey} tersendiri di backend, jadi tetap fetch seluruh
 * getPlayer() lalu cari hero yang cocok - sama seperti HeroesViewModel, cuma di-filter ke
 * satu hero. Dipisah dari HeroesViewModel (bukan share instance) karena route HeroDetail
 * punya NavBackStackEntry sendiri di Navigation Compose, jadi viewModel() defaultnya juga
 * bikin instance ViewModel baru - lihat NavGraph.kt.
 */
class HeroDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TbhRepository(ConnectionPreferences(application))

    private val _uiState = MutableStateFlow<HeroDetailUiState>(HeroDetailUiState.Loading)
    val uiState: StateFlow<HeroDetailUiState> = _uiState.asStateFlow()

    fun load(heroKey: Long) {
        viewModelScope.launch {
            _uiState.value = HeroDetailUiState.Loading
            when (val result = repository.getPlayer()) {
                is TbhResult.Success -> {
                    val hero = result.data.heroes.find { it.heroKey == heroKey }
                    _uiState.value = if (hero != null) {
                        HeroDetailUiState.Success(hero)
                    } else {
                        HeroDetailUiState.NotFound
                    }
                }

                is TbhResult.Error -> {
                    _uiState.value = HeroDetailUiState.Error(result.message)
                }

                TbhResult.NotConnected -> {
                    _uiState.value = HeroDetailUiState.NotConnected
                }
            }
        }
    }
}
