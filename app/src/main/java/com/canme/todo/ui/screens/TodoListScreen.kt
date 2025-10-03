package com.canme.todo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.ui.TodoViewModel
import com.canme.todo.ui.components.AddTodoDialog
import com.canme.todo.ui.components.CategoryFilterChips
import com.canme.todo.ui.components.CategorySelectionDialog
import com.canme.todo.ui.components.DeleteConfirmationDialog
import com.canme.todo.ui.components.DraggableList
import com.canme.todo.ui.components.EmptyState
import com.canme.todo.ui.components.SwipeableTodoItem
import com.canme.todo.ui.theme.Dimensions
import com.canme.todo.ui.theme.TodoTheme

/**
 * Main screen composable that displays the todo list with all integrated UI components.
 * Handles the complete todo management workflow including adding, toggling, and deleting todos.
 * 
 * @param viewModel The TodoViewModel for state management and business logic
 * @param modifier Optional modifier for styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val todos by viewModel.todos.collectAsState(initial = emptyList())
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Optimize recompositions by deriving state
    val isEmpty by remember { derivedStateOf { todos.isEmpty() } }
    val isLoadingWithData by remember { derivedStateOf { uiState.isLoading && todos.isNotEmpty() && !uiState.showEditDialog && !uiState.isAddingTodo } }
    
    // Show error messages as snackbars with retry action
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            val result = if (uiState.lastFailedOperation != null) {
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = "Retry"
                )
            } else {
                snackbarHostState.showSnackbar(message)
            }
            
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.retryLastOperation()
            } else {
                viewModel.clearError()
            }
        }
    }
    
    // Show undo message as snackbar
    LaunchedEffect(uiState.showUndoMessage) {
        uiState.showUndoMessage?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Undo"
            )
            
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearUndoState()
            }
        }
    }
    
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Todo",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddTodoDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.semantics {
                    contentDescription = "Add new todo. Opens dialog to create a new todo item."
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add todo icon"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category filter chips
            CategoryFilterChips(
                selectedCategory = selectedCategoryFilter,
                onCategorySelected = { viewModel.setCategoryFilter(it) },
                isLoading = uiState.isLoading,
                modifier = Modifier.padding(horizontal = Dimensions.screenPadding)
            )
            
            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
            
            // Show loading indicator when filtering categories or performing operations
            if (isLoadingWithData) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimensions.spaceSmall),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (isEmpty) {
                // Show empty state when no todos exist
                EmptyState(
                    onAddFirstTodo = { viewModel.showAddTodoDialog() },
                    selectedCategory = selectedCategoryFilter,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Show todo list with drag-and-drop support
                DraggableList(
                    items = todos,
                    onMove = { fromIndex, toIndex ->
                        viewModel.reorderTodos(todos, fromIndex, toIndex)
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Dimensions.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) { index, todo, isDragging, onDragStart, onDrag, onDragEnd ->
                    SwipeableTodoItem(
                        todo = todo,
                        onToggleCompletion = { viewModel.toggleTodoCompletion(it) },
                        onDeleteRequested = { viewModel.showDeleteDialog(it) },
                        onDeleteDirectly = { viewModel.deleteTodoDirectly(it) },
                        onCategoryClick = { viewModel.showCategoryDialog(it) },
                        onEditRequested = { viewModel.showEditTodoDialog(it) },
                        onDragStart = { onDragStart(Offset.Zero) },
                        onDrag = onDrag,
                        onDragEnd = onDragEnd,
                        isDragging = isDragging,
                        showDragHandle = true
                    )
                }
            }
        }
    }
    
    // Add/Edit Todo Dialog (unified)
    AddTodoDialog(
        isVisible = uiState.isAddingTodo,
        todoText = uiState.newTodoText,
        selectedCategory = uiState.selectedCategory,
        errorMessage = uiState.errorMessage,
        isLoading = uiState.isLoading,
        onTextChange = { viewModel.updateNewTodoText(it) },
        onCategoryChange = { viewModel.updateSelectedCategory(it) },
        onConfirm = { viewModel.addTodo() },
        onDismiss = { 
            if (uiState.isEditMode) {
                viewModel.hideEditTodoDialog()
            } else {
                viewModel.hideAddTodoDialog()
            }
        },
        isEditMode = uiState.isEditMode
    )
    
    // Delete Confirmation Dialog
    DeleteConfirmationDialog(
        isVisible = uiState.showDeleteDialog,
        todoToDelete = uiState.todoToDelete,
        isLoading = uiState.isLoading,
        onConfirm = { viewModel.deleteTodo() },
        onDismiss = { viewModel.hideDeleteDialog() }
    )



    // Category Selection Dialog
    CategorySelectionDialog(
        isVisible = uiState.showCategoryDialog,
        currentTodo = uiState.todoToEditCategory,
        onCategorySelected = { category ->
            viewModel.updateTodoCategory(category)
        },
        onDismiss = { viewModel.hideCategoryDialog() }
    )
}

@Preview(showBackground = true)
@Composable
private fun TodoListScreenEmptyPreview() {
    TodoTheme {
        // Preview with empty state - would need mock ViewModel
        // This is a simplified preview showing the structure
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "My Todo",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Todo"
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                EmptyState(
                    onAddFirstTodo = { },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoListScreenWithItemsPreview() {
    TodoTheme {
        // Preview with sample todos
        val sampleTodos = listOf(
            Todo(id = 1, text = "Complete project documentation", isCompleted = false, category = TodoCategory.WORK),
            Todo(id = 2, text = "Review pull requests", isCompleted = true, category = TodoCategory.WORK),
            Todo(id = 3, text = "Plan team meeting agenda", isCompleted = false, category = TodoCategory.PERSONAL)
        )
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "My Todo",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Todo"
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                DraggableList(
                    items = sampleTodos,
                    onMove = { _, _ -> },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Dimensions.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) { _, todo, isDragging, onDragStart, onDrag, onDragEnd ->
                    SwipeableTodoItem(
                        todo = todo,
                        onToggleCompletion = { },
                        onDeleteRequested = { },
                        onDeleteDirectly = { },
                        onCategoryClick = { },
                        onDragStart = { onDragStart(Offset.Zero) },
                        onDrag = onDrag,
                        onDragEnd = onDragEnd,
                        isDragging = isDragging
                    )
                }
            }
        }
    }
}
