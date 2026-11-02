package com.escaner.app.domain

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.escaner.app.data.model.DocumentoPDF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfCompiler {

    fun generarNombreArchivo(timestamp: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val formattedDate = sdf.format(Date(timestamp))
        return "Escaner_$formattedDate.pdf"
    }

    suspend fun compilarPdf(
        context: Context,
        bitmaps: List<Bitmap>,
        timestamp: Long = System.currentTimeMillis()
    ): Result<DocumentoPDF> = withContext(Dispatchers.IO) {
        if (bitmaps.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("No hay páginas para compilar"))
        }

        val nombreArchivo = generarNombreArchivo(timestamp)
        val appContext = context.applicationContext

        val pdfDocument = PdfDocument()

        try {
            // Render each page sequentially into the PdfDocument
            bitmaps.forEachIndexed { index, bitmap ->
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)

                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
                page.canvas.drawBitmap(bitmap, 0f, 0f, paint)

                pdfDocument.finishPage(page)
            }

            var outUri: Uri? = null
            var rutaFisica: String = ""
            var fileSize: Long = 0

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/Escaner")
                    put(MediaStore.MediaColumns.DATE_ADDED, timestamp / 1000)
                }

                val uri = appContext.contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    contentValues
                ) ?: throw IllegalStateException("No se pudo crear el archivo en MediaStore")

                appContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }

                outUri = uri
                rutaFisica = "${Environment.DIRECTORY_DOCUMENTS}/Escaner/$nombreArchivo"

                // Query size
                appContext.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    fileSize = pfd.statSize
                }
            } else {
                // Android 8.0 - 9.0 (API 26-28)
                val docsDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "Escaner"
                )
                if (!docsDir.exists()) {
                    docsDir.mkdirs()
                }

                val targetFile = File(docsDir, nombreArchivo)
                FileOutputStream(targetFile).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }

                fileSize = targetFile.length()
                rutaFisica = targetFile.absolutePath

                outUri = try {
                    FileProvider.getUriForFile(
                        appContext,
                        "${appContext.packageName}.fileprovider",
                        targetFile
                    )
                } catch (e: Exception) {
                    Uri.fromFile(targetFile)
                }
            }

            val doc = DocumentoPDF(
                id = timestamp,
                nombre = nombreArchivo,
                uri = outUri ?: Uri.EMPTY,
                rutaFisica = rutaFisica,
                fechaCreacion = timestamp,
                tamanoBytes = fileSize,
                cantidadPaginas = bitmaps.size
            )

            Result.success(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            try {
                pdfDocument.close()
            } catch (ignored: Exception) {}
        }
    }
}
