package com.escaner.app.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.escaner.app.data.model.PoligonoCuatroPuntos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.max

object PerspectiveCorrector {

    fun calcularDimensionesDestino(esquinas: PoligonoCuatroPuntos): Pair<Float, Float> {
        val topWidth = hypot(esquinas.topRight.x - esquinas.topLeft.x, esquinas.topRight.y - esquinas.topLeft.y)
        val bottomWidth = hypot(esquinas.bottomRight.x - esquinas.bottomLeft.x, esquinas.bottomRight.y - esquinas.bottomLeft.y)
        val anchoMax = max(topWidth, bottomWidth).coerceAtLeast(100f)

        val leftHeight = hypot(esquinas.bottomLeft.x - esquinas.topLeft.x, esquinas.bottomLeft.y - esquinas.topLeft.y)
        val rightHeight = hypot(esquinas.bottomRight.x - esquinas.topRight.x, esquinas.bottomRight.y - esquinas.topRight.y)
        val altoMax = max(leftHeight, rightHeight).coerceAtLeast(100f)

        return Pair(anchoMax, altoMax)
    }

    suspend fun corregirPerspectiva(
        bitmapOriginal: Bitmap,
        esquinas: PoligonoCuatroPuntos
    ): Bitmap = withContext(Dispatchers.Default) {
        val (anchoDestino, altoDestino) = calcularDimensionesDestino(esquinas)

        val destWidth = anchoDestino.toInt().coerceAtLeast(1)
        val destHeight = altoDestino.toInt().coerceAtLeast(1)

        val destBitmap = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(destBitmap)

        val srcPoints = esquinas.toFloatArray()
        val dstPoints = floatArrayOf(
            0f, 0f,
            anchoDestino, 0f,
            anchoDestino, altoDestino,
            0f, altoDestino
        )

        val matrix = Matrix()
        matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)

        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
            isDither = true
        }

        canvas.drawBitmap(bitmapOriginal, matrix, paint)
        destBitmap
    }
}
