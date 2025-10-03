package com.canme.todo.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Consistent spacing and dimension values for the Todo application.
 * 
 * Provides a centralized system for spacing, padding, and sizing
 * to ensure visual consistency throughout the app.
 */
object Dimensions {
    // Spacing scale based on 4dp grid system
    val spaceExtraSmall = 4.dp
    val spaceSmall = 8.dp
    val spaceMedium = 16.dp
    val spaceLarge = 24.dp
    val spaceExtraLarge = 32.dp
    
    // Component specific dimensions
    val buttonHeight = 48.dp
    val textFieldHeight = 56.dp
    val fabSize = 56.dp
    val iconSize = 24.dp
    val iconSizeSmall = 16.dp
    val iconSizeLarge = 32.dp
    
    // Layout dimensions
    val screenPadding = 16.dp
    val cardPadding = 16.dp
    val dialogPadding = 24.dp
    val listItemPadding = 16.dp
    
    // Elevation values
    val elevationSmall = 2.dp
    val elevationMedium = 4.dp
    val elevationLarge = 8.dp
    
    // Border and divider
    val borderWidth = 1.dp
    val dividerHeight = 1.dp
    
    // Minimum touch target size (accessibility)
    val minTouchTarget = 48.dp
}