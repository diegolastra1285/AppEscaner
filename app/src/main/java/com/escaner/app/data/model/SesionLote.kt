package com.escaner.app.data.model

import java.util.UUID

enum class EstadoSesion {
    CAPTURA,
    AJUSTE_BORDES,
    REVISION,
    GUARDANDO,
    COMPILADO_EXITOSO,
    CANCELADO
}

data class SesionLote(
    val id: String = UUID.randomUUID().toString(),
    val estado: EstadoSesion = EstadoSesion.CAPTURA,
    val fechaInicio: Long = System.currentTimeMillis(),
    val paginas: List<PaginaDocumento> = emptyList()
) {
    init {
        require(paginas.size <= 20) { "Un lote no puede superar las 20 páginas (actual: ${paginas.size})" }
    }

    val puedeCompilar: Boolean
        get() = paginas.isNotEmpty() && paginas.size <= 20

    fun reenumerarPaginas(): SesionLote {
        val reenumeradas = paginas.mapIndexed { index, pagina ->
            pagina.copy(indice = index + 1)
        }
        return copy(paginas = reenumeradas)
    }
}
