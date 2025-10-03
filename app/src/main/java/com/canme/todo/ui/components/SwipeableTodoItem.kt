package com.canme.todo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.ui.theme.TodoTheme

/**
 * Wrapper around [TodoItem] that provides accessibility actions for assistive technologies.
 * Swipe gestures can be re-enabled later without changing the item contract.
 */
@Composable
fun SwipeableTodoItem(
    todo: Todo,
    onToggleCompletion: (Todo) -> Unit,
    onDeleteRequested: (Todo) -> Unit,
    onDeleteDirectly: (Todo) -> Unit,
    onCategoryClick: (Todo) -> Unit = {},
    onEditRequested: (Todo) -> Unit = {},
    onDragStart: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    showDragHandle: Boolean = true
) {
    TodoItem(
        todo = todo,
        onToggleCompletion = onToggleCompletion,
        onLongPress = onDeleteRequested,
        onCategoryClick = onCategoryClick,
        onEditRequested = onEditRequested,
        onDragStart = onDragStart,
        onDrag = onDrag,
        onDragEnd = onDragEnd,
        isDragging = isDragging,
        showDragHandle = showDragHandle,
        modifier = modifier.semantics {
            customActions = listOf(
                CustomAccessibilityAction(
                    label = if (todo.isCompleted) "Mark as incomplete" else "Mark as complete",
                    action = {
                        onToggleCompletion(todo)
                        true
                    }
                ),
                CustomAccessibilityAction(
                    label = "Delete todo",
                    action = {
                        onDeleteRequested(todo)
                        true
                    }
                )
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SwipeableTodoItemPreview() {
    TodoTheme {
        SwipeableTodoItem(
            todo = Todo(
                id = 1,
                text = "Sample swipeable todo item",
                isCompleted = false,
                category = TodoCategory.PERSONAL
            ),
            onToggleCompletion = {},
            onDeleteRequested = {},
            onDeleteDirectly = {},
            onCategoryClick = {},
            onEditRequested = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwipeableTodoItemCompletedPreview() {
    TodoTheme {
        SwipeableTodoItem(
            todo = Todo(
                id = 2,
                text = "Completed swipeable todo item",
                isCompleted = true,
                category = TodoCategory.WORK
            ),
            onToggleCompletion = {},
            onDeleteRequested = {},
            onDeleteDirectly = {},
            onCategoryClick = {},
            onEditRequested = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwipeableTodoItemDraggingPreview() {
    TodoTheme {
        SwipeableTodoItem(
            todo = Todo(
                id = 3,
                text = "Dragging swipeable todo item",
                isCompleted = false,
                category = TodoCategory.SHOPPING
            ),
            onToggleCompletion = {},
            onDeleteRequested = {},
            onDeleteDirectly = {},
            onCategoryClick = {},
            onEditRequested = {},
            isDragging = true
        )
    }
}
