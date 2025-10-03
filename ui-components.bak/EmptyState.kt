package com.canme.todo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.canme.todo.ui.theme.Dimensions
import com.canme.todo.ui.theme.TodoTheme

/**
 * Composable that displays an empty state when no todo items exist.
 * Provides appropriate messaging and a call-to-action for adding the first todo.
 * 
 * @param onAddFirstTodo Callback when the user wants to add their first todo
 * @param modifier Optional modifier for styling
 */
@Composable
fun EmptyState(
    onAddFirstTodo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Empty todo list. No todos have been created yet."
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(Dimensions.spaceExtraLarge)
        ) {
            // Icon
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = "Empty todo list illustration",
                modifier = Modifier
                    .size(80.dp)
                    .semantics {
                        contentDescription = "Checkmark icon representing completed tasks"
                    },
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(Dimensions.spaceLarge))
            
            // Title
            Text(
                text = "No todos yet",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics {
                    heading()
                    contentDescription = "No todos yet. This is the main heading for the empty state."
                }
            )
            
            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
            
            // Description
            Text(
                text = "Start organizing your tasks by adding your first todo item",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics {
                    contentDescription = "Description: Start organizing your tasks by adding your first todo item"
                }
            )
            
            Spacer(modifier = Modifier.height(Dimensions.spaceExtraLarge))
            
            // Call-to-action button
            Button(
                onClick = onAddFirstTodo,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.semantics {
                    contentDescription = "Add your first todo button. Tap to create your first todo item."
                }
            ) {
                Text(
                    text = "Add Your First Todo",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    TodoTheme {
        EmptyState(
            onAddFirstTodo = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 568)
@Composable
private fun EmptyStateSmallScreenPreview() {
    TodoTheme {
        EmptyState(
            onAddFirstTodo = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Composable
private fun EmptyStateLargeScreenPreview() {
    TodoTheme {
        EmptyState(
            onAddFirstTodo = {}
        )
    }
}