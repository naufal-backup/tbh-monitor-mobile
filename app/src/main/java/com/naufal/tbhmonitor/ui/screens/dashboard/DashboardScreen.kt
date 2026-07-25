package com.naufal.tbhmonitor.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.naufal.tbhmonitor.data.model.Hero
import com.naufal.tbhmonitor.data.model.Player
import com.naufal.tbhmonitor.ui.theme.LocalTbhColors
import com.naufal.tbhmonitor.ui.theme.TbhMonitorTheme

/** Jumlah hero yang ditampilin di preview list Dashboard, sisanya lewat "Lihat Semua". */
private const val HERO_PREVIEW_COUNT = 5

/**
 * Ringkasan gold, jumlah hero, jumlah item, dan progres rune - juga daftar hero singkat
 * (README fitur desktop app: "Dashboard - ringkasan gold, jumlah hero, item, progres rune,
 * dan daftar hero singkat").
 */
@Composable
fun DashboardScreen(
    onViewAllHeroes: () -> Unit,
    onDisconnected: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.autoRefreshLoop()
    }

    DashboardContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onViewAllHeroes = onViewAllHeroes,
        onDisconnectClick = { viewModel.disconnect(onDisconnected) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onRefresh: () -> Unit,
    onViewAllHeroes: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is DashboardUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is DashboardUiState.NotConnected -> {
                StatusMessage(
                    title = "Belum Terhubung",
                    message = "Belum ada server yang terhubung. Kembali ke layar awal buat scan QR / input manual.",
                    actionLabel = "Ke Layar Awal",
                    onAction = onDisconnectClick
                )
            }

            is DashboardUiState.Error -> {
                StatusMessage(
                    title = "Gagal Memuat Data",
                    message = uiState.message,
                    actionLabel = "Coba Lagi",
                    onAction = onRefresh,
                    secondaryActionLabel = "Ganti Koneksi",
                    onSecondaryAction = onDisconnectClick
                )
            }

            is DashboardUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    DashboardBody(
                        player = uiState.player,
                        refreshErrorMessage = uiState.refreshErrorMessage,
                        onViewAllHeroes = onViewAllHeroes,
                        onDisconnectClick = onDisconnectClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardBody(
    player: Player,
    refreshErrorMessage: String?,
    onViewAllHeroes: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dashboard", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = onDisconnectClick) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "Ganti Koneksi"
                    )
                }
            }
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
            val extendedColors = LocalTbhColors.current
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        label = "GOLD",
                        value = formatNumber(player.gold),
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "HEROES",
                        value = player.heroes.size.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        label = "ITEMS",
                        value = player.items.size.toString(),
                        color = extendedColors.bossBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "RUNES",
                        value = "${player.unlockedRuneCount}/${player.runes.size}",
                        color = extendedColors.runePurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Hero", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onViewAllHeroes) {
                    Text("Lihat Semua")
                }
            }
        }

        if (player.heroes.isEmpty()) {
            item {
                Text(
                    text = "Belum ada data hero.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(player.heroes.sortedByDescending { it.level }.take(HERO_PREVIEW_COUNT)) { hero ->
                HeroPreviewRow(hero)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.displaySmall, color = color)
        }
    }
}

@Composable
private fun HeroPreviewRow(hero: Hero, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Hero #${hero.heroKey}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Lv. ${hero.level}", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun StatusMessage(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
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
        if (secondaryActionLabel != null && onSecondaryAction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onSecondaryAction) { Text(secondaryActionLabel) }
        }
    }
}

/** Format angka pakai pemisah ribuan (mis. 1234567 -> "1,234,567") buat angka gold yang gede. */
private fun formatNumber(value: Long): String = "%,d".format(value)

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    TbhMonitorTheme {
        DashboardContent(
            uiState = DashboardUiState.Success(
                player = Player(
                    heroes = listOf(
                        Hero(heroKey = 100001, level = 42),
                        Hero(heroKey = 100002, level = 35),
                        Hero(heroKey = 100003, level = 28)
                    )
                )
            ),
            onRefresh = {},
            onViewAllHeroes = {},
            onDisconnectClick = {}
        )
    }
}
