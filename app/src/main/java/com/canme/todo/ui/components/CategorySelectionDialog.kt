package com.canme.todo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.ui.theme.Dimensions
import com.canme.todo.ui.theme.TodoTheme

/**
 * Dialog for selecting a category for a todo item.
 * Shows all available categories with icons and allows selection.
 * 
 * @param isVisible Whether the dialog is visible
 * @param currentTodo The todo whose category is being edited
 * @param onCategorySelected Callback when a category is selected
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun CategorySelectionDialog(
    isVisible: Boolean,
    currentTodo: Todo?,
    onCategorySelected: (TodoCategory) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible && currentTodo != null) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.screenPadding),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = Dimensions.elevationMedium
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.dialogPadding)
                ) {
                    // Dialog title
                    Text(
                        text = "Change Category",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            contentDescription = "Change category dialog"
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                    
                    // Current todo info
                    Text(
                        text = "\"${currentTodo.text}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics {
                            contentDescription = "Todo text: ${currentTodo.text}"
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                    
                    // Category options
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                    ) {
                        items(TodoCategory.values()) { category ->
                            CategoryOption(
                                category = category,
                                isSelected = category == currentTodo.category,
                                onSelected = { onCategorySelected(category) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                    
                    // Cancel button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.semantics {
                                contentDescription = "Cancel category change"
                            }
                        ) {
                            Text(
                                text = "Cancel",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual category option in the selection dialog.
 */
@Composable
private fun CategoryOption(
    category: TodoCategory,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelected() }
            .padding(Dimensions.listItemPadding)
            .semantics {
                contentDescription = if (isSelected) {
                    "${category.displayName} category, currently selected"
                } else {
                    "${category.displayName} category. Tap to select."
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
    ) {
        // Category icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Category name
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategorySelectionDialogPreview() {
    TodoTheme {
        CategorySelectionDialog(
            isVisible = true,
            currentTodo = Todo(
                id = 1,
                text = "Sample todo for category change",
                isCompleted = false,
                category = TodoCategory.PERSONAL
            ),
            onCategorySelected = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryOptionPreview() {
    TodoTheme {
        Column {
            CategoryOption(
                category = TodoCategory.WORK,
                isSelected = true,
                onSelected = {}
            )
            CategoryOption(
                category = TodoCategory.PERSONAL,
                isSelected = false,
                onSelected = {}
            )
        }
    }
}