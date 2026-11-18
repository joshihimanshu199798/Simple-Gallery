package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalCompleteUiTheme = staticCompositionLocalOf {
    AppThemeCatalog.defaultDarkTheme
}

@Composable
private fun rememberAnimatedThemeSpec(target: CompleteUiThemeSpec): CompleteUiThemeSpec {
    val colorSpring = spring<Color>(stiffness = Spring.StiffnessMediumLow)
    val background by animateColorAsState(target.background, colorSpring, label = "theme_bg")
    val backgroundSecondary by animateColorAsState(target.backgroundSecondary, colorSpring, label = "theme_bg_sec")
    val surface by animateColorAsState(target.surface, colorSpring, label = "theme_surface")
    val surfaceVariant by animateColorAsState(target.surfaceVariant, colorSpring, label = "theme_surface_var")
    val primaryAccent by animateColorAsState(target.primaryAccent, colorSpring, label = "theme_primary")
    val onPrimaryAccent by animateColorAsState(target.onPrimaryAccent, colorSpring, label = "theme_on_primary")
    val secondaryAccent by animateColorAsState(target.secondaryAccent, colorSpring, label = "theme_secondary")
    val onSecondaryAccent by animateColorAsState(target.onSecondaryAccent, colorSpring, label = "theme_on_secondary")
    val tertiaryAccent by animateColorAsState(target.tertiaryAccent, colorSpring, label = "theme_tertiary")
    val textPrimary by animateColorAsState(target.textPrimary, colorSpring, label = "theme_text_pri")
    val textSecondary by animateColorAsState(target.textSecondary, colorSpring, label = "theme_text_sec")
    val borderStroke by animateColorAsState(target.borderStroke, colorSpring, label = "theme_border")
    val dockBackground by animateColorAsState(target.dockBackground, colorSpring, label = "theme_dock_bg")
    val captionOverlayBackground by animateColorAsState(target.captionOverlayBackground, colorSpring, label = "theme_caption_bg")

    return target.copy(
        background = background,
        backgroundSecondary = backgroundSecondary,
        surface = surface,
        surfaceVariant = surfaceVariant,
        primaryAccent = primaryAccent,
        onPrimaryAccent = onPrimaryAccent,
        secondaryAccent = secondaryAccent,
        onSecondaryAccent = onSecondaryAccent,
        tertiaryAccent = tertiaryAccent,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        borderStroke = borderStroke,
        dockBackground = dockBackground,
        captionOverlayBackground = captionOverlayBackground
    )
}

fun CompleteUiThemeSpec.toMaterialColorScheme(): ColorScheme {
    return if (isLight) {
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryAccent,
            primaryContainer = primaryAccent.copy(alpha = 0.14f),
            onPrimaryContainer = textPrimary,
            secondary = secondaryAccent,
            onSecondary = onSecondaryAccent,
            secondaryContainer = secondaryAccent.copy(alpha = 0.15f),
            onSecondaryContainer = textPrimary,
            tertiary = tertiaryAccent,
            onTertiary = Color.White,
            tertiaryContainer = tertiaryAccent.copy(alpha = 0.18f),
            onTertiaryContainer = textPrimary,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = textSecondary,
            outline = borderStroke,
            error = Color(0xFFD32F2F),
            onError = Color.White
        )
    } else {
        darkColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryAccent,
            primaryContainer = primaryAccent.copy(alpha = 0.22f),
            onPrimaryContainer = textPrimary,
            secondary = secondaryAccent,
            onSecondary = onSecondaryAccent,
            secondaryContainer = secondaryAccent.copy(alpha = 0.22f),
            onSecondaryContainer = textPrimary,
            tertiary = tertiaryAccent,
            onTertiary = Color.Black,
            tertiaryContainer = tertiaryAccent.copy(alpha = 0.24f),
            onTertiaryContainer = textPrimary,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = textSecondary,
            outline = borderStroke,
            error = Color(0xFFFF897D),
            onError = Color(0xFF410002)
        )
    }
}

@Composable
fun SimpleGalleryTheme(
    themeSpec: CompleteUiThemeSpec = AppThemeCatalog.defaultDarkTheme,
    content: @Composable () -> Unit
) {
    val animatedThemeSpec = rememberAnimatedThemeSpec(themeSpec)
    val colorScheme = animatedThemeSpec.toMaterialColorScheme()

    CompositionLocalProvider(LocalCompleteUiTheme provides animatedThemeSpec) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

