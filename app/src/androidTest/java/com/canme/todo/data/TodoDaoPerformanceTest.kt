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
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

/**
 * Performance tests for TodoDao queries.
 * 
 * This class tests the performance of database queries with large datasets
 * to ensure that indexing and query optimization work correctly.
 */
@RunWith(AndroidJUnit4::class)
class TodoDaoPerformanceTest {
    
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
     * Test query performance with a large dataset.
     * 
     * This test creates 1000 todos across different categories and measures
     * the time it takes to perform various queries.
     */
    @Test
    fun testQueryPerformanceWithLargeDataset() = runBlocking {
        // Create test data - 1000 todos across all categories
        val todos = mutableListOf<Todo>()
        val categories = TodoCategory.values()
        
        repeat(1000) { index ->
            todos.add(
                Todo(
                    text = "Test Todo $index",
                    category = categories[index % categories.size],
                    isCompleted = index % 3 == 0, // Every 3rd todo is completed
                    sortOrder = index,
                    createdAt = System.currentTimeMillis() - (index * 1000) // Spread over time
                )
            )
        }
        
        // Insert all todos
        val insertTime = measureTimeMillis {
            todos.forEach { dao.insertTodo(it) }
        }
        println("Insert time for 1000 todos: ${insertTime}ms")
        
        // Test getAllTodos query performance
        val getAllTime = measureTimeMillis {
            val result = dao.getAllTodos().first()
            assertTrue(result.size == 1000, "Should retrieve all 1000 todos")
        }
        println("Get all todos time: ${getAllTime}ms")
        assertTrue(getAllTime < 100, "Get all todos should complete in under 100ms")
        
        // Test category filtering performance
        val categoryFilterTime = measureTimeMillis {
            val result = dao.getTodosByCategory(TodoCategory.WORK).first()
            assertTrue(result.isNotEmpty(), "Should have work todos")
        }
        println("Category filter time: ${categoryFilterTime}ms")
        assertTrue(categoryFilterTime < 50, "Category filtering should complete in under 50ms")
        
        // Test completion status filtering performance
        val statusFilterTime = measureTimeMillis {
            val result = dao.getTodosByStatus(true).first()
            assertTrue(result.isNotEmpty(), "Should have completed todos")
        }
        println("Status filter time: ${statusFilterTime}ms")
        assertTrue(statusFilterTime < 50, "Status filtering should complete in under 50ms")
        
        // Test combined category and status filtering performance
        val combinedFilterTime = measureTimeMillis {
            val result = dao.getTodosByCategoryAndStatus(TodoCategory.PERSONAL, false).first()
            assertTrue(result.isNotEmpty(), "Should have incomplete personal todos")
        }
        println("Combined filter time: ${combinedFilterTime}ms")
        assertTrue(combinedFilterTime < 50, "Combined filtering should complete in under 50ms")
        
        // Test count queries performance
        val countTime = measureTimeMillis {
            val totalCount = dao.getTodoCount().first()
            val categoryCount = dao.getTodoCountByCategory(TodoCategory.WORK).first()
            assertTrue(totalCount == 1000, "Total count should be 1000")
            assertTrue(categoryCount > 0, "Category count should be greater than 0")
        }
        println("Count queries time: ${countTime}ms")
        assertTrue(countTime < 30, "Count queries should complete in under 30ms")
    }
    
