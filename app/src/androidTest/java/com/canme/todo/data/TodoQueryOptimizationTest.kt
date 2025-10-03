package com.canme.todo.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test class for verifying query optimization and correctness.
 * 
 * This class tests that the updated queries work correctly with the new
 * database schema including category and sortOrder fields.
 */
@RunWith(AndroidJUnit4::class)
class TodoQueryOptimizationTest {
    
    private lateinit var database: TodoDatabase
    private lateinit var dao: TodoDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TodoDatabase::class.java
        ).build()
        dao = database.todoDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    /**
     * Test that todos are correctly ordered by sortOrder then createdAt.
     */
    @Test
    fun testCorrectOrdering() = runBlocking {
        val baseTime = System.currentTimeMillis()
        
        // Insert todos with mixed sort orders and creation times
        val todos = listOf(
            Todo(text = "Todo 1", sortOrder = 2, createdAt = baseTime - 1000),
            Todo(text = "Todo 2", sortOrder = 1, createdAt = baseTime - 2000),
            Todo(text = "Todo 3", sortOrder = 1, createdAt = baseTime - 1000), // Same sortOrder, newer
            Todo(text = "Todo 4", sortOrder = 0, createdAt = baseTime - 3000),
            Todo(text = "Todo 5", sortOrder = 2, createdAt = baseTime - 2000)  // Same sortOrder, older
        )
        
        // Insert in random order
        todos.shuffled().forEach { dao.insertTodo(it) }
        
        // Retrieve and verify ordering
        val result = dao.getAllTodos().first()
        
        // Expected order: sortOrder ASC, then createdAt DESC
        // Todo 4 (sortOrder=0), Todo 3 (sortOrder=1, newer), Todo 2 (sortOrder=1, older), 
        // Todo 1 (sortOrder=2, newer), Todo 5 (sortOrder=2, older)
        assertEquals("Todo 4", result[0].text)
        assertEquals("Todo 3", result[1].text)
        assertEquals("Todo 2", result[2].text)
        assertEquals("Todo 1", result[3].text)
        assertEquals("Todo 5", result[4].text)
    }
    
    /**
     * Test category filtering functionality.
     */
    @Test
    fun testCategoryFiltering() = runBlocking {
        // Insert todos with different categories
        dao.insertTodo(Todo(text = "Personal Task", category = TodoCategory.PERSONAL))
        dao.insertTodo(Todo(text = "Work Task 1", category = TodoCategory.WORK))
        dao.insertTodo(Todo(text = "Work Task 2", category = TodoCategory.WORK))
        dao.insertTodo(Todo(text = "Shopping Task", category = TodoCategory.SHOPPING))
        dao.insertTodo(Todo(text = "Health Task", category = TodoCategory.HEALTH))
        
        // Test filtering by WORK category
        val workTodos = dao.getTodosByCategory(TodoCategory.WORK).first()
        assertEquals(2, workTodos.size)
        assertTrue(workTodos.all { it.category == TodoCategory.WORK })
        
        // Test filtering by PERSONAL category
        val personalTodos = dao.getTodosByCategory(TodoCategory.PERSONAL).first()
        assertEquals(1, personalTodos.size)
        assertEquals("Personal Task", personalTodos[0].text)
        
        // Test filtering by category with no todos
        val otherTodos = dao.getTodosByCategory(TodoCategory.OTHER).first()
        assertEquals(0, otherTodos.size)
    }
    
    /**
     * Test completion status filtering.
     */
    @Test
    fun testStatusFiltering() = runBlocking {
        // Insert todos with different completion statuses
        dao.insertTodo(Todo(text = "Completed Task 1", isCompleted = true))
        dao.insertTodo(Todo(text = "Incomplete Task 1", isCompleted = false))
        dao.insertTodo(Todo(text = "Completed Task 2", isCompleted = true))
        dao.insertTodo(Todo(text = "Incomplete Task 2", isCompleted = false))
        dao.insertTodo(Todo(text = "Incomplete Task 3", isCompleted = false))
        
        // Test filtering completed todos
        val completedTodos = dao.getTodosByStatus(true).first()
        assertEquals(2, completedTodos.size)
        assertTrue(completedTodos.all { it.isCompleted })
        
        // Test filtering incomplete todos
        val incompleteTodos = dao.getTodosByStatus(false).first()
        assertEquals(3, incompleteTodos.size)
        assertTrue(incompleteTodos.all { !it.isCompleted })
    }
    
    /**
     * Test combined category and status filtering.
     */
    @Test
    fun testCombinedFiltering() = runBlocking {
        // Insert todos with various combinations
        dao.insertTodo(Todo(text = "Personal Completed", category = TodoCategory.PERSONAL, isCompleted = true))
        dao.insertTodo(Todo(text = "Personal Incomplete", category = TodoCategory.PERSONAL, isCompleted = false))
        dao.insertTodo(Todo(text = "Work Completed", category = TodoCategory.WORK, isCompleted = true))
        dao.insertTodo(Todo(text = "Work Incomplete 1", category = TodoCategory.WORK, isCompleted = false))
        dao.insertTodo(Todo(text = "Work Incomplete 2", category = TodoCategory.WORK, isCompleted = false))
        
        // Test work + incomplete
        val workIncomplete = dao.getTodosByCategoryAndStatus(TodoCategory.WORK, false).first()
        assertEquals(2, workIncomplete.size)
        assertTrue(workIncomplete.all { it.category == TodoCategory.WORK && !it.isCompleted })
        
        // Test personal + completed
        val personalCompleted = dao.getTodosByCategoryAndStatus(TodoCategory.PERSONAL, true).first()
        assertEquals(1, personalCompleted.size)
        assertEquals("Personal Completed", personalCompleted[0].text)
        
        // Test combination with no results
        val shoppingCompleted = dao.getTodosByCategoryAndStatus(TodoCategory.SHOPPING, true).first()
        assertEquals(0, shoppingCompleted.size)
    }
    
    /**
     * Test count queries for performance and correctness.
     */
    @Test
    fun testCountQueries() = runBlocking {
        // Insert test data
        repeat(5) { dao.insertTodo(Todo(text = "Personal $it", category = TodoCategory.PERSONAL)) }
        repeat(3) { dao.insertTodo(Todo(text = "Work $it", category = TodoCategory.WORK)) }
        repeat(2) { dao.insertTodo(Todo(text = "Shopping $it", category = TodoCategory.SHOPPING)) }
        
        // Test total count
        val totalCount = dao.getTodoCount().first()
        assertEquals(10, totalCount)
        
        // Test category counts
        val personalCount = dao.getTodoCountByCategory(TodoCategory.PERSONAL).first()
        assertEquals(5, personalCount)
        
        val workCount = dao.getTodoCountByCategory(TodoCategory.WORK).first()
        assertEquals(3, workCount)
        
        val shoppingCount = dao.getTodoCountByCategory(TodoCategory.SHOPPING).first()
        assertEquals(2, shoppingCount)
        
        val healthCount = dao.getTodoCountByCategory(TodoCategory.HEALTH).first()
        assertEquals(0, healthCount)
    }
    
    /**
     * Test reordering functionality.
     */
    @Test
    fun testReordering() = runBlocking {
        // Insert todos with initial order
        val todo1 = Todo(text = "First", sortOrder = 0)
        val todo2 = Todo(text = "Second", sortOrder = 1)
        val todo3 = Todo(text = "Third", sortOrder = 2)
        
        val id1 = dao.insertTodo(todo1)
        val id2 = dao.insertTodo(todo2)
        val id3 = dao.insertTodo(todo3)
        
        // Get initial order
        val initialTodos = dao.getAllTodos().first()
        assertEquals("First", initialTodos[0].text)
        assertEquals("Second", initialTodos[1].text)
        assertEquals("Third", initialTodos[2].text)
        
        // Reorder: move "Third" to first position
        val reorderedTodos = listOf(
            initialTodos[2].copy(sortOrder = 0), // Third -> first
            initialTodos[0].copy(sortOrder = 1), // First -> second
            initialTodos[1].copy(sortOrder = 2)  // Second -> third
        )
        
        dao.reorderTodos(reorderedTodos)
        
        // Verify new order
        val newOrder = dao.getAllTodos().first()
        assertEquals("Third", newOrder[0].text)
        assertEquals("First", newOrder[1].text)
        assertEquals("Second", newOrder[2].text)
    }
    
    /**
     * Test that ordering works correctly within categories.
     */
    @Test
    fun testOrderingWithinCategories() = runBlocking {
        val baseTime = System.currentTimeMillis()
        
        // Insert work todos with different sort orders
        dao.insertTodo(Todo(text = "Work Task 1", category = TodoCategory.WORK, sortOrder = 2, createdAt = baseTime - 1000))
        dao.insertTodo(Todo(text = "Work Task 2", category = TodoCategory.WORK, sortOrder = 1, createdAt = baseTime - 2000))
        dao.insertTodo(Todo(text = "Work Task 3", category = TodoCategory.WORK, sortOrder = 1, createdAt = baseTime - 500))
        
        // Get work todos and verify ordering
        val workTodos = dao.getTodosByCategory(TodoCategory.WORK).first()
        
        // Should be ordered by sortOrder ASC, then createdAt DESC
        assertEquals("Work Task 3", workTodos[0].text) // sortOrder=1, newer
        assertEquals("Work Task 2", workTodos[1].text) // sortOrder=1, older
        assertEquals("Work Task 1", workTodos[2].text) // sortOrder=2
    }
}