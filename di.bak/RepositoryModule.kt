package com.canme.todo.di

import com.canme.todo.data.TodoDao
import com.canme.todo.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository-related dependencies.
 * This module is installed in the SingletonComponent, making the provided
 * dependencies available throughout the entire application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * Provides the TodoRepository instance as a singleton.
     * The repository depends on TodoDao which is provided by DatabaseModule.
     * 
     * @param todoDao TodoDao instance provided by Hilt from DatabaseModule
     * @return TodoRepository instance
     */
    @Provides
    @Singleton
    fun provideTodoRepository(todoDao: TodoDao): TodoRepository {
        return TodoRepository(todoDao)
    }
}