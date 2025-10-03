package com.canme.todo.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
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
 * UI tests for drag-and-drop functionality in the Todo app.
 * Tests drag gesture detection, visual feedback, and reordering interactions.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DragDropFunctionalityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockRepository: TodoRepository

    @Inject
    lateinit var viewModel: TodoViewModel

    private val testTodos = listOf(
        Todo(1, "First todo", false, TodoCategory.PERSONAL, 0, System.currentTimeMillis()),
        Todo(2, "Second todo", false, TodoCategory.WORK, 1, System.currentTimeMillis()),
        Todo(3, "Third todo", true, TodoCategory.SHOPPING, 2, System.currentTimeMillis()),
        Todo(4, "Fourth todo", false, TodoCategory.HEALTH, 3, System.currentTimeMillis()),
        Todo(5, "Fifth todo", false, TodoCategory.OTHER, 4, System.currentTimeMillis())
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

    @Test
    fun todoItems_displayDragHandles() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When & Then - Each todo item should display a drag handle
        testTodos.forEach { todo ->
            composeTestRule.onNodeWithTag("drag_handle_${todo.id}").assertIsDisplayed()
        }
    }

    @Test
    fun todoItems_dragHandlesAreInteractive() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When & Then - Drag handles should be clickable/touchable
        testTodos.forEach { todo ->
            composeTestRule.onNodeWithTag("drag_handle_${todo.id}")
                .assertIsDisplayed()
                .performTouchInput {
                    // Test that touch input is registered
                    down(center)
                    up()
                }
        }
    }

    @Test
    fun dragGesture_providesVisualFeedback() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Start drag gesture on first todo
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                // Simulate drag start - should trigger visual feedback
                moveTo(center + Offset(0f, 50f))
            }

        // Then - Todo item should show drag state visual feedback
        composeTestRule.onNodeWithTag("todo_item_1_dragging").assertExists()
    }

    @Test
    fun dragGesture_elevatesItemDuringDrag() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Perform drag gesture
        composeTestRule.onNodeWithTag("drag_handle_1")
            .performTouchInput {
                down(center)
                moveTo(center + Offset(0f, 100f))
            }

        // Then - Item should have elevated appearance during drag
        composeTestRule.onNodeWithTag("todo_item_1_elevated").assertExists()
    }

    @Test
    fun dragGesture_showsDropTargetIndicators() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Start dragging first item
        composeTestRule.onNodeWithTag("drag_handle_1")
            .performTouchInput {
                down(center)
                moveTo(center + Offset(0f, 150f)) // Drag down significantly
            }

        // Then - Drop target indicators should be visible
        composeTestRule.onNodeWithTag("drop_target_indicator").assertExists()
    }

    @Test
    fun dragAndDrop_reordersItemsCorrectly() = runTest {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Drag first item down to third position
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                moveTo(center + Offset(0f, 200f)) // Move down by 2 positions
                up()
            }

        // Then - Repository should be called with reordered list
        verify(mockRepository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 5 &&
            reorderedList[0].text == "Second todo" &&
            reorderedList[1].text == "Third todo" &&
            reorderedList[2].text == "First todo" && // Moved to third position
            reorderedList[3].text == "Fourth todo" &&
            reorderedList[4].text == "Fifth todo"
        })
    }

    @Test
    fun dragAndDrop_movingItemUp() = runTest {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Drag fourth item up to second position
        composeTestRule.onNodeWithTag("todo_item_4")
            .performTouchInput {
                down(center)
                moveTo(center - Offset(0f, 200f)) // Move up by 2 positions
                up()
            }

        // Then - Repository should be called with reordered list
        verify(mockRepository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 5 &&
            reorderedList[0].text == "First todo" &&
            reorderedList[1].text == "Fourth todo" && // Moved to second position
            reorderedList[2].text == "Second todo" &&
            reorderedList[3].text == "Third todo" &&
            reorderedList[4].text == "Fifth todo"
        })
    }

    @Test
    fun dragAndDrop_cancelledDragRestoresOriginalOrder() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Start drag but cancel it (drag outside bounds or release without valid drop)
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                moveTo(center + Offset(500f, 0f)) // Drag far to the right (invalid drop zone)
                up()
            }

        // Then - No reordering should occur
        verify(mockRepository, never()).reorderTodos(any())
        
        // Original order should be maintained in UI
        composeTestRule.onNodeWithText("First todo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second todo").assertIsDisplayed()
    }

    @Test
    fun dragAndDrop_worksWithDifferentCategories() = runTest {
        // Given - Todos with mixed categories
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Drag a work todo to a different position among personal/shopping todos
        composeTestRule.onNodeWithTag("todo_item_2") // Work todo
            .performTouchInput {
                down(center)
                moveTo(center + Offset(0f, 300f)) // Move to fourth position
                up()
            }

        // Then - Reordering should work regardless of categories
        verify(mockRepository).reorderTodos(argThat { reorderedList ->
            reorderedList.any { it.text == "Second todo" && it.category == TodoCategory.WORK }
        })
    }

    @Test
    fun dragAndDrop_preservesTodoProperties() = runTest {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Drag completed todo to different position
        composeTestRule.onNodeWithTag("todo_item_3") // Third todo (completed)
            .performTouchInput {
                down(center)
                moveTo(center - Offset(0f, 100f)) // Move up one position
                up()
            }

        // Then - Todo properties should be preserved during reordering
        verify(mockRepository).reorderTodos(argThat { reorderedList ->
            val movedTodo = reorderedList.find { it.text == "Third todo" }
            movedTodo != null &&
            movedTodo.isCompleted == true &&
            movedTodo.category == TodoCategory.SHOPPING &&
            movedTodo.id == 3L
        })
    }

    @Test
    fun dragAndDrop_worksWithLongPress() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Long press to initiate drag
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                // Simulate long press duration
                advanceEventTime(1000) // 1 second
                moveTo(center + Offset(0f, 100f))
                up()
            }

        // Then - Drag should be initiated and visual feedback should appear
        // Note: This test verifies that long press can initiate drag mode
        composeTestRule.onNodeWithTag("todo_item_1_dragging").assertExists()
    }

    @Test
    fun dragAndDrop_animatesItemMovement() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Perform drag and drop
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                moveTo(center + Offset(0f, 150f))
                up()
            }

        // Then - Animation should occur (items should smoothly transition)
        // Note: Testing animations in Compose tests is limited, but we can verify
        // that the reordering animation container exists
        composeTestRule.onNodeWithTag("reorder_animation_container").assertExists()
    }

    @Test
    fun dragAndDrop_handlesEdgeCases() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Try to drag first item to first position (no change)
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                moveTo(center + Offset(0f, 10f)) // Very small movement
                up()
            }

        // Then - Should handle gracefully without unnecessary repository calls
        // Note: Depending on implementation, this might still call repository
        // but with the same order, which should be handled efficiently
    }

    @Test
    fun dragAndDrop_worksWithScrolling() {
        // Given - Large list that requires scrolling
        val largeTodoList = (1..20).map { index ->
            Todo(
                id = index.toLong(),
                text = "Todo $index",
                isCompleted = false,
                category = TodoCategory.PERSONAL,
                sortOrder = index - 1,
                createdAt = System.currentTimeMillis() + index
            )
        }
        `when`(mockRepository.getAllTodos()).thenReturn(MutableStateFlow(largeTodoList))
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Drag item while list is scrollable
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                moveTo(center + Offset(0f, 500f)) // Large movement that might require scrolling
                up()
            }

        // Then - Should handle drag with scrolling
        verify(mockRepository).reorderTodos(any())
    }

    @Test
    fun dragAndDrop_accessibilitySupport() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When & Then - Drag handles should have proper accessibility labels
        testTodos.forEach { todo ->
            composeTestRule.onNodeWithTag("drag_handle_${todo.id}")
                .assertIsDisplayed()
            
            // Verify accessibility content description exists
            composeTestRule.onNodeWithContentDescription("Drag to reorder ${todo.text}")
                .assertExists()
        }
    }

    @Test
    fun dragAndDrop_hapticFeedback() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Start drag gesture
        composeTestRule.onNodeWithTag("drag_handle_1")
            .performTouchInput {
                down(center)
                moveTo(center + Offset(0f, 50f))
            }

        // Then - Haptic feedback should be triggered
        // Note: Testing haptic feedback directly is challenging in UI tests
        // This test documents the expected behavior
        // In a real implementation, you might verify that a haptic feedback
        // method was called or that the appropriate haptic feedback component exists
    }

    @Test
    fun dragAndDrop_performanceWithLargeList() = runTest {
        // Given - Very large list
        val largeTodoList = (1..1000).map { index ->
            Todo(
                id = index.toLong(),
                text = "Todo $index",
                isCompleted = index % 2 == 0,
                category = TodoCategory.values()[index % TodoCategory.values().size],
                sortOrder = index - 1,
                createdAt = System.currentTimeMillis() + index
            )
        }
        `when`(mockRepository.getAllTodos()).thenReturn(MutableStateFlow(largeTodoList))
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Perform drag operation on large list
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                moveTo(center + Offset(0f, 100f))
                up()
            }

        // Then - Should handle large list efficiently
        verify(mockRepository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 1000
        })
    }

    @Test
    fun dragAndDrop_multipleSimultaneousDrags() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Try to start multiple drags (should only allow one)
        composeTestRule.onNodeWithTag("todo_item_1")
            .performTouchInput {
                down(center)
                moveTo(center + Offset(0f, 50f))
                // Don't release - keep first drag active
            }

        // Try to start second drag
        composeTestRule.onNodeWithTag("todo_item_2")
            .performTouchInput {
                down(center) // This should be ignored or handled gracefully
            }

        // Then - Only one drag should be active
        composeTestRule.onAllNodesWithTag("todo_item_dragging")
            .assertCountEquals(1) // Only one item should be in dragging state
    }
}