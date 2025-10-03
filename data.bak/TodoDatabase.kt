package com.canme.todo.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * Room database class for the Todo application.
 * Defines the database configuration and serves as the main access point
 * for the underlying connection to the app's persisted data.
 */
@Database(
    entities = [Todo::class],
    version = 1,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    
    /**
     * Provides access to the TodoDao for database operations.
     */
    abstract fun todoDao(): TodoDao
    
    companion object {
        // Singleton prevents multiple instances of database opening at the same time
        @Volatile
        private var INSTANCE: TodoDatabase? = null
        
        /**
         * Gets the singleton instance of the TodoDatabase.
         * If the database doesn't exist, it creates one.
         * 
         * @param context The application context
         * @return The TodoDatabase instance
         */
        fun getDatabase(context: Context): TodoDatabase {
            // If the INSTANCE is not null, return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                )
                    // Strategy for handling migrations when schema changes
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // Return instance
                instance
            }
        }
        
        /**
         * Creates an in-memory database for testing purposes.
         * This database will be cleared when the process is killed.
         * 
         * @param context The application context
         * @return A TodoDatabase instance for testing
         */
        fun getInMemoryDatabase(context: Context): TodoDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                TodoDatabase::class.java
            )
                .allowMainThreadQueries() // Only for testing
                .build()
        }
    }
}