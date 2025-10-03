package com.canme.todo.di

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.canme.todo.data.TodoDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull

/**
 * Unit tests for Hilt dependency injection modules.
 * Verifies that the modules can correctly provide their dependencies.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HiltModulesTest {
    
    private lateinit var context: Context
    private lateinit var database: TodoDatabase
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }
    
    @After
    fun tearDown() {
        if (::database.isInitialized) {
            database.close()
        }
    }
    
    @Test
    fun `DatabaseModule provides TodoDatabase`() {
        // Test that DatabaseModule can provide a TodoDatabase instance
        val database = DatabaseModule.provideTodoDatabase(context)
        
        assertNotNull(database)
        this.database = database
    }
    
    @Test
    fun `DatabaseModule provides TodoDao`() {
        // Test that DatabaseModule can provide a TodoDao instance
        val database = DatabaseModule.provideTodoDatabase(context)
        val todoDao = DatabaseModule.provideTodoDao(database)
        
        assertNotNull(todoDao)
        this.database = database
    }
    
    @Test
    fun `RepositoryModule provides TodoRepository`() {
        // Test that RepositoryModule can provide a TodoRepository instance
        val database = DatabaseModule.provideTodoDatabase(context)
        val todoDao = DatabaseModule.provideTodoDao(database)
        val repository = RepositoryModule.provideTodoRepository(todoDao)
        
        assertNotNull(repository)
        this.database = database
    }
    
    @Test
    fun `dependency chain works correctly`() {
        // Test the complete dependency chain: Context -> Database -> DAO -> Repository
        val database = DatabaseModule.provideTodoDatabase(context)
        val todoDao = DatabaseModule.provideTodoDao(database)
        val repository = RepositoryModule.provideTodoRepository(todoDao)
        
        // Verify all dependencies are properly created
        assertNotNull(database)
        assertNotNull(todoDao)
        assertNotNull(repository)
        
        this.database = database
    }
}