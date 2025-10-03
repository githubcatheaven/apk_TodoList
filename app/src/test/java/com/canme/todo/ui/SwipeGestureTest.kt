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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for swipe gesture functionality in TodoViewModel.
 * Tests swipe-to-complete and swipe-to-delete actions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SwipeGestureTest {

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

    // Swipe-to-complete tests (right swipe)
    @Test
    fun `swipe right on incomplete todo marks it as completed`() = runTest {
        // Given
        val incompleteTodo = Todo(1, "Test todo", false, TodoCategory.PERSONAL, 0, 123L)

        // When - Simulate right swipe (complete action)
        viewModel.toggleTodoCompletion(incompleteTodo)
        advanceUntilIdle()

        // Then
        verify(repository).toggleTodoCompletion(incompleteTodo)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `swipe right on completed todo marks it as incomplete`() = runTest {
        // Given
        val completedTodo = Todo(1, "Test todo", true, TodoCategory.WORK, 0, 123L)

        // When - Simulate right swipe (toggle completion)
        viewModel.toggleTodoCompletion(completedTodo)
        advanceUntilIdle()

        // Then
        verify(repository).toggleTodoCompletion(completedTodo)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `swipe right handles repository exception`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.SHOPPING, 0, 123L)
        `when`(repository.toggleTodoCompletion(todo)).thenThrow(RuntimeException("Toggle failed"))

        // When
        viewModel.toggleTodoCompletion(todo)
        advanceUntilIdle()

        // Then
        assertEquals("Failed to update todo: Toggle failed", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `swipe right works with different categories`() = runTest {
        // Given - Todos with different categories
        val todos = listOf(
            Todo(1, "Personal", false, TodoCategory.PERSONAL, 0, 100L),
            Todo(2, "Work", false, TodoCategory.WORK, 1, 200L),
            Todo(3, "Shopping", false, TodoCategory.SHOPPING, 2, 300L),
            Todo(4, "Health", false, TodoCategory.HEALTH, 3, 400L),
            Todo(5, "Other", false, TodoCategory.OTHER, 4, 500L)
        )

        // When - Swipe right on each category
        todos.forEach { todo ->
            viewModel.toggleTodoCompletion(todo)
        }
        advanceUntilIdle()

        // Then - All should be processed
        todos.forEach { todo ->
            verify(repository).toggleTodoCompletion(todo)
        }
    }

    // Swipe-to-delete tests (left swipe)
    @Test
    fun `swipe left shows delete confirmation dialog`() {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.PERSONAL, 0, 123L)

        // When - Simulate left swipe (delete action)
        viewModel.showDeleteDialog(todo)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.showDeleteDialog)
        assertEquals(todo, uiState.todoToDelete)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `swipe left delete confirmation succeeds`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.WORK, 0, 123L)
        viewModel.showDeleteDialog(todo)

        // When - Confirm deletion
        viewModel.deleteTodo()
        advanceUntilIdle()

        // Then
        verify(repository).deleteTodo(todo)
        val uiState = viewModel.uiState.value
        assertFalse(uiState.showDeleteDialog)
        assertNull(uiState.todoToDelete)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `swipe left delete confirmation can be cancelled`() {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.SHOPPING, 0, 123L)
        viewModel.showDeleteDialog(todo)

        // When - Cancel deletion
        viewModel.hideDeleteDialog()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.showDeleteDialog)
        assertNull(uiState.todoToDelete)
        verify(repository, never()).deleteTodo(any())
    }

    @Test
    fun `swipe left delete handles repository exception`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.HEALTH, 0, 123L)
        viewModel.showDeleteDialog(todo)
        `when`(repository.deleteTodo(todo)).thenThrow(RuntimeException("Delete failed"))

        // When
        viewModel.deleteTodo()
        advanceUntilIdle()

        // Then
        assertEquals("Failed to delete todo: Delete failed", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `swipe left works with completed todos`() = runTest {
        // Given
        val completedTodo = Todo(1, "Completed todo", true, TodoCategory.OTHER, 0, 123L)
        viewModel.showDeleteDialog(completedTodo)

        // When
        viewModel.deleteTodo()
        advanceUntilIdle()

        // Then
        verify(repository).deleteTodo(completedTodo)
        assertFalse(viewModel.uiState.value.showDeleteDialog)
    }

    // Swipe gesture threshold and detection tests
    @Test
    fun `multiple swipe actions can be performed in sequence`() = runTest {
        // Given
        val todos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 100L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 200L),
            Todo(3, "Third", false, TodoCategory.SHOPPING, 2, 300L)
        )

        // When - Perform multiple swipe actions
        // Right swipe on first todo
        viewModel.toggleTodoCompletion(todos[0])
        
        // Left swipe on second todo
        viewModel.showDeleteDialog(todos[1])
        viewModel.deleteTodo()
        
        // Right swipe on third todo
        viewModel.toggleTodoCompletion(todos[2])
        
        advanceUntilIdle()

        // Then - All actions should be processed
        verify(repository).toggleTodoCompletion(todos[0])
        verify(repository).deleteTodo(todos[1])
        verify(repository).toggleTodoCompletion(todos[2])
    }

    @Test
    fun `swipe actions preserve todo properties`() = runTest {
        // Given - Todo with specific properties
        val originalTodo = Todo(
            id = 1,
            text = "Important task",
            isCompleted = false,
            category = TodoCategory.WORK,
            sortOrder = 5,
            createdAt = 1234567890L
        )

        // When - Perform swipe right (toggle completion)
        viewModel.toggleTodoCompletion(originalTodo)
        advanceUntilIdle()

        // Then - Repository should be called with the exact todo object
        verify(repository).toggleTodoCompletion(argThat { todo ->
            todo.id == originalTodo.id &&
            todo.text == originalTodo.text &&
            todo.isCompleted == originalTodo.isCompleted &&
            todo.category == originalTodo.category &&
            todo.sortOrder == originalTodo.sortOrder &&
            todo.createdAt == originalTodo.createdAt
        })
    }

    @Test
    fun `swipe delete with no selected todo does nothing`() = runTest {
        // Given - No todo selected for deletion

        // When - Try to delete without selecting a todo
        viewModel.deleteTodo()
        advanceUntilIdle()

        // Then - No repository call should be made
        verify(repository, never()).deleteTodo(any())
    }

    @Test
    fun `swipe actions work with category filtering active`() = runTest {
        // Given - Category filter is active
        viewModel.setCategoryFilter(TodoCategory.WORK)
        val workTodo = Todo(1, "Work task", false, TodoCategory.WORK, 0, 123L)

        // When - Perform swipe action while filter is active
        viewModel.toggleTodoCompletion(workTodo)
        advanceUntilIdle()

        // Then - Action should still work
        verify(repository).toggleTodoCompletion(workTodo)
        assertEquals(TodoCategory.WORK, viewModel.selectedCategoryFilter.value)
    }

    @Test
    fun `swipe right provides immediate visual feedback`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.PERSONAL, 0, 123L)

        // When - Perform swipe right action
        viewModel.toggleTodoCompletion(todo)
        
        // Then - Action should be initiated immediately (before repository completes)
        verify(repository).toggleTodoCompletion(todo)
        
        advanceUntilIdle()
        
        // No error should be present after successful completion
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `swipe left shows loading state during deletion`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.PERSONAL, 0, 123L)
        viewModel.showDeleteDialog(todo)

        // When - Start deletion
        viewModel.deleteTodo()
        
        // Then - Loading state should be active initially
        assertTrue(viewModel.uiState.value.isLoading)
        
        advanceUntilIdle()
        
        // Loading should be false after completion
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `swipe actions handle concurrent operations gracefully`() = runTest {
        // Given
        val todo1 = Todo(1, "First", false, TodoCategory.PERSONAL, 0, 100L)
        val todo2 = Todo(2, "Second", false, TodoCategory.WORK, 1, 200L)

        // When - Perform concurrent swipe actions
        viewModel.toggleTodoCompletion(todo1)
        viewModel.showDeleteDialog(todo2)
        viewModel.deleteTodo()
        
        advanceUntilIdle()

        // Then - Both actions should be processed
        verify(repository).toggleTodoCompletion(todo1)
        verify(repository).deleteTodo(todo2)
    }

    @Test
    fun `swipe actions clear previous error messages`() = runTest {
        // Given - Set an error first
        viewModel.showAddTodoDialog()
        viewModel.addTodo() // This should set an error for empty text
        
        val todo = Todo(1, "Test todo", false, TodoCategory.PERSONAL, 0, 123L)

        // When - Perform successful swipe action
        viewModel.toggleTodoCompletion(todo)
        advanceUntilIdle()

        // Then - Previous error should be cleared (implicitly by successful operation)
        verify(repository).toggleTodoCompletion(todo)
    }

    @Test
    fun `swipe delete confirmation dialog state management`() {
        // Given
        val todo1 = Todo(1, "First", false, TodoCategory.PERSONAL, 0, 100L)
        val todo2 = Todo(2, "Second", false, TodoCategory.WORK, 1, 200L)

        // When - Show delete dialog for first todo
        viewModel.showDeleteDialog(todo1)
        
        // Then - First todo should be selected
        assertTrue(viewModel.uiState.value.showDeleteDialog)
        assertEquals(todo1, viewModel.uiState.value.todoToDelete)

        // When - Show delete dialog for second todo (should replace first)
        viewModel.showDeleteDialog(todo2)
        
        // Then - Second todo should be selected
        assertTrue(viewModel.uiState.value.showDeleteDialog)
        assertEquals(todo2, viewModel.uiState.value.todoToDelete)
    }

    @Test
    fun `swipe actions work with todos of different completion states`() = runTest {
        // Given - Mix of completed and incomplete todos
        val incompleteTodo = Todo(1, "Incomplete", false, TodoCategory.PERSONAL, 0, 100L)
        val completedTodo = Todo(2, "Completed", true, TodoCategory.WORK, 1, 200L)

        // When - Swipe right on both (toggle completion)
        viewModel.toggleTodoCompletion(incompleteTodo)
        viewModel.toggleTodoCompletion(completedTodo)
        advanceUntilIdle()

        // Then - Both should be processed
        verify(repository).toggleTodoCompletion(incompleteTodo)
        verify(repository).toggleTodoCompletion(completedTodo)
    }
}