# Design Document

## Overview

The Android Todo List application will be built using modern Android development practices with Kotlin, following the MVVM (Model-View-ViewModel) architecture pattern. The app will use Jetpack Compose for the UI, Room database for local storage, and follow Material Design 3 guidelines for a consistent and intuitive user experience.

## Architecture

### MVVM Architecture Pattern
- **Model**: Data classes and repository pattern for data management
- **View**: Jetpack Compose UI components
- **ViewModel**: Business logic and state management using Android ViewModel

### Key Components
- **MainActivity**: Single activity hosting the Compose UI
- **TodoRepository**: Data access layer abstracting Room database operations
- **TodoViewModel**: Manages UI state and business logic
- **TodoDatabase**: Room database configuration
- **TodoDao**: Data Access Object for database operations

## Components and Interfaces

### Data Layer

#### Todo Entity
```kotlin
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val category: TodoCategory = TodoCategory.PERSONAL,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TodoCategory(val displayName: String, val color: Color, val icon: ImageVector) {
    PERSONAL("Personal", Color(0xFF2196F3), Icons.Default.Person),
    WORK("Work", Color(0xFF4CAF50), Icons.Default.Work),
    SHOPPING("Shopping", Color(0xFFFF9800), Icons.Default.ShoppingCart),
    HEALTH("Health", Color(0xFFE91E63), Icons.Default.FavoriteBorder),
    OTHER("Other", Color(0xFF9C27B0), Icons.Default.Category)
}
```

#### TodoDao Interface
```kotlin
@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllTodos(): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE category = :category ORDER BY sortOrder ASC, createdAt DESC")
    fun getTodosByCategory(category: TodoCategory): Flow<List<Todo>>
    
    @Insert
    suspend fun insertTodo(todo: Todo)
    
    @Update
    suspend fun updateTodo(todo: Todo)
    
    @Delete
    suspend fun deleteTodo(todo: Todo)
    
    @Query("UPDATE todos SET sortOrder = :newOrder WHERE id = :todoId")
    suspend fun updateSortOrder(todoId: Long, newOrder: Int)
    
    @Transaction
    suspend fun reorderTodos(reorderedTodos: List<Todo>) {
        reorderedTodos.forEachIndexed { index, todo ->
            updateSortOrder(todo.id, index)
        }
    }
}
```

#### TodoRepository
```kotlin
class TodoRepository(private val todoDao: TodoDao) {
    fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos()
    fun getTodosByCategory(category: TodoCategory): Flow<List<Todo>> = todoDao.getTodosByCategory(category)
    
    suspend fun insertTodo(todo: Todo) {
        val maxOrder = todoDao.getAllTodos().first().maxOfOrNull { it.sortOrder } ?: -1
        todoDao.insertTodo(todo.copy(sortOrder = maxOrder + 1))
    }
    
    suspend fun updateTodo(todo: Todo) = todoDao.updateTodo(todo)
    suspend fun deleteTodo(todo: Todo) = todoDao.deleteTodo(todo)
    suspend fun reorderTodos(reorderedTodos: List<Todo>) = todoDao.reorderTodos(reorderedTodos)
}
```

### Presentation Layer

#### TodoViewModel
```kotlin
class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
    
    val todos = repository.getAllTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
```

#### UI State Management
```kotlin
data class TodoUiState(
    val isAddingTodo: Boolean = false,
    val isEditingTodo: Boolean = false,
    val editingTodo: Todo? = null,
    val newTodoText: String = "",
    val selectedCategory: TodoCategory = TodoCategory.PERSONAL,
    val showDeleteDialog: Boolean = false,
    val showCategoryDialog: Boolean = false,
    val todoToDelete: Todo? = null,
    val todoToChangeCategory: Todo? = null,
    val filterCategory: TodoCategory? = null,
    val isDragging: Boolean = false,
    val draggedTodo: Todo? = null
)
```

### UI Components

#### Enhanced TodoItem Component
```kotlin
@Composable
fun TodoItem(
    todo: Todo,
    onToggleCompletion: (Todo) -> Unit,
    onCategoryClick: (Todo) -> Unit,
    onTextClick: (Todo) -> Unit,
    onLongPress: (Todo) -> Unit,
    modifier: Modifier = Modifier
) {
    // Enhanced checkbox with direct onCheckedChange
    Checkbox(
        checked = todo.isCompleted,
        onCheckedChange = { _ -> onToggleCompletion(todo) }
    )
    
    // Clickable category icon
    Box(
        modifier = Modifier.clickable { onCategoryClick(todo) }
    ) {
        Icon(
            imageVector = todo.category.icon,
            tint = todo.category.color
        )
    }
    
    // Clickable text with combinedClickable for edit/delete
    Text(
        text = todo.text,
        modifier = Modifier.combinedClickable(
            onClick = { onTextClick(todo) },
            onLongClick = { onLongPress(todo) }
        )
    )
}
```

#### Enhanced DraggableList Component
```kotlin
@Composable
fun DraggableList(
    items: List<Todo>,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable LazyItemScope.(index: Int, item: Todo) -> Unit
) {
    // Enhanced drag detection with magnetic snap
    val dragThreshold = 50.dp
    val magneticSnapThreshold = 0.5f // 50% overlap
    
    // Enhanced visual feedback animations
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isDragging) 12f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
}
```

