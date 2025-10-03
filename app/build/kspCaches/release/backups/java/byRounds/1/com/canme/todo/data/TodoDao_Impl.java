package com.canme.todo.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TodoDao_Impl implements TodoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Todo> __insertionAdapterOfTodo;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Todo> __deletionAdapterOfTodo;

  private final EntityDeletionOrUpdateAdapter<Todo> __updateAdapterOfTodo;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCompletedTodos;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSortOrder;

  public TodoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTodo = new EntityInsertionAdapter<Todo>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `todos` (`id`,`text`,`isCompleted`,`category`,`sortOrder`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Todo entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getText());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(3, _tmp);
        final String _tmp_1 = __converters.fromTodoCategory(entity.getCategory());
        statement.bindString(4, _tmp_1);
        statement.bindLong(5, entity.getSortOrder());
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfTodo = new EntityDeletionOrUpdateAdapter<Todo>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `todos` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Todo entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTodo = new EntityDeletionOrUpdateAdapter<Todo>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `todos` SET `id` = ?,`text` = ?,`isCompleted` = ?,`category` = ?,`sortOrder` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Todo entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getText());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(3, _tmp);
        final String _tmp_1 = __converters.fromTodoCategory(entity.getCategory());
        statement.bindString(4, _tmp_1);
        statement.bindLong(5, entity.getSortOrder());
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteCompletedTodos = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM todos WHERE isCompleted = 1";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateSortOrder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE todos SET sortOrder = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertTodo(final Todo todo, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTodo.insertAndReturnId(todo);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTodo(final Todo todo, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTodo.handle(todo);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTodo(final Todo todo, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTodo.handle(todo);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object reorderTodos(final List<Todo> reorderedTodos,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> TodoDao.DefaultImpls.reorderTodos(TodoDao_Impl.this, reorderedTodos, __cont), $completion);
  }

  @Override
  public Object deleteCompletedTodos(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCompletedTodos.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCompletedTodos.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSortOrder(final long todoId, final int newOrder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSortOrder.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, newOrder);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, todoId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateSortOrder.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Todo>> getAllTodos() {
    final String _sql = "SELECT * FROM todos ORDER BY sortOrder ASC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"todos"}, new Callable<List<Todo>>() {
      @Override
      @NonNull
      public List<Todo> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Todo> _result = new ArrayList<Todo>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Todo _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final TodoCategory _tmpCategory;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = __converters.toTodoCategory(_tmp_1);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Todo(_tmpId,_tmpText,_tmpIsCompleted,_tmpCategory,_tmpSortOrder,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Todo>> getTodosByCategory(final TodoCategory category) {
    final String _sql = "SELECT * FROM todos WHERE category = ? ORDER BY sortOrder ASC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromTodoCategory(category);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"todos"}, new Callable<List<Todo>>() {
      @Override
      @NonNull
      public List<Todo> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Todo> _result = new ArrayList<Todo>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Todo _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final TodoCategory _tmpCategory;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = __converters.toTodoCategory(_tmp_2);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Todo(_tmpId,_tmpText,_tmpIsCompleted,_tmpCategory,_tmpSortOrder,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Todo>> getTodosByStatus(final boolean isCompleted) {
    final String _sql = "SELECT * FROM todos WHERE isCompleted = ? ORDER BY sortOrder ASC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final int _tmp = isCompleted ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"todos"}, new Callable<List<Todo>>() {
      @Override
      @NonNull
      public List<Todo> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Todo> _result = new ArrayList<Todo>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Todo _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final TodoCategory _tmpCategory;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = __converters.toTodoCategory(_tmp_2);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Todo(_tmpId,_tmpText,_tmpIsCompleted,_tmpCategory,_tmpSortOrder,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Todo>> getTodosByCategoryAndStatus(final TodoCategory category,
      final boolean isCompleted) {
    final String _sql = "SELECT * FROM todos WHERE category = ? AND isCompleted = ? ORDER BY sortOrder ASC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    final String _tmp = __converters.fromTodoCategory(category);
    _statement.bindString(_argIndex, _tmp);
    _argIndex = 2;
    final int _tmp_1 = isCompleted ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp_1);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"todos"}, new Callable<List<Todo>>() {
      @Override
      @NonNull
      public List<Todo> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Todo> _result = new ArrayList<Todo>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Todo _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final TodoCategory _tmpCategory;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = __converters.toTodoCategory(_tmp_3);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Todo(_tmpId,_tmpText,_tmpIsCompleted,_tmpCategory,_tmpSortOrder,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getTodoCount() {
    final String _sql = "SELECT COUNT(*) FROM todos";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"todos"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getTodoCountByCategory(final TodoCategory category) {
    final String _sql = "SELECT COUNT(*) FROM todos WHERE category = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromTodoCategory(category);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"todos"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(0);
            _result = _tmp_1;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
