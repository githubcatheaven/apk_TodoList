package com.canme.todo.di

import android.content.Context
import androidx.room.Room
import com.canme.todo.data.TodoDao
import com.canme.todo.data.TodoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides database-related dependencies.
 * This module is installed in the SingletonComponent, making the provided
 * dependencies available throughout the entire application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides the TodoDatabase instance as a singleton.
     * Uses the application context to create the Room database.
     * 
     * @param context Application context provided by Hilt
     * @return TodoDatabase instance
     */
    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java,
            "todo_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    /**
     * Provides the TodoDao instance.
     * The DAO is obtained from the TodoDatabase instance.
     * 
     * @param database TodoDatabase instance provided by Hilt
     * @return TodoDao instance
     */
    @Provides
    fun provideTodoDao(database: TodoDatabase): TodoDao {
        return database.todoDao()
    }
}