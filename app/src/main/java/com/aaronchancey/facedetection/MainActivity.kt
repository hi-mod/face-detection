package com.aaronchancey.facedetection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aaronchancey.facedetection.presentation.CameraPreviewScreen
import com.aaronchancey.facedetection.presentation.CameraPreviewViewModel
import com.aaronchancey.facedetection.ui.theme.FaceDetectionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

@Composable
private fun MainActivity.App() = FaceDetectionTheme {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            val viewModel by viewModels<CameraPreviewViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            CameraPreviewScreen(
                state = state,
                onIntent = viewModel::onIntent,
            )
        }
    }
}
