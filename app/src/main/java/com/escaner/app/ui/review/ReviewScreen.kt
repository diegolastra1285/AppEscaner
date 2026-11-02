package com.escaner.app.ui.review

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.escaner.app.R
import com.escaner.app.data.model.DocumentoPDF
import com.escaner.app.data.model.ModoColor
import com.escaner.app.data.model.PaginaDocumento
import com.escaner.app.data.model.SesionLote
import com.escaner.app.ui.review.components.ColorSelectorBar
import com.escaner.app.ui.review.components.ConfirmDiscardBatchDialog
import com.escaner.app.ui.review.components.ThumbnailStrip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    sesionLote: SesionLote,
    onUpdateSesion: (SesionLote) -> Unit,
    onAddPage: () -> Unit,
    onAdjustEdges: (PaginaDocumento) -> Unit,
    onFinishSave: () -> Unit,
    onDiscardBatch: () -> Unit,
    viewModel: ReviewViewModel = viewModel()
) {
    val context = LocalContext.current
    val paginaActualIndex by viewModel.paginaActualIndex.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val guardadoExitoso by viewModel.guardadoExitoso.collectAsState()

    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sesionLote.paginas.size) {
        if (sesionLote.paginas.isNotEmpty()) {
            val safeIndex = paginaActualIndex.coerceIn(0, sesionLote.paginas.size - 1)
            viewModel.seleccionarPagina(safeIndex, sesionLote)
        }
    }

    BackHandler {
        showDiscardDialog = true
    }

    val paginaActual = sesionLote.paginas.getOrNull(paginaActualIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (sesionLote.paginas.size > 1) {
                            stringResource(R.string.batch_page_indicator, paginaActualIndex + 1, sesionLote.paginas.size)
                        } else {
                            stringResource(R.string.title_review)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showDiscardDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_cancel)
                        )
                    }
                },
                actions = {
                    if (paginaActual != null) {
                        IconButton(onClick = { onAdjustEdges(paginaActual) }) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = stringResource(R.string.action_adjust_edges)
                            )
                        }
                        IconButton(onClick = { viewModel.rotarHorario(sesionLote, onUpdateSesion) }) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = stringResource(R.string.action_rotate_90)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main image preview with zoom & pan
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    var scale by remember { mutableFloatStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 5f)
                        if (scale == 1f) {
                            offset = Offset.Zero
                        } else {
                            offset += panChange
                        }
                    }

                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = "Vista previa del documento",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = transformState),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            // Color Selector Bar (FR-017)
            if (paginaActual != null) {
                ColorSelectorBar(
                    selectedMode = paginaActual.modoColor,
                    onSelectMode = { nuevoModo ->
                        viewModel.cambiarModoColor(nuevoModo, sesionLote, onUpdateSesion)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Thumbnail strip for multi-page batch (US3)
            if (sesionLote.paginas.size > 1) {
                ThumbnailStrip(
                    paginas = sesionLote.paginas,
                    selectedIndex = paginaActualIndex,
                    onSelectPage = { index ->
                        viewModel.seleccionarPagina(index, sesionLote)
                    },
                    onDeletePage = { index ->
                        viewModel.eliminarPagina(
                            index = index,
                            sesion = sesionLote,
                            onUpdateSesion = onUpdateSesion,
                            onLoteVacio = onDiscardBatch
                        )
                    }
                )
            }

            // Action Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (sesionLote.paginas.size < 20) {
                            onAddPage()
                        } else {
                            Toast.makeText(context, context.getString(R.string.max_pages_reached), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving && sesionLote.paginas.size < 20
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.action_add_page))
                }

                Button(
                    onClick = {
                        viewModel.guardarPdf(
                            context = context,
                            sesion = sesionLote,
                            onSuccess = { /* Dialog displayed */ },
                            onError = {
                                Toast.makeText(context, context.getString(R.string.error_save_pdf), Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving && sesionLote.puedeCompilar
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.action_save_pdf))
                    }
                }
            }
        }
    }

    // Save Success Dialog with Native Sharesheet action
    guardadoExitoso?.let { documento ->
        AlertDialog(
            onDismissRequest = {
                viewModel.limpiarEstadoGuardado()
                onFinishSave()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(text = stringResource(R.string.dialog_saved_title))
            },
            text = {
                Text(text = stringResource(R.string.dialog_saved_message, documento.nombre))
            },
            confirmButton = {
                Button(
                    onClick = {
                        compartirPdf(context, documento)
                        viewModel.limpiarEstadoGuardado()
                        onFinishSave()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.action_share))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.limpiarEstadoGuardado()
                        onFinishSave()
                    }
                ) {
                    Text(text = "Finalizar")
                }
            }
        )
    }

    if (showDiscardDialog) {
        ConfirmDiscardBatchDialog(
            pageCount = sesionLote.paginas.size,
            onConfirmDiscard = {
                showDiscardDialog = false
                onDiscardBatch()
            },
            onCancel = {
                showDiscardDialog = false
            }
        )
    }
}

fun compartirPdf(context: Context, documento: DocumentoPDF) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, documento.uri)
        putExtra(Intent.EXTRA_SUBJECT, documento.nombre)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(shareIntent, context.getString(R.string.action_share))
    context.startActivity(chooser)
}
