package com.escaner.app.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.escaner.app.R
import com.escaner.app.data.model.DocumentoPDF
import com.escaner.app.ui.home.components.ConfirmDeleteDialog
import com.escaner.app.ui.home.components.DocumentoItem
import com.escaner.app.ui.home.components.EmptyHistoryView
import com.escaner.app.ui.home.components.RenameDialog
import com.escaner.app.ui.review.compartirPdf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val documentos by viewModel.documentos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var documentoAEliminar by remember { mutableStateOf<DocumentoPDF?>(null) }
    var documentoARenombrar by remember { mutableStateOf<DocumentoPDF?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarDocumentos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_home),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCamera,
                icon = {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = stringResource(R.string.action_scan)
                    )
                },
                text = {
                    Text(text = stringResource(R.string.action_scan))
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                documentos.isEmpty() -> {
                    EmptyHistoryView(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                    ) {
                        items(
                            items = documentos,
                            key = { it.id }
                        ) { documento ->
                            DocumentoItem(
                                documento = documento,
                                onClick = {
                                    abrirVisorPdf(context, documento)
                                },
                                onShare = {
                                    compartirPdf(context, documento)
                                },
                                onRename = {
                                    documentoARenombrar = documento
                                },
                                onDelete = {
                                    documentoAEliminar = documento
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    documentoAEliminar?.let { doc ->
        ConfirmDeleteDialog(
            documento = doc,
            onConfirmDelete = {
                viewModel.eliminarDocumento(doc)
                documentoAEliminar = null
            },
            onDismiss = {
                documentoAEliminar = null
            }
        )
    }

    documentoARenombrar?.let { doc ->
        RenameDialog(
            documento = doc,
            nombresExistentes = documentos.map { it.nombre },
            onConfirmRename = { nuevoNombre ->
                viewModel.renombrarDocumento(doc, nuevoNombre)
                documentoARenombrar = null
            },
            onDismiss = {
                documentoARenombrar = null
            }
        )
    }
}

private fun abrirVisorPdf(context: Context, documento: DocumentoPDF) {
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(documento.uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(viewIntent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.error_no_pdf_viewer),
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error al abrir el documento",
            Toast.LENGTH_SHORT
        ).show()
    }
}
