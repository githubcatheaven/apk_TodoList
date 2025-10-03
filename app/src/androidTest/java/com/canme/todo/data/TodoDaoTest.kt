package com.canme.todo.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Instrumented tests for TodoDao.
 * Tests database operations using an in-memory Room database.
 */
@RunWith(AndroidJUnit4::class)
class TodoDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: TodoDatabase
    private lateinit var todoDao: TodoDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TodoDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        
        todoDao = database.todoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertTodo_and_getTodoById_returnsCorrectTodo() = runTest {
        // Given
        val todo = Todo(text = "Test todo", isCompleted = false, createdAt = 123L)

        // When
        todoDao.insertTodo(todo)
        val retrievedTodo = todoDao.getTodoById(1L) // Auto-generated ID should be 1

        // Then
        assertNotNull(retrievedTodo)
        assertEquals("Test todo", retrievedTodo.text)
        assertEquals(false, retrievedTodo.isCompleted)
        assertEquals(123L, retrievedTodo.createdAt)
    }

    @Test
    fun getAllTodos_returnsEmptyList_whenNoTodos() = runTest {
        // When
        val todos = todoDao.getAllTodos().first()

        // Then
        assertTrue(todos.isEmpty())
    }

    @Test
    fun getAllTodos_returnsAllTodos_orderedByCreatedAtDesc() = runTest {
        // Given
        val todo1 = Todo(text = "First todo", createdAt = 100L)
        val todo2 = Todo(text = "Second todo", createdAt = 200L)
        val todo3 = Todo(text = "Third todo", createdAt = 150L)

        // When
        todoDao.insertTodo(todo1)
        todoDao.insertTodo(todo2)
        todoDao.insertTodo(todo3)
        
        val todos = todoDao.getAllTodos().first()

        // Then
        assertEquals(3, todos.size)
        // Should be ordered by createdAt DESC (newest first)
        assertEquals("Second todo", todos[0].text) // createdAt = 200L
        assertEquals("Third todo", todos[1].text)  // createdAt = 150L
        assertEquals("First todo", todos[2].text)  // createdAt = 100L
    }

    @Test
    fun updateTodo_modifiesExistingTodo() = runTest {
        // Given
        val originalTodo = Todo(text = "Original text", isCompleted = false)
        todoDao.insertTodo(originalTodo)
        
        val insertedTodo = todoDao.getTodoById(1L)!!
        val updatedTodo = insertedTodo.copy(text = "Updated text", isCompleted = true)

        // When
        todoDao.updateTodo(updatedTodo)
        val retrievedTodo = todoDao.getTodoById(1L)

        // Then
        assertNotNull(retrievedTodo)
        assertEquals("Updated text", retrievedTodo.text)
        assertEquals(true, retrievedTodo.isCompleted)
        assertEquals(insertedTodo.id, retrievedTodo.id)
        assertEquals(insertedTodo.createdAt, retrievedTodo.createdAt)
    }

    @Test
    fun deleteTodo_removesTodo() = runTest {
        // Given
        val todo = Todo(text = "Todo to delete")
        todoDao.insertTodo(todo)
        
        val insertedTodo = todoDao.getTodoById(1L)!!

        // When
        todoDao.deleteTodo(insertedTodo)
        val retrievedTodo = todoDao.getTodoById(1L)

        // Then
        assertNull(retrievedTodo)
        
        val allTodos = todoDao.getAllTodos().first()
        assertTrue(allTodos.isEmpty())
    }

    @Test
    fun deleteAllTodos_removesAllTodos() = runTest {
        // Given
        val todo1 = Todo(text = "First todo")
        val todo2 = Todo(text = "Second todo")
        val todo3 = Todo(text = "Third todo")
        
        todoDao.insertTodo(todo1)
        todoDao.insertTodo(todo2)
        todoDao.insertTodo(todo3)

        // Verify todos were inserted
        val todosBeforeDelete = todoDao.getAllTodos().first()
        assertEquals(3, todosBeforeDelete.size)

        // When
        todoDao.deleteAllTodos()

        // Then
        val todosAfterDelete = todoDao.getAllTodos().first()
        assertTrue(todosAfterDelete.isEmpty())
    }

    @Test
    fun getTodoById_returnsNull_whenTodoDoesNotExist() = runTest {
        // When
        val retrievedTodo = todoDao.getTodoById(999L)

        // Then
        assertNull(retrievedTodo)
    }

    @Test
    fun insertMultipleTodos_andRetrieveAll_maintainsCorrectOrder() = runTest {
        // Given
        val todos = listOf(
            Todo(text = "Todo 1", createdAt = 300L),
            Todo(text = "Todo 2", createdAt = 100L),
            Todo(text = "Todo 3", createdAt = 200L),
            Todo(text = "Todo 4", createdAt = 400L)
        )

        // When
        todos.forEach { todoDao.insertTodo(it) }
        val retrievedTodos = todoDao.getAllTodos().first()

        // Then
        assertEquals(4, retrievedTodos.size)
        // Should be ordered by createdAt DESC
        assertEquals("Todo 4", retrievedTodos[0].text) // 400L
        assertEquals("Todo 1", retrievedTodos[1].text) // 300L
        assertEquals("Todo 3", retrievedTodos[2].text) // 200L
        assertEquals("Todo 2", retrievedTodos[3].text) // 100L
    }

    @Test
    fun todoDao_handlesSpecialCharacters() = runTest {
        // Given
        val specialText = "Todo with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?"
        val todo = Todo(text = specialText)

        // When
        todoDao.insertTodo(todo)
        val retrievedTodo = todoDao.getTodoById(1L)

        // Then
        assertNotNull(retrievedTodo)
        assertEquals(specialText, retrievedTodo.text)
    }

    @Test
    fun todoDao_handlesLongText() = runTest {
        // Given
        val longText = "A".repeat(1000) // 1000 character string
        val todo = Todo(text = longText)

        // When
        todoDao.insertTodo(todo)
        val retrievedTodo = todoDao.getTodoById(1L)

        // Then
        assertNotNull(retrievedTodo)
        assertEquals(longText, retrievedTodo.text)
        assertEquals(1000, retrievedTodo.text.length)
    }
}