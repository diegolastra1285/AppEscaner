package com.escaner.app.data.model

import android.graphics.PointF

data class PoligonoCuatroPuntos(
    val topLeft: PointF,
    val topRight: PointF,
    val bottomRight: PointF,
    val bottomLeft: PointF
) {
    fun toFloatArray(): FloatArray {
        return floatArrayOf(
            topLeft.x, topLeft.y,
            topRight.x, topRight.y,
            bottomRight.x, bottomRight.y,
            bottomLeft.x, bottomLeft.y
        )
    }

    fun esConvexo(): Boolean {
        val points = listOf(topLeft, topRight, bottomRight, bottomLeft)
        var positive = false
        var negative = false

        for (i in 0 until 4) {
            val p0 = points[i]
            val p1 = points[(i + 1) % 4]
            val p2 = points[(i + 2) % 4]

            val dx1 = p1.x - p0.x
            val dy1 = p1.y - p0.y
            val dx2 = p2.x - p1.x
            val dy2 = p2.y - p1.y

            val crossProduct = (dx1 * dy2) - (dy1 * dx2)

            if (crossProduct > 1e-4f) {
                positive = true
            } else if (crossProduct < -1e-4f) {
                negative = true
            } else {
                // Collinear points -> Degenerate quadrilateral
                return false
            }

            if (positive && negative) {
                // Not convex or self-intersecting
                return false
            }
        }

        return positive xor negative
    }

    companion object {
        fun fromRectConMargen(
            ancho: Float,
            alto: Float,
            margenPorcentaje: Float = 0.05f
        ): PoligonoCuatroPuntos {
            val margenX = ancho * margenPorcentaje
            val margenY = alto * margenPorcentaje

            return PoligonoCuatroPuntos(
                topLeft = PointF(margenX, margenY),
                topRight = PointF(ancho - margenX, margenY),
                bottomRight = PointF(ancho - margenX, alto - margenY),
                bottomLeft = PointF(margenX, alto - margenY)
            )
        }
    }
}
