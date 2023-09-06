package com.chow.camery.extension

import android.content.Context
import android.hardware.display.DisplayManager
import android.net.Uri
import android.view.Display
import android.widget.ImageView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.abs

fun PreviewView.showCamera(
    context: Context,
    cameraFacing: Int,
    ivCapture: ImageView,
    ivFlash: ImageView,
    takePhotoCallback: (Uri, File) -> Unit,
    toggleFlashCallback: (Boolean) -> Unit,
    showLoadingCallback: (Boolean) -> Unit
) {
    post {
        ProcessCameraProvider.getInstance(context).apply {
            addListener({
                try {
                    val cameraProvider = get()
                    val preview =
                        Preview.Builder().setTargetAspectRatio(aspectRatio(width, height)).build()
                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(
                            (context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).getDisplay(
                                Display.DEFAULT_DISPLAY
                            ).rotation
                        ).build()
                    val cameraSelector =
                        CameraSelector.Builder().requireLensFacing(cameraFacing).build()
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        context as LifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    ivCapture.setOnClickListener {
                        showLoadingCallback.invoke(true)
                        takePicture(imageCapture, context) { uri, file ->
                            takePhotoCallback.invoke(uri, file)
                        }
                    }
                    ivFlash.setOnClickListener {
                        setFlashIcon(camera) {
                            toggleFlashCallback.invoke(it)
                        }
                    }
                    preview.setSurfaceProvider(surfaceProvider)
                } catch (e: Exception) {

                }
            }, ContextCompat.getMainExecutor(context))
        }
    }
}

private fun aspectRatio(width: Int, height: Int): Int {
    val previewRatio = width.coerceAtLeast(height) / width.coerceAtMost(height)
    if (abs(previewRatio - 4.0 / 3.0) <= abs(previewRatio - 16.0 / 9.0)) {
        return AspectRatio.RATIO_4_3
    }
    return AspectRatio.RATIO_16_9
}

private fun takePicture(
    imageCapture: ImageCapture,
    context: Context,
    callback: (Uri, File) -> Unit
) {
    File(context.cacheDir, "${System.currentTimeMillis()}.jpg").let { file ->
        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(file).build(),
            Executors.newCachedThreadPool(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    callback.invoke(outputFileResults.savedUri ?: return, file)
                }

                override fun onError(exception: ImageCaptureException) {
                }

            })
    }
}

private fun setFlashIcon(camera: Camera, callback: (Boolean) -> Unit) {
    if (camera.cameraInfo.hasFlashUnit()) {
        if (camera.cameraInfo.torchState.value == 0) {
            camera.cameraControl.enableTorch(true)
            callback.invoke(true)
        } else {
            camera.cameraControl.enableTorch(false)
            callback.invoke(false)
        }
    }
}