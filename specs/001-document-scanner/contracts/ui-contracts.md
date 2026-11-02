# UI Contracts: Escaner v1

**Feature Branch**: `001-document-scanner`  
**Date**: 2026-11-02  
**Status**: Completed  

Este documento define los contratos de interfaz de usuario, flujos de navegación, eventos y estados visuales para cada una de las pantallas de la aplicación.

---

## 1. Mapa de Pantallas y Navegación

```text
[ HomeScreen (Historial) ]
       │
       ├── (FAB "Escanear") ────────────────────────┐
       │                                            │
       ▼                                            ▼
[ Diálogos de Gestión ]                    [ CameraScreen ]
- Renombrar archivo                                 │ (Disparo manual)
- Confirmar eliminación                             ▼
                                           [ CropScreen ] (Ajuste de bordes)
                                                    │ (Confirmar recorte)
                                                    ▼
                                           [ ReviewScreen ] (Revisión)
                                              ├── Alternar modo Color/BN/Gris
                                              ├── Rotar 90°
                                              ├── Eliminar miniatura
                                              ├── "Añadir página" ──► Regresa a CameraScreen
                                              └── "Guardar PDF" ──► Compila y guarda
                                                                           │
                                                                           ▼
                                                              [ SuccessScreen / Dialog ]
                                                                (Compartir / Volver)
```

---

## 2. Contratos de Pantalla

### 2.1. `HomeScreen` (Pantalla de Inicio / Historial)

- **Propósito**: Presentar la lista cronológica de documentos escaneados previamente y proveer acceso prioritario para iniciar un nuevo escaneo.
- **Componentes Visuales**:
  - `TopAppBar`: Título "Escaner" o "Mis Documentos", diseño limpio sin publicidad ni botones innecesarios.
  - `DocumentosList`: Lista vertical ordenada por fecha de creación decreciente (`DATE_ADDED DESC`).
    - Cada fila (`DocumentoItem`) contiene:
      - Icono de archivo PDF.
      - Título del archivo (ej. `Escaner_20261102_101530.pdf` o nombre personalizado).
      - Metadatos: Fecha formateada, tamaño formateado (ej. `450 KB`), cantidad de páginas.
      - Botón de acción `Compartir`: Despliega la hoja nativa de Android Sharesheet.
      - Menú contextual o botones visibles de acción: `Renombrar` y `Eliminar`.
  - `EmptyState`: Visible cuando no hay documentos (primer uso o historial vacío). Muestra una ilustración/icono amigable, texto *"Aún no tienes documentos escaneados"* y una indicación clara para pulsar el botón de escaneo.
  - `ScanFAB`: Botón de acción flotante (FAB) prominente con icono de cámara y etiqueta *"Escanear"*.
- **Eventos y Acciones**:
  - `onItemClick(documento: DocumentoPDF)`: Emite `Intent.ACTION_VIEW` con `contentUri` para abrir en visor de PDF externo.
  - `onShareClick(documento: DocumentoPDF)`: Emite `Intent.ACTION_SEND` a través del Sharesheet nativo del sistema.
  - `onRenameRequest(documento: DocumentoPDF)`: Despliega `RenameDialog`.
  - `onDeleteRequest(documento: DocumentoPDF)`: Despliega `ConfirmDeleteDialog`.
  - `onScanClick()`: Solicita permiso de cámara (si no está concedido) y navega a `CameraScreen`.

---

### 2.2. `CameraScreen` (Captura de Cámara)

- **Propósito**: Encuadre en tiempo real y captura fotográfica manual del documento físico.
- **Componentes Visuales**:
  - `CameraPreview`: Vista previa fluida de la cámara trasera a pantalla completa utilizando CameraX `PreviewView`.
  - `TopControlBar`:
    - Botón `Cerrar / Volver`: Si hay páginas en el lote activo, muestra advertencia de confirmación; si no, regresa a `HomeScreen`.
    - Botón `Linterna (Torch)`: Conmutable (icono de linterna apagada / encendida). Si el hardware no posee flash (`hasFlashUnit == false`), se oculta.
  - `BottomControlBar`:
    - Botón central `Disparador`: Botón circular prominente para captura manual (FR-002).
    - `BadgeContadorPaginas`: Si se está en un lote multipágina (ej. página 2 en adelante), muestra un indicador (ej. *"Página 2"* o *"3 páginas escaneadas"*).
- **Eventos y Acciones**:
  - `onCaptureClick()`: Ejecuta `ImageCapture.takePicture()`. Al obtener la foto, calcula la estimación inicial de bordes y navega de inmediato a `CropScreen`.
  - `onTorchToggle()`: Alterna el estado de `enableTorch`.
  - `onBackClick()`: Si hay páginas acumuladas, invoca `ConfirmDiscardBatchDialog`; de lo contrario, regresa a la pantalla anterior.

---

### 2.3. `CropScreen` (Reajuste Manual de Bordes y Perspectiva)

