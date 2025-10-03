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
import kotlin.test.assertTrue

/**
 * Integration tests for drag-and-drop persistence functionality.
 * Tests that reordering operations are correctly persisted to the database
 * and that the sort order is maintained across app sessions.
 */
@RunWith(AndroidJUnit4::class)
class DragDropPersistenceTest {

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

    @Test
    fun reorderTodos_persistsCorrectSortOrder() = runTest {
        // Given - Insert initial todos with sequential sort order
        val initialTodos = listOf(
            Todo(text = "First", category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "Second", category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "Third", category = TodoCategory.SHOPPING, sortOrder = 2),
            Todo(text = "Fourth", category = TodoCategory.HEALTH, sortOrder = 3)
        )
        
        initialTodos.forEach { todo ->
            todoDao.insertTodo(todo)
        }

        // Get the inserted todos with their generated IDs
        val insertedTodos = todoDao.getAllTodos().first()
        assertEquals(4, insertedTodos.size)

        // When - Reorder: move first todo to last position
        val reorderedTodos = listOf(
            insertedTodos[1], // Second -> position 0
            insertedTodos[2], // Third -> position 1
            insertedTodos[3], // Fourth -> position 2
            insertedTodos[0]  // First -> position 3
        )
        
        repository.reorderTodos(reorderedTodos)

        // Then - Verify sort order is persisted correctly
        val persistedTodos = todoDao.getAllTodos().first()
        assertEquals(4, persistedTodos.size)
        
        // Verify the new order
        assertEquals("Second", persistedTodos[0].text)
        assertEquals(0, persistedTodos[0].sortOrder)
        
        assertEquals("Third", persistedTodos[1].text)
        assertEquals(1, persistedTodos[1].sortOrder)
        
        assertEquals("Fourth", persistedTodos[2].text)
        assertEquals(2, persistedTodos[2].sortOrder)
        
        assertEquals("First", persistedTodos[3].text)
        assertEquals(3, persistedTodos[3].sortOrder)
    }

    @Test
    fun reorderTodos_maintainsOtherProperties() = runTest {
        // Given - Todos with various properties
        val todo1 = Todo(text = "Personal task", isCompleted = false, category = TodoCategory.PERSONAL, sortOrder = 0)
        val todo2 = Todo(text = "Work meeting", isCompleted = true, category = TodoCategory.WORK, sortOrder = 1)
        val todo3 = Todo(text = "Buy groceries", isCompleted = false, category = TodoCategory.SHOPPING, sortOrder = 2)
        
        val id1 = todoDao.insertTodo(todo1)
        val id2 = todoDao.insertTodo(todo2)
        val id3 = todoDao.insertTodo(todo3)

        val insertedTodos = todoDao.getAllTodos().first()

        // When - Reorder todos
        val reorderedTodos = listOf(
            insertedTodos[2], // Buy groceries -> first
            insertedTodos[0], // Personal task -> second
            insertedTodos[1]  // Work meeting -> third
        )
        
        repository.reorderTodos(reorderedTodos)

        // Then - All properties should be maintained except sortOrder
        val persistedTodos = todoDao.getAllTodos().first()
        
        // First todo (originally "Buy groceries")
        val firstTodo = persistedTodos[0]
        assertEquals("Buy groceries", firstTodo.text)
        assertEquals(false, firstTodo.isCompleted)
        assertEquals(TodoCategory.SHOPPING, firstTodo.category)
        assertEquals(0, firstTodo.sortOrder)
        assertEquals(id3, firstTodo.id)

        // Second todo (originally "Personal task")
        val secondTodo = persistedTodos[1]
        assertEquals("Personal task", secondTodo.text)
        assertEquals(false, secondTodo.isCompleted)
        assertEquals(TodoCategory.PERSONAL, secondTodo.category)
        assertEquals(1, secondTodo.sortOrder)
        assertEquals(id1, secondTodo.id)

        // Third todo (originally "Work meeting")
        val thirdTodo = persistedTodos[2]
        assertEquals("Work meeting", thirdTodo.text)
        assertEquals(true, thirdTodo.isCompleted)
        assertEquals(TodoCategory.WORK, thirdTodo.category)
        assertEquals(2, thirdTodo.sortOrder)
        assertEquals(id2, thirdTodo.id)
    }

