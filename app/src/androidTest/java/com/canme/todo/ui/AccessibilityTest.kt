package com.canme.todo.ui

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canme.todo.data.Todo
import com.canme.todo.ui.components.AddTodoDialog
import com.canme.todo.ui.components.DeleteConfirmationDialog
import com.canme.todo.ui.components.EmptyState
import com.canme.todo.ui.components.TodoItem
import com.canme.todo.ui.theme.TodoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility tests for the Todo app UI components.
 * Verifies that all components have proper content descriptions, semantic properties,
 * and support for assistive technologies like screen readers.
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun todoItem_hasProperAccessibilityLabels() {
        val todo = Todo(
            id = 1,
            text = "Test todo item",
            isCompleted = false
        )

        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = todo,
                    onToggleCompletion = {},
                    onLongPress = {}
                )
            }
        }

        // Verify the todo item has proper content description
        composeTestRule
            .onNodeWithContentDescription("Todo: Test todo item. Long press to delete.")
            .assertIsDisplayed()

        // Verify checkbox has proper accessibility label
        composeTestRule
            .onNodeWithContentDescription("Mark as complete")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun todoItem_completed_hasProperAccessibilityLabels() {
        val completedTodo = Todo(
            id = 1,
            text = "Completed todo item",
            isCompleted = true
        )

        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = completedTodo,
                    onToggleCompletion = {},
                    onLongPress = {}
                )
            }
        }

        // Verify completed todo has proper content description
        composeTestRule
            .onNodeWithContentDescription("Completed todo: Completed todo item. Long press to delete.")
            .assertIsDisplayed()

        // Verify checkbox has proper accessibility label for completed state
        composeTestRule
            .onNodeWithContentDescription("Mark as incomplete")
            .assertIsDisplayed()
    }

    @Test
    fun addTodoDialog_hasProperAccessibilityLabels() {
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "Sample text",
                    errorMessage = null,
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Verify dialog title has proper accessibility
        composeTestRule
            .onNodeWithText("Add New Todo")
            .assertIsDisplayed()

        // Verify text field has proper content description
        composeTestRule
            .onNodeWithContentDescription("Enter todo description. Current text: Sample text")
            .assertIsDisplayed()

        // Verify character count has accessibility label
        composeTestRule
            .onNodeWithContentDescription("Character count: 11 of 500 characters used")
            .assertIsDisplayed()

        // Verify buttons have proper accessibility labels
        composeTestRule
            .onNodeWithContentDescription("Add todo button, enabled")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule
            .onNodeWithContentDescription("Cancel adding todo")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun addTodoDialog_withError_hasProperAccessibilityLabels() {
        composeTestRule.setContent {
            TodoTheme {
                AddTodoDialog(
                    isVisible = true,
                    todoText = "",
                    errorMessage = "Todo text cannot be empty",
                    isLoading = false,
                    onTextChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Verify error message has proper accessibility
        composeTestRule
            .onNodeWithContentDescription("Error: Todo text cannot be empty")
            .assertIsDisplayed()

        // Verify disabled button has proper accessibility label
        composeTestRule
            .onNodeWithContentDescription("Add todo button, disabled because text is empty")
            .assertIsDisplayed()
    }

    @Test
    fun deleteConfirmationDialog_hasProperAccessibilityLabels() {
        val todoToDelete = Todo(
            id = 1,
            text = "Todo to delete",
            isCompleted = false
        )

        composeTestRule.setContent {
            TodoTheme {
                DeleteConfirmationDialog(
                    isVisible = true,
                    todoToDelete = todoToDelete,
                    isLoading = false,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Verify dialog has proper accessibility labels
        composeTestRule
            .onNodeWithContentDescription("Delete todo confirmation dialog")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Warning: Destructive action")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Todo to be deleted: Todo to delete")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Warning: This action cannot be undone")
            .assertIsDisplayed()

        // Verify buttons have proper accessibility
        composeTestRule
            .onNodeWithContentDescription("Delete button. Warning: This will permanently delete the todo item.")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule
            .onNodeWithContentDescription("Cancel deletion")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun emptyState_hasProperAccessibilityLabels() {
        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = {}
                )
            }
        }

        // Verify empty state has proper accessibility
        composeTestRule
            .onNodeWithContentDescription("Empty todo list. No todos have been created yet.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Checkmark icon representing completed tasks")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("No todos yet. This is the main heading for the empty state.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Add your first todo button. Tap to create your first todo item.")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun emptyState_buttonIsClickable() {
        var buttonClicked = false

        composeTestRule.setContent {
            TodoTheme {
                EmptyState(
                    onAddFirstTodo = { buttonClicked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Add your first todo button. Tap to create your first todo item.")
            .performClick()

        assert(buttonClicked) { "Empty state button should be clickable" }
    }
}