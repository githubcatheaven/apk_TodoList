 # Implementation Plan

- [x] 1. Set up Android project structure and dependencies





  - Create Android project with Kotlin and Jetpack Compose
  - Configure build.gradle files with required dependencies (Room, Hilt, Compose)
  - Set up package structure following MVVM architecture
  - Configure ProGuard rules and manifest permissions
  - _Requirements: 6.4_

- [x] 2. Implement data layer foundation




  - [x] 2.1 Create Todo data model and Room entity


    - Write Todo data class with Room annotations
    - Define primary key, table name, and column specifications
    - Add validation constraints and default values
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [x] 2.2 Implement TodoDao interface


    - Create DAO interface with CRUD operations
    - Write queries for getAllTodos, insertTodo, updateTodo, deleteTodo
    - Use Flow for reactive data updates
    - Add proper suspend functions for async operations
    - _Requirements: 1.2, 2.1, 3.4, 4.2, 5.1_

  - [x] 2.3 Set up Room database configuration


    - Create TodoDatabase abstract class with Room annotations
    - Configure database version, entities, and DAO access
    - Implement database instance creation with proper threading
    - Add database migration strategy for future updates
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 3. Create repository layer





  - [x] 3.1 Implement TodoRepository class


    - Create repository with TodoDao dependency
    - Implement methods that delegate to DAO operations
    - Add error handling for database operations
    - Use coroutines for async database access
    - _Requirements: 1.2, 2.1, 3.4, 4.2, 5.1_

- [x] 4. Set up dependency injection with Hilt




  - [x] 4.1 Configure Hilt application and modules


    - Create Application class with Hilt annotation
    - Set up DatabaseModule for Room database injection
    - Create RepositoryModule for repository injection
    - Configure proper scoping for dependencies
    - _Requirements: 6.4_

- [x] 5. Implement ViewModel layer





  - [x] 5.1 Create TodoViewModel with state management


    - Implement ViewModel with TodoRepository dependency
    - Create UI state data class for managing screen state
    - Set up StateFlow for reactive UI updates
    - Implement methods for add, update, delete, and toggle operations
    - _Requirements: 1.1, 1.2, 1.4, 2.1, 3.1, 3.2, 3.4, 4.1, 4.2_

  - [x] 5.2 Add input validation and error handling


    - Implement text validation for empty and maximum length
    - Add error state management in ViewModel
    - Create user feedback mechanisms for validation errors
    - Handle database operation exceptions gracefully
    - _Requirements: 1.3_

- [x] 6. Create Compose UI components





  - [x] 6.1 Implement TodoItem composable


    - Create composable for individual todo items
    - Add checkbox for completion status with proper state handling
    - Implement text display with strikethrough for completed items
    - Add long-press gesture detection for delete functionality
    - _Requirements: 2.3, 3.1, 3.2, 4.1, 6.1, 6.2_

  - [x] 6.2 Create AddTodoDialog composable


    - Implement modal dialog for adding new todos
    - Add text input field with validation feedback
    - Create confirm and cancel buttons with proper actions
    - Handle keyboard interactions and focus management
    - _Requirements: 1.1, 1.3, 6.1, 6.2_

  - [x] 6.3 Implement DeleteConfirmationDialog


    - Create confirmation dialog for delete operations
    - Add clear messaging about the action being performed
    - Implement confirm and cancel button functionality
    - Handle dialog state management properly
    - _Requirements: 4.1, 4.3, 6.1, 6.2_

  - [x] 6.4 Create EmptyState composable


    - Design and implement empty state UI when no todos exist
    - Add appropriate messaging and visual elements
    - Include call-to-action for adding first todo
    - Ensure proper Material Design compliance
    - _Requirements: 2.2, 6.1_

- [x] 7. Implement main screen layout





  - [x] 7.1 Create TodoListScreen composable


    - Build main screen layout with LazyColumn for todo list
    - Integrate all UI components (TodoItem, dialogs, empty state)
    - Implement FloatingActionButton for adding todos
    - Add proper state management and event handling
    - _Requirements: 2.1, 2.4, 6.1, 6.3_

  - [x] 7.2 Set up MainActivity with Compose integration


    - Configure MainActivity to host Compose UI
    - Set up theme and Material Design 3 styling
    - Integrate ViewModel with proper lifecycle management
    - Handle system UI and status bar configuration
    - _Requirements: 6.1, 6.3, 6.4_

