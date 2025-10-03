package com.aaronchancey.facedetection.presentation

import androidx.camera.core.ImageProxy

sealed interface CameraPreviewIntent {
    data class PermissionResult(val granted: Boolean) : CameraPreviewIntent
    data class ImageAvailable(val imageProxy: ImageProxy) : CameraPreviewIntent
    data object TakePhotoClicked : CameraPreviewIntent
}
