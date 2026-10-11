package com.luxkasTech.PaintresLumiere.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * The app is dark-only with a fixed brand palette, exactly like iOS — no light scheme and no
 * Material You dynamic color. Components should read [PLColor] / [PLFont] directly; the Material
 * mapping below only exists so stock Material 3 widgets (dialogs, pull-to-refresh, navigation
 * bar) pick up sensible brand colors.
 */
private val BrandColorScheme = darkColorScheme(
    primary = PLColor.goldMid,
    onPrimary = PLColor.backgroundPrimary,
    primaryContainer = PLColor.goldDeep,
    onPrimaryContainer = PLColor.textPrimary,
    secondary = PLColor.goldBright,
    onSecondary = PLColor.backgroundPrimary,
    background = PLColor.backgroundPrimary,
    onBackground = PLColor.textPrimary,
    surface = PLColor.backgroundSecondary,
    onSurface = PLColor.textPrimary,
    surfaceVariant = PLColor.backgroundElevated,
    onSurfaceVariant = PLColor.textMuted,
    surfaceContainer = PLColor.backgroundSecondary,
    surfaceContainerHigh = PLColor.backgroundElevated,
    surfaceContainerHighest = PLColor.backgroundHighlight,
    outline = PLColor.borderSubtle,
    outlineVariant = PLColor.borderSubtle,
    error = PLColor.error,
    onError = PLColor.textPrimary,
    errorContainer = PLColor.errorBackground,
    onErrorContainer = PLColor.textPrimary,
)

private val BrandTypography = Typography(
    displaySmall = PLFont.display,
    headlineMedium = PLFont.h1,
    titleLarge = PLFont.h2,
    bodyLarge = PLFont.body,
    bodyMedium = PLFont.body,
    bodySmall = PLFont.caption,
    labelLarge = PLFont.button,
    labelMedium = PLFont.caption,
    labelSmall = PLFont.label,
)

@Composable
fun PaintresLumiereTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrandColorScheme,
        typography = BrandTypography,
        content = content,
    )
}