- [x] 8. Implement core functionality integration





  - [x] 8.1 Wire up todo creation workflow


    - Connect AddTodoDialog to ViewModel add functionality
    - Implement proper state updates and UI feedback
    - Add validation error display and handling
    - Test complete add todo user flow
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [x] 8.2 Implement todo completion toggle

    - Connect TodoItem checkbox to ViewModel toggle functionality
    - Update UI state immediately for responsive feedback
    - Persist completion status changes to database
    - Add visual feedback for completion state changes
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [x] 8.3 Wire up todo deletion workflow

    - Connect long-press gesture to delete confirmation dialog
    - Implement complete deletion flow from dialog to database
    - Update UI state after successful deletion
    - Add proper error handling for deletion failures
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 9. Add comprehensive testing





  - [x] 9.1 Write unit tests for data layer


    - Create tests for Todo entity validation and behavior
    - Test TodoDao operations with in-memory database
    - Write TodoRepository tests with mocked dependencies
    - Verify proper error handling in data operations
    - _Requirements: All data persistence requirements_

  - [x] 9.2 Write ViewModel unit tests


    - Test TodoViewModel state management and business logic
    - Verify proper handling of user actions and state updates
    - Test input validation and error state management
    - Mock repository dependencies for isolated testing
    - _Requirements: 1.1, 1.2, 1.3, 3.1, 3.2, 4.1, 4.2_

  - [x] 9.3 Create Compose UI tests


    - Write tests for TodoItem interactions and state changes
    - Test dialog functionality and user input handling
    - Verify proper list display and empty state behavior
    - Test complete user workflows end-to-end
    - _Requirements: 2.1, 2.2, 2.3, 6.1, 6.2_

- [x] 10. Final integration and polish







  - [x] 10.1 Implement app theming and styling


    - Apply Material Design 3 theme throughout the app
    - Ensure consistent colors, typography, and spacing
    - Add proper dark mode support
    - Test UI on different screen sizes and orientations
    - _Requirements: 6.1, 6.3, 6.4_

  - [x] 10.2 Add accessibility features


    - Implement proper content descriptions for screen readers
    - Add semantic properties for Compose components
    - Test navigation with TalkBack and keyboard
    - Ensure proper focus management throughout the app
    - _Requirements: 6.1, 6.2_

  - [x] 10.3 Build and test APK generation



    - Configure release build with proper signing
    - Test APK installation and functionality on device
    - Verify package name (com.canme.todo) is correctly set
    - Perform final end-to-end testing on physical device
    - _Requirements: All requirements verification_

- [x] 11. Implement todo categories and filtering












  - [x] 11.1 Add category support to data layer




    - Update Todo entity to include category field and TodoCategory enum
    - Add database migration for new category column with default values
    - Update TodoDao with category-based queries and filtering methods
    - Modify TodoRepository to handle category operations
    - _Requirements: 8.1, 8.2, 8.4_

  - [x] 11.2 Create category selection UI components




    - Implement CategorySelector composable for add/edit dialogs
    - Create CategoryFilterChips for main screen filtering
    - Add category indicator to TodoItem with color and icon
    - Design category management UI with Material Design 3 styling
    - _Requirements: 8.1, 8.2, 8.3_

  - [x] 11.3 Integrate category functionality in ViewModels




    - Update TodoViewModel to handle category selection and filtering
    - Add category state management to UI state
    - Implement category filtering logic with reactive data streams
    - Update add/edit todo workflows to include category selection
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [x] 12. Implement drag-and-drop reordering








  - [x] 12.1 Add sort order support to data layer




    - Update Todo entity to include sortOrder field
    - Create database migration for sortOrder column with proper indexing
    - Add reordering methods to TodoDao with batch update support
    - Update TodoRepository with reorder functionality
    - _Requirements: 7.1, 7.2, 7.4_

  - [x] 12.2 Create draggable list UI component




    - Implement DraggableList composable with drag gesture detection
    - Add visual feedback during drag operations (elevation, scale)
    - Create drag handle UI element for each todo item
    - Handle drag state management and position calculations
    - _Requirements: 7.1, 7.3_

  - [x] 12.3 Integrate drag-and-drop in main screen




    - Replace standard LazyColumn with DraggableList in TodoListScreen
    - Update TodoViewModel to handle reorder operations
    - Implement optimistic UI updates during drag operations
    - Add persistence of new order to database after drag completion
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [x] 13. Implement swipe gestures for quick actions








  - [x] 13.1 Create swipeable row component




    - Implement SwipeableRow composable with left/right swipe detection
    - Add swipe threshold configuration and gesture recognition
    - Create background reveal animations for swipe actions
    - Handle swipe state management and reset functionality
    - _Requirements: 9.1, 9.2, 9.3_

  - [x] 13.2 Add swipe action backgrounds and icons




    - Design right-swipe background with completion styling (green, checkmark)
    - Create left-swipe background with deletion styling (red, delete icon)
    - Implement smooth reveal animations during swipe gestures
    - Add haptic feedback for swipe action thresholds
    - _Requirements: 9.1, 9.2, 9.3_

  - [x] 13.3 Integrate swipe actions in todo items




    - Wrap TodoItem components with SwipeableRow functionality
    - Connect right-swipe to todo completion toggle action
    - Connect left-swipe to delete confirmation and removal
    - Update TodoViewModel to handle swipe-triggered actions
    - _Requirements: 9.1, 9.2, 9.4_

