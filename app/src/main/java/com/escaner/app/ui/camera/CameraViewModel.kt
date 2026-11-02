package com.escaner.app.ui.camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class CameraViewModel : ViewModel() {

    private val _isTorchEnabled = MutableStateFlow(false)
    val isTorchEnabled: StateFlow<Boolean> = _isTorchEnabled.asStateFlow()

    private val _hasFlashUnit = MutableStateFlow(false)
    val hasFlashUnit: StateFlow<Boolean> = _hasFlashUnit.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    fun updateCamera(camera: Camera?) {
        camera?.cameraInfo?.hasFlashUnit()?.let { hasFlash ->
            _hasFlashUnit.value = hasFlash
        }
    }

    fun toggleTorch(camera: Camera?) {
        camera?.let {
            val newState = !_isTorchEnabled.value
            it.cameraControl.enableTorch(newState)
            _isTorchEnabled.value = newState
        }
    }

    fun capturePhoto(
        context: Context,
        imageCapture: ImageCapture?,
        onSuccess: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (imageCapture == null || _isCapturing.value) return

        _isCapturing.value = true

        val cacheDir = context.cacheDir
        val photoFile = File(cacheDir, "scan_temp_${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    _isCapturing.value = false
                    onSuccess(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    _isCapturing.value = false
                    onError(exception)
                }
            }
        )
    }
}
