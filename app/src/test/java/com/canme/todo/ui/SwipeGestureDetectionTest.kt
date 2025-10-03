package com.canme.todo.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import org.junit.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for swipe gesture detection logic and thresholds.
 * Tests the mathematical calculations and decision logic for swipe gestures.
 */
class SwipeGestureDetectionTest {

    // Swipe threshold constants (these would typically be defined in the actual implementation)
    private val swipeThreshold = 120f // Minimum distance for a swipe to be recognized
    private val velocityThreshold = 300f // Minimum velocity for a swipe
    private val maxVerticalDeviation = 50f // Maximum vertical movement allowed for horizontal swipe

    /**
     * Helper class to simulate swipe gesture detection logic
     */
    class SwipeGestureDetector {
        fun detectSwipeDirection(
            startOffset: Offset,
            endOffset: Offset,
            velocity: Velocity
        ): SwipeDirection {
            val deltaX = endOffset.x - startOffset.x
            val deltaY = endOffset.y - startOffset.y
            val distance = abs(deltaX)
            val verticalDeviation = abs(deltaY)
            
            // Check if movement meets minimum requirements
            if (distance < swipeThreshold) return SwipeDirection.NONE
            if (verticalDeviation > maxVerticalDeviation) return SwipeDirection.NONE
            if (abs(velocity.x) < velocityThreshold) return SwipeDirection.NONE
            
            return when {
                deltaX > 0 -> SwipeDirection.RIGHT
                deltaX < 0 -> SwipeDirection.LEFT
                else -> SwipeDirection.NONE
            }
        }
        
        fun calculateSwipeProgress(
            startOffset: Offset,
            currentOffset: Offset,
            maxSwipeDistance: Float = 200f
        ): Float {
            val deltaX = currentOffset.x - startOffset.x
            val progress = abs(deltaX) / maxSwipeDistance
            return progress.coerceIn(0f, 1f)
        }
        
        fun isSwipeThresholdReached(
            startOffset: Offset,
            currentOffset: Offset
        ): Boolean {
            val deltaX = abs(currentOffset.x - startOffset.x)
            return deltaX >= swipeThreshold
        }
        
        fun shouldTriggerHapticFeedback(
            startOffset: Offset,
            currentOffset: Offset,
            previousOffset: Offset
        ): Boolean {
            val currentDistance = abs(currentOffset.x - startOffset.x)
            val previousDistance = abs(previousOffset.x - startOffset.x)
            
            // Trigger haptic feedback when crossing threshold
            return previousDistance < swipeThreshold && currentDistance >= swipeThreshold
        }
    }

    enum class SwipeDirection {
        NONE, LEFT, RIGHT
    }

    private val detector = SwipeGestureDetector()

