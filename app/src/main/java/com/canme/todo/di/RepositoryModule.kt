package com.canme.todo.di

import com.canme.todo.data.TodoDao
import com.canme.todo.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies.
 * 
 * This module provides repository instances that serve as the
 * single source of truth for data operations in the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * Provides the TodoRepository instance.
     * 
     * Creates a repository instance with the TodoDao dependency.
     * The repository is provided as a singleton to maintain
     * consistent state throughout the application.
     * 
     * @param todoDao The TodoDao instance for database operations
     * @return TodoRepository instance
     */
    @Provides
    @Singleton
    fun provideTodoRepository(todoDao: TodoDao): TodoRepository {
        return TodoRepository(todoDao)
    }
}