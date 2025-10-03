package com.canme.todo.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.canme.todo.data.Todo
import com.canme.todo.ui.theme.Dimensions
import com.canme.todo.ui.theme.TodoCompletedText
import com.canme.todo.ui.theme.TodoCompletedTextDark
import com.canme.todo.ui.theme.TodoTheme

/**
 * Composable that displays an individual todo item with checkbox and text.
 * Supports completion toggling via checkbox and deletion via long press.
 * 
 * @param todo The todo item to display
 * @param onToggleCompletion Callback when the completion status should be toggled
 * @param onLongPress Callback when the item is long-pressed (for deletion)
 * @param modifier Optional modifier for styling
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoItem(
    todo: Todo,
    onToggleCompletion: (Todo) -> Unit,
    onLongPress: (Todo) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* No action on regular click */ },
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress(todo)
                }
            )
            .semantics {
                contentDescription = if (todo.isCompleted) {
                    "Completed todo: ${todo.text}. Long press to delete."
                } else {
                    "Todo: ${todo.text}. Long press to delete."
                }
                stateDescription = if (todo.isCompleted) "Completed" else "Not completed"
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimensions.elevationSmall
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.listItemPadding)
                .toggleable(
                    value = todo.isCompleted,
                    onValueChange = { onToggleCompletion(todo) },
                    role = Role.Checkbox
                ),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = null, // Handled by toggleable modifier
                modifier = Modifier
                    .size(Dimensions.minTouchTarget)
                    .clearAndSetSemantics {
                        contentDescription = if (todo.isCompleted) {
                            "Mark as incomplete"
                        } else {
                            "Mark as complete"
                        }
                    }
            )
            
            Text(
                text = todo.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (todo.isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (todo.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "Todo text: ${todo.text}"
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoItemPreview() {
    TodoTheme {
        TodoItem(
            todo = Todo(
                id = 1,
                text = "Sample todo item that demonstrates the layout and styling",
                isCompleted = false
            ),
            onToggleCompletion = {},
            onLongPress = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoItemCompletedPreview() {
    TodoTheme {
        TodoItem(
            todo = Todo(
                id = 2,
                text = "Completed todo item with strikethrough text",
                isCompleted = true
            ),
            onToggleCompletion = {},
            onLongPress = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoItemLongTextPreview() {
    TodoTheme {
        TodoItem(
            todo = Todo(
                id = 3,
                text = "This is a very long todo item text that should demonstrate how the text wraps and handles overflow with ellipsis when it exceeds the maximum number of lines allowed in the layout",
                isCompleted = false
            ),
            onToggleCompletion = {},
            onLongPress = {}
        )
    }
}