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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.canme.todo.data.TodoCategory
import com.canme.todo.ui.theme.Dimensions
import com.canme.todo.ui.theme.TodoTheme

/**
 * Composable for selecting a todo category.
 * Displays all available categories in a horizontal scrollable row with icons and labels.
 * 
 * @param selectedCategory Currently selected category
 * @param onCategorySelected Callback when a category is selected
 * @param modifier Optional modifier for styling
 */
@Composable
fun CategorySelector(
    selectedCategory: TodoCategory,
    onCategorySelected: (TodoCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(TodoCategory.values()) { category ->
                CategoryItem(
                    category = category,
                    isSelected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

/**
 * Individual category item composable.
 * 
 * @param category The category to display
 * @param isSelected Whether this category is currently selected
 * @param onClick Callback when the category is clicked
 */
@Composable
private fun CategoryItem(
    category: TodoCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(Dimensions.spaceSmall)
            .semantics {
                contentDescription = if (isSelected) {
                    "Selected category: ${category.displayName}"
                } else {
                    "Category: ${category.displayName}. Tap to select."
                }
            }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        category.color
                    } else {
                        category.color.copy(alpha = 0.2f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected) {
                    Color.White
                } else {
                    category.color
                },
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategorySelectorPreview() {
    TodoTheme {
        CategorySelector(
            selectedCategory = TodoCategory.PERSONAL,
            onCategorySelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategorySelectorWorkSelectedPreview() {
    TodoTheme {
        CategorySelector(
            selectedCategory = TodoCategory.WORK,
            onCategorySelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}