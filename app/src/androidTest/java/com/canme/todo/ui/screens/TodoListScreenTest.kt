package com.canme.todo.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performLongClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canme.todo.data.Todo
import com.canme.todo.repository.TodoRepository
import com.canme.todo.ui.TodoViewModel
import com.canme.todo.ui.theme.TodoTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Integration tests for TodoListScreen.
 * Tests complete user workflows and component integration.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TodoListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockRepository: TodoRepository

    private lateinit var viewModel: TodoViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        hiltRule.inject()
        
        // Setup default repository behavior
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(emptyList()))
        
        viewModel = TodoViewModel(mockRepository)
    }

    @Test
    fun todoListScreen_emptyState_displaysCorrectly() {
        // Given
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(emptyList()))

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("My Todos").assertIsDisplayed()
        composeTestRule.onNodeWithText("No todos yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Your First Todo").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Todo").assertIsDisplayed()
    }

    @Test
    fun todoListScreen_withTodos_displaysCorrectly() {
        // Given
        val todos = listOf(
            Todo(1, "First todo", false, 123L),
            Todo(2, "Second todo", true, 124L),
            Todo(3, "Third todo", false, 125L)
        )
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(todos))
        viewModel = TodoViewModel(mockRepository)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("My Todos").assertIsDisplayed()
        composeTestRule.onNodeWithText("First todo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second todo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Third todo").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Todo").assertIsDisplayed()
    }

    @Test
    fun todoListScreen_fabClick_opensAddDialog() {
        // Given
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(emptyList()))

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()

        // Then
        composeTestRule.onNodeWithText("Add New Todo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Todo description").assertIsDisplayed()
    }

    @Test
    fun todoListScreen_emptyStateButton_opensAddDialog() {
        // Given
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(emptyList()))

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Add Your First Todo").performClick()

        // Then
        composeTestRule.onNodeWithText("Add New Todo").assertIsDisplayed()
    }

    @Test
    fun todoListScreen_addTodoWorkflow_worksCorrectly() {
        // Given
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(emptyList()))
        `when`(mockRepository.insertTodo(any())).thenReturn(Result.success(Unit))

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        // Open add dialog
        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()
        
        // Enter text
        composeTestRule.onNodeWithText("Todo description").performTextInput("New todo item")
        
        // Confirm
        composeTestRule.onNodeWithText("Add").performClick()

        // Then
        verify(mockRepository).insertTodo(argThat { todo -> todo.text == "New todo item" })
    }

    @Test
    fun todoListScreen_cancelAddTodo_closesDialog() {
        // Given
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(emptyList()))

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        // Open and cancel dialog
        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then
        composeTestRule.onNodeWithText("Add New Todo").assertDoesNotExist()
    }

    @Test
    fun todoListScreen_longPressTodo_opensDeleteDialog() {
        // Given
        val todos = listOf(
            Todo(1, "Todo to delete", false, 123L)
        )
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(todos))
        viewModel = TodoViewModel(mockRepository)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Todo to delete").performLongClick()

        // Then
        composeTestRule.onNodeWithText("Delete Todo").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"Todo to delete\"").assertIsDisplayed()
    }

    @Test
    fun todoListScreen_confirmDelete_callsRepository() {
        // Given
        val todoToDelete = Todo(1, "Todo to delete", false, 123L)
        val todos = listOf(todoToDelete)
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(todos))
        `when`(mockRepository.deleteTodo(todoToDelete)).thenReturn(Result.success(Unit))
        viewModel = TodoViewModel(mockRepository)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        // Long press to open delete dialog
        composeTestRule.onNodeWithText("Todo to delete").performLongClick()
        
        // Confirm deletion
        composeTestRule.onNodeWithText("Delete").performClick()

        // Then
        verify(mockRepository).deleteTodo(todoToDelete)
    }

    @Test
    fun todoListScreen_cancelDelete_closesDialog() {
        // Given
        val todos = listOf(
            Todo(1, "Todo to delete", false, 123L)
        )
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(todos))
        viewModel = TodoViewModel(mockRepository)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        // Long press and cancel
        composeTestRule.onNodeWithText("Todo to delete").performLongClick()
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then
        composeTestRule.onNodeWithText("Delete Todo").assertDoesNotExist()
    }

    @Test
    fun todoListScreen_toggleTodoCompletion_callsRepository() {
        // Given
        val todo = Todo(1, "Todo to toggle", false, 123L)
        val todos = listOf(todo)
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(todos))
        `when`(mockRepository.toggleTodoCompletion(1L)).thenReturn(Result.success(Unit))
        viewModel = TodoViewModel(mockRepository)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        // Click the checkbox (find it by looking for clickable elements that aren't text inputs)
        composeTestRule.onNode(
            androidx.compose.ui.test.hasClickAction() and 
            androidx.compose.ui.test.hasSetTextAction().not()
        ).performClick()

        // Then
        verify(mockRepository).toggleTodoCompletion(1L)
    }

    @Test
    fun todoListScreen_displaysTopAppBar() {
        // Given
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(emptyList()))

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("My Todos").assertIsDisplayed()
    }

    @Test
    fun todoListScreen_multipleInteractions_workCorrectly() {
        // Given
        val todos = listOf(
            Todo(1, "First todo", false, 123L),
            Todo(2, "Second todo", true, 124L)
        )
        `when`(mockRepository.getAllTodos()).thenReturn(flowOf(todos))
        `when`(mockRepository.insertTodo(any())).thenReturn(Result.success(Unit))
        `when`(mockRepository.toggleTodoCompletion(any())).thenReturn(Result.success(Unit))
        viewModel = TodoViewModel(mockRepository)

        // When
        composeTestRule.setContent {
            TodoTheme {
                TodoListScreen(viewModel = viewModel)
            }
        }

        // Perform multiple interactions
        // 1. Toggle first todo
        composeTestRule.onNode(
            androidx.compose.ui.test.hasClickAction() and 
            androidx.compose.ui.test.hasSetTextAction().not()
        ).performClick()

        // 2. Add new todo
        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()
        composeTestRule.onNodeWithText("Todo description").performTextInput("New todo")
        composeTestRule.onNodeWithText("Add").performClick()

        // Then
        verify(mockRepository).toggleTodoCompletion(1L)
        verify(mockRepository).insertTodo(argThat { todo -> todo.text == "New todo" })
    }
}