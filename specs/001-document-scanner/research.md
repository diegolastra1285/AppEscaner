# Research: Escaner v1 - Escaneo de Documentos y Exportación a PDF

**Feature Branch**: `001-document-scanner`  
**Date**: 2026-11-02  
**Status**: Completed  

Este documento consolida las decisiones arquitectónicas, tecnológicas y de implementación tomadas para cumplir con los requisitos funcionales de `spec.md` y los 7 principios fundamentales de la Constitución del proyecto.

---

## 1. Plataforma, Lenguaje y Framework de UI

- **Decisión**: **Kotlin 2.0+ con Jetpack Compose y Material 3**, targeting Android SDK 35 (Android 15) con compatibilidad mínima Android 8.0 Oreo (API 26).
- **Racional**:
  - **Alineación con la Constitución**: Cumple con el *Principio I (Simplicidad ante todo)* y la restricción *Enfoque Nativo Móvil*. Jetpack Compose permite construir interfaces fluidas y reactivas sin la sobrecarga ni la complejidad de puentes multiplataforma.
  - **Acceso directo a APIs del SO**: Acceso sin intermediarios a CameraX, `android.graphics.pdf.PdfDocument`, `android.graphics.Matrix`, `MediaStore` y `FileProvider`.
  - **Rendimiento**: Cero sobrecarga de motores JavaScript o máquinas virtuales adicionales (como Dart o Node), lo cual garantiza el arranque veloz y el cumplimiento del criterio de éxito SC-001 (< 10 segundos para escanear y generar PDF).
- **Alternativas consideradas**:
  - *React Native / Expo*: Descartado. Introduce dependencias de runtime JS pesadas, mayor tamaño de APK (+30MB), latencia en el procesamiento de imágenes y complicaciones con plugins nativos de cámara y scoped storage.
  - *Flutter*: Descartado. Introduce el runtime de Dart, componentes gráficos no nativos del sistema y plugins de terceros para la cámara y el PDF que aumentan la fragilidad del mantenimiento.
  - *Android Views clásico (XML)*: Descartado frente a Jetpack Compose por mayor verbosidad de código y duplicidad de lógica de bindings, aunque mantendría la natividad. Compose es la vía estándar recomendada oficialmente por Android.

---

## 2. Captura de Imagen y Control de Hardware (Cámara y Linterna)

- **Decisión**: **Android Jetpack CameraX** (`androidx.camera:camera-core`, `androidx.camera:camera-camera2`, `androidx.camera:camera-lifecycle`, `androidx.camera:camera-view`).
- **Racional**:
  - **Disparo manual (FR-002)**: Configuración del caso de uso `ImageCapture` con `CAPTURE_MODE_MAXIMIZE_QUALITY` para captura en alta resolución tras pulsar el disparador.
  - **Control de linterna (FR-019)**: CameraX abstrae el control de la unidad de flash de forma reactiva mediante `cameraControl.enableTorch(Boolean)` y permite consultar la presencia de la unidad mediante `cameraInfo.hasFlashUnit()`. Si no tiene flash, el botón se oculta o deshabilita transparentemente sin errores.
  - **Ciclo de vida**: Vinculación automática con `LifecycleOwner` de Compose/Activity, pausando la cámara al pasar a segundo plano (edge case de llamadas entrantes o cambio de app).
- **Alternativas consideradas**:
  - *Camera2 API directa*: Descartada por complejidad excesiva, manejo manual de hilos, buffers y alta fragmentación entre fabricantes de hardware Android (violaría el Principio I).
  - *Camera 1 API*: Descartada por estar obsoleta (deprecated) y no soportar adecuadamente dispositivos modernos.
  - *Google ML Kit Document Scanner pre-built UI*: Descartada. Dicha librería impone un flujo cerrado y propietario controlado por Google Play Services, requiriendo dependencias opacas y privándonos del control sobre las pantallas especificadas (FR-001, FR-003, FR-017).