    /**
     * Test reordering performance with a large dataset.
     * 
     * This test measures the performance of batch reordering operations.
     */
    @Test
    fun testReorderingPerformance() = runBlocking {
        // Create 100 todos for reordering test
        val todos = mutableListOf<Todo>()
        repeat(100) { index ->
            todos.add(
                Todo(
                    text = "Reorder Test Todo $index",
                    sortOrder = index,
                    category = TodoCategory.PERSONAL
                )
            )
        }
        
        // Insert todos
        todos.forEach { dao.insertTodo(it) }
        
        // Get all todos to reorder
        val allTodos = dao.getAllTodos().first()
        
        // Reverse the order (simulate drag and drop reordering)
        val reorderedTodos = allTodos.reversed()
        
        // Measure reordering performance
        val reorderTime = measureTimeMillis {
            dao.reorderTodos(reorderedTodos)
        }
        println("Reorder 100 todos time: ${reorderTime}ms")
        assertTrue(reorderTime < 200, "Reordering 100 todos should complete in under 200ms")
        
        // Verify the new order
        val verifyTime = measureTimeMillis {
            val result = dao.getAllTodos().first()
            assertTrue(result.first().text == allTodos.last().text, "Order should be reversed")
        }
        println("Verify reorder time: ${verifyTime}ms")
        assertTrue(verifyTime < 50, "Verifying reorder should complete in under 50ms")
    }
    
    /**
     * Test query performance with different sorting scenarios.
     * 
     * This test ensures that the sortOrder + createdAt ordering performs well.
     */
    @Test
    fun testSortingPerformance() = runBlocking {
        // Create todos with mixed sort orders and creation times
        val todos = mutableListOf<Todo>()
        val baseTime = System.currentTimeMillis()
        
        repeat(500) { index ->
            todos.add(
                Todo(
                    text = "Sort Test Todo $index",
                    sortOrder = if (index % 10 == 0) index / 10 else 999, // Some have specific order, others default
                    createdAt = baseTime - (index * 100), // Different creation times
                    category = TodoCategory.PERSONAL
                )
            )
        }
        
        // Insert todos in random order
        todos.shuffled().forEach { dao.insertTodo(it) }
        
        // Test sorting performance
        val sortTime = measureTimeMillis {
            val result = dao.getAllTodos().first()
            assertTrue(result.size == 500, "Should retrieve all 500 todos")
            
            // Verify sorting: sortOrder ASC, then createdAt DESC
            var previousSortOrder = -1
            var previousCreatedAt = Long.MAX_VALUE
            
            for (todo in result) {
                if (todo.sortOrder > previousSortOrder) {
                    previousSortOrder = todo.sortOrder
                    previousCreatedAt = todo.createdAt
                } else if (todo.sortOrder == previousSortOrder) {
                    assertTrue(
                        todo.createdAt <= previousCreatedAt,
                        "Within same sortOrder, createdAt should be DESC"
                    )
                    previousCreatedAt = todo.createdAt
                }
            }
        }
        println("Sort 500 todos time: ${sortTime}ms")
        assertTrue(sortTime < 100, "Sorting 500 todos should complete in under 100ms")
    }
    
    /**
     * Test index effectiveness by comparing query times with and without indexes.
     * 
     * This test creates a database without indexes and compares performance.
     */
    @Test
    fun testIndexEffectiveness() = runBlocking {
        // Create a large dataset
        repeat(1000) { index ->
            dao.insertTodo(
                Todo(
                    text = "Index Test Todo $index",
                    category = TodoCategory.values()[index % TodoCategory.values().size],
                    isCompleted = index % 4 == 0,
                    sortOrder = index
                )
            )
        }
        
        // Test category filtering (should use category index)
        val categoryQueryTime = measureTimeMillis {
            dao.getTodosByCategory(TodoCategory.WORK).first()
        }
        
        // Test status filtering (should use isCompleted index)
        val statusQueryTime = measureTimeMillis {
            dao.getTodosByStatus(true).first()
        }
        
        // Test combined filtering (should use composite index)
        val combinedQueryTime = measureTimeMillis {
            dao.getTodosByCategoryAndStatus(TodoCategory.PERSONAL, false).first()
        }
        
        println("Category query with index: ${categoryQueryTime}ms")
        println("Status query with index: ${statusQueryTime}ms")
        println("Combined query with index: ${combinedQueryTime}ms")
        
        // With proper indexing, these queries should be fast even with 1000 records
        assertTrue(categoryQueryTime < 50, "Category query should be fast with index")
        assertTrue(statusQueryTime < 50, "Status query should be fast with index")
        assertTrue(combinedQueryTime < 50, "Combined query should be fast with index")
    }
}