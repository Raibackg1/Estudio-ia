package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.main.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TutorViewModel

class MainActivity : ComponentActivity() {

    private val tutorViewModel: TutorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(
                    viewModel = tutorViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

