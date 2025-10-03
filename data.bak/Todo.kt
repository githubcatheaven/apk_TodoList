package com.canme.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Todo entity representing a todo item in the database.
 * 
 * @param id Unique identifier for the todo item (auto-generated)
 * @param text The todo item description text
 * @param isCompleted Whether the todo item is completed or not
 * @param createdAt Timestamp when the todo was created (for ordering)
 */
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Validates that the todo text is not empty or blank
     */
    fun isValid(): Boolean {
        return text.isNotBlank()
    }
    
    /**
     * Returns a copy of this todo with the completion status toggled
     */
    fun toggleCompleted(): Todo {
        return copy(isCompleted = !isCompleted)
    }
}