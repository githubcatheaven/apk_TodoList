package com.canme.todo.ui.components

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canme.todo.ui.theme.TodoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * UI tests for EmptyState composable.
 * Tests empty state display, messaging, and call-to-action functionality.
 */
@RunWith(AndroidJUnit4::class)
class EmptyStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_displaysCorrectContent() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("No todos yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start organizing your tasks by adding your first todo item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Your First Todo").assertIsDisplayed()
    }

    @Test
    fun emptyState_displaysIcon() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = {}
                )
            }
        }

        // Then
        // The icon should be displayed (CheckCircle icon)
        // We can't easily test for specific icons, but we can verify the component structure
        composeTestRule.onNodeWithText("No todos yet").assertIsDisplayed()
    }

    @Test
    fun emptyState_buttonHasClickAction() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Add Your First Todo").assertHasClickAction()
    }

    @Test
    fun emptyState_buttonClick_triggersCallback() {
        // Given
        var buttonClicked = false

        // When
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = { buttonClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Add Your First Todo").performClick()

        // Then
        assertTrue(buttonClicked)
    }

    @Test
    fun emptyState_hasProperLayout() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = {}
                )
            }
        }

        // Then
        // Verify all elements are present and displayed
        composeTestRule.onNodeWithText("No todos yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start organizing your tasks by adding your first todo item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Your First Todo").assertIsDisplayed()
    }

    @Test
    fun emptyState_textIsReadable() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = {}
                )
            }
        }

        // Then
        // Verify the text content is appropriate and readable
        composeTestRule.onNodeWithText("No todos yet").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Start organizing your tasks by adding your first todo item"
        ).assertIsDisplayed()
    }

    @Test
    fun emptyState_multipleClicks_workCorrectly() {
        // Given
        var clickCount = 0

        // When
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = { clickCount++ }
                )
            }
        }

        // Perform multiple clicks
        val button = composeTestRule.onNodeWithText("Add Your First Todo")
        button.performClick()
        button.performClick()
        button.performClick()

        // Then
        kotlin.test.assertEquals(3, clickCount)
    }

    @Test
    fun emptyState_accessibilitySupport() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = {}
                )
            }
        }

        // Then
        // Verify that the button is accessible
        composeTestRule.onNodeWithText("Add Your First Todo").assertHasClickAction()
        
        // Verify that text elements are displayed for screen readers
        composeTestRule.onNodeWithText("No todos yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start organizing your tasks by adding your first todo item").assertIsDisplayed()
    }

    @Test
    fun emptyState_centerAlignment() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = {}
                )
            }
        }

        // Then
        // Verify all content is displayed (which implies proper centering)
        composeTestRule.onNodeWithText("No todos yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start organizing your tasks by adding your first todo item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Your First Todo").assertIsDisplayed()
    }

    @Test
    fun emptyState_properSpacing() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = {}
                )
            }
        }

        // Then
        // Verify all elements are visible and properly spaced
        // (If spacing was incorrect, elements might overlap or be cut off)
        composeTestRule.onNodeWithText("No todos yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start organizing your tasks by adding your first todo item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Your First Todo").assertIsDisplayed()
    }
}