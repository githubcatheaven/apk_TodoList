package com.canme.todo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canme.todo.data.Todo
import com.canme.todo.data.TodoCategory
import com.canme.todo.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing todo-related UI state and business logic.
 * 
 * This ViewModel handles all todo operations including adding, updating,
 * deleting, and toggling completion status. It maintains the UI state
 * and provides reactive data streams for the UI to observe.
 * 
 * @property repository The TodoRepository for data operations
 */
@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {
    
    // UI State for the add todo dialog and category filtering
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
    
    // Category filter state
    private val _selectedCategoryFilter = MutableStateFlow<TodoCategory?>(null)
    val selectedCategoryFilter: StateFlow<TodoCategory?> = _selectedCategoryFilter.asStateFlow()
    
    // Filtered todos based on category selection
    val todos = _selectedCategoryFilter.flatMapLatest { categoryFilter ->
        if (categoryFilter == null) {
            repository.getAllTodos()
        } else {
            repository.getTodosByCategory(categoryFilter)
        }
    }
    
    // Combined state for easier UI consumption
    val combinedState = combine(todos, _uiState, _selectedCategoryFilter) { todoList, uiState, categoryFilter ->
        CombinedTodoState(
            todos = todoList,
            uiState = uiState,
            selectedCategoryFilter = categoryFilter
        )
    }
    
    /**
     * Show the add todo dialog.
     */
    fun showAddTodoDialog() {
        _uiState.value = _uiState.value.copy(
            isAddingTodo = true,
            newTodoText = "",
            selectedCategory = TodoCategory.PERSONAL,
            errorMessage = null
        )
    }
    
    /**
     * Hide the add todo dialog.
     */
    fun hideAddTodoDialog() {
        _uiState.value = _uiState.value.copy(
            isAddingTodo = false,
            newTodoText = "",
            selectedCategory = TodoCategory.PERSONAL,
            errorMessage = null
        )
    }
    
    /**
     * Show the edit todo dialog using the same AddTodoDialog component.
     */
    fun showEditTodoDialog(todo: Todo) {
        _uiState.value = _uiState.value.copy(
            isAddingTodo = true, // Reuse the same dialog state
            isEditMode = true,
            todoToEdit = todo,
            newTodoText = todo.text, // Pre-fill with current text
            selectedCategory = todo.category, // Pre-fill with current category
            errorMessage = null
        )
    }
    
    /**
     * Hide the edit todo dialog.
     */
    fun hideEditTodoDialog() {
        _uiState.value = _uiState.value.copy(
            isAddingTodo = false,
            isEditMode = false,
            todoToEdit = null,
            newTodoText = "",
            selectedCategory = TodoCategory.PERSONAL,
            errorMessage = null
        )
    }
    
    /**
     * Update the text in the new todo input field.
     * 
     * @param text The new text value
     */
    fun updateNewTodoText(text: String) {
        _uiState.value = _uiState.value.copy(
            newTodoText = text,
            errorMessage = null
        )
    }
    
    /**
     * Update the text in the edit todo dialog.
     * This now reuses the same newTodoText field.
     * 
     * @param text The new text value
     */
    fun updateEditingTodoText(text: String) {
        updateNewTodoText(text) // Reuse the same method
    }
    
    /**
     * Update the selected category for the new todo.
     * 
     * @param category The selected category
     */
    fun updateSelectedCategory(category: TodoCategory) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category
        )
    }
    
    /**
     * Set the category filter for displaying todos.
     * 
     * @param category The category to filter by, null for all todos
     */
    fun setCategoryFilter(category: TodoCategory?) {
        // Add loading state for category filtering
        _uiState.value = _uiState.value.copy(isLoading = true)
        _selectedCategoryFilter.value = category
        
        // Clear loading state after a brief delay to show visual feedback
        viewModelScope.launch {
            kotlinx.coroutines.delay(100) // Brief delay for visual feedback
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    /**
     * Add a new todo item or save edited todo.
     */
    fun addTodo() {
        if (_uiState.value.isEditMode) {
            saveEditedTodo()
        } else {
            addNewTodo()
        }
    }
    
    /**
     * Add a new todo item.
     */
    private fun addNewTodo() {
        val currentText = _uiState.value.newTodoText
        val selectedCategory = _uiState.value.selectedCategory
        
        if (!repository.isValidTodoText(currentText)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Todo text cannot be empty"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                repository.insertTodo(currentText, selectedCategory)
                _uiState.value = _uiState.value.copy(
                    isAddingTodo = false,
                    isEditMode = false,
                    newTodoText = "",
                    selectedCategory = TodoCategory.PERSONAL,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to add todo. Tap to retry.",
                    lastFailedOperation = { addNewTodo() }
                )
            }
        }
    }
    
    /**
     * Save the edited todo text and category.
     */
    private fun saveEditedTodo() {
        val todoToEdit = _uiState.value.todoToEdit ?: return
        val newText = _uiState.value.newTodoText
        val newCategory = _uiState.value.selectedCategory
        
        if (!repository.isValidTodoText(newText)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Todo text cannot be empty"
            )
            return
        }
        
        if (newText == todoToEdit.text && newCategory == todoToEdit.category) {
            // No changes, just close dialog
            hideEditTodoDialog()
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                // Update both text and category if they changed
                val updatedTodo = todoToEdit.copy(text = newText, category = newCategory)
                repository.updateTodo(updatedTodo)
                
                _uiState.value = _uiState.value.copy(
                    isAddingTodo = false,
                    isEditMode = false,
                    todoToEdit = null,
                    newTodoText = "",
                    selectedCategory = TodoCategory.PERSONAL,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to save changes. Please try again.",
                    lastFailedOperation = { saveEditedTodo() }
                )
            }
        }
    }
    
    /**
     * Toggle the completion status of a todo.
     * 
     * @param todo The todo to toggle
     */
    fun toggleTodoCompletion(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.toggleTodoCompletion(todo)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update todo. Tap to retry.",
                    lastFailedOperation = { toggleTodoCompletion(todo) }
                )
            }
        }
    }
    
    /**
     * Show the delete confirmation dialog for a todo.
     * 
     * @param todo The todo to delete
     */
    fun showDeleteDialog(todo: Todo) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            todoToDelete = todo
        )
    }
    
    /**
     * Hide the delete confirmation dialog.
     */
    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            todoToDelete = null
        )
    }
    
    /**
     * Delete the currently selected todo.
     */
    fun deleteTodo() {
        val todoToDelete = _uiState.value.todoToDelete ?: return
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                repository.deleteTodo(todoToDelete)
                _uiState.value = _uiState.value.copy(
                    showDeleteDialog = false,
                    todoToDelete = null,
                    isLoading = false,
                    deletedTodo = todoToDelete,
                    showUndoMessage = "Todo deleted"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to delete todo. Tap to retry.",
                    lastFailedOperation = { deleteTodo() }
                )
            }
        }
    }
    
    /**
     * Delete a todo directly without confirmation dialog (for swipe actions).
     */
    fun deleteTodoDirectly(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.deleteTodo(todo)
                _uiState.value = _uiState.value.copy(
                    deletedTodo = todo,
                    showUndoMessage = "Todo deleted"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete todo. Tap to retry.",
                    lastFailedOperation = { deleteTodoDirectly(todo) }
                )
            }
        }
    }
    
    /**
     * Undo the last delete operation.
     */
    fun undoDelete() {
        val deletedTodo = _uiState.value.deletedTodo ?: return
        
        viewModelScope.launch {
            try {
                repository.insertTodo(deletedTodo.text, deletedTodo.category)
                _uiState.value = _uiState.value.copy(
                    deletedTodo = null,
                    showUndoMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to restore todo: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear the undo message and deleted todo state.
     */
    fun clearUndoState() {
        _uiState.value = _uiState.value.copy(
            deletedTodo = null,
            showUndoMessage = null
        )
    }
    
    /**
     * Show the category selection dialog for a todo.
     */
    fun showCategoryDialog(todo: Todo) {
        _uiState.value = _uiState.value.copy(
            showCategoryDialog = true,
            todoToEditCategory = todo
        )
    }
    
    /**
     * Hide the category selection dialog.
     */
    fun hideCategoryDialog() {
        _uiState.value = _uiState.value.copy(
            showCategoryDialog = false,
            todoToEditCategory = null
        )
    }
    
    /**
     * Update the category of a todo.
     */
    fun updateTodoCategory(newCategory: TodoCategory) {
        val todoToUpdate = _uiState.value.todoToEditCategory ?: return
        
        viewModelScope.launch {
            try {
                repository.updateTodoCategory(todoToUpdate, newCategory)
                _uiState.value = _uiState.value.copy(
                    showCategoryDialog = false,
                    todoToEditCategory = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update category: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            lastFailedOperation = null
        )
    }
    
    /**
     * Retry the last failed operation.
     */
    fun retryLastOperation() {
        _uiState.value.lastFailedOperation?.invoke()
        _uiState.value = _uiState.value.copy(lastFailedOperation = null)
    }
    
    /**
     * Delete all completed todos.
     */
    fun deleteCompletedTodos() {
        viewModelScope.launch {
            try {
                repository.deleteCompletedTodos()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete completed todos: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Reorder todos by moving an item from one position to another.
     * This method will be called with the current list state from the UI.
     * 
     * @param currentTodos The current list of todos from the UI
     * @param fromIndex The original index of the item
     * @param toIndex The target index for the item
     */
    fun reorderTodos(currentTodos: List<Todo>, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                // Validate indices
                if (fromIndex < 0 || toIndex < 0 || 
                    fromIndex >= currentTodos.size || toIndex >= currentTodos.size ||
                    fromIndex == toIndex) {
                    return@launch
                }
                
                // Show loading state for reordering
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Create a mutable copy and perform the reorder
                val reorderedTodos = currentTodos.toMutableList()
                val movedItem = reorderedTodos.removeAt(fromIndex)
                reorderedTodos.add(toIndex, movedItem)
                
                // Update sort orders and persist to database
                repository.reorderTodos(reorderedTodos)
                
                // Clear loading state
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to reorder todos. Tap to retry.",
                    lastFailedOperation = { reorderTodos(currentTodos, fromIndex, toIndex) }
                )
            }
        }
    }
}

/**
 * UI state for todo-related screens.
 */
data class TodoUiState(
    val isAddingTodo: Boolean = false,
    val newTodoText: String = "",
    val selectedCategory: TodoCategory = TodoCategory.PERSONAL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false,
    val todoToDelete: Todo? = null,
    val lastFailedOperation: (() -> Unit)? = null, // For retry functionality
    val deletedTodo: Todo? = null, // For undo functionality
    val showUndoMessage: String? = null, // Message to show with undo option
    val showCategoryDialog: Boolean = false, // For category editing
    val todoToEditCategory: Todo? = null, // Todo whose category is being edited
    val isEditMode: Boolean = false, // Whether the AddTodoDialog is in edit mode
    val todoToEdit: Todo? = null, // Todo being edited
    // Legacy fields for backward compatibility (can be removed later)
    val showEditDialog: Boolean = false, // For text editing
    val editingTodoText: String = "", // Text being edited
    val editDialogErrorMessage: String? = null // Error message for edit dialog
)

/**
 * Combined state for easier UI consumption.
 */
data class CombinedTodoState(
    val todos: List<Todo>,
    val uiState: TodoUiState,
    val selectedCategoryFilter: TodoCategory? = null
)