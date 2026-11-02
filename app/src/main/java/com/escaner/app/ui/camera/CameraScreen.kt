package com.escaner.app.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.escaner.app.ui.camera.components.CameraControls
import com.escaner.app.ui.home.components.PermissionDeniedDialog
import com.escaner.app.ui.review.components.ConfirmDiscardBatchDialog
import java.io.File

@Composable
fun CameraScreen(
    paginasCount: Int,
    onPhotoCaptured: (File) -> Unit,
    onBack: () -> Unit,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val isTorchEnabled by viewModel.isTorchEnabled.collectAsState()
    val hasFlashUnit by viewModel.hasFlashUnit.collectAsState()

    BackHandler {
        if (paginasCount > 0) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()
                        imageCapture = capture

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            val boundCamera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )
                            camera = boundCamera
                            viewModel.updateCamera(boundCamera)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            CameraControls(
                hasFlashUnit = hasFlashUnit,
                isTorchEnabled = isTorchEnabled,
                paginasCount = paginasCount,
                onTorchToggle = { viewModel.toggleTorch(camera) },
                onCaptureClick = {
                    viewModel.capturePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        onSuccess = { photoFile ->
                            onPhotoCaptured(photoFile)
                        },
                        onError = {
                            // Handled cleanly
                        }
                    )
                },
                onCloseClick = {
                    if (paginasCount > 0) {
                        showDiscardDialog = true
                    } else {
                        onBack()
                    }
                }
            )
        }
    }

    if (showPermissionDialog) {
        PermissionDeniedDialog(
            onDismiss = {
                showPermissionDialog = false
                onBack()
            },
            onGrantPermission = {
                showPermissionDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }

    if (showDiscardDialog) {
        ConfirmDiscardBatchDialog(
            pageCount = paginasCount,
            onConfirmDiscard = {
                showDiscardDialog = false
                onBack()
            },
            onCancel = {
                showDiscardDialog = false
            }
        )
    }
}
