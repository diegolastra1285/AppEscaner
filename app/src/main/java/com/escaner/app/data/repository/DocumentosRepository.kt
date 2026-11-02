package com.escaner.app.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.graphics.pdf.PdfRenderer
import androidx.core.content.FileProvider
import com.escaner.app.data.model.DocumentoPDF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DocumentosRepository(private val context: Context) {

    private val applicationContext = context.applicationContext

    suspend fun obtenerDocumentos(): List<DocumentoPDF> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<DocumentoPDF>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.RELATIVE_PATH
            )

            val selection = "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ? AND ${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
            val selectionArgs = arrayOf("%Documents/Escaner%", "application/pdf")
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

            try {
                applicationContext.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                    val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn) ?: "Documento.pdf"
                        val size = cursor.getLong(sizeColumn)
                        val dateAddedSec = cursor.getLong(dateAddedColumn)
                        val relPath = cursor.getString(pathColumn) ?: "Documents/Escaner/"
                        val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)

                        val pageCount = contarPaginasPdf(contentUri)

                        if (DocumentoPDF.validarNombreArchivo(name)) {
                            lista.add(
                                DocumentoPDF(
                                    id = id,
                                    nombre = name,
                                    uri = contentUri,
                                    rutaFisica = "$relPath$name",
                                    fechaCreacion = if (dateAddedSec > 0) dateAddedSec * 1000 else System.currentTimeMillis(),
                                    tamanoBytes = size,
                                    cantidadPaginas = pageCount
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback for API 26-28 or if query returned empty while physical files exist in Documents/Escaner
        if (lista.isEmpty()) {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Escaner")
            if (dir.exists() && dir.isDirectory) {
                val files = dir.listFiles { file -> file.isFile && file.name.endsWith(".pdf", ignoreCase = true) }
                files?.sortedByDescending { it.lastModified() }?.forEachIndexed { index, file ->
                    val uri = try {
                        FileProvider.getUriForFile(
                            applicationContext,
                            "${applicationContext.packageName}.fileprovider",
                            file
                        )
                    } catch (e: Exception) {
                        Uri.fromFile(file)
                    }

                    val pageCount = contarPaginasArchivo(file)

                    if (DocumentoPDF.validarNombreArchivo(file.name)) {
                        lista.add(
                            DocumentoPDF(
                                id = index.toLong() + 1,
                                nombre = file.name,
                                uri = uri,
                                rutaFisica = file.absolutePath,
                                fechaCreacion = file.lastModified(),
                                tamanoBytes = file.length(),
                                cantidadPaginas = pageCount
                            )
                        )
                    }
                }
            }
        }

        lista
    }

    suspend fun eliminarDocumento(documento: DocumentoPDF): Boolean = withContext(Dispatchers.IO) {
        var eliminado = false
        try {
            val rows = applicationContext.contentResolver.delete(documento.uri, null, null)
            if (rows > 0) eliminado = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val file = File(documento.rutaFisica)
        if (file.exists()) {
            val fileDeleted = file.delete()
            eliminado = eliminado || fileDeleted
        }

        eliminado
    }

    suspend fun renombrarDocumento(documento: DocumentoPDF, nuevoNombreBase: String): Boolean = withContext(Dispatchers.IO) {
        val nuevoNombreConExtension = if (nuevoNombreBase.endsWith(".pdf", ignoreCase = true)) {
            nuevoNombreBase
        } else {
            "$nuevoNombreBase.pdf"
        }

        if (!DocumentoPDF.validarNombreArchivo(nuevoNombreConExtension)) {
            return@withContext false
        }

        var renombrado = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nuevoNombreConExtension)
                }
                val updated = applicationContext.contentResolver.update(documento.uri, values, null, null)
                if (updated > 0) renombrado = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val originalFile = File(documento.rutaFisica)
        if (originalFile.exists()) {
            val destinoFile = File(originalFile.parentFile, nuevoNombreConExtension)
            if (!destinoFile.exists()) {
                val fileRenamed = originalFile.renameTo(destinoFile)
                renombrado = renombrado || fileRenamed
            }
        }

        renombrado
    }

    suspend fun limpiarCacheTemporal() = withContext(Dispatchers.IO) {
        try {
            val cacheDir = applicationContext.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("scan_temp_") || file.name.endsWith(".jpg") || file.name.endsWith(".tmp")) {
                    file.delete()
                }
            }
            applicationContext.externalCacheDir?.listFiles()?.forEach { file ->
                if (file.name.startsWith("scan_temp_") || file.name.endsWith(".jpg") || file.name.endsWith(".tmp")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun contarPaginasPdf(uri: Uri): Int {
        return try {
            applicationContext.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    renderer.pageCount.coerceAtLeast(1)
                }
            } ?: 1
        } catch (e: Exception) {
            1
        }
    }

    private fun contarPaginasArchivo(file: File): Int {
        return try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    renderer.pageCount.coerceAtLeast(1)
                }
            }
        } catch (e: Exception) {
            1
        }
    }
}
