package com.escaner.app.ui.crop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.escaner.app.R
import com.escaner.app.data.model.PaginaDocumento
import com.escaner.app.ui.crop.components.PolygonOverlayView
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    imageFile: File?,
    paginaExistente: PaginaDocumento?,
    onCropConfirmed: (PaginaDocumento) -> Unit,
    onBack: () -> Unit,
    viewModel: CropViewModel = viewModel()
) {
    val bitmap by viewModel.originalBitmap.collectAsState()
    val polygon by viewModel.polygon.collectAsState()
    val isConvex by viewModel.isConvex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(imageFile) {
        if (imageFile != null) {
            viewModel.inicializar(imageFile, paginaExistente)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_crop)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_cancel)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restablecerMargenSeguridad() }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = stringResource(R.string.action_reset)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.restablecerMargenSeguridad() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.action_reset))
                    }

                    Button(
                        onClick = {
                            if (imageFile != null) {
                                viewModel.confirmarRecorte(
                                    imageFile = imageFile,
                                    paginaExistente = paginaExistente,
                                    onComplete = onCropConfirmed
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isConvex && !isLoading
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.action_confirm_crop))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading || bitmap == null || polygon == null) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                PolygonOverlayView(
                    bitmap = bitmap!!,
                    polygon = polygon!!,
                    onCornerDrag = { cornerIndex, newPoint ->
                        viewModel.actualizarEsquina(cornerIndex, newPoint)
                    }
                )

                if (!isConvex) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.error_polygon_non_convex),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
