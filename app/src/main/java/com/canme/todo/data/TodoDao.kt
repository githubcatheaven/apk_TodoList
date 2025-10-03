package com.canme.todo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Todo entities.
 * 
 * Provides methods to interact with the todos table in the Room database.
 * All operations are suspend functions for coroutine support, and queries
 * return Flow for reactive UI updates.
 */
@Dao
interface TodoDao {
    
    /**
     * Get all todos ordered by sort order then creation date (newest first).
     * 
     * @return Flow of list of all todos for reactive UI updates
     */
    @Query("SELECT * FROM todos ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllTodos(): Flow<List<Todo>>
    
    /**
     * Get todos filtered by category, ordered by sort order then creation date.
     * 
     * @param category The category to filter by
     * @return Flow of filtered todos for reactive UI updates
     */
    @Query("SELECT * FROM todos WHERE category = :category ORDER BY sortOrder ASC, createdAt DESC")
    fun getTodosByCategory(category: TodoCategory): Flow<List<Todo>>
    
    /**
     * Insert a new todo into the database.
     * 
     * @param todo The todo item to insert
     * @return The ID of the inserted todo
     */
    @Insert
    suspend fun insertTodo(todo: Todo): Long
    
    /**
     * Update an existing todo in the database.
     * 
     * @param todo The todo item to update
     */
    @Update
    suspend fun updateTodo(todo: Todo)
    
    /**
     * Delete a todo from the database.
     * 
     * @param todo The todo item to delete
     */
    @Delete
    suspend fun deleteTodo(todo: Todo)
    
    /**
     * Get todos by completion status.
     * 
     * @param isCompleted Whether to get completed or incomplete todos
     * @return Flow of filtered todos
     */
    @Query("SELECT * FROM todos WHERE isCompleted = :isCompleted ORDER BY sortOrder ASC, createdAt DESC")
    fun getTodosByStatus(isCompleted: Boolean): Flow<List<Todo>>
    
    /**
     * Get todos by category and completion status.
     * 
     * @param category The category to filter by
     * @param isCompleted Whether to get completed or incomplete todos
     * @return Flow of filtered todos
     */
    @Query("SELECT * FROM todos WHERE category = :category AND isCompleted = :isCompleted ORDER BY sortOrder ASC, createdAt DESC")
    fun getTodosByCategoryAndStatus(category: TodoCategory, isCompleted: Boolean): Flow<List<Todo>>
    
    /**
     * Get the count of all todos.
     * 
     * @return Flow of the total number of todos
     */
    @Query("SELECT COUNT(*) FROM todos")
    fun getTodoCount(): Flow<Int>
    
    /**
     * Get the count of todos by category.
     * 
     * @param category The category to count
     * @return Flow of the count of todos in the category
     */
    @Query("SELECT COUNT(*) FROM todos WHERE category = :category")
    fun getTodoCountByCategory(category: TodoCategory): Flow<Int>
    
    /**
     * Delete all completed todos.
     */
    @Query("DELETE FROM todos WHERE isCompleted = 1")
    suspend fun deleteCompletedTodos()
    
    /**
     * Update the sort order of a specific todo.
     * 
     * @param todoId The ID of the todo to update
     * @param newOrder The new sort order value
     */
    @Query("UPDATE todos SET sortOrder = :newOrder WHERE id = :todoId")
    suspend fun updateSortOrder(todoId: Long, newOrder: Int)
    
    /**
     * Reorder multiple todos in a single transaction.
     * 
     * @param reorderedTodos List of todos with updated sort orders
     */
    @Transaction
    suspend fun reorderTodos(reorderedTodos: List<Todo>) {
        reorderedTodos.forEachIndexed { index, todo ->
            updateSortOrder(todo.id, index)
        }
    }
}