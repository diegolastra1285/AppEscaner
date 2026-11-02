package com.escaner.app.data.model

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DocumentoPDF(
    val id: Long,
    val nombre: String,
    val uri: Uri,
    val rutaFisica: String,
    val fechaCreacion: Long,
    val tamanoBytes: Long,
    val cantidadPaginas: Int
) {
    init {
        require(id >= 0) { "El ID debe ser mayor o igual a 0" }
        require(validarNombreArchivo(nombre)) { "Nombre de archivo inválido: $nombre" }
        require(fechaCreacion > 0) { "La fecha de creación debe ser mayor a 0" }
        require(tamanoBytes >= 0) { "El tamaño en bytes no puede ser negativo" }
        require(cantidadPaginas >= 1) { "La cantidad de páginas debe ser al menos 1" }
    }

    val tamanoFormateado: String
        get() = formatearTamanoBytes(tamanoBytes)

    val fechaFormateada: String
        get() = formatearFecha(fechaCreacion)

    companion object {
        private val NOMBRE_REGEX = Regex("""^[^\\/:*?"<>|]+$""")

        fun validarNombreBase(nombreBase: String): Boolean {
            return nombreBase.isNotBlank() && NOMBRE_REGEX.matches(nombreBase)
        }

        fun validarNombreArchivo(nombreCompleto: String): Boolean {
            return nombreCompleto.endsWith(".pdf", ignoreCase = true) &&
                    validarNombreBase(nombreCompleto.removeSuffix(".pdf"))
        }

        fun formatearTamanoBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024) {
                return if (kb >= 100) "${kb.toInt()} KB" else String.format(Locale("es", "CO"), "%.1f KB", kb)
            }
            val mb = kb / 1024.0
            return String.format(Locale("es", "CO"), "%.1f MB", mb)
        }

        fun formatearFecha(timestampMillis: Long): String {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply { timeInMillis = timestampMillis }

            val timeFormat = SimpleDateFormat("h:mm a", Locale("es", "CO"))
            val horaStr = timeFormat.format(Date(timestampMillis)).lowercase(Locale("es", "CO"))

            val isSameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

            if (isSameDay) {
                return "Hoy, $horaStr"
            }

            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

            if (isYesterday) {
                return "Ayer, $horaStr"
            }

            val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("es", "CO"))
            return dateFormat.format(Date(timestampMillis))
        }
    }
}
