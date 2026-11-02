# Implementation Plan: Escaner v1 - Escaneo de Documentos y Exportación a PDF

**Branch**: `001-document-scanner` | **Date**: 2026-11-02 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-document-scanner/spec.md`

## Summary

Escaner v1 es una herramienta nativa para Android orientada a la digitalización rápida, limpia y privada de facturas, remisiones y comprobantes físicos. El enfoque técnico implementa una arquitectura nativa en Kotlin con Jetpack Compose y CameraX para la captura manual y control de linterna, corrección de perspectiva mediante proyección 2D nativa (`android.graphics.Matrix.setPolyToPoly`), procesamiento visual de alto contraste/color, compilación secuencial a formato estándar PDF mediante `android.graphics.pdf.PdfDocument`, y persistencia local pública en `Documentos/Escaner/` usando Scoped Storage (`MediaStore.Files`) con integración directa a la hoja nativa de compartir (`Android Sharesheet`).

## Technical Context

**Language/Version**: Kotlin 2.0+ / Android SDK (Min API 26: Android 8.0 Oreo, Target API 35: Android 15), JDK 17.

**Primary Dependencies**:
- Android Jetpack Compose BOM & Material 3 (UI declarativa nativa).
- Jetpack CameraX 1.4+ (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) para captura y control de hardware.
- Android Graphics (`android.graphics.Matrix`, `android.graphics.pdf.PdfDocument`, `ColorMatrix`) para transformación geométrica, filtrado y compilación PDF sin librerías externas.
- AndroidX Core & FileProvider (`androidx.core.content.FileProvider`) para compartir seguro.
- Kotlin Coroutines & StateFlow para reactividad y manejo asíncrono.
- *Cero librerías de analítica, publicidad, red o servicios en la nube*.

**Storage**: Local Scoped Storage (`MediaStore.Files` en `Documents/Escaner` para Android 10+ / API 29+ y almacenamiento público estándar para API 26-28). Consulta directa de archivos físicos para sincronización inmediata del historial.

**Testing**: JUnit 4 / JUnit 5 + AndroidX Test (Robolectric para tests unitarios de modelos y lógica de transformación, Compose UI Test para componentes visuales).

**Target Platform**: Teléfonos móviles Android (API 26+).

**Project Type**: Aplicación móvil nativa Android (`mobile-app`).

**Performance Goals**:
- Tiempo de captura a PDF guardado: < 10 segundos para 1 página (SC-001).
- Despliegue de Sharesheet de compartir: < 1 segundo (SC-005).
- Lote multipágina: Soporte estable de hasta 20 páginas consecutivas sin OutOfMemory (SC-006).

**Constraints**:
- Operación 100% fuera de línea: Sin permiso `android.permission.INTERNET` en el manifiesto (Principio II, III, SC-002).
- Cero publicidad, cobros o interrupciones (Principio II, SC-004).
- Almacenamiento visible y accesible en `Documentos/Escaner/` (Principio VII, SC-003).
- Textos e interfaz en español claro y adaptado (Principio V).

**Scale/Scope**:
- 4 pantallas de flujo: `HomeScreen` (Historial), `CameraScreen` (Captura), `CropScreen` (Ajuste de esquinas), `ReviewScreen` (Revisión y Lote).
- 4 diálogos modales: `ConfirmDeleteDialog`, `RenameDialog`, `ConfirmDiscardBatchDialog`, `PermissionDeniedDialog`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio Constitucional | Requisito / Restricción | Estado | Justificación / Mecanismo de Cumplimiento |
| :--- | :--- | :--- | :--- |
| **I. Simplicidad ante todo** | Prohibida sobreingeniería; elegir la solución más simple | **PASS** | Uso exclusivo de APIs nativas de Android (`Matrix.setPolyToPoly`, `PdfDocument`). Cero C++ / OpenCV o runtimes ajenos. |
| **II. Cero publicidad y cero intrusión** | Cero anuncios, paywalls, suscripciones o ratings | **PASS** | No se incluye ninguna dependencia de publicidad ni SDKs comerciales. |
| **III. Operación local y privacidad absoluta** | 100% en el teléfono, sin cuentas ni servidores | **PASS** | El manifiesto omite intencionalmente el permiso `INTERNET`. Las fotos y PDFs nunca abandonan el dispositivo. |
| **IV. Cero alcance fantasma (No Phantom Scope)** | Solo lo especificado en `spec.md` (FR-001 a FR-019) | **PASS** | Todas las pantallas y entidades coinciden exactamente con los 19 requisitos funcionales aprobados. |
| **V. Idioma y contexto local** | Español claro adaptado a Colombia | **PASS** | Todas las cadenas de interfaz y mensajes están definidos en español cotidiano (`res/values/strings.xml`). |
| **VI. Verificable por persona no técnica** | Criterios comprobables en mano con teléfono | **PASS** | Escenarios paso a paso documentados en [quickstart.md](./quickstart.md) ejecutables con el teléfono en mano. |
| **VII. Respeto por el almacenamiento** | Guardar en ubicaciones estándar visibles | **PASS** | Persistencia directa en `Documentos/Escaner/` mediante `MediaStore.Files`, accesible desde "Mis Archivos". |
| **Restricciones de Plataforma** | Enfoque nativo móvil, flujo directo a PDF, independencia de red | **PASS** | Arquitectura nativa Kotlin + Jetpack Compose con compilación directa a PDF y Modo Avión total. |

*Evaluación de Gates: APROBADO sin excepciones.*

## Project Structure

### Documentation (this feature)

```text
specs/001-document-scanner/
├── plan.md              # Este plan de implementación
├── research.md          # Investigación y decisiones técnicas (Fase 0)
├── data-model.md        # Modelos de datos, ciclo de vida y transiciones (Fase 1)
├── quickstart.md        # Guía de validación y escenarios de prueba (Fase 1)
├── contracts/           # Contratos de interfaces
│   ├── ui-contracts.md      # Contratos de UI, pantallas y eventos
│   └── system-contracts.md  # Contratos de integración Android (Storage, Sharesheet, Intents)
└── tasks.md             # Tareas de implementación desglosadas (Fase 2 - /speckit-tasks)
```

### Source Code Layout (repository root)

```text
app/
├── build.gradle.kts
├── proguard-rules.pro
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── java/com/escaner/app/
    │   │   ├── MainActivity.kt
    │   │   ├── EscanerApplication.kt
    │   │   ├── data/
    │   │   │   ├── model/
    │   │   │   │   ├── DocumentoPDF.kt
    │   │   │   │   ├── SesionLote.kt
    │   │   │   │   ├── PaginaDocumento.kt
    │   │   │   │   ├── PoligonoCuatroPuntos.kt
    │   │   │   │   ├── ModoColor.kt
    │   │   │   │   └── RotacionGrados.kt
    │   │   │   └── repository/
    │   │   │       └── DocumentosRepository.kt
    │   │   ├── domain/
    │   │   │   ├── EdgeDetector.kt
    │   │   │   ├── PerspectiveCorrector.kt
    │   │   │   ├── ImageColorFilter.kt
    │   │   │   └── PdfCompiler.kt
    │   │   └── ui/
    │   │       ├── theme/
    │   │       │   ├── Color.kt
    │   │       │   ├── Theme.kt
    │   │       │   └── Type.kt
    │   │       ├── home/
    │   │       │   ├── HomeScreen.kt
    │   │       │   ├── HomeViewModel.kt
    │   │       │   └── components/
    │   │       │       ├── DocumentoItem.kt
    │   │       │       ├── EmptyHistoryView.kt
    │   │       │       ├── RenameDialog.kt
    │   │       │       └── ConfirmDeleteDialog.kt
    │   │       ├── camera/
    │   │       │   ├── CameraScreen.kt
    │   │       │   ├── CameraViewModel.kt
    │   │       │   └── components/
    │   │       │       └── CameraControls.kt
    │   │       ├── crop/
    │   │       │   ├── CropScreen.kt
    │   │       │   ├── CropViewModel.kt
    │   │       │   └── components/
    │   │       │       └── PolygonOverlayView.kt
    │   │       ├── review/
    │   │       │   ├── ReviewScreen.kt
    │   │       │   ├── ReviewViewModel.kt
    │   │       │   └── components/
    │   │       │       ├── ColorSelectorBar.kt
    │   │       │       ├── ThumbnailStrip.kt
    │   │       │       └── ConfirmDiscardBatchDialog.kt
    │   │       └── navigation/
    │   │           └── AppNavigation.kt
    │   └── res/
    │       ├── drawable/
    │       ├── mipmap/
    │       ├── values/
    │       │   ├── colors.xml
    │       │   ├── strings.xml
    │       │   └── themes.xml
    │       └── xml/
    │           └── file_paths.xml
    ├── test/
    │   └── java/com/escaner/app/
    │       ├── domain/
    │       │   ├── PerspectiveCorrectorTest.kt
    │       │   ├── ImageColorFilterTest.kt
    │       │   └── PdfCompilerTest.kt
    │       └── data/
    │           └── DocumentoPDFValidationTest.kt
    └── androidTest/
        └── java/com/escaner/app/
            └── ui/
                └── HomeScreenUiTest.kt
gradle/
├── wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
build.gradle.kts
settings.gradle.kts
```

**Structure Decision**: Se elige la estructura estándar nativa de Android de un solo módulo de aplicación (`app/`), organizada limpiamente por capas (`data`, `domain`, `ui`) y por funcionalidad en la capa de interfaz (`home`, `camera`, `crop`, `review`). Esta estructura maximiza la claridad, evita la sobreingeniería de multi-módulos innecesarios en un MVP (cumpliendo el Principio I) y garantiza una compilación rápida.

## Complexity Tracking

*N/A - Ninguna violación detectada; el diseño se adhiere 100% a la constitución del proyecto y aprovecha las herramientas nativas del SDK de Android.*
