package com.escaner.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.escaner.app.data.model.DocumentoPDF
import com.escaner.app.data.repository.DocumentosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DocumentosRepository(application)

    private val _documentos = MutableStateFlow<List<DocumentoPDF>>(emptyList())
    val documentos: StateFlow<List<DocumentoPDF>> = _documentos.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        cargarDocumentos()
    }

    fun cargarDocumentos() {
        viewModelScope.launch {
            _isLoading.value = true
            val docs = repository.obtenerDocumentos()
            _documentos.value = docs
            _isLoading.value = false
        }
    }

    fun eliminarDocumento(documento: DocumentoPDF) {
        viewModelScope.launch {
            repository.eliminarDocumento(documento)
            cargarDocumentos()
        }
    }

    fun renombrarDocumento(documento: DocumentoPDF, nuevoNombre: String) {
        viewModelScope.launch {
            repository.renombrarDocumento(documento, nuevoNombre)
            cargarDocumentos()
        }
    }

    fun verificarExiste(documento: DocumentoPDF): Boolean {
        val file = File(documento.rutaFisica)
        return file.exists() || documento.uri != android.net.Uri.EMPTY
    }
}
