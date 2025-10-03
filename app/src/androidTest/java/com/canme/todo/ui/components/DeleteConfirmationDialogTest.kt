package com.canme.todo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canme.todo.data.Todo
import com.canme.todo.ui.theme.TodoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for DeleteConfirmationDialog composable.
 * Tests dialog functionality, confirmation flow, and user interactions.
 */
@RunWith(AndroidJUnit4::class)
class DeleteConfirmationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun deleteDialog_notVisible_doesNotDisplay() {
        // Given
        val todo = Todo(id = 1, text = "Test todo", isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = false,
                    todoToDelete = todo,
                    isLoading = false,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Delete Todo").assertDoesNotExist()
    }

    @Test
    fun deleteDialog_noTodoToDelete_doesNotDisplay() {
        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = null,
                    isLoading = false,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Delete Todo").assertDoesNotExist()
    }

    @Test
    fun deleteDialog_visible_displaysCorrectly() {
        // Given
        val todo = Todo(id = 1, text = "Test todo to delete", isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = false,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Delete Todo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete this todo item?").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"Test todo to delete\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("This action cannot be undone.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun deleteDialog_displaysWarningIcon() {
        // Given
        val todo = Todo(id = 1, text = "Test todo", isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = false,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Warning").assertIsDisplayed()
    }

    @Test
    fun deleteDialog_displaysTodoText() {
        // Given
        val todoText = "Important todo to be deleted"
        val todo = Todo(id = 1, text = todoText, isCompleted = true)

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = false,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("\"$todoText\"").assertIsDisplayed()
    }

    @Test
    fun deleteDialog_displaysLongTodoText_withTruncation() {
        // Given
        val longText = "This is a very long todo item text that should demonstrate how the dialog handles overflow and truncation when the todo text is too long to display completely"
        val todo = Todo(id = 1, text = longText, isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = false,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        // The text should be displayed (even if truncated)
        composeTestRule.onNodeWithText("\"$longText\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun deleteDialog_deleteButtonClick_triggersOnConfirm() {
        // Given
        val todo = Todo(id = 1, text = "Test todo", isCompleted = false)
        var confirmClicked = false

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = false,
                    onConfirm = { confirmClicked = true },
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Delete").performClick()

        // Then
        assertTrue(confirmClicked)
    }

    @Test
    fun deleteDialog_cancelButtonClick_triggersOnDismiss() {
        // Given
        val todo = Todo(id = 1, text = "Test todo", isCompleted = false)
        var dismissClicked = false

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = false,
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
    fun deleteDialog_loadingState_disablesButtons() {
        // Given
        val todo = Todo(id = 1, text = "Test todo", isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = true,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Delete").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Cancel").assertIsNotEnabled()
    }

    @Test
    fun deleteDialog_loadingState_showsProgressIndicator() {
        // Given
        val todo = Todo(id = 1, text = "Test todo", isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = true,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        // Progress indicator should be displayed
        composeTestRule.onNode(
            androidx.compose.ui.test.hasProgressBarRangeInfo(0f..1f)
        ).assertIsDisplayed()
    }

    @Test
    fun deleteDialog_normalState_buttonsEnabled() {
        // Given
        val todo = Todo(id = 1, text = "Test todo", isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = false,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Delete").assertIsEnabled()
        composeTestRule.onNodeWithText("Cancel").assertIsEnabled()
    }

    @Test
    fun deleteDialog_completedTodo_displaysCorrectly() {
        // Given
        val completedTodo = Todo(id = 1, text = "Completed todo", isCompleted = true)

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = completedTodo,
                    isLoading = false,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("\"Completed todo\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Todo").assertIsDisplayed()
    }

    @Test
    fun deleteDialog_multipleInteractions_workCorrectly() {
        // Given
        val todo = Todo(id = 1, text = "Interactive todo", isCompleted = false)
        var confirmClicks = 0
        var dismissClicks = 0

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = false,
                    onConfirm = { confirmClicks++ },
                    onDismiss = { dismissClicks++ }
                )
            }
        }

        // Perform multiple interactions
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then
        assertEquals(1, confirmClicks)
        assertEquals(1, dismissClicks)
    }

    @Test
    fun deleteDialog_specialCharacters_displaysCorrectly() {
        // Given
        val specialText = "Todo with special chars: !@#$%^&*()"
        val todo = Todo(id = 1, text = specialText, isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todo,
                    isLoading = false,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("\"$specialText\"").assertIsDisplayed()
    }
}