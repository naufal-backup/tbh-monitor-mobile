package com.naufal.tbhmonitor.ui.screens.heroes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naufal.tbhmonitor.data.model.Hero
import com.naufal.tbhmonitor.ui.theme.TbhMonitorTheme

/**
 * Tab bottom nav ke-2 - daftar semua hero (README fitur desktop app: "Heroes - detail level,
 * EXP, ability points, skill & gear per slot" - detail lengkapnya ada di [HeroDetailScreen],
 * di sini cuma ringkasan per baris).
 */
@Composable
fun HeroesScreen(
    onHeroClick: (heroKey: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HeroesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.autoRefreshLoop()
    }

    HeroesContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onHeroClick = onHeroClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroesContent(
    uiState: HeroesUiState,
    onRefresh: () -> Unit,
    onHeroClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is HeroesUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is HeroesUiState.NotConnected -> {
                StatusMessage(
                    title = "Belum Terhubung",
                    message = "Belum ada server yang terhubung.",
                    actionLabel = "Coba Lagi",
                    onAction = onRefresh
                )
            }

            is HeroesUiState.Error -> {
                StatusMessage(
                    title = "Gagal Memuat Data",
                    message = uiState.message,
                    actionLabel = "Coba Lagi",
                    onAction = onRefresh
                )
            }

            is HeroesUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    HeroesBody(
                        heroes = uiState.heroes,
                        refreshErrorMessage = uiState.refreshErrorMessage,
                        onHeroClick = onHeroClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroesBody(
    heroes: List<Hero>,
    refreshErrorMessage: String?,
    onHeroClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "Heroes", style = MaterialTheme.typography.headlineMedium)
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

        if (heroes.isEmpty()) {
            item {
                Text(
                    text = "Belum ada data hero.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(heroes.sortedByDescending { it.level }) { hero ->
                HeroRow(hero = hero, onClick = { onHeroClick(hero.heroKey) })
            }
        }
    }
}

@Composable
private fun HeroRow(hero: Hero, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val badgeColor = if (hero.isUnlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(badgeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hero.isUnlocked) Icons.Filled.Person else Icons.Filled.Lock,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Hero #${hero.heroKey}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Lv.${hero.level} \u2022 EXP ${formatExp(hero.exp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            UnlockBadge(isUnlocked = hero.isUnlocked)
        }
    }
}

/** Badge "UNLOCKED" / "LOCKED", padanan langsung dari badge hero di desktop app dashboard. */
@Composable
private fun UnlockBadge(isUnlocked: Boolean, modifier: Modifier = Modifier) {
    val color = if (isUnlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isUnlocked) "UNLOCKED" else "LOCKED",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
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

/** Format EXP gede pakai suffix K/M/B, sama gayanya dengan format gold di desktop app. */
private fun formatExp(exp: Double): String = when {
    exp >= 1_000_000_000 -> "%.2fB".format(exp / 1_000_000_000)
    exp >= 1_000_000 -> "%.2fM".format(exp / 1_000_000)
    exp >= 1_000 -> "%.2fK".format(exp / 1_000)
    else -> "%.2f".format(exp)
}

@Preview(showBackground = true)
@Composable
private fun HeroesContentPreview() {
    TbhMonitorTheme {
        HeroesContent(
            uiState = HeroesUiState.Success(
                heroes = listOf(
                    Hero(heroKey = 100001, level = 101, exp = 95_980_000.0, isUnlocked = true),
                    Hero(heroKey = 100002, level = 1, exp = 0.0, isUnlocked = false)
                )
            ),
            onRefresh = {},
            onHeroClick = {}
        )
    }
}
