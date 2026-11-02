package com.escaner.app.domain

import com.escaner.app.data.model.ModoColor
import com.escaner.app.data.model.RotacionGrados
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ImageColorFilterTest {

    @Test
    fun rotacionGrados_avanzaCiclicamente() {
        var rotacion = RotacionGrados.ROT_0
        assertEquals(0, rotacion.grados)

        rotacion = rotacion.rotarHorario()
        assertEquals(RotacionGrados.ROT_90, rotacion)
        assertEquals(90, rotacion.grados)

        rotacion = rotacion.rotarHorario()
        assertEquals(RotacionGrados.ROT_180, rotacion)
        assertEquals(180, rotacion.grados)

        rotacion = rotacion.rotarHorario()
        assertEquals(RotacionGrados.ROT_270, rotacion)
        assertEquals(270, rotacion.grados)

        rotacion = rotacion.rotarHorario()
        assertEquals(RotacionGrados.ROT_0, rotacion)
        assertEquals(0, rotacion.grados)
    }

    @Test
    fun colorMatrix_verificaModosColorDisponibles() {
        val modos = ModoColor.entries
        assertEquals(3, modos.size)
        assertEquals(ModoColor.BLANCO_Y_NEGRO, modos[0])
        assertEquals(ModoColor.COLOR, modos[1])
        assertEquals(ModoColor.ESCALA_GRISES, modos[2])
    }

    @Test
    fun colorMatrixArray_escalasValidas() {
        val bwMatrix = ImageColorFilter.obtenerColorMatrix(ModoColor.BLANCO_Y_NEGRO)
        assertNotNull(bwMatrix)

        val grayMatrix = ImageColorFilter.obtenerColorMatrix(ModoColor.ESCALA_GRISES)
        assertNotNull(grayMatrix)

        val colorMatrix = ImageColorFilter.obtenerColorMatrix(ModoColor.COLOR)
        assertNotNull(colorMatrix)
    }
}