- [x] 14. Update database schema and migrations








  - [x] 14.1 Create database migration for new fields




    - Write Room migration from version 1 to 2 for category and sortOrder fields
    - Add proper default values for existing todos (category: PERSONAL, sortOrder: createdAt)
    - Test migration with existing data to ensure no data loss
    - Update database version and migration configuration
    - _Requirements: 7.4, 8.4_

  - [x] 14.2 Update queries for new functionality




    - Modify existing queries to support ordering by sortOrder then createdAt
    - Add category filtering queries with proper indexing
    - Optimize query performance for large todo lists
    - Test query performance with sample data sets
    - _Requirements: 7.4, 8.3, 8.4_

- [x] 15. Enhanced testing for new features








  - [x] 15.1 Write tests for category functionality




    - Create unit tests for TodoCategory enum and related logic
    - Test category filtering in repository and ViewModel layers
    - Write UI tests for category selection and filtering interactions
    - Test database migration with category data
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [x] 15.2 Write tests for drag-and-drop functionality




    - Create unit tests for reordering logic in repository and ViewModel
    - Test drag gesture detection and position calculations
    - Write integration tests for drag-and-drop UI interactions
    - Test persistence of reordered todo lists
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [x] 15.3 Write tests for swipe gesture functionality




    - Create unit tests for swipe gesture detection and thresholds
    - Test swipe action triggers (complete/delete) in ViewModel
    - Write UI tests for swipe gesture interactions and animations
    - Test swipe action persistence and state updates
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [x] 16. Final integration and enhanced APK build








  - [x] 16.1 Integrate all new features in main UI




    - Combine category filtering, drag-and-drop, and swipe gestures in TodoListScreen
    - Ensure proper interaction between all features (no conflicts)
    - Update empty state to reflect category filtering
    - Test complete user workflows with all new features
    - _Requirements: 7.1, 7.2, 8.1, 8.2, 9.1, 9.2_

  - [x] 16.2 Performance optimization and polish




    - Optimize list rendering performance with large datasets
    - Add loading states for category filtering and reordering
    - Implement proper error handling for all new operations
    - Add accessibility support for new gesture interactions
    - _Requirements: 6.2, 7.3, 8.2, 9.3_

  - [x] 16.3 Build and test enhanced APK




    - Build debug and release APKs with all new features
    - Test complete functionality on physical devices
    - Verify performance with large todo lists and frequent interactions
    - Conduct final end-to-end testing of enhanced todo app
    - _Requirements: All enhanced requirements verification_

- [x] 17. Implement enhanced checkbox interaction functionality







  - [x] 17.1 Fix checkbox click handling in TodoItem component




    - Replace complex gesture detection with direct Checkbox onCheckedChange callback
    - Remove pointerInput and detectTapGestures for checkbox interaction
    - Ensure immediate visual feedback and state updates
    - Test checkbox reliability across different interaction scenarios
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [x] 18. Implement enhanced category icon interaction








  - [x] 18.1 Add clickable category icon functionality




    - Replace pointerInput with direct clickable modifier for category icon
    - Create category selection dialog that reuses existing UI patterns
    - Implement category change persistence independent of other operations
    - Test category changes don't affect completion status or text edits
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [x] 19. Implement enhanced text editing functionality











  - [x] 19.1 Add click-to-edit text functionality





    - Replace pointerInput with combinedClickable for text area
    - Reuse AddTodoDialog component for editing existing todos
    - Pre-fill dialog with current todo text and maintain category
    - Update TodoViewModel to handle edit vs add operations
    - Test text editing preserves other todo properties
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_
- [x] 20. Implement enhanced drag-and-drop with magnetic effects

- [ ] 20. Implement enhanced drag-and-drop with magnetic effects





  - [x] 20.1 Enhance drag-and-drop visual feedback and magnetic snap







    - Increase drag visual effects (1.05x scale, 12dp elevation, 0.9 alpha)
    - Implement magnetic snap effect at 50% overlap threshold
    - Add spring-based animations with medium bouncy damping
    - Optimize drag calculations for smooth 60fps performance
    - Test magnetic snap behavior and visual feedback quality
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [x] 21. Update version and build enhanced APK









  - [x] 21.1 Update app version to 1.0.2




    - Increment versionCode and update versionName in build.gradle.kts
    - Update version following 1.0.x pattern (x = 1-100)
    - Verify version changes are properly applied
    - _Requirements: Version management_

  - [x] 21.2 Build and test release APK with all enhancements




    - Build release APK with all 4 enhanced features
    - Test checkbox clicking, category icon clicking, text editing, and enhanced drag-and-drop
    - Verify all interactions work reliably and independently
    - Conduct comprehensive testing of enhanced user experience
    - _Requirements: 10.1-10.4, 11.1-11.5, 12.1-12.5, 13.1-13.5_