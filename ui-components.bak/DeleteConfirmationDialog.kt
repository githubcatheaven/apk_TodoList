package com.canme.todo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canme.todo.data.Todo
import com.canme.todo.ui.theme.Dimensions
import com.canme.todo.ui.theme.TodoTheme

/**
 * Dialog composable for confirming todo item deletion.
 * Provides clear messaging about the action and proper confirmation flow.
 * 
 * @param isVisible Whether the dialog should be shown
 * @param todoToDelete The todo item that will be deleted, null if no todo selected
 * @param isLoading Whether a delete operation is in progress
 * @param onConfirm Callback when the user confirms the deletion
 * @param onDismiss Callback when the dialog should be dismissed
 */
@Composable
fun DeleteConfirmationDialog(
    isVisible: Boolean,
    todoToDelete: Todo?,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible || todoToDelete == null) return
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = "Warning icon",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics {
                    contentDescription = "Warning: Destructive action"
                }
            )
        },
        title = {
            Text(
                text = "Delete Todo",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics {
                    contentDescription = "Delete todo confirmation dialog"
                }
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete this todo item?",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        contentDescription = "Confirmation question: Are you sure you want to delete this todo item?"
                    }
                )
                
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                
                // Show the todo text that will be deleted
                Text(
                    text = "\"${todoToDelete.text}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics {
                        contentDescription = "Todo to be deleted: ${todoToDelete.text}"
                    }
                )
                
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                
                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics {
                        contentDescription = "Warning: This action cannot be undone"
                    }
                )
                
                // Loading indicator
                if (isLoading) {
                    Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Deleting todo, please wait"
                            }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.semantics {
                    contentDescription = "Delete button. Warning: This will permanently delete the todo item."
                }
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error,
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
                    contentDescription = "Cancel deletion"
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
private fun DeleteConfirmationDialogPreview() {
    TodoTheme {
        DeleteConfirmationDialog(
            isVisible = true,
            todoToDelete = Todo(
                id = 1,
                text = "Sample todo item to be deleted",
                isCompleted = false
            ),
            isLoading = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun DeleteConfirmationDialogLoadingPreview() {
    TodoTheme {
        DeleteConfirmationDialog(
            isVisible = true,
            todoToDelete = Todo(
                id = 2,
                text = "Todo being deleted...",
                isCompleted = true
            ),
            isLoading = true,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun DeleteConfirmationDialogLongTextPreview() {
    TodoTheme {
        DeleteConfirmationDialog(
            isVisible = true,
            todoToDelete = Todo(
                id = 3,
                text = "This is a very long todo item text that demonstrates how the dialog handles overflow and truncation when the todo text is too long to display completely in the confirmation dialog",
                isCompleted = false
            ),
            isLoading = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}