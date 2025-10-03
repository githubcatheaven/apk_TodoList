package com.canme.todo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Todo entities.
 * Provides methods to interact with the todos table in the database.
 */
@Dao
interface TodoDao {
    
    /**
     * Retrieves all todos from the database, ordered by creation date (newest first).
     * Returns a Flow for reactive updates when data changes.
     */
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<Todo>>
    
    /**
     * Inserts a new todo into the database.
     * @param todo The todo item to insert
     */
    @Insert
    suspend fun insertTodo(todo: Todo)
    
    /**
     * Updates an existing todo in the database.
     * @param todo The todo item to update
     */
    @Update
    suspend fun updateTodo(todo: Todo)
    
    /**
     * Deletes a todo from the database.
     * @param todo The todo item to delete
     */
    @Delete
    suspend fun deleteTodo(todo: Todo)
    
    /**
     * Retrieves a specific todo by its ID.
     * @param id The ID of the todo to retrieve
     * @return The todo item or null if not found
     */
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): Todo?
    
    /**
     * Deletes all todos from the database.
     * Useful for testing or clearing all data.
     */
    @Query("DELETE FROM todos")
    suspend fun deleteAllTodos()
}