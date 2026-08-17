package com.example.painmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.painmap.data.repository.AiTriageRepositoryImpl
import com.example.painmap.data.repository.PainRecordRepositoryImpl
import com.example.painmap.ui.navigation.PainMapNavHost
import com.example.painmap.ui.screens.painmap.PainMapViewModel
import com.example.painmap.ui.theme.PainMapAITheme

class MainActivity : ComponentActivity() {

    private val painRecordRepository by lazy { PainRecordRepositoryImpl(applicationContext) }
    private val aiTriageRepository by lazy { AiTriageRepositoryImpl() }

    private val viewModel: PainMapViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PainMapViewModel(
                    painRecordRepository = painRecordRepository,
                    aiTriageRepository = aiTriageRepository
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PainMapAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PainMapNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
