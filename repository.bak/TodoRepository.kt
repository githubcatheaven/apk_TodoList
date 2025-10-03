package com.canme.todo.repository

import com.canme.todo.data.Todo
import com.canme.todo.data.TodoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Repository class that provides a clean API for data access to the rest of the application.
 * Acts as a single source of truth for todo data and abstracts the data layer implementation.
 */
class TodoRepository(
    private val todoDao: TodoDao
) {
    
    /**
     * Retrieves all todos from the database as a Flow for reactive updates.
     * Includes error handling to provide a graceful fallback.
     * 
     * @return Flow of list of todos, ordered by creation date (newest first)
     */
    fun getAllTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos()
            .catch { exception ->
                // Log error and emit empty list as fallback
                // In a production app, you might want to use a proper logging framework
                println("Error fetching todos: ${exception.message}")
                emit(emptyList())
            }
    }
    
    /**
     * Inserts a new todo into the database.
     * Validates the todo before insertion and handles potential errors.
     * 
     * @param todo The todo item to insert
     * @return Result indicating success or failure with error details
     */
    suspend fun insertTodo(todo: Todo): Result<Unit> {
        return try {
            // Validate todo before insertion
            if (!todo.isValid()) {
                Result.failure(IllegalArgumentException("Todo text cannot be empty"))
            } else {
                todoDao.insertTodo(todo)
                Result.success(Unit)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    /**
     * Updates an existing todo in the database.
     * Validates the todo before update and handles potential errors.
     * 
     * @param todo The todo item to update
     * @return Result indicating success or failure with error details
     */
    suspend fun updateTodo(todo: Todo): Result<Unit> {
        return try {
            // Validate todo before update
            if (!todo.isValid()) {
                Result.failure(IllegalArgumentException("Todo text cannot be empty"))
            } else {
                todoDao.updateTodo(todo)
                Result.success(Unit)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    /**
     * Deletes a todo from the database.
     * Handles potential errors during deletion.
     * 
     * @param todo The todo item to delete
     * @return Result indicating success or failure with error details
     */
    suspend fun deleteTodo(todo: Todo): Result<Unit> {
        return try {
            todoDao.deleteTodo(todo)
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    /**
     * Retrieves a specific todo by its ID.
     * Handles potential errors during retrieval.
     * 
     * @param id The ID of the todo to retrieve
     * @return Result containing the todo item or error details
     */
    suspend fun getTodoById(id: Long): Result<Todo?> {
        return try {
            val todo = todoDao.getTodoById(id)
            Result.success(todo)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    /**
     * Toggles the completion status of a todo item.
     * Convenience method that combines retrieval, update, and error handling.
     * 
     * @param todoId The ID of the todo to toggle
     * @return Result indicating success or failure with error details
     */
    suspend fun toggleTodoCompletion(todoId: Long): Result<Unit> {
        return try {
            val todo = todoDao.getTodoById(todoId)
            if (todo != null) {
                val updatedTodo = todo.toggleCompleted()
                todoDao.updateTodo(updatedTodo)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Todo with ID $todoId not found"))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    /**
     * Deletes all todos from the database.
     * Useful for testing or clearing all data.
     * 
     * @return Result indicating success or failure with error details
     */
    suspend fun deleteAllTodos(): Result<Unit> {
        return try {
            todoDao.deleteAllTodos()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}