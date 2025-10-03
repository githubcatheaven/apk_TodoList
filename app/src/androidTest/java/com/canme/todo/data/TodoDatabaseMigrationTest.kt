package com.canme.todo.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Test class for database migrations.
 * 
 * This class tests the migration from version 1 to version 2 of the TodoDatabase,
 * ensuring that existing data is preserved and new fields are properly added.
 */
@RunWith(AndroidJUnit4::class)
class TodoDatabaseMigrationTest {
    
    private val TEST_DB = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TodoDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )
    
    /**
     * Test migration from version 1 to version 2.
     * 
     * This test:
     * 1. Creates a version 1 database with sample data
     * 2. Runs the migration to version 2
     * 3. Validates that data is preserved and new fields have correct default values
     */
    @Test
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // Insert test data in version 1 format (without category and sortOrder)
            execSQL("""
                INSERT INTO todos (id, text, isCompleted, createdAt) 
                VALUES (1, 'Test Todo 1', 0, 1000)
            """)
            execSQL("""
                INSERT INTO todos (id, text, isCompleted, createdAt) 
                VALUES (2, 'Test Todo 2', 1, 2000)
            """)
            execSQL("""
                INSERT INTO todos (id, text, isCompleted, createdAt) 
                VALUES (3, 'Test Todo 3', 0, 3000)
            """)
            close()
        }
        
        // Run the migration to version 2
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, TodoDatabase.MIGRATION_1_2)
        
        // Verify the migration worked correctly
        val cursor = db.query("SELECT * FROM todos ORDER BY id")
        
        // Check first todo
        cursor.moveToFirst()
        assertEquals(1, cursor.getLong(cursor.getColumnIndexOrThrow("id")))
        assertEquals("Test Todo 1", cursor.getString(cursor.getColumnIndexOrThrow("text")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("isCompleted")))
        assertEquals(1000, cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")))
        assertEquals("PERSONAL", cursor.getString(cursor.getColumnIndexOrThrow("category")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("sortOrder")))
        
        // Check second todo
        cursor.moveToNext()
        assertEquals(2, cursor.getLong(cursor.getColumnIndexOrThrow("id")))
        assertEquals("Test Todo 2", cursor.getString(cursor.getColumnIndexOrThrow("text")))
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("isCompleted")))
        assertEquals(2000, cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")))
        assertEquals("PERSONAL", cursor.getString(cursor.getColumnIndexOrThrow("category")))
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("sortOrder")))
        
        // Check third todo
        cursor.moveToNext()
        assertEquals(3, cursor.getLong(cursor.getColumnIndexOrThrow("id")))
        assertEquals("Test Todo 3", cursor.getString(cursor.getColumnIndexOrThrow("text")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("isCompleted")))
        assertEquals(3000, cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")))
        assertEquals("PERSONAL", cursor.getString(cursor.getColumnIndexOrThrow("category")))
        assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("sortOrder")))
        
        cursor.close()
        db.close()
    }
    
    /**
     * Test that the migrated database can be used with the new Room setup.
     * 
     * This test ensures that after migration, the database works correctly
     * with the new TodoDao methods and entity structure.
     */
    @Test
    fun testMigratedDatabaseWithRoom() = runBlocking {
        // Create version 1 database with test data
        var db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL("""
                INSERT INTO todos (id, text, isCompleted, createdAt) 
                VALUES (1, 'Migrated Todo', 0, 1000)
            """)
            close()
        }
        
        // Run migration
        helper.runMigrationsAndValidate(TEST_DB, 2, true, TodoDatabase.MIGRATION_1_2)
        
        // Create Room database instance and test operations
        val roomDb = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TodoDatabase::class.java,
            TEST_DB
        )
            .addMigrations(TodoDatabase.MIGRATION_1_2)
            .build()
        
        val dao = roomDb.todoDao()
        
        // Test that we can read the migrated data
        val todos = dao.getAllTodos()
        assertNotNull(todos)
        
        // Test that we can insert new data with categories
        val newTodo = Todo(
            text = "New Todo with Category",
            category = TodoCategory.WORK,
            sortOrder = 10
        )
        dao.insertTodo(newTodo)
        
        // Test category filtering works
        val workTodos = dao.getTodosByCategory(TodoCategory.WORK)
        assertNotNull(workTodos)
        
        roomDb.close()
    }
    
    /**
     * Test migration with empty database.
     * 
     * Ensures that migration works correctly even when there's no existing data.
     */
    @Test
    fun testMigrationWithEmptyDatabase() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // Don't insert any data
            close()
        }
        
        // Run migration on empty database
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, TodoDatabase.MIGRATION_1_2)
        
        // Verify schema is correct
        val cursor = db.query("PRAGMA table_info(todos)")
        val columnNames = mutableSetOf<String>()
        
        while (cursor.moveToNext()) {
            columnNames.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
        }
        
        // Verify all expected columns exist
        assert(columnNames.contains("id"))
        assert(columnNames.contains("text"))
        assert(columnNames.contains("isCompleted"))
        assert(columnNames.contains("createdAt"))
        assert(columnNames.contains("category"))
        assert(columnNames.contains("sortOrder"))
        
        cursor.close()
        db.close()
    }
    
    /**
     * Test migration from version 2 to version 3 (adding indexes).
     * 
     * This test ensures that performance indexes are properly created.
     */
    @Test
    fun testMigration2To3() {
        // Create version 2 database with sample data
        var db = helper.createDatabase(TEST_DB, 2).apply {
            execSQL("""
                INSERT INTO todos (id, text, isCompleted, createdAt, category, sortOrder) 
                VALUES (1, 'Test Todo 1', 0, 1000, 'PERSONAL', 0)
            """)
            execSQL("""
                INSERT INTO todos (id, text, isCompleted, createdAt, category, sortOrder) 
                VALUES (2, 'Test Todo 2', 1, 2000, 'WORK', 1)
            """)
            close()
        }
        
        // Run migration to version 3
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, TodoDatabase.MIGRATION_2_3)
        
        // Verify indexes were created by checking index list
        val indexCursor = db.query("PRAGMA index_list(todos)")
        val indexNames = mutableSetOf<String>()
        
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(indexCursor.getColumnIndexOrThrow("name")))
        }
        
        // Verify expected indexes exist
        assert(indexNames.contains("index_todos_sortOrder"))
        assert(indexNames.contains("index_todos_category"))
        assert(indexNames.contains("index_todos_isCompleted"))
        assert(indexNames.contains("index_todos_category_isCompleted"))
        assert(indexNames.contains("index_todos_createdAt"))
        
        indexCursor.close()
        
        // Verify data is still intact
        val dataCursor = db.query("SELECT * FROM todos ORDER BY id")
        
        dataCursor.moveToFirst()
        assertEquals(1, dataCursor.getLong(dataCursor.getColumnIndexOrThrow("id")))
        assertEquals("Test Todo 1", dataCursor.getString(dataCursor.getColumnIndexOrThrow("text")))
        
        dataCursor.moveToNext()
        assertEquals(2, dataCursor.getLong(dataCursor.getColumnIndexOrThrow("id")))
        assertEquals("Test Todo 2", dataCursor.getString(dataCursor.getColumnIndexOrThrow("text")))
        
        dataCursor.close()
        db.close()
    }
    
    /**
     * Test complete migration chain from version 1 to 3.
     * 
     * This test ensures that migrating through multiple versions works correctly.
     */
    @Test
    fun testCompleteMigrationChain1To3() {
        // Create version 1 database with sample data
        var db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL("""
                INSERT INTO todos (id, text, isCompleted, createdAt) 
                VALUES (1, 'Migration Test Todo', 0, 1000)
            """)
            close()
        }
        
        // Run complete migration chain to version 3
        db = helper.runMigrationsAndValidate(
            TEST_DB, 3, true, 
            TodoDatabase.MIGRATION_1_2, 
            TodoDatabase.MIGRATION_2_3
        )
        
        // Verify final schema has all columns and indexes
        val columnCursor = db.query("PRAGMA table_info(todos)")
        val columnNames = mutableSetOf<String>()
        
        while (columnCursor.moveToNext()) {
            columnNames.add(columnCursor.getString(columnCursor.getColumnIndexOrThrow("name")))
        }
        
        // Verify all columns exist
        assert(columnNames.contains("id"))
        assert(columnNames.contains("text"))
        assert(columnNames.contains("isCompleted"))
        assert(columnNames.contains("createdAt"))
        assert(columnNames.contains("category"))
        assert(columnNames.contains("sortOrder"))
        
        columnCursor.close()
        
        // Verify indexes exist
        val indexCursor = db.query("PRAGMA index_list(todos)")
        val indexNames = mutableSetOf<String>()
        
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(indexCursor.getColumnIndexOrThrow("name")))
        }
        
        assert(indexNames.contains("index_todos_sortOrder"))
        assert(indexNames.contains("index_todos_category"))
        
        indexCursor.close()
        
        // Verify data integrity and default values
        val dataCursor = db.query("SELECT * FROM todos")
        dataCursor.moveToFirst()
        
        assertEquals(1, dataCursor.getLong(dataCursor.getColumnIndexOrThrow("id")))
        assertEquals("Migration Test Todo", dataCursor.getString(dataCursor.getColumnIndexOrThrow("text")))
        assertEquals("PERSONAL", dataCursor.getString(dataCursor.getColumnIndexOrThrow("category")))
        assertEquals(0, dataCursor.getInt(dataCursor.getColumnIndexOrThrow("sortOrder")))
        
        dataCursor.close()
        db.close()
    }
}