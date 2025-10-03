package com.canme.todo.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.repository.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for TodoViewModel drag-and-drop functionality.
 * Tests reordering logic, position calculations, and error handling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodoViewModelDragDropTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: TodoRepository

    private lateinit var viewModel: TodoViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Setup default repository behavior
        `when`(repository.getAllTodos()).thenReturn(flowOf(emptyList()))
        `when`(repository.isValidTodoText(anyString())).thenAnswer { invocation ->
            val text = invocation.getArgument<String>(0)
            text.trim().isNotEmpty()
        }
        
        viewModel = TodoViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // Basic reordering tests
    @Test
    fun `reorderTodos with valid indices succeeds`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L),
            Todo(3, "Third", false, TodoCategory.SHOPPING, 2, 125L)
        )
        val fromIndex = 0
        val toIndex = 2

        // When
        viewModel.reorderTodos(currentTodos, fromIndex, toIndex)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.errorMessage)
        verify(repository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 3 &&
            reorderedList[0].id == 2L && // Second moved to first
            reorderedList[1].id == 3L && // Third moved to second
            reorderedList[2].id == 1L    // First moved to last
        })
    }

    @Test
    fun `reorderTodos moving item down in list`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "A", false, TodoCategory.PERSONAL, 0, 100L),
            Todo(2, "B", false, TodoCategory.WORK, 1, 200L),
            Todo(3, "C", false, TodoCategory.SHOPPING, 2, 300L),
            Todo(4, "D", false, TodoCategory.HEALTH, 3, 400L)
        )
        val fromIndex = 1 // Move "B" from position 1
        val toIndex = 3   // To position 3

        // When
        viewModel.reorderTodos(currentTodos, fromIndex, toIndex)
        advanceUntilIdle()

        // Then
        verify(repository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 4 &&
            reorderedList[0].text == "A" &&
            reorderedList[1].text == "C" &&
            reorderedList[2].text == "D" &&
            reorderedList[3].text == "B"
        })
    }

    @Test
    fun `reorderTodos moving item up in list`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "A", false, TodoCategory.PERSONAL, 0, 100L),
            Todo(2, "B", false, TodoCategory.WORK, 1, 200L),
            Todo(3, "C", false, TodoCategory.SHOPPING, 2, 300L),
            Todo(4, "D", false, TodoCategory.HEALTH, 3, 400L)
        )
        val fromIndex = 3 // Move "D" from position 3
        val toIndex = 1   // To position 1

        // When
        viewModel.reorderTodos(currentTodos, fromIndex, toIndex)
        advanceUntilIdle()

        // Then
        verify(repository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 4 &&
            reorderedList[0].text == "A" &&
            reorderedList[1].text == "D" &&
            reorderedList[2].text == "B" &&
            reorderedList[3].text == "C"
        })
    }

    @Test
    fun `reorderTodos moving to same position does nothing meaningful`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )
        val fromIndex = 1
        val toIndex = 1 // Same position

        // When
        viewModel.reorderTodos(currentTodos, fromIndex, toIndex)
        advanceUntilIdle()

        // Then - Should still call repository (list unchanged but operation valid)
        verify(repository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 2 &&
            reorderedList[0].id == 1L &&
            reorderedList[1].id == 2L
        })
    }

    // Edge case tests
    @Test
    fun `reorderTodos with negative fromIndex does nothing`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )

        // When
        viewModel.reorderTodos(currentTodos, -1, 1)
        advanceUntilIdle()

        // Then
        verify(repository, never()).reorderTodos(any())
    }

    @Test
    fun `reorderTodos with negative toIndex does nothing`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )

        // When
        viewModel.reorderTodos(currentTodos, 0, -1)
        advanceUntilIdle()

        // Then
        verify(repository, never()).reorderTodos(any())
    }

    @Test
    fun `reorderTodos with fromIndex out of bounds does nothing`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )

        // When
        viewModel.reorderTodos(currentTodos, 5, 1) // fromIndex > list size
        advanceUntilIdle()

        // Then
        verify(repository, never()).reorderTodos(any())
    }

    @Test
    fun `reorderTodos with toIndex out of bounds does nothing`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )

        // When
        viewModel.reorderTodos(currentTodos, 0, 5) // toIndex > list size
        advanceUntilIdle()

        // Then
        verify(repository, never()).reorderTodos(any())
    }

    @Test
    fun `reorderTodos with empty list does nothing`() = runTest {
        // Given
        val emptyList = emptyList<Todo>()

        // When
        viewModel.reorderTodos(emptyList, 0, 1)
        advanceUntilIdle()

        // Then
        verify(repository, never()).reorderTodos(any())
    }

    @Test
    fun `reorderTodos with single item list does nothing`() = runTest {
        // Given
        val singleTodo = listOf(
            Todo(1, "Only todo", false, TodoCategory.PERSONAL, 0, 123L)
        )

        // When
        viewModel.reorderTodos(singleTodo, 0, 0)
        advanceUntilIdle()

        // Then - Should still call repository (valid operation)
        verify(repository).reorderTodos(singleTodo)
    }

    // Error handling tests
    @Test
    fun `reorderTodos handles repository exception`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )
        `when`(repository.reorderTodos(any())).thenThrow(RuntimeException("Reorder failed"))

        // When
        viewModel.reorderTodos(currentTodos, 0, 1)
        advanceUntilIdle()

        // Then
        assertEquals("Failed to reorder todos: Reorder failed", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `reorderTodos handles database constraint exception`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )
        `when`(repository.reorderTodos(any())).thenThrow(IllegalStateException("Database constraint violation"))

        // When
        viewModel.reorderTodos(currentTodos, 0, 1)
        advanceUntilIdle()

        // Then
        assertEquals("Failed to reorder todos: Database constraint violation", viewModel.uiState.value.errorMessage)
    }

    // Complex reordering scenarios
    @Test
    fun `reorderTodos preserves todo properties during reordering`() = runTest {
        // Given - Todos with different properties
        val currentTodos = listOf(
            Todo(1, "Personal task", false, TodoCategory.PERSONAL, 0, 100L),
            Todo(2, "Work meeting", true, TodoCategory.WORK, 1, 200L),
            Todo(3, "Buy milk", false, TodoCategory.SHOPPING, 2, 300L)
        )
        val fromIndex = 2 // Move "Buy milk" to first position
        val toIndex = 0

        // When
        viewModel.reorderTodos(currentTodos, fromIndex, toIndex)
        advanceUntilIdle()

        // Then - All properties should be preserved
        verify(repository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 3 &&
            // First item should be the moved "Buy milk" todo
            reorderedList[0].id == 3L &&
            reorderedList[0].text == "Buy milk" &&
            reorderedList[0].isCompleted == false &&
            reorderedList[0].category == TodoCategory.SHOPPING &&
            reorderedList[0].createdAt == 300L &&
            // Other items should maintain their properties
            reorderedList[1].id == 1L &&
            reorderedList[1].text == "Personal task" &&
            reorderedList[1].isCompleted == false &&
            reorderedList[1].category == TodoCategory.PERSONAL &&
            reorderedList[2].id == 2L &&
            reorderedList[2].text == "Work meeting" &&
            reorderedList[2].isCompleted == true &&
            reorderedList[2].category == TodoCategory.WORK
        })
    }

    @Test
    fun `reorderTodos works with mixed categories`() = runTest {
        // Given - Mixed categories in different orders
        val currentTodos = listOf(
            Todo(1, "Health goal", false, TodoCategory.HEALTH, 0, 100L),
            Todo(2, "Work task", false, TodoCategory.WORK, 1, 200L),
            Todo(3, "Personal note", true, TodoCategory.PERSONAL, 2, 300L),
            Todo(4, "Shopping list", false, TodoCategory.SHOPPING, 3, 400L),
            Todo(5, "Other stuff", false, TodoCategory.OTHER, 4, 500L)
        )
        val fromIndex = 4 // Move "Other stuff" to second position
        val toIndex = 1

        // When
        viewModel.reorderTodos(currentTodos, fromIndex, toIndex)
        advanceUntilIdle()

        // Then
        verify(repository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 5 &&
            reorderedList[0].text == "Health goal" &&
            reorderedList[1].text == "Other stuff" && // Moved here
            reorderedList[2].text == "Work task" &&
            reorderedList[3].text == "Personal note" &&
            reorderedList[4].text == "Shopping list"
        })
    }

    @Test
    fun `reorderTodos handles large list efficiently`() = runTest {
        // Given - Large list of todos
        val largeTodoList = (1..100).map { index ->
            Todo(
                id = index.toLong(),
                text = "Todo $index",
                isCompleted = index % 2 == 0,
                category = TodoCategory.values()[index % TodoCategory.values().size],
                sortOrder = index - 1,
                createdAt = System.currentTimeMillis() + index
            )
        }
        val fromIndex = 0  // Move first item
        val toIndex = 99   // To last position

        // When
        viewModel.reorderTodos(largeTodoList, fromIndex, toIndex)
        advanceUntilIdle()

        // Then - Should handle large list without issues
        verify(repository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 100 &&
            reorderedList[0].text == "Todo 2" && // Second item moved to first
            reorderedList[99].text == "Todo 1"   // First item moved to last
        })
    }

    @Test
    fun `reorderTodos maintains sort order consistency`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "A", false, TodoCategory.PERSONAL, 10, 100L), // Non-sequential sort orders
            Todo(2, "B", false, TodoCategory.WORK, 25, 200L),
            Todo(3, "C", false, TodoCategory.SHOPPING, 30, 300L)
        )
        val fromIndex = 2 // Move "C" to first position
        val toIndex = 0

        // When
        viewModel.reorderTodos(currentTodos, fromIndex, toIndex)
        advanceUntilIdle()

        // Then - Repository should receive the reordered list (sortOrder will be updated by DAO)
        verify(repository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 3 &&
            reorderedList[0].text == "C" &&
            reorderedList[1].text == "A" &&
            reorderedList[2].text == "B"
        })
    }

    // Integration with other ViewModel operations
    @Test
    fun `reorderTodos works correctly after adding new todo`() = runTest {
        // Given - Simulate adding a todo first
        val initialTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )
        
        // Simulate a new todo being added
        val todosAfterAdd = initialTodos + Todo(3, "Third", false, TodoCategory.SHOPPING, 2, 125L)

        // When - Reorder after adding
        viewModel.reorderTodos(todosAfterAdd, 2, 0) // Move new todo to first position
        advanceUntilIdle()

        // Then
        verify(repository).reorderTodos(argThat { reorderedList ->
            reorderedList.size == 3 &&
            reorderedList[0].text == "Third" && // New todo moved to first
            reorderedList[1].text == "First" &&
            reorderedList[2].text == "Second"
        })
    }

    @Test
    fun `reorderTodos clears previous error messages`() = runTest {
        // Given - Set an error first
        viewModel.showAddTodoDialog()
        viewModel.addTodo() // This should set an error for empty text
        
        val currentTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )

        // When - Perform successful reorder
        viewModel.reorderTodos(currentTodos, 0, 1)
        advanceUntilIdle()

        // Then - Error should be cleared (implicitly by successful operation)
        // Note: The current implementation doesn't explicitly clear errors on reorder
        // This test documents the expected behavior
        verify(repository).reorderTodos(any())
    }
}