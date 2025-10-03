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
 * Hilt module for providing database-related dependencies.
 * 
 * This module provides the Room database and DAO instances
 * as singletons throughout the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides the TodoDatabase instance.
     * 
     * Creates a Room database instance with the application context.
     * The database is created as a singleton to ensure there's only
     * one instance throughout the app lifecycle.
     * 
     * @param context Application context
     * @return TodoDatabase instance
     */
    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java,
            TodoDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    /**
     * Provides the TodoDao instance.
     * 
     * Extracts the DAO from the database instance.
     * 
     * @param database The TodoDatabase instance
     * @return TodoDao instance
     */
    @Provides
    fun provideTodoDao(database: TodoDatabase): TodoDao {
        return database.todoDao()
    }
}