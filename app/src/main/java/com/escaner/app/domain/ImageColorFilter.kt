package com.escaner.app.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import com.escaner.app.data.model.ModoColor
import com.escaner.app.data.model.RotacionGrados
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageColorFilter {

    fun obtenerColorMatrix(modo: ModoColor): ColorMatrix {
        return when (modo) {
            ModoColor.COLOR -> {
                // Identity color matrix
                ColorMatrix()
            }
            ModoColor.ESCALA_GRISES -> {
                // Standard luminance Y = 0.299R + 0.587G + 0.114B
                ColorMatrix().apply {
                    setSaturation(0f)
                }
            }
            ModoColor.BLANCO_Y_NEGRO -> {
                // High contrast document binarization / text sharpening
                val contrast = 3.0f
                val brightness = -180f
                val lr = 0.299f * contrast
                val lg = 0.587f * contrast
                val lb = 0.114f * contrast

                ColorMatrix(
                    floatArrayOf(
                        lr, lg, lb, 0f, brightness,
                        lr, lg, lb, 0f, brightness,
                        lr, lg, lb, 0f, brightness,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
        }
    }

    suspend fun rotarBitmap(
        bitmap: Bitmap,
        rotacion: RotacionGrados
    ): Bitmap = withContext(Dispatchers.Default) {
        if (rotacion == RotacionGrados.ROT_0) {
            return@withContext bitmap
        }

        val matrix = Matrix().apply {
            postRotate(rotacion.grados.toFloat())
        }

        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    suspend fun aplicarFiltro(
        bitmap: Bitmap,
        modo: ModoColor
    ): Bitmap = withContext(Dispatchers.Default) {
        if (modo == ModoColor.COLOR) {
            return@withContext bitmap
        }

        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(obtenerColorMatrix(modo))
        }

        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        output
    }

    suspend fun procesarImagenCompleta(
        bitmapOriginal: Bitmap,
        modo: ModoColor,
        rotacion: RotacionGrados
    ): Bitmap = withContext(Dispatchers.Default) {
        val filtrado = aplicarFiltro(bitmapOriginal, modo)
        rotarBitmap(filtrado, rotacion)
    }
}