    // Basic swipe direction detection tests
    @Test
    fun `detectSwipeDirection returns RIGHT for rightward swipe above threshold`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val endOffset = Offset(250f, 105f) // 150px right, 5px down
        val velocity = Velocity(400f, 0f)

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.RIGHT, direction)
    }

    @Test
    fun `detectSwipeDirection returns LEFT for leftward swipe above threshold`() {
        // Given
        val startOffset = Offset(200f, 100f)
        val endOffset = Offset(50f, 95f) // 150px left, 5px up
        val velocity = Velocity(-400f, 0f)

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.LEFT, direction)
    }

    @Test
    fun `detectSwipeDirection returns NONE for swipe below distance threshold`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val endOffset = Offset(150f, 100f) // Only 50px right (below 120px threshold)
        val velocity = Velocity(400f, 0f)

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.NONE, direction)
    }

    @Test
    fun `detectSwipeDirection returns NONE for swipe below velocity threshold`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val endOffset = Offset(250f, 100f) // 150px right (above distance threshold)
        val velocity = Velocity(200f, 0f) // Below 300f velocity threshold

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.NONE, direction)
    }

    @Test
    fun `detectSwipeDirection returns NONE for excessive vertical movement`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val endOffset = Offset(250f, 200f) // 150px right, 100px down (exceeds 50px vertical limit)
        val velocity = Velocity(400f, 0f)

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.NONE, direction)
    }

    // Swipe progress calculation tests
    @Test
    fun `calculateSwipeProgress returns 0 for no movement`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val currentOffset = Offset(100f, 100f)

        // When
        val progress = detector.calculateSwipeProgress(startOffset, currentOffset)

        // Then
        assertEquals(0f, progress)
    }

    @Test
    fun `calculateSwipeProgress returns 0_5 for half max distance`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val currentOffset = Offset(200f, 100f) // 100px right (half of 200px max)

        // When
        val progress = detector.calculateSwipeProgress(startOffset, currentOffset, 200f)

        // Then
        assertEquals(0.5f, progress)
    }

    @Test
    fun `calculateSwipeProgress returns 1 for max distance or beyond`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val currentOffset = Offset(350f, 100f) // 250px right (exceeds 200px max)

        // When
        val progress = detector.calculateSwipeProgress(startOffset, currentOffset, 200f)

        // Then
        assertEquals(1f, progress)
    }

    @Test
    fun `calculateSwipeProgress works for leftward movement`() {
        // Given
        val startOffset = Offset(200f, 100f)
        val currentOffset = Offset(100f, 100f) // 100px left

        // When
        val progress = detector.calculateSwipeProgress(startOffset, currentOffset, 200f)

        // Then
        assertEquals(0.5f, progress)
    }

    // Threshold detection tests
    @Test
    fun `isSwipeThresholdReached returns false below threshold`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val currentOffset = Offset(180f, 100f) // 80px right (below 120px threshold)

        // When
        val thresholdReached = detector.isSwipeThresholdReached(startOffset, currentOffset)

        // Then
        assertFalse(thresholdReached)
    }

    @Test
    fun `isSwipeThresholdReached returns true at threshold`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val currentOffset = Offset(220f, 100f) // 120px right (exactly at threshold)

        // When
        val thresholdReached = detector.isSwipeThresholdReached(startOffset, currentOffset)

        // Then
        assertTrue(thresholdReached)
    }

    @Test
    fun `isSwipeThresholdReached returns true above threshold`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val currentOffset = Offset(250f, 100f) // 150px right (above threshold)

        // When
        val thresholdReached = detector.isSwipeThresholdReached(startOffset, currentOffset)

        // Then
        assertTrue(thresholdReached)
    }

    @Test
    fun `isSwipeThresholdReached works for leftward movement`() {
        // Given
        val startOffset = Offset(200f, 100f)
        val currentOffset = Offset(70f, 100f) // 130px left (above threshold)

        // When
        val thresholdReached = detector.isSwipeThresholdReached(startOffset, currentOffset)

        // Then
        assertTrue(thresholdReached)
    }

    // Haptic feedback trigger tests
    @Test
    fun `shouldTriggerHapticFeedback returns true when crossing threshold`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val previousOffset = Offset(210f, 100f) // 110px right (below threshold)
        val currentOffset = Offset(230f, 100f) // 130px right (above threshold)

        // When
        val shouldTrigger = detector.shouldTriggerHapticFeedback(startOffset, currentOffset, previousOffset)

        // Then
        assertTrue(shouldTrigger)
    }

    @Test
    fun `shouldTriggerHapticFeedback returns false when staying below threshold`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val previousOffset = Offset(190f, 100f) // 90px right (below threshold)
        val currentOffset = Offset(200f, 100f) // 100px right (still below threshold)

        // When
        val shouldTrigger = detector.shouldTriggerHapticFeedback(startOffset, currentOffset, previousOffset)

        // Then
        assertFalse(shouldTrigger)
    }

    @Test
    fun `shouldTriggerHapticFeedback returns false when staying above threshold`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val previousOffset = Offset(230f, 100f) // 130px right (above threshold)
        val currentOffset = Offset(250f, 100f) // 150px right (still above threshold)

        // When
        val shouldTrigger = detector.shouldTriggerHapticFeedback(startOffset, currentOffset, previousOffset)

        // Then
        assertFalse(shouldTrigger)
    }

    // Edge case tests
    @Test
    fun `detectSwipeDirection handles zero velocity`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val endOffset = Offset(250f, 100f) // 150px right
        val velocity = Velocity(0f, 0f) // Zero velocity

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.NONE, direction)
    }

    @Test
    fun `detectSwipeDirection handles negative velocity for right swipe`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val endOffset = Offset(250f, 100f) // 150px right
        val velocity = Velocity(-400f, 0f) // Negative velocity (inconsistent with movement)

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.NONE, direction)
    }

    @Test
    fun `detectSwipeDirection handles exact threshold values`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val endOffset = Offset(220f, 150f) // Exactly 120px right, exactly 50px down
        val velocity = Velocity(300f, 0f) // Exactly at velocity threshold

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.RIGHT, direction)
    }

    @Test
    fun `calculateSwipeProgress handles zero max distance`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val currentOffset = Offset(150f, 100f)

        // When
        val progress = detector.calculateSwipeProgress(startOffset, currentOffset, 0f)

        // Then
        // Should handle division by zero gracefully
        assertTrue(progress.isInfinite() || progress.isNaN() || progress == 1f)
    }

    // Complex gesture scenarios
    @Test
    fun `detectSwipeDirection handles diagonal movement within tolerance`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val endOffset = Offset(250f, 140f) // 150px right, 40px down (within 50px tolerance)
        val velocity = Velocity(400f, 100f)

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.RIGHT, direction)
    }

    @Test
    fun `detectSwipeDirection prioritizes horizontal movement over vertical`() {
        // Given
        val startOffset = Offset(100f, 100f)
        val endOffset = Offset(250f, 130f) // 150px right, 30px down
        val velocity = Velocity(400f, 200f) // Strong horizontal, moderate vertical velocity

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.RIGHT, direction)
    }

    @Test
    fun `swipe detection works with floating point precision`() {
        // Given
        val startOffset = Offset(100.5f, 100.3f)
        val endOffset = Offset(220.7f, 149.8f) // ~120.2px right, ~49.5px down
        val velocity = Velocity(300.1f, 0f)

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.RIGHT, direction)
    }

    // Performance and boundary tests
    @Test
    fun `swipe calculations handle large coordinate values`() {
        // Given
        val startOffset = Offset(10000f, 10000f)
        val endOffset = Offset(10150f, 10000f) // 150px right
        val velocity = Velocity(400f, 0f)

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)
        val progress = detector.calculateSwipeProgress(startOffset, endOffset)

        // Then
        assertEquals(SwipeDirection.RIGHT, direction)
        assertTrue(progress >= 0f && progress <= 1f)
    }

    @Test
    fun `swipe calculations handle negative coordinates`() {
        // Given
        val startOffset = Offset(-100f, -100f)
        val endOffset = Offset(50f, -100f) // 150px right
        val velocity = Velocity(400f, 0f)

        // When
        val direction = detector.detectSwipeDirection(startOffset, endOffset, velocity)

        // Then
        assertEquals(SwipeDirection.RIGHT, direction)
    }
}