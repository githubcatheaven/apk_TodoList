package com.canme.todo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.canme.todo.ui.theme.Dimensions
import com.canme.todo.ui.theme.TodoTheme

/**
 * Dialog composable for adding new todo items.
 * Provides text input with validation, character count, and proper keyboard handling.
 * 
 * @param isVisible Whether the dialog should be shown
 * @param todoText Current text in the input field
 * @param errorMessage Error message to display, null if no error
 * @param isLoading Whether a save operation is in progress
 * @param onTextChange Callback when the text changes
 * @param onConfirm Callback when the user confirms adding the todo
 * @param onDismiss Callback when the dialog should be dismissed
 * @param maxLength Maximum allowed character length for todo text
 */
@Composable
fun AddTodoDialog(
    isVisible: Boolean,
    todoText: String,
    errorMessage: String?,
    isLoading: Boolean,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    maxLength: Int = 500
) {
    if (!isVisible) return
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
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
                text = "Add New Todo",
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
                            keyboardController?.hide()
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
                        "Add todo button, enabled"
                    } else {
                        "Add todo button, disabled because text is empty"
                    }
                }
            ) {
                Text(
                    text = "Add",
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
                    contentDescription = "Cancel adding todo"
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
            errorMessage = null,
            isLoading = false,
            onTextChange = {},
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
            errorMessage = "Todo text cannot be empty",
            isLoading = false,
            onTextChange = {},
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
            errorMessage = null,
            isLoading = true,
            onTextChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun AddTodoDialogLongTextPreview() {
    TodoTheme {
        AddTodoDialog(
            isVisible = true,
            todoText = "This is a very long todo text that demonstrates how the character count indicator works and shows when approaching the limit",
            errorMessage = null,
            isLoading = false,
            onTextChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}