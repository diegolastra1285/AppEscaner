package com.escaner.app.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import com.escaner.app.data.model.PoligonoCuatroPuntos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

object EdgeDetector {

    private const val MAX_DIMENSION_THUMBNAIL = 600f
    private const val FALLBACK_MARGIN_PERCENT = 0.05f

    suspend fun detectarEsquinas(archivoImagen: File): PoligonoCuatroPuntos = withContext(Dispatchers.Default) {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(archivoImagen.absolutePath, boundsOptions)

        val origWidth = boundsOptions.outWidth
        val origHeight = boundsOptions.outHeight

        if (origWidth <= 0 || origHeight <= 0) {
            return@withContext PoligonoCuatroPuntos.fromRectConMargen(1000f, 1000f, FALLBACK_MARGIN_PERCENT)
        }

        val maxDim = max(origWidth, origHeight)
        var sampleSize = 1
        while ((maxDim / (sampleSize * 2)) >= MAX_DIMENSION_THUMBNAIL) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val thumbnail = try {
            BitmapFactory.decodeFile(archivoImagen.absolutePath, decodeOptions)
        } catch (e: Exception) {
            null
        }

        if (thumbnail == null) {
            return@withContext PoligonoCuatroPuntos.fromRectConMargen(
                origWidth.toFloat(),
                origHeight.toFloat(),
                FALLBACK_MARGIN_PERCENT
            )
        }

        val thumbWidth = thumbnail.width
        val thumbHeight = thumbnail.height

        val escalaX = origWidth.toFloat() / thumbWidth
        val escalaY = origHeight.toFloat() / thumbHeight

        val polígonoEnMiniatura = estimarBordesEnBitmap(thumbnail)

        // Rescale corners to original image size
        val resultado = PoligonoCuatroPuntos(
            topLeft = PointF(polígonoEnMiniatura.topLeft.x * escalaX, polígonoEnMiniatura.topLeft.y * escalaY),
            topRight = PointF(polígonoEnMiniatura.topRight.x * escalaX, polígonoEnMiniatura.topRight.y * escalaY),
            bottomRight = PointF(polígonoEnMiniatura.bottomRight.x * escalaX, polígonoEnMiniatura.bottomRight.y * escalaY),
            bottomLeft = PointF(polígonoEnMiniatura.bottomLeft.x * escalaX, polígonoEnMiniatura.bottomLeft.y * escalaY)
        )

        if (!resultado.esConvexo()) {
            return@withContext PoligonoCuatroPuntos.fromRectConMargen(
                origWidth.toFloat(),
                origHeight.toFloat(),
                FALLBACK_MARGIN_PERCENT
            )
        }

        resultado
    }

    private fun estimarBordesEnBitmap(bitmap: Bitmap): PoligonoCuatroPuntos {
        val width = bitmap.width
        val height = bitmap.height

        // Fallback default
        val fallback = PoligonoCuatroPuntos.fromRectConMargen(
            width.toFloat(),
            height.toFloat(),
            FALLBACK_MARGIN_PERCENT
        )

        try {
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val luminancias = IntArray(width * height)
            for (i in pixels.indices) {
                val color = pixels[i]
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                luminancias[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            }

            val marginW = (width * 0.05f).toInt()
            val marginH = (height * 0.05f).toInt()

            var minX = width
            var maxX = 0
            var minY = height
            var maxY = 0

            val thresholdGradient = 40

            // Sample across a grid
            for (y in marginH until height - marginH step 4) {
                for (x in marginW until width - marginW step 4) {
                    val idx = y * width + x
                    val current = luminancias[idx]
                    val right = luminancias[idx + 1]
                    val bottom = luminancias[idx + width]

                    val gradX = kotlin.math.abs(right - current)
                    val gradY = kotlin.math.abs(bottom - current)

                    if (gradX > thresholdGradient || gradY > thresholdGradient) {
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
            }

            if (minX < maxX && minY < maxY && (maxX - minX) > width * 0.3f && (maxY - minY) > height * 0.3f) {
                val candidato = PoligonoCuatroPuntos(
                    topLeft = PointF(minX.toFloat(), minY.toFloat()),
                    topRight = PointF(maxX.toFloat(), minY.toFloat()),
                    bottomRight = PointF(maxX.toFloat(), maxY.toFloat()),
                    bottomLeft = PointF(minX.toFloat(), maxY.toFloat())
                )
                if (candidato.esConvexo()) {
                    return candidato
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return fallback
    }
}
