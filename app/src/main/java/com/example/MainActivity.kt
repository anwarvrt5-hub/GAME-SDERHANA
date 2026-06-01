package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.game.GameViewModel
import com.example.ui.TetratroGameApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val viewModel = GameViewModel(applicationContext)

    setContent {
      MyApplicationTheme {
        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold")
        ) { innerPadding ->
          // Render the whole gameplay workspace
          TetratroGameApp(
            viewModel = viewModel,
            onExitPressed = { finish() }
          )
        }
      }
    }
  }
}
