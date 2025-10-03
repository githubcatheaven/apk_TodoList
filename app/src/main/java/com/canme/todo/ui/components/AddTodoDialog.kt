package com.canme.todo.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.canme.todo.data.TodoCategory
import com.canme.todo.ui.theme.Dimensions
import com.canme.todo.ui.theme.TodoTheme

/**
 * Dialog composable for adding new todo items or editing existing ones.
 * Provides text input with validation, character count, category selection, and proper keyboard handling.
 * 
 * @param isVisible Whether the dialog should be shown
 * @param todoText Current text in the input field
 * @param selectedCategory Currently selected category
 * @param errorMessage Error message to display, null if no error
 * @param isLoading Whether a save operation is in progress
 * @param onTextChange Callback when the text changes
 * @param onCategoryChange Callback when the category changes
 * @param onConfirm Callback when the user confirms adding/editing the todo
 * @param onDismiss Callback when the dialog should be dismissed
 * @param maxLength Maximum allowed character length for todo text
 * @param isEditMode Whether this dialog is being used for editing (true) or adding (false)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddTodoDialog(
    isVisible: Boolean,
    todoText: String,
    selectedCategory: TodoCategory,
    errorMessage: String?,
    isLoading: Boolean,
    onTextChange: (String) -> Unit,
    onCategoryChange: (TodoCategory) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    maxLength: Int = 500,
    isEditMode: Boolean = false
) {
    if (!isVisible) return
    
    val focusRequester = remember { FocusRequester() }
    
    // Auto-focus the text field when dialog opens
    LaunchedEffect(isVisible) {
        if (isVisible) {
            focusRequester.requestFocus()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Edit Todo" else "Add New Todo",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = todoText,
                    onValueChange = onTextChange,
                    label = { Text("Todo description") },
                    placeholder = { Text("Enter your todo item...") },
                    isError = errorMessage != null,
                    enabled = !isLoading,
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (todoText.isNotBlank()) {
                                onConfirm()
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .semantics {
                            contentDescription = "Enter todo description. Current text: ${todoText.ifEmpty { "empty" }}"
                        },
                    shape = MaterialTheme.shapes.small
                )
                
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                
                // Character count indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${todoText.length}/$maxLength",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (todoText.length > maxLength * 0.8) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Character count: ${todoText.length} of $maxLength characters used"
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                
                // Category selector
                CategorySelector(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategoryChange
                )
                
                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics {
                            contentDescription = "Error: $errorMessage"
                        }
                    )
                }
                
                // Loading indicator
                if (isLoading) {
                    Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Saving todo, please wait"
                            },
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading && todoText.isNotBlank(),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.semantics {
                    contentDescription = if (todoText.isNotBlank()) {
                        if (isEditMode) "Save todo button, enabled" else "Add todo button, enabled"
                    } else {
                        if (isEditMode) "Save todo button, disabled because text is empty" else "Add todo button, disabled because text is empty"
                    }
                }
            ) {
                Text(
                    text = if (isEditMode) "Save" else "Add",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.semantics {
                    contentDescription = if (isEditMode) "Cancel editing todo" else "Cancel adding todo"
                }
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        shape = MaterialTheme.shapes.large
    )
}

@Preview
@Composable
private fun AddTodoDialogPreview() {
    TodoTheme {
        AddTodoDialog(
            isVisible = true,
            todoText = "Sample todo text",
            selectedCategory = TodoCategory.PERSONAL,
            errorMessage = null,
            isLoading = false,
            onTextChange = {},
            onCategoryChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun AddTodoDialogWithErrorPreview() {
    TodoTheme {
        AddTodoDialog(
            isVisible = true,
            todoText = "",
            selectedCategory = TodoCategory.WORK,
            errorMessage = "Todo text cannot be empty",
            isLoading = false,
            onTextChange = {},
            onCategoryChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun AddTodoDialogLoadingPreview() {
    TodoTheme {
        AddTodoDialog(
            isVisible = true,
            todoText = "Adding this todo...",
            selectedCategory = TodoCategory.SHOPPING,
            errorMessage = null,
            isLoading = true,
            onTextChange = {},
            onCategoryChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}