---

## 3. Detección de Bordes y Corrección de Perspectiva (Enderezado Plano)

- **Decisión**: **Detección de contornos basada en contraste/gradiente en miniatura de procesamiento + Corrección de perspectiva mediante `android.graphics.Matrix.setPolyToPoly`**.
- **Racional**:
  - **Matriz de transformación nativa**: Android incluye desde el API 1 el método `Matrix.setPolyToPoly(src, 0, dst, 0, 4)`. Al pasarle 4 puntos de origen (`src`: las 4 esquinas del documento) y 4 puntos de destino (`dst`: `[0, 0, W, 0, W, H, 0, H]`), calcula la matriz de proyección de perspectiva 2D exacta. Al dibujar el bitmap con dicha matriz sobre un `Canvas`, se obtiene la imagen enderezada y rectificada sin recurrir a librerías externas.
  - **Detección automática de esquinas**: Para la detección inicial (FR-002), se escala temporalmente la captura a una imagen pequeña de baja resolución (ej. 600px de ancho) donde se aplican umbrales de luminancia/gradientes para hallar el cuadrilátero dominante de la hoja.
  - **Alineación con Edge Case de bajo contraste**: Si el fondo carece de contraste o el algoritmo no distingue bordes con alta certidumbre, el sistema toma por defecto las 4 esquinas del encuadre con un margen del 5% de seguridad hacia adentro (`fromBounds(W, H, 0.05f)`), habilitando la pantalla de ajuste manual interactivo (FR-003).
  - **Cero dependencias C++ pesadas**: Elimina la necesidad de incluir OpenCV o binarios `.so`, manteniendo el APK ligero y respetando la *Justificación de Dependencias* de la Constitución.
- **Alternativas consideradas**:
  - *OpenCV para Android*: Descartado. Añade entre 25 MB y 40 MB de binarios nativos C++ (`.so`) para cada arquitectura (arm64-v8a, armeabi-v7a, x86_64), aumentando exponencialmente el tamaño de descarga y la complejidad del build.

---

## 4. Modos de Color y Procesamiento Visual

