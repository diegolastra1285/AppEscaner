package com.escaner.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PdfCompilerTest {

    @Test
    fun generarNombreArchivo_formatoCorrecto() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 2, 10, 15, 30)
        }

        val nombre = PdfCompiler.generarNombreArchivo(cal.timeInMillis)
        assertEquals("Escaner_20261102_101530.pdf", nombre)
    }

    @Test
    fun generarNombreArchivo_regexValido() {
        val nombre = PdfCompiler.generarNombreArchivo()
        val regex = Regex("""^Escaner_\d{8}_\d{6}\.pdf$""")
        assertTrue("El nombre generado $nombre no coincide con el formato esperado", regex.matches(nombre))
    }
}
