package com.canme.todo.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.canme.todo.TodoApplication
import com.canme.todo.repository.TodoRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertNotNull

/**
 * Integration test for Hilt dependency injection.
 * Verifies that Hilt can properly inject dependencies in an Android environment.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var todoRepository: TodoRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun testHiltCanInjectTodoRepository() {
        // Verify that Hilt can inject the TodoRepository
        assertNotNull(todoRepository)
    }
    
    @Test
    fun testApplicationIsHiltApplication() {
        // Verify that the application is properly configured with Hilt
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val application = context.applicationContext as TodoApplication
        assertNotNull(application)
    }
}