- **Decisión**: **Filtros visuales nativos mediante `android.graphics.ColorMatrix`, `ColorMatrixColorFilter` y umbralizado adaptativo (Binarización)**.
- **Racional**:
  - **Modo Blanco y Negro / Alto Contraste (Predeterminado)**:
    - Diseñado para facturas, recibos y remisiones con sellos o letras tenues.
    - Aplica un filtro de contraste elevado y umbralizado de luminancia para asegurar que los fondos de papel queden completamente blancos (#FFFFFF) y los trazos, sellos y textos queden en negro nítido (#000000), maximizando la legibilidad y reduciendo el peso final del archivo.
  - **Modo Color Original Optimizado**:
    - Aplica una curva suave de contraste y balance de niveles para corregir sombras tenues de la cámara manteniendo los colores reales del documento.
  - **Modo Escala de Grises**:
    - Aplica la matriz de desaturación estándar de luminancia (`Y = 0.299*R + 0.587*G + 0.114*B`).
  - **Rotación en 90° (FR-018)**:
    - Aplicación directa de `Matrix().apply { postRotate(90f) }` sobre el `Bitmap` antes de confirmar la página.
- **Alternativas consideradas**:
  - *RenderScript*: Descartado por estar desaprobado (deprecated) desde Android 12.
  - *Shaders AGSL*: Descartados por requerir Android 13+ (API 33), lo que excluiría versiones anteriores de Android.

---

## 5. Compilación y Generación de PDF

- **Decisión**: **API nativa `android.graphics.pdf.PdfDocument`**.
- **Racional**:
  - **Nativa y ligera**: Incluida de serie en el SDK de Android desde API 19. Cero bibliotecas externas añadidas al proyecto.
  - **Manejo de páginas secuenciales (FR-006)**: Cada página procesada se dibuja en un `Canvas` provisto por `PdfDocument.PageInfo.Builder(width, height, pageNumber).create()`.
  - **Consumo de memoria controlado (SC-006)**: Para soportar lotes de hasta 20 páginas sin error de OutOfMemory (OOM), las imágenes se procesan secuencialmente y se escriben directamente al flujo de salida (`FileOutputStream`), liberando los bitmaps temporales de memoria tras su renderizado en el PDF.
- **Alternativas consideradas**:
  - *iText / PDFBox-Android*: Descartado. Requiere licencias complejas (AGPL/comercial), aumenta el tamaño del APK y agrega dependencias innecesarias frente a la solución nativa del sistema.

---

## 6. Persistencia y Almacenamiento Público (`Documentos/Escaner/`)

- **Decisión**: **Almacenamiento público en `Documents/Escaner` mediante `MediaStore.Files` en Android 10+ (API 29+) y `Environment.getExternalStoragePublicDirectory` en Android 8.0-9.0 (API 26-28)**.
- **Racional**:
  - **Cumplimiento del Principio VII y SC-003**: Los archivos quedan guardados físicamente en la carpeta pública `Documentos/Escaner/` del almacenamiento interno del teléfono. Cualquier explorador de archivos nativo (como "Mis Archivos" de Samsung, "Files by Google" o exploradores estándar) puede listarlos, abrirlos y moverlos directamente.
  - **Permisos limpios (Scoped Storage)**: En Android 10 y superior, guardar y modificar archivos creados por la propia app en la colección `Documents` de `MediaStore` **no requiere permisos en tiempo de ejecución de almacenamiento**.
  - **Nomenclatura (FR-008)**: Nombres automáticos con formato estricto `Escaner_AAAAMMDD_HHMMSS.pdf` basados en el reloj del sistema.
  - **Gestión de archivos (FR-009, FR-015, FR-016)**:
    - Listado: Consulta a `MediaStore.Files` filtrando por ruta relativa `Documents/Escaner/` ordenada por `DATE_ADDED DESC`.
    - Eliminación: Invocación de `contentResolver.delete(uri, null, null)` borrando el archivo físico.
    - Renombrado: Actualización de `MediaStore.MediaColumns.DISPLAY_NAME` previa validación de nombre único y caracteres válidos.
- **Alternativas consideradas**:
  - *Almacenamiento privado de la app (`context.filesDir`)*: Descartado rotundamente. Violaría el Principio VII ya que los documentos quedarían ocultos y se borrarían si el usuario desinstala la aplicación.

---

## 7. Compartir y Visualización Nativa

- **Decisión**: **`FileProvider` con `Intent.ACTION_SEND` (Android Sharesheet) y `Intent.ACTION_VIEW`**.
- **Racional**:
  - **Hojas nativas (FR-011, FR-012, SC-005)**: Despliegue inmediato en < 1 segundo de la hoja de compartir del sistema (`Android Sharesheet`) adjuntando el `Uri` público seguro (`content://...`) con flag `FLAG_GRANT_READ_URI_PERMISSION`.
  - **Apertura de PDF**: Intención `ACTION_VIEW` con tipo MIME `application/pdf`, delegando la lectura al visor predeterminado del teléfono (Google Drive PDF, visor del fabricante, etc.).
  - **Sin intermediarios**: Cero visores embebidos propietarios o librerías de renderizado de lectura dentro de la app (Principio I).

---

## 8. Privacidad Absoluta y Cero Conexión

- **Decisión**: **Omisión total de `android.permission.INTERNET` en el `AndroidManifest.xml`**.
- **Racional**:
  - Garantiza a nivel de sistema operativo y seguridad que la aplicación es incapaz de enviar datos, documentos o estadísticas a internet, cumpliendo sin excepciones con el *Principio II (Cero publicidad)*, *Principio III (Operación local y privacidad absoluta)* y el criterio *SC-002 (100% funcional en Modo Avión)*.
