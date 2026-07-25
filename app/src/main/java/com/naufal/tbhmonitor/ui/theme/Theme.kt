package com.naufal.tbhmonitor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Warna "ekstra" yang gak ada slot bakunya di Material3 ColorScheme (RunePurple, BossBlue,
 * rarity ramp item) - diakses lewat `LocalTbhColors.current` di screen manapun, bareng-bareng
 * sama `MaterialTheme.colorScheme` buat role standar (primary/background/dll).
 */
data class TbhExtendedColors(
    val bossBlue: Color,
    val bossBg: Color,
    val runePurple: Color,
    val itemGradeColors: List<Color>,
    val itemGradeBackgrounds: List<Color>
)

private val DefaultExtendedColors = TbhExtendedColors(
    bossBlue = BossBlue,
    bossBg = BossBg,
    runePurple = RunePurple,
    itemGradeColors = ItemGradeColors,
    itemGradeBackgrounds = ItemGradeBackgrounds
)

val LocalTbhColors = staticCompositionLocalOf { DefaultExtendedColors }

/**
 * TBH Monitor sengaja cuma punya SATU skema warna (dark), gak ngikutin dynamic color
 * atau light/dark system Android bawaan. Palet-nya dicontek langsung dari konstanta warna
 * desktop app (lihat Color.kt, di-cross-check ke tbh-monitor/src/main.rs baris 13-23) biar
 * identitas visual konsisten antara desktop & mobile - warna grade/status yang udah "hafal"
 * dari desktop app bakal keliatan sama persis di HP, gak acak-acakan ngikut theme HP orang.
 */
private val TbhDarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = TextPrimary,
    secondary = Green,
    onSecondary = BgDark,
    tertiary = Yellow,
    onTertiary = BgDark,
    background = BgDark,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = CardBg,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = CardBorder,
    error = ErrorRed,
    onError = BgDark
)

@Composable
fun TbhMonitorTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTbhColors provides DefaultExtendedColors) {
        MaterialTheme(
            colorScheme = TbhDarkColorScheme,
            typography = TbhTypography,
            content = content
        )
    }
}
