package com.escaner.app.data.model

import java.io.File
import java.util.UUID

enum class EstadoPagina {
    PENDIENTE,
    APROBADA
}

data class PaginaDocumento(
    val id: String = UUID.randomUUID().toString(),
    val indice: Int,
    val archivoCapturaOriginal: File,
    val esquinasCorte: PoligonoCuatroPuntos,
    val rotacion: RotacionGrados = RotacionGrados.ROT_0,
    val modoColor: ModoColor = ModoColor.BLANCO_Y_NEGRO,
    val archivoProcesadoTemp: File? = null,
    val estadoAprobacion: EstadoPagina = EstadoPagina.PENDIENTE
) {
    init {
        require(indice in 1..20) { "El índice de página debe estar entre 1 y 20 (valor recibido: $indice)" }
    }
}
