package com.escaner.app.domain

import android.graphics.PointF
import com.escaner.app.data.model.PoligonoCuatroPuntos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class PerspectiveCorrectorTest {

    @Test
    fun calcularDimensionesDestino_rectanguloEstandar() {
        val corners = PoligonoCuatroPuntos(
            topLeft = PointF(0f, 0f),
            topRight = PointF(400f, 0f),
            bottomRight = PointF(400f, 600f),
            bottomLeft = PointF(0f, 600f)
        )

        val dimensiones = PerspectiveCorrector.calcularDimensionesDestino(corners)
        assertEquals(400f, dimensiones.first, 1.0f)
        assertEquals(600f, dimensiones.second, 1.0f)
    }

    @Test
    fun calcularDimensionesDestino_cuadrilateroConPerspectiva() {
        // Trapezoid where top is shorter than bottom
        val corners = PoligonoCuatroPuntos(
            topLeft = PointF(50f, 20f),
            topRight = PointF(350f, 20f),
            bottomRight = PointF(400f, 600f),
            bottomLeft = PointF(0f, 600f)
        )

        val (ancho, alto) = PerspectiveCorrector.calcularDimensionesDestino(corners)
        assertTrue(ancho >= 400f)
        assertTrue(alto >= 580f)
    }

    @Test
    fun polygonCornersToFloatArray_genera8Elementos() {
        val corners = PoligonoCuatroPuntos(
            topLeft = PointF(10f, 20f),
            topRight = PointF(30f, 40f),
            bottomRight = PointF(50f, 60f),
            bottomLeft = PointF(70f, 80f)
        )

        val array = corners.toFloatArray()
        assertEquals(8, array.size)
        assertEquals(10f, array[0], 0.001f)
        assertEquals(20f, array[1], 0.001f)
        assertEquals(30f, array[2], 0.001f)
        assertEquals(40f, array[3], 0.001f)
        assertEquals(50f, array[4], 0.001f)
        assertEquals(60f, array[5], 0.001f)
        assertEquals(70f, array[6], 0.001f)
        assertEquals(80f, array[7], 0.001f)
    }
}
