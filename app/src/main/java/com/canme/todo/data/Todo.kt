package com.canme.todo.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Enum representing different categories for todo items.
 * 
 * Each category has a display name, associated color, and icon for UI representation.
 * 
 * @property displayName Human-readable name for the category
 * @property color Color associated with the category for UI theming
 * @property icon Material Design icon for visual representation
 */
enum class TodoCategory(
    val displayName: String,
    val color: Color,
    val icon: ImageVector
) {
    PERSONAL("Personal", Color(0xFF2196F3), Icons.Filled.AccountCircle),
    WORK("Work", Color(0xFF4CAF50), Icons.Filled.Home),
    SHOPPING("Shopping", Color(0xFFFF9800), Icons.Filled.ShoppingCart),
    HEALTH("Health", Color(0xFFE91E63), Icons.Filled.Favorite),
    OTHER("Other", Color(0xFF9C27B0), Icons.Filled.MoreVert)
}

/**
 * Data class representing a todo item in the application.
 * 
 * This entity is stored in the Room database and represents the core
 * data structure for todo items with their completion status and category.
 * 
 * @property id Unique identifier for the todo item (auto-generated)
 * @property text The text content of the todo item
 * @property isCompleted Whether the todo item has been completed
 * @property category The category this todo belongs to
 * @property sortOrder Custom sort order for user-defined positioning
 * @property createdAt Timestamp when the todo was created
 */
@Entity(
    tableName = "todos",
    indices = [
        Index(value = ["sortOrder"]),
        Index(value = ["category"]),
        Index(value = ["isCompleted"]),
        Index(value = ["category", "isCompleted"]),
        Index(value = ["createdAt"])
    ]
)
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val category: TodoCategory = TodoCategory.PERSONAL,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)