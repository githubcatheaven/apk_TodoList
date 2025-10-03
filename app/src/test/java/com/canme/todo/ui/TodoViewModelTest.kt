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
 * Unit tests for TodoViewModel.
 * Tests state management, business logic, category functionality, and user interaction handling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodoViewModelTest {

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

    // Initial State Tests
    @Test
    fun `initial state is correct`() {
        // When & Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isAddingTodo)
        assertEquals("", uiState.newTodoText)
        assertEquals(TodoCategory.PERSONAL, uiState.selectedCategory)
        assertFalse(uiState.showDeleteDialog)
        assertNull(uiState.todoToDelete)
        assertNull(uiState.errorMessage)
        assertFalse(uiState.isLoading)
        
        // Category filter should be null initially (show all)
        assertNull(viewModel.selectedCategoryFilter.value)
    }

    @Test
    fun `todos flow is initialized from repository`() = runTest {
        // Given
        val testTodos = listOf(
            Todo(1, "Test todo 1", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Test todo 2", true, TodoCategory.WORK, 1, 124L)
        )
        `when`(repository.getAllTodos()).thenReturn(flowOf(testTodos))
        
        // When
        val newViewModel = TodoViewModel(repository)
        advanceUntilIdle()

        // Then
        assertEquals(testTodos, newViewModel.todos.value)
    }

    // Category Filter Tests
    @Test
    fun `setCategoryFilter updates filter and switches data source`() = runTest {
        // Given
        val workTodos = listOf(
            Todo(1, "Work todo 1", false, TodoCategory.WORK, 0, 123L),
            Todo(2, "Work todo 2", true, TodoCategory.WORK, 1, 124L)
        )
        `when`(repository.getTodosByCategory(TodoCategory.WORK)).thenReturn(flowOf(workTodos))

        // When
        viewModel.setCategoryFilter(TodoCategory.WORK)
        advanceUntilIdle()

        // Then
        assertEquals(TodoCategory.WORK, viewModel.selectedCategoryFilter.value)
        assertEquals(workTodos, viewModel.todos.value)
        verify(repository).getTodosByCategory(TodoCategory.WORK)
    }

    @Test
    fun `setCategoryFilter to null shows all todos`() = runTest {
        // Given
        val allTodos = listOf(
            Todo(1, "Personal todo", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Work todo", true, TodoCategory.WORK, 1, 124L)
        )
        `when`(repository.getAllTodos()).thenReturn(flowOf(allTodos))
        
        // First set a filter
        viewModel.setCategoryFilter(TodoCategory.WORK)
        
        // When
        viewModel.setCategoryFilter(null)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.selectedCategoryFilter.value)
        assertEquals(allTodos, viewModel.todos.value)
        verify(repository, atLeastOnce()).getAllTodos()
    }

    @Test
    fun `combinedState includes category filter information`() = runTest {
        // Given
        val todos = listOf(Todo(1, "Test", false, TodoCategory.SHOPPING, 0, 123L))
        `when`(repository.getTodosByCategory(TodoCategory.SHOPPING)).thenReturn(flowOf(todos))

        // When
        viewModel.setCategoryFilter(TodoCategory.SHOPPING)
        advanceUntilIdle()

        // Then
        viewModel.combinedState.collect { state ->
            assertEquals(todos, state.todos)
            assertEquals(TodoCategory.SHOPPING, state.selectedCategoryFilter)
        }
    }

    // Add Todo Dialog Tests
    @Test
    fun `showAddTodoDialog updates state with default category`() {
        // When
        viewModel.showAddTodoDialog()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isAddingTodo)
        assertEquals("", uiState.newTodoText)
        assertEquals(TodoCategory.PERSONAL, uiState.selectedCategory)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `hideAddTodoDialog resets state including category`() {
        // Given
        viewModel.showAddTodoDialog()
        viewModel.updateNewTodoText("Some text")
        viewModel.updateSelectedCategory(TodoCategory.WORK)

        // When
        viewModel.hideAddTodoDialog()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isAddingTodo)
        assertEquals("", uiState.newTodoText)
        assertEquals(TodoCategory.PERSONAL, uiState.selectedCategory)
        assertNull(uiState.errorMessage)
    }

    // Category Selection Tests
    @Test
    fun `updateSelectedCategory updates category in state`() {
        // When
        viewModel.updateSelectedCategory(TodoCategory.HEALTH)

        // Then
        assertEquals(TodoCategory.HEALTH, viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `updateSelectedCategory works with all categories`() {
        // Given
        val categories = TodoCategory.values()

        // When & Then
        categories.forEach { category ->
            viewModel.updateSelectedCategory(category)
            assertEquals(category, viewModel.uiState.value.selectedCategory)
        }
    }

    // Text Input Tests
    @Test
    fun `updateNewTodoText updates text and clears error`() {
        // Given
        viewModel.showAddTodoDialog()
        viewModel.addTodo() // This should set an error for empty text

        // When
        viewModel.updateNewTodoText("New todo text")

        // Then
        assertEquals("New todo text", viewModel.uiState.value.newTodoText)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    // Add Todo Tests
    @Test
    fun `addTodo with valid text and category succeeds`() = runTest {
        // Given
        viewModel.showAddTodoDialog()
        viewModel.updateNewTodoText("Valid todo text")
        viewModel.updateSelectedCategory(TodoCategory.SHOPPING)
        `when`(repository.insertTodo("Valid todo text", TodoCategory.SHOPPING)).thenReturn(1L)

        // When
        viewModel.addTodo()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isAddingTodo)
        assertEquals("", uiState.newTodoText)
        assertEquals(TodoCategory.PERSONAL, uiState.selectedCategory) // Reset to default
        assertNull(uiState.errorMessage)
        assertFalse(uiState.isLoading)
        
        verify(repository).insertTodo("Valid todo text", TodoCategory.SHOPPING)
    }

    @Test
    fun `addTodo with empty text shows error`() {
        // Given
        viewModel.showAddTodoDialog()
        viewModel.updateNewTodoText("")

        // When
        viewModel.addTodo()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isAddingTodo) // Dialog should remain open
        assertEquals("Todo text cannot be empty", uiState.errorMessage)
        
        verify(repository, never()).insertTodo(anyString(), any())
    }

    @Test
    fun `addTodo with blank text shows error`() {
        // Given
        viewModel.showAddTodoDialog()
        viewModel.updateNewTodoText("   ")

        // When
        viewModel.addTodo()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isAddingTodo)
        assertEquals("Todo text cannot be empty", uiState.errorMessage)
        
        verify(repository, never()).insertTodo(anyString(), any())
    }

    @Test
    fun `addTodo handles repository exception`() = runTest {
        // Given
        viewModel.showAddTodoDialog()
        viewModel.updateNewTodoText("Valid text")
        `when`(repository.insertTodo(anyString(), any())).thenThrow(RuntimeException("Database error"))

        // When
        viewModel.addTodo()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Failed to add todo: Database error", uiState.errorMessage)
        assertFalse(uiState.isLoading)
    }

    // Toggle Todo Completion Tests
    @Test
    fun `toggleTodoCompletion succeeds`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.WORK, 0, 123L)

        // When
        viewModel.toggleTodoCompletion(todo)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.errorMessage)
        verify(repository).toggleTodoCompletion(todo)
    }

    @Test
    fun `toggleTodoCompletion handles repository exception`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.HEALTH, 0, 123L)
        `when`(repository.toggleTodoCompletion(todo)).thenThrow(RuntimeException("Update failed"))

        // When
        viewModel.toggleTodoCompletion(todo)
        advanceUntilIdle()

        // Then
        assertEquals("Failed to update todo: Update failed", viewModel.uiState.value.errorMessage)
    }

    // Delete Dialog Tests
    @Test
    fun `showDeleteDialog updates state correctly`() {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.OTHER, 0, 123L)

        // When
        viewModel.showDeleteDialog(todo)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.showDeleteDialog)
        assertEquals(todo, uiState.todoToDelete)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `hideDeleteDialog resets state correctly`() {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.PERSONAL, 0, 123L)
        viewModel.showDeleteDialog(todo)

        // When
        viewModel.hideDeleteDialog()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.showDeleteDialog)
        assertNull(uiState.todoToDelete)
        assertNull(uiState.errorMessage)
    }

    // Delete Todo Tests
    @Test
    fun `deleteTodo succeeds`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.SHOPPING, 0, 123L)
        viewModel.showDeleteDialog(todo)

        // When
        viewModel.deleteTodo()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.showDeleteDialog)
        assertNull(uiState.todoToDelete)
        assertNull(uiState.errorMessage)
        assertFalse(uiState.isLoading)
        
        verify(repository).deleteTodo(todo)
    }

    @Test
    fun `deleteTodo with no selected todo does nothing`() = runTest {
        // When
        viewModel.deleteTodo()
        advanceUntilIdle()

        // Then
        verify(repository, never()).deleteTodo(any())
    }

    @Test
    fun `deleteTodo handles repository exception`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.WORK, 0, 123L)
        viewModel.showDeleteDialog(todo)
        `when`(repository.deleteTodo(todo)).thenThrow(RuntimeException("Delete failed"))

        // When
        viewModel.deleteTodo()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Failed to delete todo: Delete failed", uiState.errorMessage)
        assertFalse(uiState.isLoading)
    }

    // Clear Completed Todos Tests
    @Test
    fun `deleteCompletedTodos succeeds`() = runTest {
        // When
        viewModel.deleteCompletedTodos()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.errorMessage)
        verify(repository).deleteCompletedTodos()
    }

    @Test
    fun `deleteCompletedTodos handles repository exception`() = runTest {
        // Given
        `when`(repository.deleteCompletedTodos()).thenThrow(RuntimeException("Cleanup failed"))

        // When
        viewModel.deleteCompletedTodos()
        advanceUntilIdle()

        // Then
        assertEquals("Failed to delete completed todos: Cleanup failed", viewModel.uiState.value.errorMessage)
    }

    // Reorder Tests
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
            reorderedList[0].text == "Second" &&
            reorderedList[1].text == "Third" &&
            reorderedList[2].text == "First"
        })
    }

    @Test
    fun `reorderTodos with invalid indices does nothing`() = runTest {
        // Given
        val currentTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )

        // When - invalid indices
        viewModel.reorderTodos(currentTodos, -1, 1)
        viewModel.reorderTodos(currentTodos, 0, 5)
        viewModel.reorderTodos(currentTodos, 5, 0)
        advanceUntilIdle()

        // Then
        verify(repository, never()).reorderTodos(any())
    }

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

    // Error Handling Tests
    @Test
    fun `clearError removes error message`() {
        // Given
        viewModel.showAddTodoDialog()
        viewModel.addTodo() // This sets an error

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `loading state is managed correctly during addTodo`() = runTest {
        // Given
        viewModel.showAddTodoDialog()
        viewModel.updateNewTodoText("Valid text")
        `when`(repository.insertTodo(anyString(), any())).thenReturn(1L)

        // When
        viewModel.addTodo()
        
        // Then - loading should be true initially
        assertTrue(viewModel.uiState.value.isLoading)
        
        advanceUntilIdle()
        
        // Then - loading should be false after completion
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loading state is managed correctly during deleteTodo`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.HEALTH, 0, 123L)
        viewModel.showDeleteDialog(todo)

        // When
        viewModel.deleteTodo()
        
        // Then - loading should be true initially
        assertTrue(viewModel.uiState.value.isLoading)
        
        advanceUntilIdle()
        
        // Then - loading should be false after completion
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // Category-specific workflow tests
    @Test
    fun `complete workflow with category filtering`() = runTest {
        // Given
        val workTodos = listOf(
            Todo(1, "Work task", false, TodoCategory.WORK, 0, 123L)
        )
        `when`(repository.getTodosByCategory(TodoCategory.WORK)).thenReturn(flowOf(workTodos))

        // When - Set filter, add todo, toggle completion
        viewModel.setCategoryFilter(TodoCategory.WORK)
        viewModel.showAddTodoDialog()
        viewModel.updateSelectedCategory(TodoCategory.WORK)
        viewModel.updateNewTodoText("New work task")
        `when`(repository.insertTodo("New work task", TodoCategory.WORK)).thenReturn(2L)
        viewModel.addTodo()
        advanceUntilIdle()

        // Then
        assertEquals(TodoCategory.WORK, viewModel.selectedCategoryFilter.value)
        assertFalse(viewModel.uiState.value.isAddingTodo)
        verify(repository).insertTodo("New work task", TodoCategory.WORK)
    }
}