package com.canme.todo.ui.components


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.canme.todo.data.Todo
import kotlin.math.roundToInt

/**
 * State class for managing simple drag and drop operations without animations.
 * No flash screen, no animations, just direct reordering.
 */
class DragDropState(
    private val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    var draggedDistance by mutableFloatStateOf(0f)
    var draggingItemIndex by mutableIntStateOf(-1)
    
    // 简单的拖拽阈值
    private val dragThreshold = 20f
    
    internal val isItemDragging: Boolean
        get() = draggingItemIndex != -1
    
    internal fun onDragStart(index: Int) {
        draggingItemIndex = index
        draggedDistance = 0f
    }
    
    internal fun onDragInterrupted() {
        // 简单重置，无动画
        draggingItemIndex = -1
        draggedDistance = 0f
    }
    
    internal fun onDrag(offset: Offset) {
        draggedDistance += offset.y
        
        // 只有超过阈值才开始检测
        if (kotlin.math.abs(draggedDistance) < dragThreshold) {
            return
        }
        
        val currentItem = getCurrentItem() ?: return
        val draggedItemCenter = currentItem.offset + currentItem.size / 2 + draggedDistance
        
        // 简单的目标检测
        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
        val targetItem = visibleItems.find { item ->
            item.index != draggingItemIndex && 
            draggedItemCenter >= item.offset && 
            draggedItemCenter <= item.offset + item.size
        }
        
        targetItem?.let { target ->
            val newIndex = target.index
            if (newIndex != draggingItemIndex) {
                // 直接交换，无动画
                onMove(draggingItemIndex, newIndex)
                
                // 调整拖拽距离
                val adjustment = if (newIndex > draggingItemIndex) {
                    -currentItem.size.toFloat()
                } else {
                    currentItem.size.toFloat()
                }
                
                draggedDistance += adjustment
                draggingItemIndex = newIndex
            }
        }
    }
    
    private fun getCurrentItem(): LazyListItemInfo? {
        return lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == draggingItemIndex }
    }
    
    fun checkForOverScroll(): Float {
        return when {
            draggedDistance > 0 -> (draggedDistance / 10).coerceAtMost(
                with(lazyListState.layoutInfo) {
                    (viewportEndOffset - viewportStartOffset) / 2f
                }
            )
            draggedDistance < 0 -> (draggedDistance / 10).coerceAtLeast(
                -with(lazyListState.layoutInfo) {
                    (viewportEndOffset - viewportStartOffset) / 2f
                }
            )
            else -> 0f
        }
    }
}

/**
 * Remembers and creates a DragDropState for managing drag and drop operations.
 */
@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit
): DragDropState {
    return remember(lazyListState) {
        DragDropState(
            lazyListState = lazyListState,
            onMove = onMove
        )
    }
}

/**
 * A simple draggable LazyColumn with no animations and no flash screen.
 * 
 * Features:
 * - Direct position swapping when dragged item overlaps target
 * - No animations, no visual effects, no flash screen
 * - Simple drag threshold to prevent accidental reordering
 * - Minimal visual feedback (dragged item follows finger)
 * - Optimized for performance and simplicity
 * 
 * Behavior:
 * 1. User starts dragging an item (long press drag handle)
 * 2. Item follows finger movement with simple offset
 * 3. When item center overlaps target item, positions swap immediately
 * 4. No animations or transitions, just direct data reordering
 * 5. On release, dragged item stays in final position
 * 
 * @param items List of items to display
 * @param onMove Callback when items are reordered (fromIndex, toIndex)
 * @param modifier Optional modifier for styling
 * @param contentPadding Padding for the list content
 * @param verticalArrangement Vertical arrangement of items
 * @param itemContent Composable content for each item
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> DraggableList(
    items: List<T>,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    itemContent: @Composable (index: Int, item: T, isDragging: Boolean, onDragStart: (Offset) -> Unit, onDrag: (Offset) -> Unit, onDragEnd: () -> Unit) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val dragDropState = rememberDragDropState(lazyListState, onMove)
    
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> 
                // Use a stable key for better performance with large lists
                when (item) {
                    is Todo -> item.id
                    else -> item.hashCode()
                }
            }
        ) { index, item ->
            val isDragging = index == dragDropState.draggingItemIndex
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isDragging) {
                            // 拖拽项目：只有位移和层级，无动画
                            Modifier
                                .zIndex(1f)
                                .offset { IntOffset(0, dragDropState.draggedDistance.roundToInt()) }
                        } else {
                            // 普通项目：无任何效果
                            Modifier
                        }
                    )
                    .semantics {
                        // Add accessibility actions for reordering
                        customActions = listOf(
                            CustomAccessibilityAction(
                                label = "Move up",
                                action = {
                                    if (index > 0) {
                                        onMove(index, index - 1)
                                        true
                                    } else false
                                }
                            ),
                            CustomAccessibilityAction(
                                label = "Move down",
                                action = {
                                    if (index < items.size - 1) {
                                        onMove(index, index + 1)
                                        true
                                    } else false
                                }
                            )
                        )
                    }
            ) {
                itemContent(
                    index, 
                    item, 
                    isDragging,
                    onDragStart = { dragDropState.onDragStart(index) },
                    onDrag = { offset -> dragDropState.onDrag(offset) },
                    onDragEnd = { dragDropState.onDragInterrupted() }
                )
            }
        }
    }
}