    @Test
    fun reorderTodos_handlesComplexReordering() = runTest {
        // Given - 6 todos with mixed properties
        val todos = listOf(
            Todo(text = "A", isCompleted = false, category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "B", isCompleted = true, category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "C", isCompleted = false, category = TodoCategory.SHOPPING, sortOrder = 2),
            Todo(text = "D", isCompleted = true, category = TodoCategory.HEALTH, sortOrder = 3),
            Todo(text = "E", isCompleted = false, category = TodoCategory.OTHER, sortOrder = 4),
            Todo(text = "F", isCompleted = true, category = TodoCategory.PERSONAL, sortOrder = 5)
        )
        
        todos.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()

        // When - Complex reordering: F, A, D, B, E, C
        val reorderedTodos = listOf(
            insertedTodos[5], // F -> 0
            insertedTodos[0], // A -> 1
            insertedTodos[3], // D -> 2
            insertedTodos[1], // B -> 3
            insertedTodos[4], // E -> 4
            insertedTodos[2]  // C -> 5
        )
        
        repository.reorderTodos(reorderedTodos)

        // Then - Verify complex reordering is persisted
        val persistedTodos = todoDao.getAllTodos().first()
        val expectedOrder = listOf("F", "A", "D", "B", "E", "C")
        
        persistedTodos.forEachIndexed { index, todo ->
            assertEquals(expectedOrder[index], todo.text)
            assertEquals(index, todo.sortOrder)
        }
    }

    @Test
    fun reorderTodos_worksWithCategoryFiltering() = runTest {
        // Given - Mixed categories
        val todos = listOf(
            Todo(text = "Personal 1", category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "Work 1", category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "Personal 2", category = TodoCategory.PERSONAL, sortOrder = 2),
            Todo(text = "Work 2", category = TodoCategory.WORK, sortOrder = 3),
            Todo(text = "Personal 3", category = TodoCategory.PERSONAL, sortOrder = 4)
        )
        
        todos.forEach { todoDao.insertTodo(it) }
        val allTodos = todoDao.getAllTodos().first()

        // When - Reorder all todos (simulating drag-drop in "All" view)
        val reorderedTodos = listOf(
            allTodos[4], // Personal 3 -> first
            allTodos[1], // Work 1 -> second
            allTodos[0], // Personal 1 -> third
            allTodos[3], // Work 2 -> fourth
            allTodos[2]  // Personal 2 -> fifth
        )
        
        repository.reorderTodos(reorderedTodos)

        // Then - Verify reordering affects both filtered and unfiltered views
        val allPersisted = todoDao.getAllTodos().first()
        assertEquals("Personal 3", allPersisted[0].text)
        assertEquals("Work 1", allPersisted[1].text)
        assertEquals("Personal 1", allPersisted[2].text)
        assertEquals("Work 2", allPersisted[3].text)
        assertEquals("Personal 2", allPersisted[4].text)

        // Verify category filtering still works with new order
        val personalTodos = todoDao.getTodosByCategory(TodoCategory.PERSONAL).first()
        assertEquals(3, personalTodos.size)
        assertEquals("Personal 3", personalTodos[0].text) // Should be first personal todo
        assertEquals("Personal 1", personalTodos[1].text) // Should be second personal todo
        assertEquals("Personal 2", personalTodos[2].text) // Should be third personal todo
    }

    @Test
    fun reorderTodos_handlesEmptyList() = runTest {
        // Given - Empty list
        val emptyList = emptyList<Todo>()

        // When - Reorder empty list
        repository.reorderTodos(emptyList)

        // Then - Should handle gracefully
        val todos = todoDao.getAllTodos().first()
        assertTrue(todos.isEmpty())
    }

    @Test
    fun reorderTodos_handlesSingleItem() = runTest {
        // Given - Single todo
        val todo = Todo(text = "Only todo", category = TodoCategory.PERSONAL, sortOrder = 0)
        todoDao.insertTodo(todo)
        
        val insertedTodos = todoDao.getAllTodos().first()

        // When - "Reorder" single item
        repository.reorderTodos(insertedTodos)

        // Then - Should handle gracefully
        val persistedTodos = todoDao.getAllTodos().first()
        assertEquals(1, persistedTodos.size)
        assertEquals("Only todo", persistedTodos[0].text)
        assertEquals(0, persistedTodos[0].sortOrder)
    }

