package com.naufal.tbhmonitor.ui.screens.runes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naufal.tbhmonitor.data.model.Pet
import com.naufal.tbhmonitor.data.model.Rune
import com.naufal.tbhmonitor.ui.theme.LocalTbhColors
import com.naufal.tbhmonitor.ui.theme.TbhMonitorTheme

/**
 * Tab bottom nav ke-4 - progres rune tree & daftar pet/companion (README fitur desktop app:
 * "Runes & Pets - progres rune tree dan daftar pet/companion"). Save data cuma nyimpen
 * RuneKey+Level per node (lihat Rune.kt step 3), gak ada info posisi/koneksi antar-node,
 * jadi "tree"-nya ditampilin sebagai grid progres (bukan diagram pohon literal).
 */
@Composable
fun RunesScreen(
    modifier: Modifier = Modifier,
    viewModel: RunesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.autoRefreshLoop()
    }

    RunesContent(uiState = uiState, onRefresh = viewModel::refresh, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RunesContent(
    uiState: RunesUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is RunesUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is RunesUiState.NotConnected -> {
                StatusMessage(
                    title = "Belum Terhubung",
                    message = "Belum ada server yang terhubung.",
                    actionLabel = "Coba Lagi",
                    onAction = onRefresh
                )
            }

            is RunesUiState.Error -> {
                StatusMessage(
                    title = "Gagal Memuat Data",
                    message = uiState.message,
                    actionLabel = "Coba Lagi",
                    onAction = onRefresh
                )
            }

            is RunesUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    RunesBody(runes = uiState.runes, pets = uiState.pets, refreshErrorMessage = uiState.refreshErrorMessage)
                }
            }
        }
    }
}

@Composable
private fun RunesBody(runes: List<Rune>, pets: List<Pet>, refreshErrorMessage: String?) {
    val extendedColors = LocalTbhColors.current
    val unlockedCount = runes.count { it.isUnlocked }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "Runes", style = MaterialTheme.typography.headlineMedium)
        }

        refreshErrorMessage?.let { message ->
            item {
                Text(
                    text = "Gagal refresh: $message",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            Text(
                text = "Progres: $unlockedCount / ${runes.size} node unlocked",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (runes.isEmpty()) {
            item {
                Text(
                    text = "Belum ada data rune.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    runes.sortedByDescending { it.level }.forEach { rune ->
                        RuneNodeChip(rune = rune, unlockedColor = extendedColors.runePurple)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Pets (${pets.count { it.isUnlocked }}/${pets.size})", style = MaterialTheme.typography.titleLarge)
        }

        if (pets.isEmpty()) {
            item {
                Text(
                    text = "Belum ada data pet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pets.forEach { pet ->
                        PetChip(pet = pet)
                    }
                }
            }
        }
    }
}

@Composable
private fun RuneNodeChip(rune: Rune, unlockedColor: Color, modifier: Modifier = Modifier) {
    val color = if (rune.isUnlocked) unlockedColor else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .width(76.dp)
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "#${rune.runeKey}", style = MaterialTheme.typography.labelSmall, color = color)
        Text(text = "Lv.${rune.level}", style = MaterialTheme.typography.titleSmall, color = color)
    }
}

@Composable
private fun PetChip(pet: Pet, modifier: Modifier = Modifier) {
    val color = when {
        pet.isEquipped -> MaterialTheme.colorScheme.secondary
        pet.isUnlocked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val status = when {
        pet.isEquipped -> "Equipped"
        pet.isUnlocked -> "Unlocked"
        else -> "Locked"
    }
    Column(
        modifier = modifier
            .width(90.dp)
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Pet #${pet.petKey}", style = MaterialTheme.typography.labelSmall, color = color)
        Text(text = status, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun StatusMessage(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(onClick = onAction) { Text(actionLabel) }
    }
}

@Preview(showBackground = true)
@Composable
private fun RunesContentPreview() {
    TbhMonitorTheme {
        RunesContent(
            uiState = RunesUiState.Success(
                runes = listOf(Rune(runeKey = 1, level = 5), Rune(runeKey = 2, level = 0)),
                pets = listOf(Pet(petKey = 1, isUnlocked = true, isEquipped = true), Pet(petKey = 2))
            ),
            onRefresh = {}
        )
    }
}
