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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canme.todo.ui.theme.TodoTheme

/**
 * Dialog for editing todo text content.
 * 
 * @param isVisible Whether the dialog should be shown
 * @param currentText The current text of the todo being edited
 * @param errorMessage Optional error message to display
 * @param isLoading Whether a save operation is in progress
 * @param onTextChange Callback when the text changes
 * @param onConfirm Callback when the user confirms the edit
 * @param onDismiss Callback when the dialog should be dismissed
 */
@Composable
fun EditTodoDialog(
    isVisible: Boolean,
    currentText: String,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        val focusRequester = remember { FocusRequester() }
        
        AlertDialog(
            onDismissRequest = { if (!isLoading) onDismiss() },
            title = {
                Text(
                    text = "Edit Todo",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentText,
                        onValueChange = onTextChange,
                        label = { Text("Todo text") },
                        placeholder = { Text("Enter todo text...") },
                        isError = errorMessage != null,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { 
                                if (currentText.isNotBlank() && !isLoading) {
                                    onConfirm()
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .semantics {
                                contentDescription = "Todo text input field"
                            }
                    )
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    TextButton(
                        onClick = onConfirm,
                        enabled = currentText.isNotBlank() && !isLoading
                    ) {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("Cancel")
                }
            }
        )
        
        LaunchedEffect(isVisible) {
            if (isVisible) {
                focusRequester.requestFocus()
            }
        }
    }
}

@Preview
@Composable
private fun EditTodoDialogPreview() {
    TodoTheme {
        EditTodoDialog(
            isVisible = true,
            currentText = "Sample todo text to edit",
            onTextChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun EditTodoDialogErrorPreview() {
    TodoTheme {
        EditTodoDialog(
            isVisible = true,
            currentText = "",
            errorMessage = "Todo text cannot be empty",
            onTextChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun EditTodoDialogLoadingPreview() {
    TodoTheme {
        EditTodoDialog(
            isVisible = true,
            currentText = "Updated todo text",
            isLoading = true,
            onTextChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}