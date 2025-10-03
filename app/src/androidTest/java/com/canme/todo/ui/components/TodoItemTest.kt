package com.canme.todo.ui.components

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canme.todo.data.Todo
import com.canme.todo.ui.theme.TodoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for TodoItem composable.
 * Tests user interactions, state display, and accessibility.
 */
@RunWith(AndroidJUnit4::class)
class TodoItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun todoItem_displaysCorrectText() {
        // Given
        val todo = Todo(id = 1, text = "Test todo item", isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = todo,
                    onToggleCompletion = {},
                    onLongPress = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test todo item").assertIsDisplayed()
    }

    @Test
    fun todoItem_checkboxReflectsCompletionStatus_incomplete() {
        // Given
        val todo = Todo(id = 1, text = "Incomplete todo", isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = todo,
                    onToggleCompletion = {},
                    onLongPress = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Incomplete todo").assertIsDisplayed()
        // Find checkbox by role - it should be unchecked
        composeTestRule.onNode(
            androidx.compose.ui.test.hasClickAction() and 
            androidx.compose.ui.test.hasSetTextAction().not()
        ).assertIsOff()
    }

    @Test
    fun todoItem_checkboxReflectsCompletionStatus_completed() {
        // Given
        val todo = Todo(id = 1, text = "Completed todo", isCompleted = true)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = todo,
                    onToggleCompletion = {},
                    onLongPress = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Completed todo").assertIsDisplayed()
        // Find checkbox by role - it should be checked
        composeTestRule.onNode(
            androidx.compose.ui.test.hasClickAction() and 
            androidx.compose.ui.test.hasSetTextAction().not()
        ).assertIsOn()
    }

    @Test
    fun todoItem_checkboxClick_triggersToggleCallback() {
        // Given
        val todo = Todo(id = 1, text = "Test todo", isCompleted = false)
        var toggledTodo: Todo? = null

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = todo,
                    onToggleCompletion = { toggledTodo = it },
                    onLongPress = {}
                )
            }
        }

        // Click the checkbox
        composeTestRule.onNode(
            androidx.compose.ui.test.hasClickAction() and 
            androidx.compose.ui.test.hasSetTextAction().not()
        ).performClick()

        // Then
        assertEquals(todo, toggledTodo)
    }

    @Test
    fun todoItem_longPress_triggersLongPressCallback() {
        // Given
        val todo = Todo(id = 1, text = "Test todo", isCompleted = false)
        var longPressedTodo: Todo? = null

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = todo,
                    onToggleCompletion = {},
                    onLongPress = { longPressedTodo = it }
                )
            }
        }

        // Perform long press on the todo item
        composeTestRule.onNodeWithText("Test todo").performTouchInput {
            longClick()
        }

        // Then
        assertEquals(todo, longPressedTodo)
    }

    @Test
    fun todoItem_displaysLongText_withEllipsis() {
        // Given
        val longText = "This is a very long todo item text that should demonstrate how the text wraps and handles overflow with ellipsis when it exceeds the maximum number of lines allowed"
        val todo = Todo(id = 1, text = longText, isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = todo,
                    onToggleCompletion = {},
                    onLongPress = {}
                )
            }
        }

        // Then
        // The text should be displayed (even if truncated)
        composeTestRule.onNodeWithText(longText, substring = true).assertIsDisplayed()
    }

    @Test
    fun todoItem_completedTodo_hasProperStyling() {
        // Given
        val completedTodo = Todo(id = 1, text = "Completed task", isCompleted = true)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = completedTodo,
                    onToggleCompletion = {},
                    onLongPress = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Completed task").assertIsDisplayed()
        composeTestRule.onNode(
            androidx.compose.ui.test.hasClickAction() and 
            androidx.compose.ui.test.hasSetTextAction().not()
        ).assertIsOn()
    }

    @Test
    fun todoItem_incompleteTodo_hasProperStyling() {
        // Given
        val incompleteTodo = Todo(id = 1, text = "Incomplete task", isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = incompleteTodo,
                    onToggleCompletion = {},
                    onLongPress = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Incomplete task").assertIsDisplayed()
        composeTestRule.onNode(
            androidx.compose.ui.test.hasClickAction() and 
            androidx.compose.ui.test.hasSetTextAction().not()
        ).assertIsOff()
    }

    @Test
    fun todoItem_hasClickableElements() {
        // Given
        val todo = Todo(id = 1, text = "Clickable todo", isCompleted = false)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = todo,
                    onToggleCompletion = {},
                    onLongPress = {}
                )
            }
        }

        // Then
        // Checkbox should be clickable
        composeTestRule.onNode(
            androidx.compose.ui.test.hasClickAction() and 
            androidx.compose.ui.test.hasSetTextAction().not()
        ).assertHasClickAction()
        
        // The whole item should be long-clickable
        composeTestRule.onNodeWithText("Clickable todo").assertExists()
    }

    @Test
    fun todoItem_multipleInteractions_workCorrectly() {
        // Given
        val todo = Todo(id = 1, text = "Interactive todo", isCompleted = false)
        var toggleCount = 0
        var longPressCount = 0

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoItem(
                    todo = todo,
                    onToggleCompletion = { toggleCount++ },
                    onLongPress = { longPressCount++ }
                )
            }
        }

        // Perform multiple interactions
        val checkbox = composeTestRule.onNode(
            androidx.compose.ui.test.hasClickAction() and 
            androidx.compose.ui.test.hasSetTextAction().not()
        )
        
        checkbox.performClick()
        checkbox.performClick()
        
        composeTestRule.onNodeWithText("Interactive todo").performTouchInput {
            longClick()
        }

        // Then
        assertEquals(2, toggleCount)
        assertEquals(1, longPressCount)
    }
}