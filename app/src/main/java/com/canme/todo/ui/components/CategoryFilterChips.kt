package com.canme.todo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canme.todo.data.TodoCategory
import com.canme.todo.ui.theme.Dimensions
import com.canme.todo.ui.theme.TodoTheme

/**
 * Composable for filtering todos by category using filter chips.
 * Displays "All" option plus all available categories in a horizontal scrollable row.
 * 
 * @param selectedCategory Currently selected category filter, null for "All"
 * @param onCategorySelected Callback when a category filter is selected
 * @param isLoading Whether the category filtering is in progress
 * @param modifier Optional modifier for styling
 */
@Composable
fun CategoryFilterChips(
    selectedCategory: TodoCategory?,
    onCategorySelected: (TodoCategory?) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // "All" filter chip
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = {
                    Text(
                        text = "All",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.semantics {
                    contentDescription = if (selectedCategory == null) {
                        "All categories filter, currently selected"
                    } else {
                        "All categories filter. Tap to show all todos."
                    }
                }
            )
        }
        
        // Category filter chips
        items(TodoCategory.values()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (selectedCategory == category) {
                                Color.White
                            } else {
                                category.color
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.color,
                    selectedLabelColor = Color.White,
                    containerColor = category.color.copy(alpha = 0.1f),
                    labelColor = category.color
                ),
                modifier = Modifier.semantics {
                    contentDescription = if (selectedCategory == category) {
                        "${category.displayName} category filter, currently selected"
                    } else {
                        "${category.displayName} category filter. Tap to show only ${category.displayName} todos."
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryFilterChipsPreview() {
    TodoTheme {
        CategoryFilterChips(
            selectedCategory = null,
            onCategorySelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryFilterChipsWorkSelectedPreview() {
    TodoTheme {
        CategoryFilterChips(
            selectedCategory = TodoCategory.WORK,
            onCategorySelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryFilterChipsPersonalSelectedPreview() {
    TodoTheme {
        CategoryFilterChips(
            selectedCategory = TodoCategory.PERSONAL,
            onCategorySelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}