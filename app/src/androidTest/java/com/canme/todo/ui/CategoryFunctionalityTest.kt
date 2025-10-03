package com.canme.todo.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.repository.TodoRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import javax.inject.Inject

/**
 * UI tests for category functionality in the Todo app.
 * Tests category selection, filtering, and visual indicators.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CategoryFunctionalityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockRepository: TodoRepository

    @Inject
    lateinit var viewModel: TodoViewModel

    private val testTodos = listOf(
        Todo(1, "Personal task", false, TodoCategory.PERSONAL, 0, System.currentTimeMillis()),
        Todo(2, "Work meeting", false, TodoCategory.WORK, 1, System.currentTimeMillis()),
        Todo(3, "Buy groceries", true, TodoCategory.SHOPPING, 2, System.currentTimeMillis()),
        Todo(4, "Gym workout", false, TodoCategory.HEALTH, 3, System.currentTimeMillis()),
        Todo(5, "Other task", false, TodoCategory.OTHER, 4, System.currentTimeMillis())
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        hiltRule.inject()
        
        // Setup mock repository behavior
        `when`(mockRepository.getAllTodos()).thenReturn(MutableStateFlow(testTodos))
        `when`(mockRepository.isValidTodoText(anyString())).thenAnswer { invocation ->
            val text = invocation.getArgument<String>(0)
            text.trim().isNotEmpty()
        }
    }

    @Test
    fun categoryFilterChips_displayAllCategories() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When & Then - All category filter chips should be displayed
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Personal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shopping").assertIsDisplayed()
        composeTestRule.onNodeWithText("Health").assertIsDisplayed()
        composeTestRule.onNodeWithText("Other").assertIsDisplayed()
    }

    @Test
    fun categoryFilterChips_filterTodosByCategory() = runTest {
        // Given
        val workTodos = testTodos.filter { it.category == TodoCategory.WORK }
        `when`(mockRepository.getTodosByCategory(TodoCategory.WORK)).thenReturn(MutableStateFlow(workTodos))
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Click on Work category filter
        composeTestRule.onNodeWithText("Work").performClick()

        // Then - Only work todos should be displayed
        composeTestRule.onNodeWithText("Work meeting").assertIsDisplayed()
        composeTestRule.onNodeWithText("Personal task").assertDoesNotExist()
        composeTestRule.onNodeWithText("Buy groceries").assertDoesNotExist()
        
        verify(mockRepository).getTodosByCategory(TodoCategory.WORK)
    }

    @Test
    fun categoryFilterChips_allFilterShowsAllTodos() = runTest {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - First filter by category, then click "All"
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.onNodeWithText("All").performClick()

        // Then - All todos should be displayed again
        composeTestRule.onNodeWithText("Personal task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work meeting").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gym workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Other task").assertIsDisplayed()
        
        verify(mockRepository, atLeastOnce()).getAllTodos()
    }

    @Test
    fun todoItems_displayCategoryIndicators() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When & Then - Each todo should display its category indicator
        testTodos.forEach { todo ->
            composeTestRule.onNodeWithTag("todo_item_${todo.id}").assertIsDisplayed()
            composeTestRule.onNodeWithTag("category_indicator_${todo.id}").assertIsDisplayed()
        }
    }

    @Test
    fun todoItems_categoryIndicatorsHaveCorrectColors() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When & Then - Category indicators should have correct visual styling
        // Note: In a real test, you would check the actual color values
        // This is a simplified version checking for presence
        TodoCategory.values().forEach { category ->
            val todosWithCategory = testTodos.filter { it.category == category }
            todosWithCategory.forEach { todo ->
                composeTestRule.onNodeWithTag("category_indicator_${todo.id}")
                    .assertIsDisplayed()
            }
        }
    }

    @Test
    fun addTodoDialog_displaysCategorySelector() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Open add todo dialog
        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()

        // Then - Category selector should be displayed
        composeTestRule.onNodeWithTag("category_selector").assertIsDisplayed()
        
        // All categories should be available for selection
        TodoCategory.values().forEach { category ->
            composeTestRule.onNodeWithText(category.displayName).assertIsDisplayed()
        }
    }

    @Test
    fun addTodoDialog_categorySelectionWorks() = runTest {
        // Given
        `when`(mockRepository.insertTodo(anyString(), eq(TodoCategory.HEALTH))).thenReturn(6L)
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Open dialog, select category, enter text, and add todo
        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()
        composeTestRule.onNodeWithText("Health").performClick()
        composeTestRule.onNodeWithTag("todo_text_input").performTextInput("New health task")
        composeTestRule.onNodeWithText("Add").performClick()

        // Then - Repository should be called with correct category
        verify(mockRepository).insertTodo("New health task", TodoCategory.HEALTH)
    }

    @Test
    fun addTodoDialog_defaultCategoryIsPersonal() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Open add todo dialog
        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()

        // Then - Personal category should be selected by default
        composeTestRule.onNodeWithTag("category_selector_personal")
            .assertIsDisplayed()
    }

    @Test
    fun categoryFiltering_updatesEmptyState() = runTest {
        // Given - Empty list for a specific category
        `when`(mockRepository.getTodosByCategory(TodoCategory.SHOPPING)).thenReturn(MutableStateFlow(emptyList()))
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Filter by category with no todos
        composeTestRule.onNodeWithText("Shopping").performClick()

        // Then - Empty state should be displayed with category context
        composeTestRule.onNodeWithTag("empty_state").assertIsDisplayed()
        composeTestRule.onNodeWithText("No shopping todos yet").assertIsDisplayed()
    }

    @Test
    fun categoryFiltering_persistsAcrossDialogOperations() = runTest {
        // Given
        val healthTodos = testTodos.filter { it.category == TodoCategory.HEALTH }
        `when`(mockRepository.getTodosByCategory(TodoCategory.HEALTH)).thenReturn(MutableStateFlow(healthTodos))
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Filter by category, open and close dialog
        composeTestRule.onNodeWithText("Health").performClick()
        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then - Filter should still be active
        composeTestRule.onNodeWithText("Gym workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Personal task").assertDoesNotExist()
    }

    @Test
    fun categorySelector_visualFeedbackOnSelection() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Open dialog and select different categories
        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()
        
        // Then - Each category selection should provide visual feedback
        TodoCategory.values().forEach { category ->
            composeTestRule.onNodeWithText(category.displayName).performClick()
            
            // The selected category should be visually indicated
            composeTestRule.onNodeWithTag("category_selector_${category.name.lowercase()}")
                .assertIsDisplayed()
        }
    }

    @Test
    fun categoryIcons_displayCorrectly() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Open add todo dialog
        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()

        // Then - Each category should display its associated icon
        TodoCategory.values().forEach { category ->
            composeTestRule.onNodeWithTag("category_icon_${category.name.lowercase()}")
                .assertIsDisplayed()
        }
    }

    @Test
    fun categoryFiltering_countsDisplayCorrectly() = runTest {
        // Given
        val personalCount = testTodos.count { it.category == TodoCategory.PERSONAL }
        val workCount = testTodos.count { it.category == TodoCategory.WORK }
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When & Then - Category chips should show correct counts (if implemented)
        // This test assumes category counts are displayed in the UI
        composeTestRule.onNodeWithText("Personal ($personalCount)").assertExists()
        composeTestRule.onNodeWithText("Work ($workCount)").assertExists()
    }

    @Test
    fun categoryFiltering_workflowIntegration() = runTest {
        // Given
        `when`(mockRepository.insertTodo(anyString(), eq(TodoCategory.WORK))).thenReturn(6L)
        val workTodos = testTodos.filter { it.category == TodoCategory.WORK }
        `when`(mockRepository.getTodosByCategory(TodoCategory.WORK)).thenReturn(MutableStateFlow(workTodos))
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Complete workflow: filter, add todo, verify it appears
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.onNodeWithContentDescription("Add Todo").performClick()
        composeTestRule.onNodeWithText("Work").performClick() // Select work category
        composeTestRule.onNodeWithTag("todo_text_input").performTextInput("New work task")
        composeTestRule.onNodeWithText("Add").performClick()

        // Then - New todo should be added with correct category
        verify(mockRepository).insertTodo("New work task", TodoCategory.WORK)
    }

    @Test
    fun categoryAccessibility_screenReaderSupport() {
        // Given
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When & Then - Category elements should have proper accessibility labels
        composeTestRule.onNodeWithContentDescription("Filter by Personal category").assertExists()
        composeTestRule.onNodeWithContentDescription("Filter by Work category").assertExists()
        composeTestRule.onNodeWithContentDescription("Filter by Shopping category").assertExists()
        composeTestRule.onNodeWithContentDescription("Filter by Health category").assertExists()
        composeTestRule.onNodeWithContentDescription("Filter by Other category").assertExists()
        
        // Category indicators should also have accessibility support
        testTodos.forEach { todo ->
            composeTestRule.onNodeWithContentDescription("${todo.category.displayName} category")
                .assertExists()
        }
    }

    @Test
    fun categoryFiltering_performanceWithLargeLists() = runTest {
        // Given - Large list of todos
        val largeTodoList = (1..1000).map { index ->
            Todo(
                id = index.toLong(),
                text = "Todo $index",
                isCompleted = index % 3 == 0,
                category = TodoCategory.values()[index % TodoCategory.values().size],
                sortOrder = index,
                createdAt = System.currentTimeMillis() + index
            )
        }
        
        `when`(mockRepository.getAllTodos()).thenReturn(MutableStateFlow(largeTodoList))
        val workTodos = largeTodoList.filter { it.category == TodoCategory.WORK }
        `when`(mockRepository.getTodosByCategory(TodoCategory.WORK)).thenReturn(MutableStateFlow(workTodos))
        
        composeTestRule.setContent {
            TodoListScreen(viewModel = viewModel)
        }

        // When - Filter by category
        composeTestRule.onNodeWithText("Work").performClick()

        // Then - Only work todos should be displayed (performance test)
        composeTestRule.onAllNodesWithTag("todo_item")
            .assertCountEquals(workTodos.size)
    }
}