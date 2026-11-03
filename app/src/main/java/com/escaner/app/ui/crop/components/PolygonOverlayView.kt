package com.escaner.app.ui.crop.components

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.escaner.app.data.model.PoligonoCuatroPuntos
import com.escaner.app.ui.theme.CropDimOverlayColor
import com.escaner.app.ui.theme.CropHandleColor
import com.escaner.app.ui.theme.CropPolygonStrokeColor
import kotlin.math.hypot

@Composable
fun PolygonOverlayView(
    bitmap: Bitmap,
    polygon: PoligonoCuatroPuntos,
    onCornerDrag: (cornerIndex: Int, newPosition: PointF) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggingCornerIndex by remember { mutableIntStateOf(-1) }

    // Memorizamos estados actualizados para evitar reiniciar pointerInput durante el arrastre
    val currentPolygon by rememberUpdatedState(polygon)
    val currentOnCornerDrag by rememberUpdatedState(onCornerDrag)
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(bitmap) {
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()

                    // Calcular escala y offset para centrar y ajustar la imagen en el canvas
                    val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val canvasRatio = canvasWidth / canvasHeight

                    val scale: Float
                    val offsetX: Float
                    val offsetY: Float

                    if (imgRatio > canvasRatio) {
                        scale = canvasWidth / bitmap.width.toFloat()
                        offsetX = 0f
                        offsetY = (canvasHeight - bitmap.height.toFloat() * scale) / 2f
                    } else {
                        scale = canvasHeight / bitmap.height.toFloat()
                        offsetX = (canvasWidth - bitmap.width.toFloat() * scale) / 2f
                        offsetY = 0f
                    }

                    fun imageToCanvas(p: PointF): Offset {
                        return Offset(p.x * scale + offsetX, p.y * scale + offsetY)
                    }

                    fun canvasToImage(offset: Offset): PointF {
                        val imgX = ((offset.x - offsetX) / scale).coerceIn(0f, bitmap.width.toFloat())
                        val imgY = ((offset.y - offsetY) / scale).coerceIn(0f, bitmap.height.toFloat())
                        return PointF(imgX, imgY)
                    }

                    val touchTargetRadiusPx = 54.dp.toPx()

                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val current = currentPolygon
                            val c0 = imageToCanvas(current.topLeft)
                            val c1 = imageToCanvas(current.topRight)
                            val c2 = imageToCanvas(current.bottomRight)
                            val c3 = imageToCanvas(current.bottomLeft)

                            val dists = listOf(
                                hypot(startOffset.x - c0.x, startOffset.y - c0.y),
                                hypot(startOffset.x - c1.x, startOffset.y - c1.y),
                                hypot(startOffset.x - c2.x, startOffset.y - c2.y),
                                hypot(startOffset.x - c3.x, startOffset.y - c3.y)
                            )

                            val minDist = dists.minOrNull() ?: Float.MAX_VALUE
                            if (minDist <= touchTargetRadiusPx) {
                                draggingCornerIndex = dists.indexOf(minDist)
                            } else {
                                draggingCornerIndex = -1
                            }
                        },
                        onDrag = { change, _ ->
                            if (draggingCornerIndex != -1) {
                                change.consume()
                                val imgPoint = canvasToImage(change.position)
                                currentOnCornerDrag(draggingCornerIndex, imgPoint)
                            }
                        },
                        onDragEnd = {
                            draggingCornerIndex = -1
                        },
                        onDragCancel = {
                            draggingCornerIndex = -1
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val canvasRatio = canvasWidth / canvasHeight

            val scale: Float
            val offsetX: Float
            val offsetY: Float

            if (imgRatio > canvasRatio) {
                scale = canvasWidth / bitmap.width.toFloat()
                offsetX = 0f
                offsetY = (canvasHeight - bitmap.height.toFloat() * scale) / 2f
            } else {
                scale = canvasHeight / bitmap.height.toFloat()
                offsetX = (canvasWidth - bitmap.width.toFloat() * scale) / 2f
                offsetY = 0f
            }

            val imgDestWidth = (bitmap.width * scale).toInt()
            val imgDestHeight = (bitmap.height * scale).toInt()

            // Dibujar imagen ajustada
            drawImage(
                image = imageBitmap,
                dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
                dstSize = IntSize(imgDestWidth, imgDestHeight)
            )

            // Convertir esquinas a coordenadas del Canvas
            val p0 = Offset(polygon.topLeft.x * scale + offsetX, polygon.topLeft.y * scale + offsetY)
            val p1 = Offset(polygon.topRight.x * scale + offsetX, polygon.topRight.y * scale + offsetY)
            val p2 = Offset(polygon.bottomRight.x * scale + offsetX, polygon.bottomRight.y * scale + offsetY)
            val p3 = Offset(polygon.bottomLeft.x * scale + offsetX, polygon.bottomLeft.y * scale + offsetY)

            val fullPath = Path().apply {
                moveTo(offsetX, offsetY)
                lineTo(offsetX + imgDestWidth, offsetY)
                lineTo(offsetX + imgDestWidth, offsetY + imgDestHeight)
                lineTo(offsetX, offsetY + imgDestHeight)
                close()
            }

            val polyPath = Path().apply {
                moveTo(p0.x, p0.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                close()
            }

            // Máscara oscura únicamente fuera del polígono
            val dimMaskPath = Path.combine(
                operation = PathOperation.Difference,
                path1 = fullPath,
                path2 = polyPath
            )
            drawPath(path = dimMaskPath, color = CropDimOverlayColor)

            // Líneas perimetrales del polígono
            drawPath(
                path = polyPath,
                color = CropPolygonStrokeColor,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Dibujar los 4 nodos/manejadores de esquina
            val handleRadius = 14.dp.toPx()
            val handleOuterBorder = 3.dp.toPx()
            val corners = listOf(p0, p1, p2, p3)

            corners.forEachIndexed { index, corner ->
                val isSelected = index == draggingCornerIndex
                val radius = if (isSelected) handleRadius * 1.25f else handleRadius

                // Borde blanco exterior
                drawCircle(
                    color = Color.White,
                    radius = radius + handleOuterBorder,
                    center = corner
                )
                // Centro verde del manejador
                drawCircle(
                    color = CropHandleColor,
                    radius = radius,
                    center = corner
                )
            }

            // Lupa de precisión flotante para el nodo activo
            if (draggingCornerIndex in 0..3) {
                val activeCorner = corners[draggingCornerIndex]

                val magnifierRadius = 56.dp.toPx()
                val verticalOffset = 85.dp.toPx()
                val minMargin = 16.dp.toPx()
                val magnification = 2.2f

                // Posicionamiento inteligente: por encima del dedo si hay espacio, o por debajo
                val targetX = activeCorner.x.coerceIn(magnifierRadius + minMargin, canvasWidth - magnifierRadius - minMargin)
                val targetY = if (activeCorner.y - verticalOffset - magnifierRadius >= minMargin) {
                    activeCorner.y - verticalOffset
                } else {
                    activeCorner.y + verticalOffset
                }
                val magnifierCenter = Offset(targetX, targetY)

                val lensClip = Path().apply {
                    addOval(Rect(center = magnifierCenter, radius = magnifierRadius))
                }

                // Sombra suave posterior de la lente
                drawCircle(
                    color = Color(0x66000000),
                    radius = magnifierRadius + 4.dp.toPx(),
                    center = magnifierCenter
                )

                // Renderizado ampliado dentro de la lente circular
                clipPath(lensClip) {
                    // Fondo oscuro neutro para bordes externos de la imagen
                    drawCircle(
                        color = Color(0xFF1E1E1E),
                        radius = magnifierRadius,
                        center = magnifierCenter
                    )

                    // Transformación exacta: centra el activeCorner en magnifierCenter con zoom 2.2x
                    withTransform({
                        translate(left = magnifierCenter.x, top = magnifierCenter.y)
                        scale(scaleX = magnification, scaleY = magnification, pivot = Offset.Zero)
                        translate(left = -activeCorner.x, top = -activeCorner.y)
                    }) {
                        drawImage(
                            image = imageBitmap,
                            dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
                            dstSize = IntSize(imgDestWidth, imgDestHeight)
                        )
                    }

                    // Retícula de alineación milimétrica (cruz central con separación en el centro)
                    val crosshairLength = 14.dp.toPx()
                    val centerGap = 3.dp.toPx()

                    // Contorno oscuro de la retícula para asegurar visibilidad en fondos claros
                    drawLine(
                        color = Color(0xCC000000),
                        start = Offset(magnifierCenter.x - crosshairLength, magnifierCenter.y),
                        end = Offset(magnifierCenter.x - centerGap, magnifierCenter.y),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xCC000000),
                        start = Offset(magnifierCenter.x + centerGap, magnifierCenter.y),
                        end = Offset(magnifierCenter.x + crosshairLength, magnifierCenter.y),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xCC000000),
                        start = Offset(magnifierCenter.x, magnifierCenter.y - crosshairLength),
                        end = Offset(magnifierCenter.x, magnifierCenter.y - centerGap),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xCC000000),
                        start = Offset(magnifierCenter.x, magnifierCenter.y + centerGap),
                        end = Offset(magnifierCenter.x, magnifierCenter.y + crosshairLength),
                        strokeWidth = 3.dp.toPx()
                    )

                    // Líneas principales de la retícula
                    drawLine(
                        color = CropHandleColor,
                        start = Offset(magnifierCenter.x - crosshairLength, magnifierCenter.y),
                        end = Offset(magnifierCenter.x - centerGap, magnifierCenter.y),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = CropHandleColor,
                        start = Offset(magnifierCenter.x + centerGap, magnifierCenter.y),
                        end = Offset(magnifierCenter.x + crosshairLength, magnifierCenter.y),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = CropHandleColor,
                        start = Offset(magnifierCenter.x, magnifierCenter.y - crosshairLength),
                        end = Offset(magnifierCenter.x, magnifierCenter.y - centerGap),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = CropHandleColor,
                        start = Offset(magnifierCenter.x, magnifierCenter.y + centerGap),
                        end = Offset(magnifierCenter.x, magnifierCenter.y + crosshairLength),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Punto guía central
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = magnifierCenter
                    )
                }

                // Bisel y aro blanco de la lupa
                drawCircle(
                    color = Color.White,
                    radius = magnifierRadius,
                    style = Stroke(width = 3.5.dp.toPx()),
                    center = magnifierCenter
                )
                drawCircle(
                    color = CropPolygonStrokeColor,
                    radius = magnifierRadius - 2.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx()),
                    center = magnifierCenter
                )
            }
        }
    }
}
