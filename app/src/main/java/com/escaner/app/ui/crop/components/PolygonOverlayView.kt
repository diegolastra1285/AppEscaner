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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(bitmap, polygon) {
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()

                    // Compute scale and translation of image fit in canvas
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

                    val touchTargetRadiusPx = 48.dp.toPx()

                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val c0 = imageToCanvas(polygon.topLeft)
                            val c1 = imageToCanvas(polygon.topRight)
                            val c2 = imageToCanvas(polygon.bottomRight)
                            val c3 = imageToCanvas(polygon.bottomLeft)

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
                                onCornerDrag(draggingCornerIndex, imgPoint)
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

            // Draw image scaled
            drawImage(
                image = bitmap.asImageBitmap(),
                dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
                dstSize = IntSize(imgDestWidth, imgDestHeight)
            )

            // Convert corners to canvas coordinates
            val p0 = Offset(polygon.topLeft.x * scale + offsetX, polygon.topLeft.y * scale + offsetY)
            val p1 = Offset(polygon.topRight.x * scale + offsetX, polygon.topRight.y * scale + offsetY)
            val p2 = Offset(polygon.bottomRight.x * scale + offsetX, polygon.bottomRight.y * scale + offsetY)
            val p3 = Offset(polygon.bottomLeft.x * scale + offsetX, polygon.bottomLeft.y * scale + offsetY)

            // Draw outer dim mask
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

            // Draw dim overlay everywhere outside the polygon
            drawPath(path = fullPath, color = CropDimOverlayColor)
            // Restore clear inside polygon
            drawPath(path = polyPath, color = Color.Transparent)

            // Draw polygon boundary lines
            drawPath(
                path = polyPath,
                color = CropPolygonStrokeColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw 4 corner handles
            val handleRadius = 14.dp.toPx()
            val handleOuterBorder = 3.dp.toPx()

            listOf(p0, p1, p2, p3).forEachIndexed { index, corner ->
                val isSelected = index == draggingCornerIndex
                val radius = if (isSelected) handleRadius * 1.3f else handleRadius

                // White border
                drawCircle(
                    color = Color.White,
                    radius = radius + handleOuterBorder,
                    center = corner
                )
                // Colored center handle
                drawCircle(
                    color = CropHandleColor,
                    radius = radius,
                    center = corner
                )
            }
        }
    }
}