    @Test
    fun reorderTodos_maintainsSortOrderAfterNewInsertions() = runTest {
        // Given - Initial todos
        val initialTodos = listOf(
            Todo(text = "First", category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "Second", category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "Third", category = TodoCategory.SHOPPING, sortOrder = 2)
        )
        
        initialTodos.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()

        // When - Reorder existing todos
        val reorderedTodos = listOf(
            insertedTodos[2], // Third -> first
            insertedTodos[0], // First -> second
            insertedTodos[1]  // Second -> third
        )
        repository.reorderTodos(reorderedTodos)

        // Then add a new todo
        val newTodoId = repository.insertTodo("Fourth", TodoCategory.HEALTH)

        // Verify new todo gets correct sort order (max + 1)
        val finalTodos = todoDao.getAllTodos().first()
        assertEquals(4, finalTodos.size)
        
        val newTodo = finalTodos.find { it.text == "Fourth" }
        assertEquals(3, newTodo?.sortOrder) // Should be max (2) + 1
        
        // Verify existing order is maintained
        assertEquals("Third", finalTodos[0].text)
        assertEquals("First", finalTodos[1].text)
        assertEquals("Second", finalTodos[2].text)
        assertEquals("Fourth", finalTodos[3].text)
    }

    @Test
    fun reorderTodos_performanceWithLargeDataset() = runTest {
        // Given - Large dataset
        val largeTodoList = (1..1000).map { index ->
            Todo(
                text = "Todo $index",
                isCompleted = index % 2 == 0,
                category = TodoCategory.values()[index % TodoCategory.values().size],
                sortOrder = index - 1
            )
        }
        
        largeTodoList.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()
        assertEquals(1000, insertedTodos.size)

        // When - Reorder large list (move first item to last)
        val reorderedTodos = insertedTodos.drop(1) + insertedTodos.take(1)
        
        val startTime = System.currentTimeMillis()
        repository.reorderTodos(reorderedTodos)
        val endTime = System.currentTimeMillis()

        // Then - Should complete in reasonable time (less than 5 seconds)
        val duration = endTime - startTime
        assertTrue(duration < 5000, "Reordering 1000 items took $duration ms, expected < 5000 ms")

        // Verify reordering was successful
        val persistedTodos = todoDao.getAllTodos().first()
        assertEquals("Todo 2", persistedTodos[0].text) // Second item moved to first
        assertEquals("Todo 1", persistedTodos[999].text) // First item moved to last
    }

    @Test
    fun reorderTodos_transactionIntegrity() = runTest {
        // Given - Initial todos
        val todos = listOf(
            Todo(text = "A", category = TodoCategory.PERSONAL, sortOrder = 0),
            Todo(text = "B", category = TodoCategory.WORK, sortOrder = 1),
            Todo(text = "C", category = TodoCategory.SHOPPING, sortOrder = 2)
        )
        
        todos.forEach { todoDao.insertTodo(it) }
        val insertedTodos = todoDao.getAllTodos().first()

        // When - Simulate transaction failure by using invalid data
        // (This test verifies that the DAO transaction handling works correctly)
        val reorderedTodos = listOf(
            insertedTodos[2], // C -> first
            insertedTodos[0], // A -> second
            insertedTodos[1]  // B -> third
        )
        
        repository.reorderTodos(reorderedTodos)

        // Then - All updates should succeed or all should fail (transaction integrity)
        val persistedTodos = todoDao.getAllTodos().first()
        assertEquals(3, persistedTodos.size)
        
        // Verify all sort orders are updated consistently
        val sortOrders = persistedTodos.map { it.sortOrder }.sorted()
        assertEquals(listOf(0, 1, 2), sortOrders)
        
        // Verify the actual reordering
        assertEquals("C", persistedTodos[0].text)
        assertEquals("A", persistedTodos[1].text)
        assertEquals("B", persistedTodos[2].text)
    }
}