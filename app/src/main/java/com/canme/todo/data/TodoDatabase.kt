package com.canme.todo.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

/**
 * Type converters for Room database to handle custom types.
 */
class Converters {
    @TypeConverter
    fun fromTodoCategory(category: TodoCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toTodoCategory(categoryName: String): TodoCategory {
        return TodoCategory.valueOf(categoryName)
    }
}

/**
 * Room database for the Todo application.
 * 
 * This database contains the todos table and provides access to the TodoDao.
 * The database is configured with version 3 and includes the Todo entity with category support and performance indexes.
 */
@Database(
    entities = [Todo::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    
    /**
     * Provides access to the TodoDao for database operations.
     */
    abstract fun todoDao(): TodoDao
    
    companion object {
        /**
         * Database name constant.
         */
        const val DATABASE_NAME = "todo_database"
        
        /**
         * Migration from version 1 to 2 - adds category and sortOrder columns.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add category column with default value PERSONAL
                database.execSQL("ALTER TABLE todos ADD COLUMN category TEXT NOT NULL DEFAULT 'PERSONAL'")
                
                // Add sortOrder column with default value 0
                database.execSQL("ALTER TABLE todos ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                
                // Update sortOrder based on creation time (older todos get higher sort order)
                database.execSQL("""
                    UPDATE todos 
                    SET sortOrder = (
                        SELECT COUNT(*) 
                        FROM todos t2 
                        WHERE t2.createdAt <= todos.createdAt
                    ) - 1
                """.trimIndent())
            }
        }
        
        /**
         * Migration from version 2 to 3 - adds performance indexes.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add indexes for better query performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_todos_sortOrder ON todos (sortOrder)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_todos_category ON todos (category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_todos_isCompleted ON todos (isCompleted)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_todos_category_isCompleted ON todos (category, isCompleted)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_todos_createdAt ON todos (createdAt)")
            }
        }
        
        /**
         * Singleton instance of the database.
         * 
         * This ensures only one instance of the database is created
         * throughout the application lifecycle.
         */
        @Volatile
        private var INSTANCE: TodoDatabase? = null
        
        /**
         * Get the singleton instance of the TodoDatabase.
         * 
         * This method is thread-safe and ensures only one instance
         * of the database is created.
         * 
         * @param context Application context
         * @return TodoDatabase instance
         */
        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Create an in-memory database for testing purposes.
         * 
         * @param context Application context
         * @return In-memory TodoDatabase instance
         */
        fun createInMemoryDatabase(context: Context): TodoDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                TodoDatabase::class.java
            ).build()
        }
    }
}