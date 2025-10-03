package com.canme.todo.di;

import com.canme.todo.data.TodoDao;
import com.canme.todo.data.TodoDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DatabaseModule_ProvideTodoDaoFactory implements Factory<TodoDao> {
  private final Provider<TodoDatabase> databaseProvider;

  public DatabaseModule_ProvideTodoDaoFactory(Provider<TodoDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public TodoDao get() {
    return provideTodoDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideTodoDaoFactory create(
      Provider<TodoDatabase> databaseProvider) {
    return new DatabaseModule_ProvideTodoDaoFactory(databaseProvider);
  }

  public static TodoDao provideTodoDao(TodoDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTodoDao(database));
  }
}
