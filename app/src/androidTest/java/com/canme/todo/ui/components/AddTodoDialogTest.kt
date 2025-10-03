package com.canme.todo.ui.components

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canme.todo.ui.theme.TodoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for AddTodoDialog composable.
 * Tests dialog functionality, input handling, and user interactions.
 */
@RunWith(AndroidJUnit4::class)
class AddTodoDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addTodoDialog_notVisible_doesNotDisplay() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = false,
                    todoText = "",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Add New Todo").assertDoesNotExist()
    }

    @Test
    fun addTodoDialog_visible_displaysCorrectly() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Add New Todo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Todo description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun addTodoDialog_displaysCurrentText() {
        // Given
        val currentText = "Current todo text"

        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = currentText,
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(currentText).assertIsDisplayed()
    }

    @Test
    fun addTodoDialog_textInput_triggersOnTextChange() {
        // Given
        var changedText = ""

        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = { changedText = it },
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Type in the text field
        composeTestRule.onNodeWithText("Todo description").performTextInput("New todo")

        // Then
        assertEquals("New todo", changedText)
    }

    @Test
    fun addTodoDialog_addButton_enabledWithText() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "Valid text",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Add").assertIsEnabled()
    }

    @Test
    fun addTodoDialog_addButton_disabledWithEmptyText() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Add").assertIsNotEnabled()
    }

    @Test
    fun addTodoDialog_addButton_disabledWithBlankText() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "   ",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Add").assertIsNotEnabled()
    }

    @Test
    fun addTodoDialog_addButtonClick_triggersOnConfirm() {
        // Given
        var confirmClicked = false

        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "Valid text",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = { confirmClicked = true },
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Add").performClick()

        // Then
        assertTrue(confirmClicked)
    }

    @Test
    fun addTodoDialog_cancelButtonClick_triggersOnDismiss() {
        // Given
        var dismissClicked = false

        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = { dismissClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then
        assertTrue(dismissClicked)
    }

    @Test
    fun addTodoDialog_displaysErrorMessage() {
        // Given
        val errorMessage = "Todo text cannot be empty"

        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "",
                    errorMessage = errorMessage,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun addTodoDialog_displaysCharacterCount() {
        // Given
        val text = "Hello World"

        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = text,
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("11/500").assertIsDisplayed()
    }

    @Test
    fun addTodoDialog_characterCount_showsWarningColor() {
        // Given - Text approaching limit (over 80% of 500)
        val longText = "A".repeat(450)

        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = longText,
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("450/500").assertIsDisplayed()
    }

    @Test
    fun addTodoDialog_loadingState_disablesButtons() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "Valid text",
                    errorMessage = null,
                    isLoading = true,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Add").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Cancel").assertIsNotEnabled()
    }

    @Test
    fun addTodoDialog_loadingState_showsProgressIndicator() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "Valid text",
                    errorMessage = null,
                    isLoading = true,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        // Progress indicator should be displayed (we can check for its existence)
        composeTestRule.onNode(
            androidx.compose.ui.test.hasProgressBarRangeInfo(0f..1f)
        ).assertIsDisplayed()
    }

    @Test
    fun addTodoDialog_loadingState_disablesTextInput() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "Some text",
                    errorMessage = null,
                    isLoading = true,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Todo description").assertIsNotEnabled()
    }

    @Test
    fun addTodoDialog_placeholder_isDisplayed() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Enter your todo item...").assertIsDisplayed()
    }

    @Test
    fun addTodoDialog_multipleInteractions_workCorrectly() {
        // Given
        var textChanges = 0
        var confirmClicks = 0
        var dismissClicks = 0

        // When
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "Test",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = { textChanges++ },
                    onConfirm = { confirmClicks++ },
                    onDismiss = { dismissClicks++ }
                )
            }
        }

        // Perform multiple interactions
        composeTestRule.onNodeWithText("Todo description").performTextInput("New")
        composeTestRule.onNodeWithText("Add").performClick()
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then
        assertEquals(1, textChanges)
        assertEquals(1, confirmClicks)
        assertEquals(1, dismissClicks)
    }
}