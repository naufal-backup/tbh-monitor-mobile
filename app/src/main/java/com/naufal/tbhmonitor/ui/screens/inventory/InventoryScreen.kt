package com.naufal.tbhmonitor.ui.screens.inventory

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naufal.tbhmonitor.data.model.Item
import com.naufal.tbhmonitor.data.model.ItemMeta
import com.naufal.tbhmonitor.data.model.gradeName
import com.naufal.tbhmonitor.data.model.grade
import com.naufal.tbhmonitor.data.model.itemType
import com.naufal.tbhmonitor.ui.theme.LocalTbhColors
import com.naufal.tbhmonitor.ui.theme.TbhMonitorTheme

private enum class SortOption(val label: String) {
    QUANTITY("Quantity"),
    GRADE("Grade"),
    LEVEL("Level")
}

/**
 * Tab bottom nav ke-3 - daftar item + search/filter/sorting (README fitur desktop app:
 * "Inventory - list, search, filter, sorting"). Grade & tipe item didapat dari itemKey
 * lewat [com.naufal.tbhmonitor.data.model.ItemMeta] (gak ada endpoint/data terpisah).
 */
@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.autoRefreshLoop()
    }

    InventoryContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryContent(
    uiState: InventoryUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is InventoryUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is InventoryUiState.NotConnected -> {
                StatusMessage(
                    title = "Belum Terhubung",
                    message = "Belum ada server yang terhubung.",
                    actionLabel = "Coba Lagi",
                    onAction = onRefresh
                )
            }

            is InventoryUiState.Error -> {
                StatusMessage(
                    title = "Gagal Memuat Data",
                    message = uiState.message,
                    actionLabel = "Coba Lagi",
                    onAction = onRefresh
                )
            }

            is InventoryUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    InventoryBody(items = uiState.items, refreshErrorMessage = uiState.refreshErrorMessage)
                }
            }
        }
    }
}

@Composable
private fun InventoryBody(items: List<Item>, refreshErrorMessage: String?) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedGrade by rememberSaveable { mutableStateOf(-1) } // -1 = semua grade
    var sortOption by rememberSaveable { mutableStateOf(SortOption.QUANTITY) }

    val filteredSorted = items
        .filter { item ->
            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                item.itemKey.toString().contains(query) ||
                item.uniqueId.toString().contains(query) ||
                item.itemType.lowercase().contains(query) ||
                item.gradeName.lowercase().contains(query)
            val matchesGrade = selectedGrade == -1 || item.grade == selectedGrade
            matchesQuery && matchesGrade
        }
        .let { filtered ->
            when (sortOption) {
                SortOption.QUANTITY -> filtered.sortedByDescending { it.quantity }
                SortOption.GRADE -> filtered.sortedByDescending { it.grade }
                SortOption.LEVEL -> filtered.sortedByDescending { it.level }
            }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "Inventory", style = MaterialTheme.typography.headlineMedium)
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari item (key, tipe, grade)...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )
        }

        item {
            GradeFilterRow(selectedGrade = selectedGrade, onGradeSelected = { selectedGrade = it })
        }

        item {
            SortRow(selected = sortOption, onSelected = { sortOption = it })
        }

        item {
            Text(
                text = "${filteredSorted.size} item",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (filteredSorted.isEmpty()) {
            item {
                Text(
                    text = "Gak ada item yang cocok.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // uniqueId cuma dijamin unik buat item equipment per-instance -
            // item stackable (material, currency, chest) sering gak punya field
            // "UniqueId" di save data & default ke 0, jadi banyak item bisa
            // punya uniqueId yang sama. Pakai key = { it.uniqueId } doang di
            // sini bikin LazyColumn crash (IllegalArgumentException: Key was
            // already used) begitu ada 2+ item kayak gitu. Gabung sama index
            // biar keynya selalu unik.
            itemsIndexed(filteredSorted, key = { index, item -> "${item.uniqueId}-${item.itemKey}-$index" }) { _, item ->
                ItemRow(item)
            }
        }
    }
}

@Composable
private fun GradeFilterRow(selectedGrade: Int, onGradeSelected: (Int) -> Unit) {
    val extendedColors = LocalTbhColors.current
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selectedGrade == -1,
                onClick = { onGradeSelected(-1) },
                label = { Text("All") }
            )
        }
        items(extendedColors.itemGradeColors.indices.toList()) { grade ->
            val color = extendedColors.itemGradeColors[grade]
            FilterChip(
                selected = selectedGrade == grade,
                onClick = { onGradeSelected(grade) },
                label = { Text(ItemMeta.gradeName(grade)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.copy(alpha = 0.25f))
            )
        }
    }
}

@Composable
private fun SortRow(selected: SortOption, onSelected: (SortOption) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Urutkan:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        SortOption.entries.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelected(option) },
                label = { Text(option.label) }
            )
        }
    }
}

@Composable
private fun ItemRow(item: Item, modifier: Modifier = Modifier) {
    val extendedColors = LocalTbhColors.current
    val gradeColor = extendedColors.itemGradeColors.getOrElse(item.grade) { extendedColors.itemGradeColors.first() }
    val gradeBg = extendedColors.itemGradeBackgrounds.getOrElse(item.grade) { extendedColors.itemGradeBackgrounds.first() }

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
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(gradeBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = itemTypeIcon(item.itemType),
                        contentDescription = item.itemType,
                        tint = gradeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "${item.itemType} #${item.itemKey}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Lv.${item.level} \u2022 x${item.quantity}" +
                            if (item.isChaotic) " \u2022 Chaotic" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .background(gradeBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.gradeName,
                    style = MaterialTheme.typography.labelSmall,
                    color = gradeColor
                )
            }
        }
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
private fun InventoryContentPreview() {
    TbhMonitorTheme {
        InventoryContent(
            uiState = InventoryUiState.Success(
                items = listOf(
                    Item(uniqueId = 1, itemKey = 300012, quantity = 3, level = 10),
                    Item(uniqueId = 2, itemKey = 500034, quantity = 1, level = 5, isChaotic = true)
                )
            ),
            onRefresh = {}
        )
    }
}
