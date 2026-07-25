package com.naufal.tbhmonitor.ui.screens.heroes

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
 * Detail 1 hero (README fitur desktop app: "level, EXP, ability points, skill & gear per
 * slot"). Gear per-slot beneran (nama & stat tiap item) butuh data item yang belum ada
 * padanannya di mobile ini (lihat catatan di data/model/ItemMeta.kt), jadi untuk sekarang
 * gear ditampilin sebagai daftar equippedItemIds mentah - masih berguna buat lihat berapa
 * slot yang keisi, cuma belum nampilin nama/stat itemnya.
 */
@Composable
fun HeroDetailScreen(
    heroKey: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HeroDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(heroKey) {
        viewModel.load(heroKey)
    }

    HeroDetailContent(
        heroKey = heroKey,
        uiState = uiState,
        onBack = onBack,
        onRetry = { viewModel.load(heroKey) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroDetailContent(
    heroKey: Long,
    uiState: HeroDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Hero #$heroKey") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is HeroDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is HeroDetailUiState.NotFound -> {
                    StatusMessage(
                        title = "Hero Tidak Ditemukan",
                        message = "Data hero ini gak ada di save data terbaru.",
                        actionLabel = "Coba Lagi",
                        onAction = onRetry
                    )
                }

                is HeroDetailUiState.Error -> {
                    StatusMessage(
                        title = "Gagal Memuat Data",
                        message = uiState.message,
                        actionLabel = "Coba Lagi",
                        onAction = onRetry
                    )
                }

                is HeroDetailUiState.NotConnected -> {
                    StatusMessage(
                        title = "Belum Terhubung",
                        message = "Belum ada server yang terhubung.",
                        actionLabel = "Kembali",
                        onAction = onBack
                    )
                }

                is HeroDetailUiState.Success -> {
                    HeroDetailBody(hero = uiState.hero)
                }
            }
        }
    }
}

@Composable
private fun HeroDetailBody(hero: Hero, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DetailStatCard(label = "LEVEL", value = hero.level.toString(), modifier = Modifier.weight(1f))
                DetailStatCard(
                    label = "STATUS",
                    value = if (hero.isUnlocked) "Unlocked" else "Locked",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            DetailStatCard(label = "EXP", value = "%,.2f".format(hero.exp), modifier = Modifier.fillMaxWidth())
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DetailStatCard(
                    label = "ABILITY POINT",
                    value = "${hero.allocatedAbilityPoint}/${hero.abilityPoint}",
                    modifier = Modifier.weight(1f)
                )
                DetailStatCard(
                    label = "SISA POINT",
                    value = hero.remainingAbilityPoint.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(text = "Skill (${hero.skillCount})", style = MaterialTheme.typography.titleMedium)
        }
        if (hero.equippedSkillKeys.isEmpty()) {
            item {
                Text(
                    text = "Belum ada skill yang di-equip.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(hero.equippedSkillKeys) { skillKey ->
                RowChip(label = "Skill #$skillKey")
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Gear (${hero.equippedItemIds.size})", style = MaterialTheme.typography.titleMedium)
        }
        if (hero.equippedItemIds.isEmpty()) {
            item {
                Text(
                    text = "Belum ada gear yang di-equip.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(hero.equippedItemIds) { itemId ->
                RowChip(label = "Item #$itemId")
            }
        }
    }
}

@Composable
private fun DetailStatCard(label: String, value: String, modifier: Modifier = Modifier) {
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
            Text(text = value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun RowChip(label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
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

@Preview(showBackground = true)
@Composable
private fun HeroDetailContentPreview() {
    TbhMonitorTheme {
        HeroDetailContent(
            heroKey = 100001,
            uiState = HeroDetailUiState.Success(
                hero = Hero(
                    heroKey = 100001,
                    level = 101,
                    exp = 95_980_000.0,
                    isUnlocked = true,
                    abilityPoint = 50,
                    allocatedAbilityPoint = 42,
                    equippedItemIds = listOf(1, 2, 3),
                    equippedSkillKeys = listOf(10, 11)
                )
            ),
            onBack = {},
            onRetry = {}
        )
    }
}
