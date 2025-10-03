package com.canme.todo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Swipe direction enum for identifying swipe actions
 */
enum class SwipeDirection {
    LEFT, RIGHT, NONE
}

/**
 * Configuration for swipe thresholds and behavior
 */
data class SwipeConfig(
    val threshold: Float = 0.5f, // 50% of width to trigger action
    val resistanceThreshold: Float = 0.75f, // Point where resistance starts
    val maxSwipeDistance: Float = 1.0f, // Maximum swipe distance as fraction of width
    val animationDurationMs: Int = 300
)

/**
 * A composable that wraps content with swipe gesture detection and background reveal animations.
 * Supports left and right swipe actions with configurable thresholds and visual feedback.
 * 
 * @param onSwipeLeft Callback when left swipe threshold is reached
 * @param onSwipeRight Callback when right swipe threshold is reached
 * @param leftBackground Composable for left swipe background (shown when swiping right)
 * @param rightBackground Composable for right swipe background (shown when swiping left)
 * @param config Swipe configuration for thresholds and behavior
 * @param modifier Optional modifier for styling
 * @param content The main content to display and make swipeable
 */
@Composable
fun SwipeableRow(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    leftBackground: @Composable BoxScope.(isActive: Boolean) -> Unit,
    rightBackground: @Composable BoxScope.(isActive: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    config: SwipeConfig = SwipeConfig(),
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    // Animation state for smooth transitions
    val offsetX = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    var currentSwipeDirection by remember { mutableStateOf(SwipeDirection.NONE) }
    
    // Track container width for threshold calculations
    var containerWidth by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .onSizeChanged { size ->
                containerWidth = size.width.toFloat()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        hasTriggeredHaptic = false
                        currentSwipeDirection = SwipeDirection.NONE
                    },
                    onDragEnd = {
                        isDragging = false
                        
                        val currentOffset = offsetX.value
                        val threshold = containerWidth * config.threshold
                        
                        // Determine if action should be triggered
                        when {
                            currentOffset > threshold -> {
                                // Right swipe (revealing left background)
                                onSwipeRight()
                            }
                            currentOffset < -threshold -> {
                                // Left swipe (revealing right background)
                                onSwipeLeft()
                            }
                        }
                        
                        // Reset position with animation
                        coroutineScope.launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(config.animationDurationMs)
                            )
                        }
                        
                        hasTriggeredHaptic = false
                        currentSwipeDirection = SwipeDirection.NONE
                    },
                    onDrag = { _, dragAmount ->
                        // Only handle horizontal drag when it's clearly horizontal movement
                        if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y) * 2) {
                            val newOffset = offsetX.value + dragAmount.x
                        val maxDistance = containerWidth * config.maxSwipeDistance
                        val resistancePoint = containerWidth * config.resistanceThreshold
                        
                        // Apply resistance when approaching max distance
                        val constrainedOffset = when {
                            newOffset > 0 -> {
                                if (newOffset > resistancePoint) {
                                    val excess = newOffset - resistancePoint
                                    val resistance = excess * 0.3f // 30% resistance
                                    (resistancePoint + resistance).coerceAtMost(maxDistance)
                                } else {
                                    newOffset.coerceAtMost(maxDistance)
                                }
                            }
                            newOffset < 0 -> {
                                if (newOffset < -resistancePoint) {
                                    val excess = abs(newOffset) - resistancePoint
                                    val resistance = excess * 0.3f
                                    -(resistancePoint + resistance).coerceAtMost(maxDistance)
                                } else {
                                    newOffset.coerceAtLeast(-maxDistance)
                                }
                            }
                            else -> newOffset
                        }
                        
                        // Update swipe direction and trigger haptic feedback at threshold
                        val threshold = containerWidth * config.threshold
                        val newDirection = when {
                            constrainedOffset > threshold -> SwipeDirection.RIGHT
                            constrainedOffset < -threshold -> SwipeDirection.LEFT
                            else -> SwipeDirection.NONE
                        }
                        
                        if (newDirection != SwipeDirection.NONE && 
                            newDirection != currentSwipeDirection && 
                            !hasTriggeredHaptic) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            hasTriggeredHaptic = true
                            currentSwipeDirection = newDirection
                        } else if (newDirection == SwipeDirection.NONE && hasTriggeredHaptic) {
                            hasTriggeredHaptic = false
                            currentSwipeDirection = SwipeDirection.NONE
                        }
                        
                        coroutineScope.launch {
                            offsetX.snapTo(constrainedOffset)
                        }
                        }
                    }
                )
            }
    ) {

        
        // Left background (shown when swiping right)
        if (offsetX.value > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                val threshold = containerWidth * config.threshold
                val isActive = offsetX.value > threshold
                leftBackground(isActive)
            }
        }
        
        // Right background (shown when swiping left)
        if (offsetX.value < 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                val threshold = containerWidth * config.threshold
                val isActive = offsetX.value < -threshold
                rightBackground(isActive)
            }
        }
        
        // Main content with offset
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) },
            content = content
        )
    }
}