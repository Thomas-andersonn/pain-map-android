package com.example.painmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.painmap.ui.screens.MainDashboardScreen
import com.example.painmap.ui.theme.PainMapAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PainMapAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainDashboardScreen(
                        onStartAssessment = {
                            // Handler for starting assessment flow
                        }
                    )
                }
            }
        }
    }
}