#### Main Screen Composables
- **TodoListScreen**: Main container composable with enhanced drag-and-drop support
- **TodoItem**: Individual todo item with enhanced interactions:
  - Direct checkbox click handling with `onCheckedChange`
  - Clickable category icon with isolated click detection  
  - Clickable text area for editing with `combinedClickable`
  - Swipe gestures and drag handle support
- **AddTodoDialog**: Modal dialog for adding/editing todos with category selection
  - Reused for both add and edit operations
  - Pre-filled text for edit mode
  - Same validation and UI patterns
- **CategorySelectionDialog**: Dedicated dialog for changing todo categories
- **DeleteConfirmationDialog**: Confirmation dialog for deletions
- **CategoryFilterChips**: Horizontal scrollable category filter chips
- **SwipeableRow**: Wrapper composable for swipe-to-action functionality
- **DraggableList**: Enhanced LazyColumn with magnetic drag-and-drop:
  - 50% overlap threshold for magnetic snap
  - Spring-based animations with enhanced visual feedback
  - Smooth 60fps performance optimization
- **EmptyState**: Display when no todos exist

## Data Models

### Todo Data Class
- **id**: Unique identifier (auto-generated)
- **text**: Todo item description
- **isCompleted**: Completion status
- **category**: TodoCategory enum for organization
- **sortOrder**: Integer for custom user ordering
- **createdAt**: Timestamp for default ordering

### Database Schema
- Single table `todos` with Room SQLite implementation
- Automatic primary key generation
- Indexed by creation timestamp for ordering

## Interaction Patterns

### Enhanced Checkbox Interaction
- **Implementation**: Direct `onCheckedChange` callback with immediate state updates
- **Click Handling**: Replace complex gesture detection with standard Checkbox component
- **State Management**: Immediate UI state update followed by database persistence
- **Reliability**: Single-responsibility click handling to prevent interaction conflicts

### Enhanced Category Icon Interaction  
- **Implementation**: Clickable category icon with isolated click detection
- **Dialog Reuse**: Use existing category selection dialog from add/edit workflows
- **State Isolation**: Category changes independent of completion status and text edits
- **Visual Feedback**: Immediate icon and color updates upon selection

### Enhanced Text Editing
- **Implementation**: Click-to-edit using existing AddTodoDialog component
- **Dialog Reuse**: Repurpose AddTodoDialog for editing with pre-filled text
- **State Management**: Pass current todo text as initial value to dialog
- **Persistence**: Update existing todo instead of creating new one

### Enhanced Drag and Drop Reordering
- **Implementation**: Custom LazyColumn with enhanced drag detection using `detectDragGestures`
- **Magnetic Snap Effect**: 
  - Trigger threshold: 50% overlap with target item height
  - Animation: Spring-based physics with `Spring.DampingRatioMediumBouncy`
  - Calculation: Enhanced overlap detection for smoother transitions
- **Enhanced Visual Feedback**: 
  - Scale: 1.05x during drag (increased from 1.03x)
  - Elevation: 12dp shadow (increased from 8dp)
  - Alpha: 0.9 transparency (enhanced from 0.92)
  - Animation: Medium bouncy spring with medium stiffness
- **Performance**: Optimized drag calculations with smooth 60fps animations
- **Persistence**: Batch database updates for reordering operations

### Swipe Gestures
- **Right Swipe (Complete)**: 
  - Threshold: 50% of item width
  - Animation: Green background with checkmark icon
  - Action: Toggle completion status
- **Left Swipe (Delete)**:
  - Threshold: 50% of item width  
  - Animation: Red background with delete icon
  - Action: Show confirmation dialog, then delete

### Category Management
- **Visual Indicators**: Colored leading icon and subtle background tint
- **Filtering**: Chip-based filter UI with "All" option
- **Selection**: Dropdown or bottom sheet in add/edit dialogs
- **Persistence**: Enum stored as string in database

## Error Handling

### Input Validation
- Empty todo text validation with user feedback
- Maximum character limit (500 characters) with visual indicator
- Trim whitespace from input

### Database Operations
- Coroutine-based async operations with proper exception handling
- Graceful degradation if database operations fail
- User feedback for operation success/failure

### UI Error States
- Loading states during database operations
- Error messages for failed operations
- Retry mechanisms for transient failures

## Testing Strategy

### Unit Tests
- **TodoRepository**: Test all CRUD operations
- **TodoViewModel**: Test state management and business logic
- **TodoDao**: Test database queries and operations

### Integration Tests
- **Database**: Test Room database schema and migrations
- **Repository + DAO**: Test data flow between layers

### UI Tests
- **Compose UI**: Test user interactions and state changes
- **End-to-End**: Test complete user workflows
- **Accessibility**: Test screen reader compatibility and navigation

### Test Structure
```
src/test/java/com/canme/todo/
├── repository/TodoRepositoryTest.kt
├── viewmodel/TodoViewModelTest.kt
└── dao/TodoDaoTest.kt

src/androidTest/java/com/canme/todo/
├── database/TodoDatabaseTest.kt
├── ui/TodoListScreenTest.kt
└── integration/TodoAppIntegrationTest.kt
```

## Technology Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt
- **Async Operations**: Coroutines + Flow

### Android Jetpack Components
- **ViewModel**: State management and lifecycle awareness
- **Room**: Local database abstraction
- **Compose**: Modern declarative UI toolkit
- **Navigation**: Single-activity navigation (if expanded)
- **Hilt**: Dependency injection framework

### Build Configuration
- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 24 (Android 7.0)
- **Compile SDK**: 34
- **Build Tool**: Gradle with Kotlin DSL
- **Package Name**: com.canme.todo