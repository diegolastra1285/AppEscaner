# System Integration Contracts: Escaner v1

**Feature Branch**: `001-document-scanner`  
**Date**: 2026-11-02  
**Status**: Completed  

Este documento define los contratos de integración con el sistema operativo Android: almacenamiento público mediante Scoped Storage, intercambio seguro mediante FileProvider, hoja nativa de compartir (`Android Sharesheet`) y delegación en visores externos de PDF.

---

## 1. Contrato de Almacenamiento Local Público

### 1.1. Ubicación y Estructura de Directorios

- **Ruta de Almacenamiento**: `Documentos/Escaner/` (en el almacenamiento compartido primario del dispositivo).
- **Formato del archivo**: Formato estándar Adobe PDF (`application/pdf`).
- **Nomenclatura Automática**: `Escaner_AAAAMMDD_HHMMSS.pdf`
  - Ejemplo: `Escaner_20261102_101530.pdf`
  - Generado a partir de la fecha y hora local del sistema en el momento de pulsar "Guardar PDF".

### 1.2. Interfaz de Escritura (Android 10+ / API 29+)

Utiliza la colección `MediaStore.Files` sin requerir permisos de almacenamiento en tiempo de ejecución:

```kotlin
val contentValues = ContentValues().apply {
    put(MediaStore.MediaColumns.DISPLAY_NAME, "Escaner_20261102_101530.pdf")
    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/Escaner")
    put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
}
val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
```

### 1.3. Interfaz de Escritura de Respaldo (Android 8.0 - 9.0 / API 26-28)

En versiones anteriores a Android 10 con permiso `WRITE_EXTERNAL_STORAGE` concedido:

```kotlin
val docsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Escaner")
if (!docsDir.exists()) docsDir.mkdirs()
val file = File(docsDir, "Escaner_20261102_101530.pdf")
val outputStream = FileOutputStream(file)
```

### 1.4. Interfaz de Consulta de Historial

Consulta los archivos existentes en la carpeta `Documents/Escaner/`:
- **URI de consulta**: `MediaStore.Files.getContentUri("external")`
- **Proyección**: `[_ID, DISPLAY_NAME, SIZE, DATE_MODIFIED, DATE_ADDED]`
- **Selección**: `RELATIVE_PATH LIKE ? OR RELATIVE_PATH = ?` con argumentos `["Documents/Escaner/%", "Documents/Escaner"]` y `MIME_TYPE = 'application/pdf'`
- **Orden**: `DATE_ADDED DESC` (los más recientes primero).

---

## 2. Contrato de Compartir (`Android Sharesheet`)

### 2.1. Proveedor de Archivos (`FileProvider`)

Para cumplir con las políticas de seguridad de Android (`FileUriExposedException`), el archivo se comparte mediante un URI `content://` otorgado a través de `androidx.core.content.FileProvider`.

- **Definición en AndroidManifest.xml**:
  ```xml
  <provider
      android:name="androidx.core.content.FileProvider"
      android:authorities="${applicationId}.fileprovider"
      android:exported="false"
      android:grantUriPermissions="true">
      <meta-data
          android:name="android.support.FILE_PROVIDER_PATHS"
          android:resource="@xml/file_paths" />
  </provider>
  ```

- **Contrato de Intención (Sharesheet)**:
  ```kotlin
  val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "application/pdf"
      putExtra(Intent.EXTRA_STREAM, fileUri)
      putExtra(Intent.EXTRA_SUBJECT, fileName)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  }
  val chooser = Intent.createChooser(shareIntent, "Compartir documento")
  context.startActivity(chooser)
  ```

- **Criterio de Aceptación**: Despliegue en menos de 1 segundo (SC-005) sin intermediarios ni marcas de agua.

---

## 3. Contrato de Visualización Externa de PDF

### 3.1. Delegación en Visores del Sistema

La aplicación delega la apertura del documento al visor PDF configurado por el usuario en su teléfono:

```kotlin
val viewIntent = Intent(Intent.ACTION_VIEW).apply {
    setDataAndType(fileUri, "application/pdf")
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

try {
    context.startActivity(viewIntent)
} catch (e: ActivityNotFoundException) {
    // Si el usuario no tiene ninguna app para leer PDF instalada
    mostrarMensajeError("No se encontró ninguna aplicación instalada para abrir archivos PDF.")
}
```

---

## 4. Contrato de Operación Local (Sin Red)

### 4.1. Restricción Absoluta de Conectividad

- **Permiso de Internet**: El manifiesto `AndroidManifest.xml` **NO** incluye `<uses-permission android:name="android.permission.INTERNET" />`.
- **Garantía Técnica**: Cualquier intento de apertura de sockets o conexiones HTTP por parte de cualquier componente es bloqueado a nivel de sandbox del kernel por el propio sistema operativo Android.
