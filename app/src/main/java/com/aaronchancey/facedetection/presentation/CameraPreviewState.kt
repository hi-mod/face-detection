package com.aaronchancey.facedetection.presentation

import com.google.mlkit.vision.face.Face

data class CameraPreviewState(
    val hasCameraPermission: Boolean = false,
    val processing: Boolean = false,
    val faces: List<Face> = emptyList(),
    val sourceImageWidth: Int = 0,
    val sourceImageHeight: Int = 0,
)
