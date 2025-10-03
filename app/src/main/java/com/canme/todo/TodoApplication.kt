package com.canme.todo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the Todo app.
 * 
 * This class is annotated with @HiltAndroidApp to enable Hilt dependency injection
 * throughout the application. It serves as the entry point for the Hilt dependency
 * injection framework.
 */
@HiltAndroidApp
class TodoApplication : Application()