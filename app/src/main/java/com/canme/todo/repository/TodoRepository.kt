package com.canme.todo.repository

import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.data.TodoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for Todo data operations.
 * 
 * This repository provides a clean API for the UI layer to interact with
 * the todo data. It abstracts the data source (Room database) and provides
 * a single source of truth for todo data.
 * 
 * @property todoDao The DAO for database operations
 */
@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao
) {
    
    /**
     * Get all todos as a Flow for reactive UI updates.
     * 
     * @return Flow of list of all todos ordered by sort order then creation date
     */
    fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos()
    
    /**
     * Get todos filtered by category.
     * 
     * @param category The category to filter by
     * @return Flow of filtered todos ordered by sort order then creation date
     */
    fun getTodosByCategory(category: TodoCategory): Flow<List<Todo>> = 
        todoDao.getTodosByCategory(category)
    
    /**
     * Insert a new todo into the database.
     * 
     * @param text The text content of the todo
     * @param category The category for the todo (defaults to PERSONAL)
     * @return The ID of the inserted todo
     */
    suspend fun insertTodo(text: String, category: TodoCategory = TodoCategory.PERSONAL): Long {
        // Get the current max sort order and increment it
        val maxOrder = todoDao.getAllTodos().first().maxOfOrNull { it.sortOrder } ?: -1
        val todo = Todo(
            text = text.trim(),
            category = category,
            sortOrder = maxOrder + 1
        )
        return todoDao.insertTodo(todo)
    }
    
    /**
     * Toggle the completion status of a todo.
     * 
     * @param todo The todo to toggle
     */
    suspend fun toggleTodoCompletion(todo: Todo) {
        val updatedTodo = todo.copy(isCompleted = !todo.isCompleted)
        todoDao.updateTodo(updatedTodo)
    }
    
    /**
     * Update the text of a todo.
     * 
     * @param todo The todo to update
     * @param newText The new text content
     */
    suspend fun updateTodoText(todo: Todo, newText: String) {
        val updatedTodo = todo.copy(text = newText.trim())
        todoDao.updateTodo(updatedTodo)
    }
    
    /**
     * Update the category of a todo.
     * 
     * @param todo The todo to update
     * @param newCategory The new category
     */
    suspend fun updateTodoCategory(todo: Todo, newCategory: TodoCategory) {
        val updatedTodo = todo.copy(category = newCategory)
        todoDao.updateTodo(updatedTodo)
    }
    
    /**
     * Update a todo with new data.
     * 
     * @param todo The updated todo
     */
    suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }
    
    /**
     * Delete a todo from the database.
     * 
     * @param todo The todo to delete
     */
    suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo)
    }
    
    /**
     * Get todos filtered by completion status.
     * 
     * @param isCompleted Whether to get completed or incomplete todos
     * @return Flow of filtered todos
     */
    fun getTodosByStatus(isCompleted: Boolean): Flow<List<Todo>> {
        return todoDao.getTodosByStatus(isCompleted)
    }
    
    /**
     * Get todos filtered by category and completion status.
     * 
     * @param category The category to filter by
     * @param isCompleted Whether to get completed or incomplete todos
     * @return Flow of filtered todos
     */
    fun getTodosByCategoryAndStatus(category: TodoCategory, isCompleted: Boolean): Flow<List<Todo>> {
        return todoDao.getTodosByCategoryAndStatus(category, isCompleted)
    }
    
    /**
     * Get the total count of todos.
     * 
     * @return Flow of the total number of todos
     */
    fun getTodoCount(): Flow<Int> = todoDao.getTodoCount()
    
    /**
     * Get the count of todos by category.
     * 
     * @param category The category to count
     * @return Flow of the count of todos in the category
     */
    fun getTodoCountByCategory(category: TodoCategory): Flow<Int> = 
        todoDao.getTodoCountByCategory(category)
    
    /**
     * Delete all completed todos.
     */
    suspend fun deleteCompletedTodos() {
        todoDao.deleteCompletedTodos()
    }
    
    /**
     * Reorder todos by updating their sort order.
     * 
     * @param reorderedTodos List of todos in their new order
     */
    suspend fun reorderTodos(reorderedTodos: List<Todo>) {
        todoDao.reorderTodos(reorderedTodos)
    }
    
    /**
     * Check if the todo text is valid (not empty or blank).
     * 
     * @param text The text to validate
     * @return True if the text is valid, false otherwise
     */
    fun isValidTodoText(text: String): Boolean {
        return text.trim().isNotEmpty()
    }
}