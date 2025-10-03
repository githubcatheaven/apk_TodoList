package com.canme.todo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performLongClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end integration test for the Todo application.
 * 
 * This test verifies the complete user workflow from app launch to
 * performing all major operations (add, complete, delete todos).
 * 
 * Tests the integration between:
 * - UI components
 * - ViewModel
 * - Repository
 * - Database
 * - Navigation and state management
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun completeUserWorkflow_addCompleteDeleteTodos() {
        // 1. Verify empty state is displayed initially
        composeTestRule
            .onNodeWithText("No todos yet")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Add Your First Todo")
            .assertIsDisplayed()

        // 2. Add first todo via empty state button
        composeTestRule
            .onNodeWithContentDescription("Add your first todo button. Tap to create your first todo item.")
            .performClick()

        // 3. Verify add todo dialog appears
        composeTestRule
            .onNodeWithText("Add New Todo")
            .assertIsDisplayed()

        // 4. Enter todo text
        val firstTodoText = "Buy groceries for the week"
        composeTestRule
            .onNodeWithContentDescription("Enter todo description. Current text: empty")
            .performTextInput(firstTodoText)

        // 5. Confirm adding the todo
        composeTestRule
            .onNodeWithText("Add")
            .performClick()

        // 6. Verify todo appears in the list
        composeTestRule
            .onNodeWithText(firstTodoText)
            .assertIsDisplayed()

        // 7. Add second todo via FAB
        composeTestRule
            .onNodeWithContentDescription("Add new todo. Opens dialog to create a new todo item.")
            .performClick()

        val secondTodoText = "Complete project documentation"
        composeTestRule
            .onNodeWithContentDescription("Enter todo description. Current text: empty")
            .performTextInput(secondTodoText)

        composeTestRule
            .onNodeWithText("Add")
            .performClick()

        // 8. Verify both todos are displayed
        composeTestRule
            .onNodeWithText(firstTodoText)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(secondTodoText)
            .assertIsDisplayed()

        // 9. Mark first todo as complete
        composeTestRule
            .onNodeWithContentDescription("Todo: $firstTodoText. Long press to delete.")
            .assertIsDisplayed()

        // Find and click the checkbox for the first todo
        composeTestRule
            .onNodeWithContentDescription("Mark as complete")
            .performClick()

        // 10. Verify todo is marked as completed (should have strikethrough)
        composeTestRule
            .onNodeWithContentDescription("Completed todo: $firstTodoText. Long press to delete.")
            .assertIsDisplayed()

        // 11. Add third todo to test with multiple items
        composeTestRule
            .onNodeWithContentDescription("Add new todo. Opens dialog to create a new todo item.")
            .performClick()

        val thirdTodoText = "Schedule dentist appointment"
        composeTestRule
            .onNodeWithContentDescription("Enter todo description. Current text: empty")
            .performTextInput(thirdTodoText)

        composeTestRule
            .onNodeWithText("Add")
            .performClick()

        // 12. Verify all three todos are present
        composeTestRule
            .onNodeWithText(firstTodoText)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(secondTodoText)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(thirdTodoText)
            .assertIsDisplayed()

        // 13. Delete the second todo
        composeTestRule
            .onNodeWithContentDescription("Todo: $secondTodoText. Long press to delete.")
            .performLongClick()

        // 14. Verify delete confirmation dialog appears
        composeTestRule
            .onNodeWithText("Delete Todo")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Are you sure you want to delete this todo item?")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("\"$secondTodoText\"")
            .assertIsDisplayed()

        // 15. Confirm deletion
        composeTestRule
            .onNodeWithContentDescription("Delete button. Warning: This will permanently delete the todo item.")
            .performClick()

        // 16. Verify todo is removed from the list
        composeTestRule
            .onNodeWithText(secondTodoText)
            .assertDoesNotExist()

        // 17. Verify remaining todos are still present
        composeTestRule
            .onNodeWithText(firstTodoText)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(thirdTodoText)
            .assertIsDisplayed()

        // 18. Toggle completion status of the completed todo
        composeTestRule
            .onNodeWithContentDescription("Mark as incomplete")
            .performClick()

        // 19. Verify todo is now marked as incomplete
        composeTestRule
            .onNodeWithContentDescription("Todo: $firstTodoText. Long press to delete.")
            .assertIsDisplayed()

        // 20. Test canceling a deletion
        composeTestRule
            .onNodeWithContentDescription("Todo: $thirdTodoText. Long press to delete.")
            .performLongClick()

        composeTestRule
            .onNodeWithText("Delete Todo")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Cancel deletion")
            .performClick()

        // 21. Verify todo is still present after canceling
        composeTestRule
            .onNodeWithText(thirdTodoText)
            .assertIsDisplayed()

        // 22. Test adding todo with validation (empty text)
        composeTestRule
            .onNodeWithContentDescription("Add new todo. Opens dialog to create a new todo item.")
            .performClick()

        // Try to add without entering text
        composeTestRule
            .onNodeWithContentDescription("Add todo button, disabled because text is empty")
            .assertIsDisplayed()

        // Cancel the dialog
        composeTestRule
            .onNodeWithContentDescription("Cancel adding todo")
            .performClick()

        // 23. Final verification - ensure app state is consistent
        composeTestRule
            .onNodeWithText(firstTodoText)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(thirdTodoText)
            .assertIsDisplayed()

        // Verify the app title is displayed
        composeTestRule
            .onNodeWithText("My Todos")
            .assertIsDisplayed()
    }

    @Test
    fun testAppLaunchAndBasicNavigation() {
        // Verify app launches successfully
        composeTestRule
            .onNodeWithText("My Todos")
            .assertIsDisplayed()

        // Verify FAB is present and clickable
        composeTestRule
            .onNodeWithContentDescription("Add new todo. Opens dialog to create a new todo item.")
            .assertIsDisplayed()
            .performClick()

        // Verify dialog opens
        composeTestRule
            .onNodeWithText("Add New Todo")
            .assertIsDisplayed()

        // Close dialog
        composeTestRule
            .onNodeWithContentDescription("Cancel adding todo")
            .performClick()

        // Verify we're back to main screen
        composeTestRule
            .onNodeWithText("My Todos")
            .assertIsDisplayed()
    }

    @Test
    fun testCharacterCountInAddDialog() {
        // Open add todo dialog
        composeTestRule
            .onNodeWithContentDescription("Add new todo. Opens dialog to create a new todo item.")
            .performClick()

        // Enter some text
        val testText = "Test todo item"
        composeTestRule
            .onNodeWithContentDescription("Enter todo description. Current text: empty")
            .performTextInput(testText)

        // Verify character count is displayed
        composeTestRule
            .onNodeWithContentDescription("Character count: ${testText.length} of 500 characters used")
            .assertIsDisplayed()

        // Cancel dialog
        composeTestRule
            .onNodeWithContentDescription("Cancel adding todo")
            .performClick()
    }
}