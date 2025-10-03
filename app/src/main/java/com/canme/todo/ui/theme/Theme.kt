package com.canme.todo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light color scheme for the Todo app following Material Design 3 guidelines
 */
private val LightColorScheme = lightColorScheme(
    primary = TodoPrimary,
    onPrimary = TodoOnPrimary,
    primaryContainer = TodoPrimaryContainer,
    onPrimaryContainer = TodoOnPrimaryContainer,
    secondary = TodoSecondary,
    onSecondary = TodoOnSecondary,
    secondaryContainer = TodoSecondaryContainer,
    onSecondaryContainer = TodoOnSecondaryContainer,
    tertiary = TodoTertiary,
    onTertiary = TodoOnTertiary,
    tertiaryContainer = TodoTertiaryContainer,
    onTertiaryContainer = TodoOnTertiaryContainer,
    error = TodoError,
    onError = TodoOnError,
    errorContainer = TodoErrorContainer,
    onErrorContainer = TodoOnErrorContainer,
    background = TodoBackground,
    onBackground = TodoOnBackground,
    surface = TodoSurface,
    onSurface = TodoOnSurface,
    surfaceVariant = TodoSurfaceVariant,
    onSurfaceVariant = TodoOnSurfaceVariant,
    outline = TodoOutline,
    outlineVariant = TodoOutlineVariant,
)

/**
 * Dark color scheme for the Todo app following Material Design 3 guidelines
 */
private val DarkColorScheme = darkColorScheme(
    primary = TodoPrimaryDark,
    onPrimary = TodoOnPrimaryDark,
    primaryContainer = TodoPrimaryContainerDark,
    onPrimaryContainer = TodoOnPrimaryContainerDark,
    secondary = TodoSecondaryDark,
    onSecondary = TodoOnSecondaryDark,
    secondaryContainer = TodoSecondaryContainerDark,
    onSecondaryContainer = TodoOnSecondaryContainerDark,
    tertiary = TodoTertiaryDark,
    onTertiary = TodoOnTertiaryDark,
    tertiaryContainer = TodoTertiaryContainerDark,
    onTertiaryContainer = TodoOnTertiaryContainerDark,
    error = TodoErrorDark,
    onError = TodoOnErrorDark,
    errorContainer = TodoErrorContainerDark,
    onErrorContainer = TodoOnErrorContainerDark,
    background = TodoBackgroundDark,
    onBackground = TodoOnBackgroundDark,
    surface = TodoSurfaceDark,
    onSurface = TodoOnSurfaceDark,
    surfaceVariant = TodoSurfaceVariantDark,
    onSurfaceVariant = TodoOnSurfaceVariantDark,
    outline = TodoOutlineDark,
    outlineVariant = TodoOutlineVariantDark,
)

/**
 * Main theme composable for the Todo application.
 * 
 * Supports both light and dark themes with Material Design 3 color schemes.
 * Includes dynamic color support for Android 12+ devices and proper edge-to-edge configuration.
 * 
 * @param darkTheme Whether to use dark theme. Defaults to system preference.
 * @param dynamicColor Whether to use dynamic colors on Android 12+. Defaults to true.
 * @param content The content to be themed.
 */
@Composable
fun TodoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Configure edge-to-edge display with transparent system bars
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
            // Set appropriate status bar icon colors based on theme
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}