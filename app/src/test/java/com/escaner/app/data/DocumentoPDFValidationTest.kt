package com.escaner.app.data

import com.escaner.app.data.model.DocumentoPDF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentoPDFValidationTest {

    @Test
    fun validarNombreBase_nombresValidos() {
        assertTrue(DocumentoPDF.validarNombreBase("Factura_123"))
        assertTrue(DocumentoPDF.validarNombreBase("Recibo luz enero"))
        assertTrue(DocumentoPDF.validarNombreBase("Escaner_20261102_101530"))
        assertTrue(DocumentoPDF.validarNombreBase("remision-456"))
    }

    @Test
    fun validarNombreBase_nombresInvalidos() {
        assertFalse(DocumentoPDF.validarNombreBase(""))
        assertFalse(DocumentoPDF.validarNombreBase("   "))
        assertFalse(DocumentoPDF.validarNombreBase("Factura/Enero"))
        assertFalse(DocumentoPDF.validarNombreBase("Archivo\\test"))
        assertFalse(DocumentoPDF.validarNombreBase("Documento:1"))
        assertFalse(DocumentoPDF.validarNombreBase("Comprobante*"))
        assertFalse(DocumentoPDF.validarNombreBase("Pregunta?"))
        assertFalse(DocumentoPDF.validarNombreBase("Texto\"comillas"))
        assertFalse(DocumentoPDF.validarNombreBase("Etiqueta<1>"))
        assertFalse(DocumentoPDF.validarNombreBase("Pipe|Separador"))
    }

    @Test
    fun validarNombreArchivo_extensionPdfRequerida() {
        assertTrue(DocumentoPDF.validarNombreArchivo("Escaner_20261102_101530.pdf"))
        assertTrue(DocumentoPDF.validarNombreArchivo("factura.PDF"))
        assertFalse(DocumentoPDF.validarNombreArchivo("factura.jpg"))
        assertFalse(DocumentoPDF.validarNombreArchivo("factura"))
        assertFalse(DocumentoPDF.validarNombreArchivo(".pdf"))
    }

    @Test
    fun formatearTamanoBytes_formateaCorrectamente() {
        assertEquals("500 B", DocumentoPDF.formatearTamanoBytes(500))
        assertTrue(DocumentoPDF.formatearTamanoBytes(1024).contains("KB"))
        assertTrue(DocumentoPDF.formatearTamanoBytes(450 * 1024).contains("KB"))
        assertTrue(DocumentoPDF.formatearTamanoBytes(2 * 1024 * 1024).contains("MB"))
    }
}
