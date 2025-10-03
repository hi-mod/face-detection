package com.aaronchancey.facedetection.presentation

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraPreviewViewModel : ViewModel() {

    private var hasLoadedInitialData = false

    private val faceDetection = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build(),
    )

    private val _state = MutableStateFlow(CameraPreviewState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CameraPreviewState(),
        )

    fun onIntent(intent: CameraPreviewIntent) {
        when (intent) {
            is CameraPreviewIntent.PermissionResult -> {
                _state.update { it.copy(hasCameraPermission = intent.granted) }
            }

            CameraPreviewIntent.TakePhotoClicked -> _state.update { it.copy(processing = true) }
            is CameraPreviewIntent.ImageAvailable -> processImage(intent.imageProxy)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) = imageProxy.image?.let { image ->
        viewModelScope.launch {
            faceDetection.process(InputImage.fromMediaImage(image, 90))
                .addOnSuccessListener { faces ->
                    _state.update {
                        it.copy(
                            faces = faces,
                            sourceImageHeight = imageProxy.height,
                            sourceImageWidth = imageProxy.width,
                        )
                    }
                }
                .addOnFailureListener { it.printStackTrace() }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
