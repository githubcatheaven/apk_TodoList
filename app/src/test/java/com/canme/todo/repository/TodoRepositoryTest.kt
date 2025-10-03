package com.canme.todo.repository

import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.data.TodoDao
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for TodoRepository.
 * Tests the repository layer functionality including category operations and error handling.
 */
class TodoRepositoryTest {

    @Mock
    private lateinit var todoDao: TodoDao

    private lateinit var todoRepository: TodoRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        todoRepository = TodoRepository(todoDao)
    }

    // Basic CRUD operations tests
    @Test
    fun `getAllTodos returns flow from dao`() = runTest {
        // Given
        val todos = listOf(
            Todo(1, "Test todo 1", false, TodoCategory.PERSONAL, 0, System.currentTimeMillis()),
            Todo(2, "Test todo 2", true, TodoCategory.WORK, 1, System.currentTimeMillis())
        )
        `when`(todoDao.getAllTodos()).thenReturn(flowOf(todos))

        // When
        val result = todoRepository.getAllTodos()

        // Then
        result.collect { todoList ->
            assertEquals(2, todoList.size)
            assertEquals("Test todo 1", todoList[0].text)
            assertEquals("Test todo 2", todoList[1].text)
            assertEquals(TodoCategory.PERSONAL, todoList[0].category)
            assertEquals(TodoCategory.WORK, todoList[1].category)
        }
    }

    @Test
    fun `insertTodo with valid text and default category creates todo`() = runTest {
        // Given
        val text = "Valid todo text"
        val existingTodos = emptyList<Todo>()
        `when`(todoDao.getAllTodos()).thenReturn(flowOf(existingTodos))
        `when`(todoDao.insertTodo(any())).thenReturn(1L)

        // When
        val result = todoRepository.insertTodo(text)

        // Then
        assertEquals(1L, result)
        verify(todoDao).insertTodo(argThat { todo ->
            todo.text == text.trim() && 
            todo.category == TodoCategory.PERSONAL &&
            todo.sortOrder == 0
        })
    }

    @Test
    fun `insertTodo with specific category creates todo with correct category`() = runTest {
        // Given
        val text = "Work todo"
        val category = TodoCategory.WORK
        val existingTodos = listOf(
            Todo(1, "Existing", false, TodoCategory.PERSONAL, 0, System.currentTimeMillis())
        )
        `when`(todoDao.getAllTodos()).thenReturn(flowOf(existingTodos))
        `when`(todoDao.insertTodo(any())).thenReturn(2L)

        // When
        val result = todoRepository.insertTodo(text, category)

        // Then
        assertEquals(2L, result)
        verify(todoDao).insertTodo(argThat { todo ->
            todo.text == text &&
            todo.category == category &&
            todo.sortOrder == 1 // Should be max + 1
        })
    }

    @Test
    fun `insertTodo trims whitespace from text`() = runTest {
        // Given
        val text = "  Todo with spaces  "
        val existingTodos = emptyList<Todo>()
        `when`(todoDao.getAllTodos()).thenReturn(flowOf(existingTodos))
        `when`(todoDao.insertTodo(any())).thenReturn(1L)

        // When
        todoRepository.insertTodo(text)

        // Then
        verify(todoDao).insertTodo(argThat { todo ->
            todo.text == "Todo with spaces"
        })
    }

    @Test
    fun `toggleTodoCompletion toggles completion status`() = runTest {
        // Given
        val todo = Todo(1, "Test todo", false, TodoCategory.PERSONAL, 0, System.currentTimeMillis())

        // When
        todoRepository.toggleTodoCompletion(todo)

        // Then
        verify(todoDao).updateTodo(argThat { updatedTodo ->
            updatedTodo.id == todo.id &&
            updatedTodo.isCompleted == true &&
            updatedTodo.text == todo.text &&
            updatedTodo.category == todo.category
        })
    }

    @Test
    fun `deleteTodo calls dao deleteTodo`() = runTest {
        // Given
        val todo = Todo(1, "Todo to delete", false, TodoCategory.SHOPPING, 0, System.currentTimeMillis())

        // When
        todoRepository.deleteTodo(todo)

        // Then
        verify(todoDao).deleteTodo(todo)
    }

    // Category-specific tests
    @Test
    fun `getTodosByCategory returns filtered todos from dao`() = runTest {
        // Given
        val category = TodoCategory.WORK
        val workTodos = listOf(
            Todo(1, "Work todo 1", false, TodoCategory.WORK, 0, System.currentTimeMillis()),
            Todo(2, "Work todo 2", true, TodoCategory.WORK, 1, System.currentTimeMillis())
        )
        `when`(todoDao.getTodosByCategory(category)).thenReturn(flowOf(workTodos))

        // When
        val result = todoRepository.getTodosByCategory(category)

        // Then
        result.collect { todoList ->
            assertEquals(2, todoList.size)
            todoList.forEach { todo ->
                assertEquals(TodoCategory.WORK, todo.category)
            }
        }
        verify(todoDao).getTodosByCategory(category)
    }

    @Test
    fun `updateTodoCategory updates todo with new category`() = runTest {
        // Given
        val originalTodo = Todo(1, "Test todo", false, TodoCategory.PERSONAL, 0, System.currentTimeMillis())
        val newCategory = TodoCategory.HEALTH

        // When
        todoRepository.updateTodoCategory(originalTodo, newCategory)

        // Then
        verify(todoDao).updateTodo(argThat { updatedTodo ->
            updatedTodo.id == originalTodo.id &&
            updatedTodo.text == originalTodo.text &&
            updatedTodo.isCompleted == originalTodo.isCompleted &&
            updatedTodo.category == newCategory &&
            updatedTodo.sortOrder == originalTodo.sortOrder &&
            updatedTodo.createdAt == originalTodo.createdAt
        })
    }

    @Test
    fun `getTodosByStatus returns filtered todos by completion status`() = runTest {
        // Given
        val completedTodos = listOf(
            Todo(1, "Completed 1", true, TodoCategory.PERSONAL, 0, System.currentTimeMillis()),
            Todo(2, "Completed 2", true, TodoCategory.WORK, 1, System.currentTimeMillis())
        )
        `when`(todoDao.getTodosByStatus(true)).thenReturn(flowOf(completedTodos))

        // When
        val result = todoRepository.getTodosByStatus(true)

        // Then
        result.collect { todoList ->
            assertEquals(2, todoList.size)
            todoList.forEach { todo ->
                assertTrue(todo.isCompleted)
            }
        }
        verify(todoDao).getTodosByStatus(true)
    }

    @Test
    fun `getTodosByCategoryAndStatus returns filtered todos by category and status`() = runTest {
        // Given
        val category = TodoCategory.SHOPPING
        val isCompleted = false
        val filteredTodos = listOf(
            Todo(1, "Shopping item 1", false, TodoCategory.SHOPPING, 0, System.currentTimeMillis()),
            Todo(2, "Shopping item 2", false, TodoCategory.SHOPPING, 1, System.currentTimeMillis())
        )
        `when`(todoDao.getTodosByCategoryAndStatus(category, isCompleted)).thenReturn(flowOf(filteredTodos))

        // When
        val result = todoRepository.getTodosByCategoryAndStatus(category, isCompleted)

        // Then
        result.collect { todoList ->
            assertEquals(2, todoList.size)
            todoList.forEach { todo ->
                assertEquals(TodoCategory.SHOPPING, todo.category)
                assertFalse(todo.isCompleted)
            }
        }
        verify(todoDao).getTodosByCategoryAndStatus(category, isCompleted)
    }

    @Test
    fun `getTodoCountByCategory returns count from dao`() = runTest {
        // Given
        val category = TodoCategory.HEALTH
        val count = 5
        `when`(todoDao.getTodoCountByCategory(category)).thenReturn(flowOf(count))

        // When
        val result = todoRepository.getTodoCountByCategory(category)

        // Then
        result.collect { actualCount ->
            assertEquals(count, actualCount)
        }
        verify(todoDao).getTodoCountByCategory(category)
    }

    @Test
    fun `getTodoCount returns total count from dao`() = runTest {
        // Given
        val totalCount = 10
        `when`(todoDao.getTodoCount()).thenReturn(flowOf(totalCount))

        // When
        val result = todoRepository.getTodoCount()

        // Then
        result.collect { count ->
            assertEquals(totalCount, count)
        }
        verify(todoDao).getTodoCount()
    }

    @Test
    fun `deleteCompletedTodos calls dao deleteCompletedTodos`() = runTest {
        // When
        todoRepository.deleteCompletedTodos()

        // Then
        verify(todoDao).deleteCompletedTodos()
    }

    @Test
    fun `updateTodoText updates todo with new text`() = runTest {
        // Given
        val originalTodo = Todo(1, "Original text", false, TodoCategory.OTHER, 0, System.currentTimeMillis())
        val newText = "  Updated text  "

        // When
        todoRepository.updateTodoText(originalTodo, newText)

        // Then
        verify(todoDao).updateTodo(argThat { updatedTodo ->
            updatedTodo.id == originalTodo.id &&
            updatedTodo.text == "Updated text" && // Should be trimmed
            updatedTodo.isCompleted == originalTodo.isCompleted &&
            updatedTodo.category == originalTodo.category &&
            updatedTodo.sortOrder == originalTodo.sortOrder &&
            updatedTodo.createdAt == originalTodo.createdAt
        })
    }

    // Validation tests
    @Test
    fun `isValidTodoText returns true for valid text`() {
        // Given
        val validTexts = listOf("Valid todo", "  Valid with spaces  ", "123", "Special chars !@#")

        // When & Then
        validTexts.forEach { text ->
            assertTrue(todoRepository.isValidTodoText(text), "Text '$text' should be valid")
        }
    }

    @Test
    fun `isValidTodoText returns false for invalid text`() {
        // Given
        val invalidTexts = listOf("", "   ", "\n\t  ", "\r\n")

        // When & Then
        invalidTexts.forEach { text ->
            assertFalse(todoRepository.isValidTodoText(text), "Text '$text' should be invalid")
        }
    }

    // Reordering tests
    @Test
    fun `reorderTodos calls dao reorderTodos`() = runTest {
        // Given
        val reorderedTodos = listOf(
            Todo(2, "Second", false, TodoCategory.PERSONAL, 0, System.currentTimeMillis()),
            Todo(1, "First", false, TodoCategory.WORK, 1, System.currentTimeMillis())
        )

        // When
        todoRepository.reorderTodos(reorderedTodos)

        // Then
        verify(todoDao).reorderTodos(reorderedTodos)
    }

    // Sort order calculation tests
    @Test
    fun `insertTodo calculates correct sort order for empty list`() = runTest {
        // Given
        val text = "First todo"
        val emptyTodos = emptyList<Todo>()
        `when`(todoDao.getAllTodos()).thenReturn(flowOf(emptyTodos))
        `when`(todoDao.insertTodo(any())).thenReturn(1L)

        // When
        todoRepository.insertTodo(text)

        // Then
        verify(todoDao).insertTodo(argThat { todo ->
            todo.sortOrder == 0
        })
    }

    @Test
    fun `insertTodo calculates correct sort order for existing todos`() = runTest {
        // Given
        val text = "New todo"
        val existingTodos = listOf(
            Todo(1, "First", false, TodoCategory.PERSONAL, 0, System.currentTimeMillis()),
            Todo(2, "Second", false, TodoCategory.WORK, 5, System.currentTimeMillis()),
            Todo(3, "Third", false, TodoCategory.SHOPPING, 2, System.currentTimeMillis())
        )
        `when`(todoDao.getAllTodos()).thenReturn(flowOf(existingTodos))
        `when`(todoDao.insertTodo(any())).thenReturn(4L)

        // When
        todoRepository.insertTodo(text)

        // Then
        verify(todoDao).insertTodo(argThat { todo ->
            todo.sortOrder == 6 // Max sortOrder (5) + 1
        })
    }
}