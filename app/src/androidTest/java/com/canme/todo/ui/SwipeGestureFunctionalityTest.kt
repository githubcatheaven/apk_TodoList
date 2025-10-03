package com.canme.todo.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.repository.TodoRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import javax.inject.Inject

/**
 * UI tests for swipe gesture functionality in the Todo app.
 * Tests swipe-to-complete (right swipe) and swipe-to-delete (left swipe) interactions.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SwipeGestureFunctionalityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockRepository: TodoRepository

    @Inject
    lateinit var viewModel: TodoViewModel

    private val testTodos = listOf(
        Todo(1, "Incomplete task", false, TodoCategory.PERSONAL, 0, System.currentTimeMillis()),
        Todo(2, "Completed task", true, TodoCategory.WORK, 1, System.currentTimeMillis()),
        Todo(3, "Shopping item", false, TodoCategory.SHOPPING, 2, System.currentTimeMillis()),
        Todo(4, "Health goal", false, TodoCategory.HEALTH, 3, System.currentTimeMillis()),
        Todo(5, "Other task", true, TodoCategory.OTHER, 4, System.currentTimeMillis())
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        hiltRule.inject()
        
        // Setup mock repository behavior
        `when`(mockRepository.getAllTodos()).thenReturn(MutableStateFlow(testTodos))
        `when`(mockRepository.isValidTodoText(anyString())).thenAnswer { invocation ->
            val text = invocation.getArgument<String>(0)
            text.trim().isNotEmpty()
        }
    }

    // Right swipe (complete) tests
    @Test
    fun rightSwipe_showsCompleteActionBackground() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Start right swipe on incomplete todo
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeRight(endX = centerX + 200f) // Partial swipe to show background
            }

        // Then - Complete action background should be visible
        composeTestRule.onNodeWithTag("swipe_complete_background_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("swipe_complete_icon_1").assertIsDisplayed()
    }

    @Test
    fun rightSwipe_completesTodo() = runTest {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Complete right swipe on incomplete todo
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeRight() // Full swipe
            }

        // Then - Todo should be marked as completed
        verify(mockRepository).toggleTodoCompletion(argThat { todo ->
            todo.id == 1L && todo.text == "Incomplete task"
        })
    }

    @Test
    fun rightSwipe_incompletesCompletedTodo() = runTest {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Right swipe on completed todo
        composeTestRule.onNodeWithTag("todo_item_2")
            .performTouchInput {
                swipeRight()
            }

        // Then - Todo should be marked as incomplete
        verify(mockRepository).toggleTodoCompletion(argThat { todo ->
            todo.id == 2L && todo.text == "Completed task"
        })
    }

    @Test
    fun rightSwipe_showsCorrectIconForIncompleteTask() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Start right swipe on incomplete todo
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeRight(endX = centerX + 150f)
            }

        // Then - Check mark icon should be displayed
        composeTestRule.onNodeWithContentDescription("Mark as complete").assertIsDisplayed()
    }

    @Test
    fun rightSwipe_showsCorrectIconForCompletedTask() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Start right swipe on completed todo
        composeTestRule.onNodeWithTag("todo_item_2")
            .performTouchInput {
                swipeRight(endX = centerX + 150f)
            }

        // Then - Undo icon should be displayed
        composeTestRule.onNodeWithContentDescription("Mark as incomplete").assertIsDisplayed()
    }

    @Test
    fun rightSwipe_backgroundColorChangesBasedOnCompletionState() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Swipe incomplete todo
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeRight(endX = centerX + 150f)
            }

        // Then - Green background for completion action
        composeTestRule.onNodeWithTag("swipe_complete_background_1")
            .assertIsDisplayed()

        // When - Swipe completed todo
        composeTestRule.onNodeWithTag("todo_item_2")
            .performTouchInput {
                swipeRight(endX = centerX + 150f)
            }

        // Then - Different background color for incomplete action
        composeTestRule.onNodeWithTag("swipe_incomplete_background_2")
            .assertIsDisplayed()
    }

    // Left swipe (delete) tests
    @Test
    fun leftSwipe_showsDeleteActionBackground() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Start left swipe
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeLeft(endX = centerX - 200f) // Partial swipe to show background
            }

        // Then - Delete action background should be visible
        composeTestRule.onNodeWithTag("swipe_delete_background_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("swipe_delete_icon_1").assertIsDisplayed()
    }

    @Test
    fun leftSwipe_showsDeleteConfirmationDialog() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Complete left swipe
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeLeft() // Full swipe
            }

        // Then - Delete confirmation dialog should appear
        composeTestRule.onNodeWithText("Delete Todo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete this todo?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun leftSwipe_deleteConfirmationDeletesTodo() = runTest {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Left swipe and confirm deletion
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeLeft()
            }
        composeTestRule.onNodeWithText("Delete").performClick()

        // Then - Todo should be deleted
        verify(mockRepository).deleteTodo(argThat { todo ->
            todo.id == 1L && todo.text == "Incomplete task"
        })
    }

    @Test
    fun leftSwipe_deleteConfirmationCanBeCancelled() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Left swipe and cancel deletion
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeLeft()
            }
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then - Dialog should disappear and no deletion should occur
        composeTestRule.onNodeWithText("Delete Todo").assertDoesNotExist()
        verify(mockRepository, never()).deleteTodo(any())
    }

    @Test
    fun leftSwipe_showsRedBackgroundWithDeleteIcon() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Start left swipe
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeLeft(endX = centerX - 150f)
            }

        // Then - Red background with delete icon should be visible
        composeTestRule.onNodeWithTag("swipe_delete_background_1").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete todo").assertIsDisplayed()
    }

    // Swipe threshold tests
    @Test
    fun partialRightSwipe_doesNotTriggerAction() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Partial right swipe (below threshold)
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeRight(endX = centerX + 50f) // Small swipe, below threshold
            }

        // Then - No action should be triggered
        verify(mockRepository, never()).toggleTodoCompletion(any())
    }

    @Test
    fun partialLeftSwipe_doesNotTriggerAction() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Partial left swipe (below threshold)
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeLeft(endX = centerX - 50f) // Small swipe, below threshold
            }

        // Then - No action should be triggered
        composeTestRule.onNodeWithText("Delete Todo").assertDoesNotExist()
    }

    @Test
    fun swipeThreshold_providesVisualFeedback() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Swipe to threshold
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeRight(endX = centerX + 120f) // At threshold
            }

        // Then - Visual feedback should indicate action will trigger
        composeTestRule.onNodeWithTag("swipe_threshold_reached_1").assertExists()
    }

    // Animation and visual feedback tests
    @Test
    fun swipeGesture_animatesBackgroundReveal() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Perform swipe gesture
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                moveTo(center.copy(x = center.x + 100f))
                // Background should animate in as swipe progresses
            }

        // Then - Background animation should be visible
        composeTestRule.onNodeWithTag("swipe_background_animation_1").assertExists()
    }

    @Test
    fun swipeGesture_resetsOnRelease() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Start swipe but release before threshold
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                moveTo(center.copy(x = center.x + 80f)) // Below threshold
                up()
            }

        // Then - Item should return to original position
        composeTestRule.onNodeWithTag("swipe_complete_background_1").assertDoesNotExist()
    }

    // Multiple swipe interactions
    @Test
    fun multipleSwipes_workIndependently() = runTest {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Swipe multiple todos
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput { swipeRight() }
        
        composeTestRule.onNodeWithTag("todo_item_3")
            .performTouchInput { swipeLeft() }
        
        composeTestRule.onNodeWithText("Delete").performClick()

        // Then - Both actions should be processed
        verify(mockRepository).toggleTodoCompletion(argThat { it.id == 1L })
        verify(mockRepository).deleteTodo(argThat { it.id == 3L })
    }

    @Test
    fun swipeGesture_worksWithDifferentCategories() = runTest {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Swipe todos from different categories
        testTodos.forEach { todo ->
            composeTestRule.onNodeWithTag("todo_item_${todo.id}")
                .performTouchInput { swipeRight() }
        }

        // Then - All should be processed regardless of category
        testTodos.forEach { todo ->
            verify(mockRepository).toggleTodoCompletion(todo)
        }
    }

    // Accessibility tests
    @Test
    fun swipeGesture_hasAccessibilitySupport() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When & Then - Swipe actions should have accessibility descriptions
        composeTestRule.onNodeWithContentDescription("Swipe right to complete, swipe left to delete")
            .assertExists()
    }

    @Test
    fun swipeGesture_alternativeAccessibilityActions() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When & Then - Alternative accessibility actions should be available
        composeTestRule.onNodeWithTag("todo_item_1")
            .assertIsDisplayed()
        
        // Verify accessibility actions are available
        composeTestRule.onNodeWithContentDescription("Complete todo").assertExists()
        composeTestRule.onNodeWithContentDescription("Delete todo").assertExists()
    }

    // Haptic feedback tests
    @Test
    fun swipeGesture_triggersHapticFeedback() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Reach swipe threshold
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                swipeRight(endX = centerX + 150f) // Past threshold
            }

        // Then - Haptic feedback should be triggered
        // Note: Testing haptic feedback directly is challenging in UI tests
        // This test documents the expected behavior
        composeTestRule.onNodeWithTag("haptic_feedback_triggered_1").assertExists()
    }

    // Edge cases
    @Test
    fun swipeGesture_worksWithLongTodoText() {
        // Given - Todo with very long text
        val longTextTodo = Todo(
            id = 99,
            text = "This is a very long todo text that might wrap to multiple lines and could potentially interfere with swipe gesture detection",
            isCompleted = false,
            category = TodoCategory.PERSONAL,
            sortOrder = 99,
            createdAt = System.currentTimeMillis()
        )
        `when`(mockRepository.getAllTodos()).thenReturn(MutableStateFlow(listOf(longTextTodo)))
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Swipe todo with long text
        composeTestRule.onNodeWithTag("todo_item_99")
            .performTouchInput { swipeRight() }

        // Then - Swipe should still work
        verify(mockRepository).toggleTodoCompletion(longTextTodo)
    }

    @Test
    fun swipeGesture_worksInFilteredView() = runTest {
        // Given - Category filter active
        val workTodos = testTodos.filter { it.category == TodoCategory.WORK }
        `when`(mockRepository.getTodosByCategory(TodoCategory.WORK)).thenReturn(MutableStateFlow(workTodos))
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // Set filter
        composeTestRule.onNodeWithText("Work").performClick()

        // When - Swipe in filtered view
        composeTestRule.onNodeWithTag("todo_item_2")
            .performTouchInput { swipeRight() }

        // Then - Swipe should work in filtered view
        verify(mockRepository).toggleTodoCompletion(argThat { it.id == 2L })
    }

    @Test
    fun swipeGesture_performanceWithManyItems() = runTest {
        // Given - Large list of todos
        val largeTodoList = (1..100).map { index ->
            Todo(
                id = index.toLong(),
                text = "Todo $index",
                isCompleted = index % 2 == 0,
                category = TodoCategory.PERSONAL,
                sortOrder = index - 1,
                createdAt = System.currentTimeMillis() + index
            )
        }
        `when`(mockRepository.getAllTodos()).thenReturn(MutableStateFlow(largeTodoList))
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Swipe item in large list
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput { swipeRight() }

        // Then - Should handle large list efficiently
        verify(mockRepository).toggleTodoCompletion(argThat { it.id == 1L })
    }
}