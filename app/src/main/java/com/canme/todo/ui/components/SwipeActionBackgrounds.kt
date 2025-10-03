package com.canme.todo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canme.todo.ui.theme.TodoTheme

/**
 * Background component for right swipe action (completion toggle).
 * Shows a green background with checkmark icon that scales based on swipe progress.
 * 
 * @param isActive Whether the swipe threshold has been reached
 * @param modifier Optional modifier for styling
 */
@Composable
fun CompleteActionBackground(
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1.0f,
        animationSpec = tween(150),
        label = "complete_action_scale"
    )
    
    val backgroundColor = if (isActive) {
        Color(0xFF4CAF50) // Bright green when active
    } else {
        Color(0xFF81C784) // Lighter green when inactive
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwipeActionIcon(
                icon = Icons.Default.Check,
                contentDescription = "Mark as complete",
                backgroundColor = Color.White.copy(alpha = 0.2f),
                iconColor = Color.White,
                scale = scale
            )
        }
    }
}

/**
 * Background component for right swipe action on completed todos (mark as incomplete).
 * Shows an orange background with unchecked icon that scales based on swipe progress.
 * 
 * @param isActive Whether the swipe threshold has been reached
 * @param modifier Optional modifier for styling
 */
@Composable
fun IncompleteActionBackground(
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1.0f,
        animationSpec = tween(150),
        label = "incomplete_action_scale"
    )
    
    val backgroundColor = if (isActive) {
        Color(0xFFFF9800) // Bright orange when active
    } else {
        Color(0xFFFFB74D) // Lighter orange when inactive
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwipeActionIcon(
                icon = Icons.Default.Refresh,
                contentDescription = "Mark as incomplete",
                backgroundColor = Color.White.copy(alpha = 0.2f),
                iconColor = Color.White,
                scale = scale
            )
        }
    }
}

/**
 * Background component for left swipe action (deletion).
 * Shows a red background with delete icon that scales based on swipe progress.
 * 
 * @param isActive Whether the swipe threshold has been reached
 * @param modifier Optional modifier for styling
 */
@Composable
fun DeleteActionBackground(
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1.0f,
        animationSpec = tween(150),
        label = "delete_action_scale"
    )
    
    val backgroundColor = if (isActive) {
        Color(0xFFD32F2F) // Bright red when active
    } else {
        Color(0xFFE57373) // Lighter red when inactive
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwipeActionIcon(
                icon = Icons.Default.Delete,
                contentDescription = "Delete todo",
                backgroundColor = Color.White.copy(alpha = 0.2f),
                iconColor = Color.White,
                scale = scale
            )
        }
    }
}

/**
 * Reusable icon component for swipe actions with scaling animation and background.
 * 
 * @param icon The vector icon to display
 * @param contentDescription Accessibility description for the icon
 * @param backgroundColor Background color for the icon container
 * @param iconColor Color of the icon itself
 * @param scale Scale factor for animation
 * @param modifier Optional modifier for styling
 */
@Composable
private fun SwipeActionIcon(
    icon: ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    iconColor: Color,
    scale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompleteActionBackgroundPreview() {
    TodoTheme {
        Box(
            modifier = Modifier.size(300.dp, 80.dp)
        ) {
            CompleteActionBackground(isActive = false)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompleteActionBackgroundActivePreview() {
    TodoTheme {
        Box(
            modifier = Modifier.size(300.dp, 80.dp)
        ) {
            CompleteActionBackground(isActive = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteActionBackgroundPreview() {
    TodoTheme {
        Box(
            modifier = Modifier.size(300.dp, 80.dp)
        ) {
            DeleteActionBackground(isActive = false)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteActionBackgroundActivePreview() {
    TodoTheme {
        Box(
            modifier = Modifier.size(300.dp, 80.dp)
        ) {
            DeleteActionBackground(isActive = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IncompleteActionBackgroundPreview() {
    TodoTheme {
        Box(
            modifier = Modifier.size(300.dp, 80.dp)
        ) {
            IncompleteActionBackground(isActive = false)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IncompleteActionBackgroundActivePreview() {
    TodoTheme {
        Box(
            modifier = Modifier.size(300.dp, 80.dp)
        ) {
            IncompleteActionBackground(isActive = true)
        }
    }
}