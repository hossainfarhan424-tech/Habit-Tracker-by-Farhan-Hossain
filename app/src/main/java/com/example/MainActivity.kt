package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.HabitTrackerApp
import com.example.ui.HabitViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: HabitViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()
      MyApplicationTheme(darkTheme = isDarkTheme) {
        HabitTrackerApp(viewModel = viewModel)
      }
    }
  }
}
