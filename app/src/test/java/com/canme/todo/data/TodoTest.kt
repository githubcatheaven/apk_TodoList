package com.canme.todo.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for Todo entity and TodoCategory enum.
 * Tests the Todo data class behavior and TodoCategory functionality.
 */
class TodoTest {

    @Test
    fun `todo has default values when created with minimal parameters`() {
        // Given & When
        val todo = Todo(text = "Test todo")

        // Then
        assertEquals(0L, todo.id)
        assertEquals("Test todo", todo.text)
        assertFalse(todo.isCompleted)
        assertEquals(TodoCategory.PERSONAL, todo.category)
        assertEquals(0, todo.sortOrder)
        assertTrue(todo.createdAt > 0)
    }

    @Test
    fun `todo preserves all properties when created with full parameters`() {
        // Given
        val id = 123L
        val text = "Custom todo"
        val isCompleted = true
        val category = TodoCategory.WORK
        val sortOrder = 5
        val createdAt = 1234567890L

        // When
        val todo = Todo(
            id = id, 
            text = text, 
            isCompleted = isCompleted, 
            category = category,
            sortOrder = sortOrder,
            createdAt = createdAt
        )

        // Then
        assertEquals(id, todo.id)
        assertEquals(text, todo.text)
        assertEquals(isCompleted, todo.isCompleted)
        assertEquals(category, todo.category)
        assertEquals(sortOrder, todo.sortOrder)
        assertEquals(createdAt, todo.createdAt)
    }

    @Test
    fun `todo with different categories are not equal`() {
        // Given
        val todo1 = Todo(id = 1, text = "Test", category = TodoCategory.PERSONAL, createdAt = 123L)
        val todo2 = Todo(id = 1, text = "Test", category = TodoCategory.WORK, createdAt = 123L)

        // When & Then
        assertNotEquals(todo1, todo2)
        assertNotEquals(todo1.hashCode(), todo2.hashCode())
    }

    @Test
    fun `two todos with same properties including category are equal`() {
        // Given
        val todo1 = Todo(
            id = 1, 
            text = "Test", 
            isCompleted = false, 
            category = TodoCategory.SHOPPING,
            sortOrder = 2,
            createdAt = 123L
        )
        val todo2 = Todo(
            id = 1, 
            text = "Test", 
            isCompleted = false, 
            category = TodoCategory.SHOPPING,
            sortOrder = 2,
            createdAt = 123L
        )

        // When & Then
        assertEquals(todo1, todo2)
        assertEquals(todo1.hashCode(), todo2.hashCode())
    }

    @Test
    fun `copy creates new instance with modified category`() {
        // Given
        val originalTodo = Todo(
            id = 1, 
            text = "Original", 
            isCompleted = false, 
            category = TodoCategory.PERSONAL,
            createdAt = 123L
        )

        // When
        val copiedTodo = originalTodo.copy(category = TodoCategory.HEALTH, isCompleted = true)

        // Then
        assertEquals(originalTodo.id, copiedTodo.id)
        assertEquals(originalTodo.text, copiedTodo.text)
        assertTrue(copiedTodo.isCompleted)
        assertEquals(TodoCategory.HEALTH, copiedTodo.category)
        assertEquals(originalTodo.createdAt, copiedTodo.createdAt)
    }

    @Test
    fun `copy creates new instance with modified sortOrder`() {
        // Given
        val originalTodo = Todo(id = 1, text = "Original", sortOrder = 0, createdAt = 123L)

        // When
        val copiedTodo = originalTodo.copy(sortOrder = 10)

        // Then
        assertEquals(originalTodo.id, copiedTodo.id)
        assertEquals(originalTodo.text, copiedTodo.text)
        assertEquals(originalTodo.isCompleted, copiedTodo.isCompleted)
        assertEquals(originalTodo.category, copiedTodo.category)
        assertEquals(10, copiedTodo.sortOrder)
        assertEquals(originalTodo.createdAt, copiedTodo.createdAt)
    }

    // TodoCategory enum tests
    @Test
    fun `TodoCategory PERSONAL has correct properties`() {
        // Given
        val category = TodoCategory.PERSONAL

        // When & Then
        assertEquals("Personal", category.displayName)
        assertEquals(Color(0xFF2196F3), category.color)
        assertEquals(Icons.Filled.AccountCircle, category.icon)
    }

    @Test
    fun `TodoCategory WORK has correct properties`() {
        // Given
        val category = TodoCategory.WORK

        // When & Then
        assertEquals("Work", category.displayName)
        assertEquals(Color(0xFF4CAF50), category.color)
        assertEquals(Icons.Filled.Home, category.icon)
    }

    @Test
    fun `TodoCategory SHOPPING has correct properties`() {
        // Given
        val category = TodoCategory.SHOPPING

        // When & Then
        assertEquals("Shopping", category.displayName)
        assertEquals(Color(0xFFFF9800), category.color)
        assertEquals(Icons.Filled.ShoppingCart, category.icon)
    }

    @Test
    fun `TodoCategory HEALTH has correct properties`() {
        // Given
        val category = TodoCategory.HEALTH

        // When & Then
        assertEquals("Health", category.displayName)
        assertEquals(Color(0xFFE91E63), category.color)
        assertEquals(Icons.Filled.Favorite, category.icon)
    }

    @Test
    fun `TodoCategory OTHER has correct properties`() {
        // Given
        val category = TodoCategory.OTHER

        // When & Then
        assertEquals("Other", category.displayName)
        assertEquals(Color(0xFF9C27B0), category.color)
        assertEquals(Icons.Filled.MoreVert, category.icon)
    }

    @Test
    fun `TodoCategory enum has all expected values`() {
        // Given
        val expectedCategories = setOf(
            TodoCategory.PERSONAL,
            TodoCategory.WORK,
            TodoCategory.SHOPPING,
            TodoCategory.HEALTH,
            TodoCategory.OTHER
        )

        // When
        val actualCategories = TodoCategory.values().toSet()

        // Then
        assertEquals(expectedCategories, actualCategories)
        assertEquals(5, TodoCategory.values().size)
    }

    @Test
    fun `TodoCategory values have unique display names`() {
        // Given
        val categories = TodoCategory.values()

        // When
        val displayNames = categories.map { it.displayName }
        val uniqueDisplayNames = displayNames.toSet()

        // Then
        assertEquals(displayNames.size, uniqueDisplayNames.size)
    }

    @Test
    fun `TodoCategory values have unique colors`() {
        // Given
        val categories = TodoCategory.values()

        // When
        val colors = categories.map { it.color }
        val uniqueColors = colors.toSet()

        // Then
        assertEquals(colors.size, uniqueColors.size)
    }

    @Test
    fun `TodoCategory can be used in when expressions`() {
        // Given
        val categories = TodoCategory.values()

        // When & Then
        categories.forEach { category ->
            val result = when (category) {
                TodoCategory.PERSONAL -> "personal"
                TodoCategory.WORK -> "work"
                TodoCategory.SHOPPING -> "shopping"
                TodoCategory.HEALTH -> "health"
                TodoCategory.OTHER -> "other"
            }
            assertTrue(result.isNotEmpty())
        }
    }
}