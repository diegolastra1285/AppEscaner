package com.escaner.app.ui.crop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.escaner.app.data.model.EstadoPagina
import com.escaner.app.data.model.PaginaDocumento
import com.escaner.app.data.model.PoligonoCuatroPuntos
import com.escaner.app.domain.EdgeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CropViewModel : ViewModel() {

    private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
    val originalBitmap: StateFlow<Bitmap?> = _originalBitmap.asStateFlow()

    private val _polygon = MutableStateFlow<PoligonoCuatroPuntos?>(null)
    val polygon: StateFlow<PoligonoCuatroPuntos?> = _polygon.asStateFlow()

    private val _isConvex = MutableStateFlow(true)
    val isConvex: StateFlow<Boolean> = _isConvex.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun inicializar(imageFile: File, paginaExistente: PaginaDocumento?) {
        viewModelScope.launch {
            _isLoading.value = true

            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(imageFile.absolutePath)
            }
            _originalBitmap.value = bitmap

            if (bitmap != null) {
                if (paginaExistente != null) {
                    _polygon.value = paginaExistente.esquinasCorte
                    _isConvex.value = paginaExistente.esquinasCorte.esConvexo()
                } else {
                    val detectadas = EdgeDetector.detectarEsquinas(imageFile)
                    _polygon.value = detectadas
                    _isConvex.value = detectadas.esConvexo()
                }
            }

            _isLoading.value = false
        }
    }

    fun actualizarEsquina(cornerIndex: Int, newPoint: PointF) {
        val current = _polygon.value ?: return

        val candidate = when (cornerIndex) {
            0 -> current.copy(topLeft = newPoint)
            1 -> current.copy(topRight = newPoint)
            2 -> current.copy(bottomRight = newPoint)
            3 -> current.copy(bottomLeft = newPoint)
            else -> current
        }

        val convex = candidate.esConvexo()
        _isConvex.value = convex
        _polygon.value = candidate
    }

    fun restablecerMargenSeguridad() {
        val bitmap = _originalBitmap.value ?: return
        val defaultPolygon = PoligonoCuatroPuntos.fromRectConMargen(
            bitmap.width.toFloat(),
            bitmap.height.toFloat(),
            0.05f
        )
        _polygon.value = defaultPolygon
        _isConvex.value = true
    }

    fun confirmarRecorte(
        imageFile: File,
        paginaExistente: PaginaDocumento?,
        onComplete: (PaginaDocumento) -> Unit
    ) {
        val poly = _polygon.value ?: return
        if (!poly.esConvexo()) return

        val resultado = if (paginaExistente != null) {
            paginaExistente.copy(
                esquinasCorte = poly,
                estadoAprobacion = EstadoPagina.APROBADA
            )
        } else {
            PaginaDocumento(
                indice = 1,
                archivoCapturaOriginal = imageFile,
                esquinasCorte = poly,
                estadoAprobacion = EstadoPagina.APROBADA
            )
        }

        onComplete(resultado)
    }
}
