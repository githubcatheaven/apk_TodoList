package com.canme.todo.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.ui.theme.Dimensions
import com.canme.todo.ui.theme.TodoTheme

/**
 * Composable that displays an individual todo item with checkbox and text.
 * Supports completion toggling, category editing, text editing, and drag handle.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoItem(
    todo: Todo,
    onToggleCompletion: (Todo) -> Unit,
    onLongPress: (Todo) -> Unit,
    onCategoryClick: (Todo) -> Unit = {},
    onEditRequested: (Todo) -> Unit = {},
    onDragStart: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    showDragHandle: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (todo.isCompleted) {
                    "Completed todo: ${todo.text}. Tap to mark as incomplete. Long press to delete."
                } else {
                    "Todo: ${todo.text}. Tap to mark as complete. Long press to delete."
                }
                stateDescription = if (todo.isCompleted) "Completed" else "Not completed"
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else Dimensions.elevationSmall
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
                .padding(Dimensions.listItemPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { _ -> onToggleCompletion(todo) },
                modifier = Modifier
                    .size(Dimensions.minTouchTarget)
                    .semantics {
                        contentDescription = if (todo.isCompleted) {
                            "Mark as incomplete"
                        } else {
                            "Mark as complete"
                        }
                    }
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(todo.category.color.copy(alpha = 0.2f))
                    .clickable { onCategoryClick(todo) }
                    .semantics {
                        contentDescription = "Category: ${todo.category.displayName}. Tap to change category."
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = todo.category.icon,
                    contentDescription = null,
                    tint = todo.category.color,
                    modifier = Modifier.size(16.dp)
                )
            }

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
                    .combinedClickable(
                        onClick = { onEditRequested(todo) },
                        onLongClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress(todo)
                        }
                    )
                    .semantics {
                        contentDescription = "Todo text: ${todo.text}. Tap to edit, long press to delete."
                    }
            )

            if (showDragHandle) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drag to reorder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(24.dp)
                        .pointerInput(todo.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDragStart()
                                },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() },
                                onDrag = { _, dragAmount ->
                                    onDrag(Offset(0f, dragAmount.y))
                                }
                            )
                        }
                        .semantics {
                            contentDescription = "Drag handle for reordering todo. Long press and drag to reorder."
                        }
                )
            }
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
                isCompleted = false,
                category = TodoCategory.PERSONAL
            ),
            onToggleCompletion = {},
            onLongPress = {},
            onCategoryClick = {},
            onEditRequested = {}
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
                isCompleted = true,
                category = TodoCategory.WORK
            ),
            onToggleCompletion = {},
            onLongPress = {},
            onCategoryClick = {},
            onEditRequested = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoItemDraggingPreview() {
    TodoTheme {
        TodoItem(
            todo = Todo(
                id = 3,
                text = "Todo item being dragged with elevated shadow",
                isCompleted = false,
                category = TodoCategory.SHOPPING
            ),
            onToggleCompletion = {},
            onLongPress = {},
            onCategoryClick = {},
            onEditRequested = {},
            isDragging = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoItemLongTextPreview() {
    TodoTheme {
        TodoItem(
            todo = Todo(
                id = 4,
                text = "This is a very long todo item text that should demonstrate how the text wraps and handles overflow with ellipsis when it exceeds the maximum number of lines allowed in the layout",
                isCompleted = false,
                category = TodoCategory.HEALTH
            ),
            onToggleCompletion = {},
            onLongPress = {},
            onCategoryClick = {},
            onEditRequested = {}
        )
    }
}
