package com.canme.todo.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Todo app theme system.
 * Verifies that color schemes are properly configured and accessible.
 */
class ThemeTest {

    @Test
    fun `light color scheme should have proper primary colors`() {
        val lightScheme = lightColorScheme(
            primary = TodoPrimary,
            onPrimary = TodoOnPrimary,
            primaryContainer = TodoPrimaryContainer,
            onPrimaryContainer = TodoOnPrimaryContainer
        )
        
        assertEquals(TodoPrimary, lightScheme.primary)
        assertEquals(TodoOnPrimary, lightScheme.onPrimary)
        assertEquals(TodoPrimaryContainer, lightScheme.primaryContainer)
        assertEquals(TodoOnPrimaryContainer, lightScheme.onPrimaryContainer)
    }

    @Test
    fun `dark color scheme should have proper primary colors`() {
        val darkScheme = darkColorScheme(
            primary = TodoPrimaryDark,
            onPrimary = TodoOnPrimaryDark,
            primaryContainer = TodoPrimaryContainerDark,
            onPrimaryContainer = TodoOnPrimaryContainerDark
        )
        
        assertEquals(TodoPrimaryDark, darkScheme.primary)
        assertEquals(TodoOnPrimaryDark, darkScheme.onPrimary)
        assertEquals(TodoPrimaryContainerDark, darkScheme.primaryContainer)
        assertEquals(TodoOnPrimaryContainerDark, darkScheme.onPrimaryContainer)
    }

    @Test
    fun `semantic colors should be properly defined`() {
        assertNotEquals(Color.Unspecified, TodoCompletedText)
        assertNotEquals(Color.Unspecified, TodoCompletedTextDark)
        assertNotEquals(Color.Unspecified, TodoHighPriority)
        assertNotEquals(Color.Unspecified, TodoMediumPriority)
        assertNotEquals(Color.Unspecified, TodoLowPriority)
    }

    @Test
    fun `dimensions should have positive values`() {
        assertTrue("Space extra small should be positive", Dimensions.spaceExtraSmall.value > 0)
        assertTrue("Space small should be positive", Dimensions.spaceSmall.value > 0)
        assertTrue("Space medium should be positive", Dimensions.spaceMedium.value > 0)
        assertTrue("Space large should be positive", Dimensions.spaceLarge.value > 0)
        assertTrue("Space extra large should be positive", Dimensions.spaceExtraLarge.value > 0)
        
        assertTrue("Button height should be positive", Dimensions.buttonHeight.value > 0)
        assertTrue("FAB size should be positive", Dimensions.fabSize.value > 0)
        assertTrue("Icon size should be positive", Dimensions.iconSize.value > 0)
    }

    @Test
    fun `shapes should have proper corner radius values`() {
        assertTrue("Extra small shape should have positive radius", 
            Shapes.extraSmall.topStart.toPx(1f, 1f) > 0)
        assertTrue("Small shape should have positive radius", 
            Shapes.small.topStart.toPx(1f, 1f) > 0)
        assertTrue("Medium shape should have positive radius", 
            Shapes.medium.topStart.toPx(1f, 1f) > 0)
        assertTrue("Large shape should have positive radius", 
            Shapes.large.topStart.toPx(1f, 1f) > 0)
        assertTrue("Extra large shape should have positive radius", 
            Shapes.extraLarge.topStart.toPx(1f, 1f) > 0)
    }
}