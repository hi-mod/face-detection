package com.aaronchancey.facedetection.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ExperimentalUseCaseApi
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.TransformExperimental
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aaronchancey.facedetection.R
import com.aaronchancey.facedetection.ui.theme.FaceDetectionTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@androidx.annotation.OptIn(ExperimentalGetImage::class, ExperimentalUseCaseApi::class)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    state: CameraPreviewState,
    onIntent: (CameraPreviewIntent) -> Unit,
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS or CameraController.IMAGE_CAPTURE)
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
            ) { imageProxy ->
                imageProxy.image?.let { image ->
                    onIntent(CameraPreviewIntent.ImageAvailable(imageProxy))
                }
            }
        }
    }

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        onIntent(CameraPreviewIntent.PermissionResult(cameraPermissionState.status.isGranted))
    }

    if (state.hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CameraPreviewContent(
                modifier = Modifier.fillMaxSize(),
                cameraController = cameraController,
                state = state,
            )
            ShutterButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                cameraController = cameraController,
                context = context,
                onIntent = onIntent,
            )
            if (state.processing) {
                CircularProgressIndicator()
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val textToShow = stringResource(R.string.needCameraPermission)
            Text(textToShow, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text(stringResource(R.string.grantCameraPermission))
            }
        }
    }
}

@androidx.annotation.OptIn(TransformExperimental::class)
@Composable
private fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    cameraController: LifecycleCameraController,
    state: CameraPreviewState,
) {
    val context = LocalContext.current
    cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                previewView.apply {
                    controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            state.faces.forEach { face ->
                val mapped = mapRect(face.boundingBox, state.sourceImageWidth, state.sourceImageHeight, size.width.toInt(), size.height.toInt(), 90)
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(mapped.left, mapped.top),
                    size = Size(mapped.width(), mapped.height()),
                    style = Stroke(width = 3f),
                )
            }
        }
    }
}

@Composable
private fun ShutterButton(
    modifier: Modifier = Modifier,
    cameraController: LifecycleCameraController,
    context: Context,
    onIntent: (CameraPreviewIntent) -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = {
            takePhoto(
                controller = cameraController,
                applicationContext = context,
                onPhotoTaken = { bitmap ->
                    onIntent(CameraPreviewIntent.TakePhotoClicked)
                },
            )
        },
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = stringResource(R.string.takePhoto),
        )
    }
}

private fun takePhoto(
    controller: LifecycleCameraController,
    applicationContext: Context,
    onPhotoTaken: (Bitmap) -> Unit,
) = controller.takePicture(
    ContextCompat.getMainExecutor(applicationContext),
    object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            super.onCaptureSuccess(image)

            val matrix = Matrix().apply {
                postRotate(image.imageInfo.rotationDegrees.toFloat())
            }
            val rotatedBitmap = Bitmap.createBitmap(
                image.toBitmap(),
                0,
                0,
                image.width,
                image.height,
                matrix,
                true,
            )

            onPhotoTaken(rotatedBitmap)
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
            exception.printStackTrace()
        }
    },
)

fun mapRect(
    boundingBox: Rect,
    imageWidth: Int,
    imageHeight: Int,
    viewWidth: Int,
    viewHeight: Int,
    rotationDegrees: Int,
): RectF {
    val scaleX: Float
    val scaleY: Float

    when (rotationDegrees) {
        0, 180 -> {
            scaleX = viewWidth.toFloat() / imageWidth
            scaleY = viewHeight.toFloat() / imageHeight
        }
        90, 270 -> {
            scaleX = viewWidth.toFloat() / imageHeight
            scaleY = viewHeight.toFloat() / imageWidth
        }
        else -> error("Unsupported rotation: $rotationDegrees")
    }

    val mapped = RectF(
        boundingBox.left * scaleX,
        boundingBox.top * scaleY,
        boundingBox.right * scaleX,
        boundingBox.bottom * scaleY,
    )

    return mapped
}

@Preview
@Composable
private fun CameraPreviewScreenPreview() {
    FaceDetectionTheme {
        CameraPreviewScreen(
            state = CameraPreviewState(),
            onIntent = {},
        )
    }
}
