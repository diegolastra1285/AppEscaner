package com.escaner.app.ui.review

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.escaner.app.data.model.DocumentoPDF
import com.escaner.app.data.model.ModoColor
import com.escaner.app.data.model.PaginaDocumento
import com.escaner.app.data.model.RotacionGrados
import com.escaner.app.data.model.SesionLote
import com.escaner.app.data.repository.DocumentosRepository
import com.escaner.app.domain.ImageColorFilter
import com.escaner.app.domain.PdfCompiler
import com.escaner.app.domain.PerspectiveCorrector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewViewModel : ViewModel() {

    private val _paginaActualIndex = MutableStateFlow(0)
    val paginaActualIndex: StateFlow<Int> = _paginaActualIndex.asStateFlow()

    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _guardadoExitoso = MutableStateFlow<DocumentoPDF?>(null)
    val guardadoExitoso: StateFlow<DocumentoPDF?> = _guardadoExitoso.asStateFlow()

    fun seleccionarPagina(index: Int, sesion: SesionLote) {
        if (index in sesion.paginas.indices) {
            _paginaActualIndex.value = index
            cargarPreview(sesion.paginas[index])
        }
    }

    fun cargarPreview(pagina: PaginaDocumento) {
        viewModelScope.launch {
            val bitmap = procesarPaginaABitmap(pagina)
            _previewBitmap.value = bitmap
        }
    }

    fun cambiarModoColor(
        modo: ModoColor,
        sesion: SesionLote,
        onUpdateSesion: (SesionLote) -> Unit
    ) {
        val index = _paginaActualIndex.value
        if (index in sesion.paginas.indices) {
            val paginaActualizada = sesion.paginas[index].copy(modoColor = modo)
            val nuevaLista = sesion.paginas.toMutableList().apply {
                set(index, paginaActualizada)
            }
            val nuevaSesion = sesion.copy(paginas = nuevaLista)
            onUpdateSesion(nuevaSesion)
            cargarPreview(paginaActualizada)
        }
    }

    fun rotarHorario(
        sesion: SesionLote,
        onUpdateSesion: (SesionLote) -> Unit
    ) {
        val index = _paginaActualIndex.value
        if (index in sesion.paginas.indices) {
            val paginaActual = sesion.paginas[index]
            val paginaActualizada = paginaActual.copy(rotacion = paginaActual.rotacion.rotarHorario())
            val nuevaLista = sesion.paginas.toMutableList().apply {
                set(index, paginaActualizada)
            }
            val nuevaSesion = sesion.copy(paginas = nuevaLista)
            onUpdateSesion(nuevaSesion)
            cargarPreview(paginaActualizada)
        }
    }

    fun eliminarPagina(
        index: Int,
        sesion: SesionLote,
        onUpdateSesion: (SesionLote) -> Unit,
        onLoteVacio: () -> Unit
    ) {
        if (index in sesion.paginas.indices) {
            val paginaAEliminar = sesion.paginas[index]
            paginaAEliminar.archivoCapturaOriginal.delete()
            paginaAEliminar.archivoProcesadoTemp?.delete()

            val nuevaLista = sesion.paginas.toMutableList().apply {
                removeAt(index)
            }

            if (nuevaLista.isEmpty()) {
                onUpdateSesion(sesion.copy(paginas = emptyList()))
                onLoteVacio()
            } else {
                val reenumerada = sesion.copy(paginas = nuevaLista).reenumerarPaginas()
                onUpdateSesion(reenumerada)
                val nuevoIndex = (_paginaActualIndex.value).coerceAtMost(reenumerada.paginas.size - 1)
                _paginaActualIndex.value = nuevoIndex
                cargarPreview(reenumerada.paginas[nuevoIndex])
            }
        }
    }

    fun guardarPdf(
        context: Context,
        sesion: SesionLote,
        onSuccess: (DocumentoPDF) -> Unit,
        onError: () -> Unit
    ) {
        if (sesion.paginas.isEmpty() || _isSaving.value) return

        _isSaving.value = true

        viewModelScope.launch {
            try {
                val bitmaps = sesion.paginas.map { pagina ->
                    procesarPaginaABitmap(pagina)
                }

                val resultado = PdfCompiler.compilarPdf(context, bitmaps)

                resultado.fold(
                    onSuccess = { doc ->
                        // Clean up temporary files
                        DocumentosRepository(context).limpiarCacheTemporal()
                        _guardadoExitoso.value = doc
                        _isSaving.value = false
                        onSuccess(doc)
                    },
                    onFailure = {
                        _isSaving.value = false
                        onError()
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _isSaving.value = false
                onError()
            }
        }
    }

    fun limpiarEstadoGuardado() {
        _guardadoExitoso.value = null
    }

    private suspend fun procesarPaginaABitmap(pagina: PaginaDocumento): Bitmap = withContext(Dispatchers.IO) {
        val originalBitmap = BitmapFactory.decodeFile(pagina.archivoCapturaOriginal.absolutePath)
            ?: Bitmap.createBitmap(800, 1000, Bitmap.Config.ARGB_8888)

        val rectificado = PerspectiveCorrector.corregirPerspectiva(originalBitmap, pagina.esquinasCorte)
        val finalBitmap = ImageColorFilter.procesarImagenCompleta(rectificado, pagina.modoColor, pagina.rotacion)

        if (originalBitmap != rectificado && originalBitmap != finalBitmap) {
            originalBitmap.recycle()
        }
        finalBitmap
    }
}