- **Propósito**: Mostrar la imagen capturada original con los 4 vértices del polígono de recorte y permitir su reajuste táctil interactivo.
- **Componentes Visuales**:
  - `ImageCanvas`: Muestra la fotografía completa con overlay semi-transparente que oscurece el fondo exterior.
  - `InteractivePolygonOverlay`:
    - Dibuja líneas conectoras entre las 4 esquinas (`topLeft`, `topRight`, `bottomRight`, `bottomLeft`).
    - 4 nodos de control táctiles (manijas circulares) agrandadas con respuesta háptica y área de toque amplia (mínimo 48dp) para arrastre con el dedo.
    - Lupa de precisión / Magnifier (opcional en UI para ver con claridad el vértice bajo el dedo).
  - `BottomBar`:
    - Botón `Restablecer / Toda la imagen`: Restablece los 4 puntos a los bordes de la toma con margen de seguridad.
    - Botón `Confirmar recorte`: Aplica `Matrix.setPolyToPoly` con las esquinas definidas y navega a `ReviewScreen`.
- **Eventos y Acciones**:
  - `onCornerDrag(corner: CornerIndex, newPosition: PointF)`: Actualiza la posición de la esquina garantizando que el polígono resultante sea convexo.
  - `onConfirmCrop()`: Genera la imagen rectificada en memoria/cache y avanza a `ReviewScreen`.

---

### 2.4. `ReviewScreen` (Revisión de Página y Gestión del Lote)

- **Propósito**: Inspeccionar la calidad de la página rectificada, ajustar contraste/color, rotar, eliminar páginas malas o añadir nuevas al lote.
- **Componentes Visuales**:
  - `MainPreview`: Visualización de alta resolución de la página procesada con zoom y desplazamiento (pan/zoom).
  - `ColorFilterSelector`: Fila de selección rápida con 3 opciones (FR-017):
    - `Blanco y Negro` (seleccionado por defecto): Alto contraste para texto nítido.
    - `Color`: Conserva tonalidades originales con balance de blancos optimizado.
    - `Escala de Grises`: Monocromático uniforme.
  - `ToolBar`:
    - Botón `Ajustar bordes`: Vuelve a `CropScreen` permitiendo corregir los vértices si la perspectiva no quedó ideal.
    - Botón `Rotar 90°`: Rota la imagen 90 grados en sentido horario (`ROT_0 -> ROT_90 -> ROT_180 -> ROT_270 -> ROT_0`).
  - `ThumbnailStrip` (Tira de miniaturas de lote multipágina):
    - Muestra miniaturas numeradas de todas las páginas del lote activo en orden cronológico.
    - Cada miniatura incluye un botón visible de icono de papelera / `Eliminar miniatura` (FR-005).
    - Indicador de página seleccionada actualmente.
  - `ActionFooter`:
    - Botón secundario `Añadir página` (`+`): Guarda la página actual en el lote y reabre la cámara para la siguiente toma (FR-004).
    - Botón primario `Guardar PDF`: Compila todas las páginas aprobadas en un único archivo `.pdf` y lo almacena en `Documentos/Escaner/` (FR-006, FR-007).
- **Eventos y Acciones**:
  - `onSelectColorMode(mode: ModoColor)`: Actualiza de inmediato la visualización de la página con el filtro elegido.
  - `onRotateClick()`: Rota la imagen 90° horario y actualiza la visualización.
  - `onDeletePage(index: Int)`: Elimina la página del lote, renumera las restantes y actualiza la visualización.
  - `onAddPageClick()`: Confirma la página en curso y regresa a `CameraScreen` para la siguiente captura.
  - `onSavePdfClick()`: Inicia el proceso de compilación a PDF y muestra pantalla o diálogo de guardado exitoso con botón de compartir.

---

### 2.5. Diálogos de Interacción y Confirmación

1. **`ConfirmDeleteDialog`**:
   - Título: *"Eliminar documento"*
   - Mensaje: *"¿Estás seguro de que deseas eliminar permanentemente el archivo '[nombre]'? Esta acción no se puede deshacer."*
   - Botón afirmativo: *"Eliminar"* (color destructivo/rojo).
   - Botón negativo: *"Cancelar"*.
2. **`RenameDialog`**:
   - Título: *"Renombrar documento"*
   - Campo de texto con el nombre actual (sin la extensión `.pdf` para facilitar edición).
   - Validaciones en vivo:
     - No puede estar vacío.
     - No puede contener caracteres ilegales (`/ \ : * ? " < > |`).
     - No puede coincidir con un archivo existente en la carpeta.
   - Botón afirmativo: *"Guardar"*.
   - Botón negativo: *"Cancelar"*.
3. **`ConfirmDiscardBatchDialog`**:
   - Título: *"¿Descartar páginas?"*
   - Mensaje: *"Tienes [N] páginas escaneadas en esta sesión. Si sales ahora, se perderán las capturas realizadas."*
   - Botón afirmativo: *"Descartar"* (destructivo).
   - Botón negativo: *"Continuar escaneando"*.
4. **`PermissionDeniedDialog`**:
   - Título: *"Permiso de cámara necesario"*
   - Mensaje: *"Para escanear tus documentos y facturas, Escaner necesita acceso a la cámara de tu teléfono. Tus fotos nunca salen de tu dispositivo."*
   - Botón afirmativo: *"Conceder permiso"* o *"Abrir ajustes"*.
   - Botón negativo: *"Cancelar"*.
