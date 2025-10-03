package com.canme.todo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Shape system for the Todo application.
 * 
 * Defines the corner radius values used throughout the app for consistent
 * visual hierarchy and modern Material Design 3 aesthetics.
 */
val Shapes = Shapes(
    // Extra small components (chips, small buttons)
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small components (buttons, text fields)
    small = RoundedCornerShape(8.dp),
    
    // Medium components (cards, dialogs)
    medium = RoundedCornerShape(12.dp),
    
    // Large components (bottom sheets, large cards)
    large = RoundedCornerShape(16.dp),
    
    // Extra large components (full screen dialogs)
    extraLarge = RoundedCornerShape(28.dp)
)