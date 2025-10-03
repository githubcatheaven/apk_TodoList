package com.canme.todo.integration

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.data.TodoDao
import com.canme.todo.data.TodoDatabase
import com.canme.todo.repository.TodoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for swipe gesture persistence functionality.
 * Tests that swipe actions (complete/delete) are correctly persisted to the database
 * and that state changes are maintained across app sessions.
 */
@RunWith(AndroidJUnit4::class)
class SwipeGesturePersistenceTest {

    private lateinit var database: TodoDatabase
    private lateinit var todoDao: TodoDao
    private lateinit var repository: TodoRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, TodoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        todoDao = database.todoDao()
        repository = TodoRepository(todoDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    // Swipe-to-complete persistence tests
    @Test
    fun swipeComplete_persistsCompletionState() = runTest {
        // Given - Insert incomplete todo
        val incompleteTodo = Todo(
            text = "Task to complete",
            isCompleted = false,
            category = TodoCategory.PERSONAL,
            sortOrder = 0
        )
        val todoId = todoDao.insertTodo(incompleteTodo)
        
        // Verify initial state
        val initialTodo = todoDao.getAllTodos().first().first()
        assertFalse(initialTodo.isCompleted)

        // When - Simulate swipe right (toggle completion)
        repository.toggleTodoCompletion(initialTodo)

        // Then - Completion state should be persisted
        val updatedTodo = todoDao.getAllTodos().first().first()
        assertTrue(updatedTodo.isCompleted)
        assertEquals(todoId, updatedTodo.id)
        assertEquals("Task to complete", updatedTodo.text)
        assertEquals(TodoCategory.PERSONAL, updatedTodo.category)
    }

    @Test
    fun swipeComplete_persistsIncompletionState() = runTest {
        // Given - Insert completed todo
        val completedTodo = Todo(
            text = "Completed task",
            isCompleted = true,
            category = TodoCategory.WORK,
            sortOrder = 0
        )
        val todoId = todoDao.insertTodo(completedTodo)
        
        // Verify initial state
        val initialTodo = todoDao.getAllTodos().first().first()
        assertTrue(initialTodo.isCompleted)

        // When - Simulate swipe right (toggle completion)
        repository.toggleTodoCompletion(initialTodo)

        // Then - Incompletion state should be persisted
        val updatedTodo = todoDao.getAllTodos().first().first()
        assertFalse(updatedTodo.isCompleted)
        assertEquals(todoId, updatedTodo.id)
        assertEquals("Completed task", updatedTodo.text)
        assertEquals(TodoCategory.WORK, updatedTodo.category)
    }

    @Test
    fun swipeComplete_maintainsOtherProperties() = runTest {
        // Given - Todo with specific properties
        val originalTodo = Todo(
            text = "Important task",
            isCompleted = false,
            category = TodoCategory.HEALTH,
            sortOrder = 5,
            createdAt = 1234567890L
        )
        todoDao.insertTodo(originalTodo)
        
        val insertedTodo = todoDao.getAllTodos().first().first()

        // When - Toggle completion
        repository.toggleTodoCompletion(insertedTodo)

        // Then - All other properties should be maintained
        val updatedTodo = todoDao.getAllTodos().first().first()
        assertEquals(insertedTodo.id, updatedTodo.id)
        assertEquals("Important task", updatedTodo.text)
        assertTrue(updatedTodo.isCompleted) // Only this should change
        assertEquals(TodoCategory.HEALTH, updatedTodo.category)
        assertEquals(5, updatedTodo.sortOrder)
        assertEquals(1234567890L, updatedTodo.createdAt)
    }

    @Test
    fun swipeComplete_worksWithMultipleTodos() = runTest {
        // Given - Multiple todos with different states
        val todos = listOf(
            Todo(text = "Todo 1", isCompleted = false, category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "Todo 2", isCompleted = true, category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "Todo 3", isCompleted = false, category = TodoCategory.SHOPPING, sortOrder = 2)
        )
        
        todos.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()

        // When - Toggle completion on first and third todos
        repository.toggleTodoCompletion(insertedTodos[0]) // false -> true
        repository.toggleTodoCompletion(insertedTodos[2]) // false -> true

        // Then - Only toggled todos should change
        val updatedTodos = todoDao.getAllTodos().first()
        assertTrue(updatedTodos[0].isCompleted) // Changed
        assertTrue(updatedTodos[1].isCompleted) // Unchanged
        assertTrue(updatedTodos[2].isCompleted) // Changed
    }

    // Swipe-to-delete persistence tests
    @Test
    fun swipeDelete_removesTodoPermanently() = runTest {
        // Given - Insert todo
        val todoToDelete = Todo(
            text = "Task to delete",
            isCompleted = false,
            category = TodoCategory.OTHER,
            sortOrder = 0
        )
        todoDao.insertTodo(todoToDelete)
        
        // Verify todo exists
        val initialTodos = todoDao.getAllTodos().first()
        assertEquals(1, initialTodos.size)
        assertEquals("Task to delete", initialTodos[0].text)

        // When - Simulate swipe left (delete)
        repository.deleteTodo(initialTodos[0])

        // Then - Todo should be permanently removed
        val remainingTodos = todoDao.getAllTodos().first()
        assertTrue(remainingTodos.isEmpty())
    }

    @Test
    fun swipeDelete_removesOnlySpecificTodo() = runTest {
        // Given - Multiple todos
        val todos = listOf(
            Todo(text = "Keep this", isCompleted = false, category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "Delete this", isCompleted = true, category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "Keep this too", isCompleted = false, category = TodoCategory.SHOPPING, sortOrder = 2)
        )
        
        todos.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()
        assertEquals(3, insertedTodos.size)

        // When - Delete middle todo
        val todoToDelete = insertedTodos.find { it.text == "Delete this" }!!
        repository.deleteTodo(todoToDelete)

        // Then - Only specified todo should be removed
        val remainingTodos = todoDao.getAllTodos().first()
        assertEquals(2, remainingTodos.size)
        assertTrue(remainingTodos.none { it.text == "Delete this" })
        assertTrue(remainingTodos.any { it.text == "Keep this" })
        assertTrue(remainingTodos.any { it.text == "Keep this too" })
    }

    @Test
    fun swipeDelete_worksWithCompletedTodos() = runTest {
        // Given - Completed todo
        val completedTodo = Todo(
            text = "Completed task to delete",
            isCompleted = true,
            category = TodoCategory.HEALTH,
            sortOrder = 0
        )
        todoDao.insertTodo(completedTodo)
        
        val insertedTodo = todoDao.getAllTodos().first().first()
        assertTrue(insertedTodo.isCompleted)

        // When - Delete completed todo
        repository.deleteTodo(insertedTodo)

        // Then - Should be deleted successfully
        val remainingTodos = todoDao.getAllTodos().first()
        assertTrue(remainingTodos.isEmpty())
    }

    @Test
    fun swipeDelete_worksWithDifferentCategories() = runTest {
        // Given - Todos from different categories
        val todos = TodoCategory.values().mapIndexed { index, category ->
            Todo(
                text = "Task in ${category.displayName}",
                isCompleted = index % 2 == 0,
                category = category,
                sortOrder = index
            )
        }
        
        todos.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()
        assertEquals(TodoCategory.values().size, insertedTodos.size)

        // When - Delete todos from specific categories
        val workTodo = insertedTodos.find { it.category == TodoCategory.WORK }!!
        val healthTodo = insertedTodos.find { it.category == TodoCategory.HEALTH }!!
        
        repository.deleteTodo(workTodo)
        repository.deleteTodo(healthTodo)

        // Then - Only specified todos should be deleted
        val remainingTodos = todoDao.getAllTodos().first()
        assertEquals(TodoCategory.values().size - 2, remainingTodos.size)
        assertTrue(remainingTodos.none { it.category == TodoCategory.WORK })
        assertTrue(remainingTodos.none { it.category == TodoCategory.HEALTH })
        assertTrue(remainingTodos.any { it.category == TodoCategory.PERSONAL })
        assertTrue(remainingTodos.any { it.category == TodoCategory.SHOPPING })
        assertTrue(remainingTodos.any { it.category == TodoCategory.OTHER })
    }

    // Combined swipe actions persistence tests
    @Test
    fun combinedSwipeActions_persistCorrectly() = runTest {
        // Given - Multiple todos
        val todos = listOf(
            Todo(text = "Complete me", isCompleted = false, category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "Delete me", isCompleted = false, category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "Incomplete me", isCompleted = true, category = TodoCategory.SHOPPING, sortOrder = 2),
            Todo(text = "Keep me unchanged", isCompleted = false, category = TodoCategory.HEALTH, sortOrder = 3)
        )
        
        todos.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()

        // When - Perform various swipe actions
        val completeMe = insertedTodos.find { it.text == "Complete me" }!!
        val deleteMe = insertedTodos.find { it.text == "Delete me" }!!
        val incompleteMe = insertedTodos.find { it.text == "Incomplete me" }!!
        
        repository.toggleTodoCompletion(completeMe) // false -> true
        repository.deleteTodo(deleteMe) // remove
        repository.toggleTodoCompletion(incompleteMe) // true -> false

        // Then - All actions should be persisted correctly
        val finalTodos = todoDao.getAllTodos().first()
        assertEquals(3, finalTodos.size) // One deleted
        
        val completedTodo = finalTodos.find { it.text == "Complete me" }!!
        assertTrue(completedTodo.isCompleted)
        
        val incompletedTodo = finalTodos.find { it.text == "Incomplete me" }!!
        assertFalse(incompletedTodo.isCompleted)
        
        val unchangedTodo = finalTodos.find { it.text == "Keep me unchanged" }!!
        assertFalse(unchangedTodo.isCompleted)
        
        // Deleted todo should not exist
        assertTrue(finalTodos.none { it.text == "Delete me" })
    }

    @Test
    fun swipeActions_maintainSortOrder() = runTest {
        // Given - Todos with specific sort order
        val todos = listOf(
            Todo(text = "First", isCompleted = false, category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "Second", isCompleted = false, category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "Third", isCompleted = false, category = TodoCategory.SHOPPING, sortOrder = 2),
            Todo(text = "Fourth", isCompleted = false, category = TodoCategory.HEALTH, sortOrder = 3)
        )
        
        todos.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()

        // When - Complete some todos and delete one
        repository.toggleTodoCompletion(insertedTodos[0]) // Complete first
        repository.deleteTodo(insertedTodos[1]) // Delete second
        repository.toggleTodoCompletion(insertedTodos[2]) // Complete third

        // Then - Sort order should be maintained for remaining todos
        val finalTodos = todoDao.getAllTodos().first()
        assertEquals(3, finalTodos.size)
        
        // Verify order is maintained (by sortOrder, then createdAt)
        assertEquals("First", finalTodos[0].text)
        assertEquals(0, finalTodos[0].sortOrder)
        assertTrue(finalTodos[0].isCompleted)
        
        assertEquals("Third", finalTodos[1].text)
        assertEquals(2, finalTodos[1].sortOrder)
        assertTrue(finalTodos[1].isCompleted)
        
        assertEquals("Fourth", finalTodos[2].text)
        assertEquals(3, finalTodos[2].sortOrder)
        assertFalse(finalTodos[2].isCompleted)
    }

    @Test
    fun swipeActions_workWithCategoryFiltering() = runTest {
        // Given - Mixed category todos
        val todos = listOf(
            Todo(text = "Personal 1", isCompleted = false, category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "Work 1", isCompleted = false, category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "Personal 2", isCompleted = true, category = TodoCategory.PERSONAL, sortOrder = 2),
            Todo(text = "Work 2", isCompleted = false, category = TodoCategory.WORK, sortOrder = 3)
        )
        
        todos.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()

        // When - Perform actions on specific categories
        val personalTodos = insertedTodos.filter { it.category == TodoCategory.PERSONAL }
        repository.toggleTodoCompletion(personalTodos[0]) // Complete Personal 1
        repository.deleteTodo(personalTodos[1]) // Delete Personal 2

        // Then - Actions should affect correct todos and category filtering should still work
        val allTodos = todoDao.getAllTodos().first()
        assertEquals(3, allTodos.size) // One deleted
        
        val remainingPersonalTodos = todoDao.getTodosByCategory(TodoCategory.PERSONAL).first()
        assertEquals(1, remainingPersonalTodos.size)
        assertEquals("Personal 1", remainingPersonalTodos[0].text)
        assertTrue(remainingPersonalTodos[0].isCompleted)
        
        val workTodos = todoDao.getTodosByCategory(TodoCategory.WORK).first()
        assertEquals(2, workTodos.size) // Unchanged
    }

    @Test
    fun swipeActions_performanceWithLargeDataset() = runTest {
        // Given - Large dataset
        val largeTodoList = (1..1000).map { index ->
            Todo(
                text = "Todo $index",
                isCompleted = index % 3 == 0,
                category = TodoCategory.values()[index % TodoCategory.values().size],
                sortOrder = index - 1
            )
        }
        
        largeTodoList.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()
        assertEquals(1000, insertedTodos.size)

        // When - Perform multiple swipe actions
        val startTime = System.currentTimeMillis()
        
        // Toggle completion on first 100 todos
        insertedTodos.take(100).forEach { todo ->
            repository.toggleTodoCompletion(todo)
        }
        
        // Delete every 10th todo from the next 100
        insertedTodos.drop(100).take(100).filterIndexed { index, _ -> index % 10 == 0 }.forEach { todo ->
            repository.deleteTodo(todo)
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Then - Should complete in reasonable time (less than 10 seconds)
        assertTrue(duration < 10000, "Swipe actions on large dataset took $duration ms, expected < 10000 ms")
        
        // Verify actions were applied correctly
        val finalTodos = todoDao.getAllTodos().first()
        assertEquals(990, finalTodos.size) // 10 deleted
        
        // Verify first 100 todos have toggled completion state
        val first100 = finalTodos.take(100)
        first100.forEach { todo ->
            val originalTodo = largeTodoList.find { it.text == todo.text }!!
            assertEquals(!originalTodo.isCompleted, todo.isCompleted)
        }
    }

    @Test
    fun swipeActions_transactionIntegrity() = runTest {
        // Given - Initial todos
        val todos = listOf(
            Todo(text = "Todo 1", isCompleted = false, category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "Todo 2", isCompleted = false, category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "Todo 3", isCompleted = false, category = TodoCategory.SHOPPING, sortOrder = 2)
        )
        
        todos.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()

        // When - Perform swipe actions (each should be atomic)
        repository.toggleTodoCompletion(insertedTodos[0])
        repository.deleteTodo(insertedTodos[1])
        repository.toggleTodoCompletion(insertedTodos[2])

        // Then - All operations should succeed completely or fail completely
        val finalTodos = todoDao.getAllTodos().first()
        assertEquals(2, finalTodos.size)
        
        // Verify specific changes
        val todo1 = finalTodos.find { it.text == "Todo 1" }!!
        assertTrue(todo1.isCompleted)
        
        val todo3 = finalTodos.find { it.text == "Todo 3" }!!
        assertTrue(todo3.isCompleted)
        
        // Todo 2 should be deleted
        assertTrue(finalTodos.none { it.text == "Todo 2" })
    }
}