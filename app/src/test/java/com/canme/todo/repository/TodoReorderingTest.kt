package com.canme.todo.repository

import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.data.TodoDao
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

/**
 * Unit tests for TodoRepository reordering functionality.
 * Tests the drag-and-drop reordering logic in the repository layer.
 */
class TodoReorderingTest {

    @Mock
    private lateinit var todoDao: TodoDao

    private lateinit var todoRepository: TodoRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        todoRepository = TodoRepository(todoDao)
    }

    @Test
    fun `reorderTodos calls dao with correct reordered list`() = runTest {
        // Given
        val originalTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L),
            Todo(3, "Third", false, TodoCategory.SHOPPING, 2, 125L)
        )
        
        val reorderedTodos = listOf(
            Todo(2, "Second", false, TodoCategory.WORK, 0, 124L),
            Todo(3, "Third", false, TodoCategory.SHOPPING, 1, 125L),
            Todo(1, "First", false, TodoCategory.PERSONAL, 2, 123L)
        )

        // When
        todoRepository.reorderTodos(reorderedTodos)

        // Then
        verify(todoDao).reorderTodos(reorderedTodos)
    }

    @Test
    fun `reorderTodos handles empty list`() = runTest {
        // Given
        val emptyList = emptyList<Todo>()

        // When
        todoRepository.reorderTodos(emptyList)

        // Then
        verify(todoDao).reorderTodos(emptyList)
    }

    @Test
    fun `reorderTodos handles single item list`() = runTest {
        // Given
        val singleTodo = listOf(
            Todo(1, "Only todo", false, TodoCategory.PERSONAL, 0, 123L)
        )

        // When
        todoRepository.reorderTodos(singleTodo)

        // Then
        verify(todoDao).reorderTodos(singleTodo)
    }

    @Test
    fun `reorderTodos preserves todo properties except sortOrder`() = runTest {
        // Given
        val originalTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", true, TodoCategory.WORK, 1, 124L)
        )
        
        // Simulate reordering where only positions change
        val reorderedTodos = listOf(
            Todo(2, "Second", true, TodoCategory.WORK, 0, 124L), // Moved to first position
            Todo(1, "First", false, TodoCategory.PERSONAL, 1, 123L) // Moved to second position
        )

        // When
        todoRepository.reorderTodos(reorderedTodos)

        // Then
        verify(todoDao).reorderTodos(argThat { todos ->
            todos.size == 2 &&
            todos[0].id == 2L &&
            todos[0].text == "Second" &&
            todos[0].isCompleted == true &&
            todos[0].category == TodoCategory.WORK &&
            todos[0].sortOrder == 0 &&
            todos[1].id == 1L &&
            todos[1].text == "First" &&
            todos[1].isCompleted == false &&
            todos[1].category == TodoCategory.PERSONAL &&
            todos[1].sortOrder == 1
        })
    }

    @Test
    fun `reorderTodos handles complex reordering scenario`() = runTest {
        // Given - 5 todos with various properties
        val originalTodos = listOf(
            Todo(1, "Todo A", false, TodoCategory.PERSONAL, 0, 100L),
            Todo(2, "Todo B", true, TodoCategory.WORK, 1, 200L),
            Todo(3, "Todo C", false, TodoCategory.SHOPPING, 2, 300L),
            Todo(4, "Todo D", true, TodoCategory.HEALTH, 3, 400L),
            Todo(5, "Todo E", false, TodoCategory.OTHER, 4, 500L)
        )
        
        // Reorder: move first to last, shift others up
        val reorderedTodos = listOf(
            Todo(2, "Todo B", true, TodoCategory.WORK, 0, 200L),
            Todo(3, "Todo C", false, TodoCategory.SHOPPING, 1, 300L),
            Todo(4, "Todo D", true, TodoCategory.HEALTH, 2, 400L),
            Todo(5, "Todo E", false, TodoCategory.OTHER, 3, 500L),
            Todo(1, "Todo A", false, TodoCategory.PERSONAL, 4, 100L)
        )

        // When
        todoRepository.reorderTodos(reorderedTodos)

        // Then
        verify(todoDao).reorderTodos(argThat { todos ->
            todos.size == 5 &&
            todos[0].id == 2L && todos[0].sortOrder == 0 &&
            todos[1].id == 3L && todos[1].sortOrder == 1 &&
            todos[2].id == 4L && todos[2].sortOrder == 2 &&
            todos[3].id == 5L && todos[3].sortOrder == 3 &&
            todos[4].id == 1L && todos[4].sortOrder == 4
        })
    }

    @Test
    fun `reorderTodos handles dao exception gracefully`() = runTest {
        // Given
        val todos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 1, 124L)
        )
        `when`(todoDao.reorderTodos(any())).thenThrow(RuntimeException("Database error"))

        // When & Then - Exception should propagate
        try {
            todoRepository.reorderTodos(todos)
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }

    @Test
    fun `insertTodo calculates correct sortOrder for reordered list`() = runTest {
        // Given - Existing todos with non-sequential sort orders (after reordering)
        val existingTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, 123L),
            Todo(2, "Second", false, TodoCategory.WORK, 5, 124L), // Gap in sort order
            Todo(3, "Third", false, TodoCategory.SHOPPING, 10, 125L)
        )
        `when`(todoDao.getAllTodos()).thenReturn(flowOf(existingTodos))
        `when`(todoDao.insertTodo(any())).thenReturn(4L)

        // When
        todoRepository.insertTodo("New todo", TodoCategory.HEALTH)

        // Then - Should use max sortOrder + 1
        verify(todoDao).insertTodo(argThat { todo ->
            todo.sortOrder == 11 // Max (10) + 1
        })
    }

    @Test
    fun `reorderTodos maintains data integrity across categories`() = runTest {
        // Given - Mixed categories in reordered list
        val reorderedTodos = listOf(
            Todo(3, "Shopping", false, TodoCategory.SHOPPING, 0, 300L),
            Todo(1, "Personal", true, TodoCategory.PERSONAL, 1, 100L),
            Todo(4, "Health", false, TodoCategory.HEALTH, 2, 400L),
            Todo(2, "Work", true, TodoCategory.WORK, 3, 200L)
        )

        // When
        todoRepository.reorderTodos(reorderedTodos)

        // Then - All properties should be preserved
        verify(todoDao).reorderTodos(argThat { todos ->
            todos.size == 4 &&
            // Verify each todo maintains its properties
            todos.find { it.id == 1L }?.let { 
                it.text == "Personal" && 
                it.isCompleted == true && 
                it.category == TodoCategory.PERSONAL &&
                it.sortOrder == 1
            } == true &&
            todos.find { it.id == 2L }?.let { 
                it.text == "Work" && 
                it.isCompleted == true && 
                it.category == TodoCategory.WORK &&
                it.sortOrder == 3
            } == true &&
            todos.find { it.id == 3L }?.let { 
                it.text == "Shopping" && 
                it.isCompleted == false && 
                it.category == TodoCategory.SHOPPING &&
                it.sortOrder == 0
            } == true &&
            todos.find { it.id == 4L }?.let { 
                it.text == "Health" && 
                it.isCompleted == false && 
                it.category == TodoCategory.HEALTH &&
                it.sortOrder == 2
            } == true
        })
    }
}