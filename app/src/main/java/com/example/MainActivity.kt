package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppActivityDetailsScreen
import com.example.ui.AppActivityDetailsViewModel
import com.example.ui.theme.WellbeingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WellbeingTheme {
                val viewModel: AppActivityDetailsViewModel = viewModel()
                AppActivityDetailsScreen(viewModel = viewModel)
            }
        }
    }
}
