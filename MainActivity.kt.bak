package com.canme.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.canme.todo.ui.screens.TodoListScreen
import com.canme.todo.ui.theme.TodoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity that hosts the Compose UI for the Todo application.
 * Configured with Hilt for dependency injection and Material Design 3 theming.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for modern Android experience
        enableEdgeToEdge()
        
        setContent {
            TodoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoListScreen()
                }
            }
        }
    }
}