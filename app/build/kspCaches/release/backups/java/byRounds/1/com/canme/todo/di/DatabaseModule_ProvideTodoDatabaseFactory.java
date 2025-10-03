package com.canme.todo.di;

import android.content.Context;
import com.canme.todo.data.TodoDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideTodoDatabaseFactory implements Factory<TodoDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideTodoDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public TodoDatabase get() {
    return provideTodoDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideTodoDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideTodoDatabaseFactory(contextProvider);
  }

  public static TodoDatabase provideTodoDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTodoDatabase(context));
  }
}
