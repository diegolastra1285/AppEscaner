# Quickstart Validation Guide: Escaner v1

**Feature Branch**: `001-document-scanner`  
**Date**: 2026-11-02  
**Status**: Completed  

Esta guía proporciona los escenarios paso a paso para verificar y validar de extremo a extremo que la aplicación cumple con todos los requisitos funcionales de [spec.md](./spec.md) y los principios de la [Constitución](../../.specify/memory/constitution.md).

---

## 1. Prerrequisitos y Entorno

- **Dispositivo de prueba**:
  - Teléfono físico con Android 8.0 (API 26) o superior (recomendado para probar cámara, flash y Sharesheet nativo), o
  - Emulador de Android Studio con cámara virtual configurada.
- **Entorno de desarrollo**:
  - Android Studio Iguana / Ladybug o superior con Android SDK 35 instalado.
  - JDK 17 o superior.
- **Herramientas de verificación**:
  - Aplicación de explorador de archivos nativa del teléfono (ej. "Mis Archivos", "Files by Google").
  - Visor de PDF nativo instalado (ej. Google Drive PDF Viewer o visor del fabricante).

---

## 2. Escenarios de Validación de Usuario

### Escenario 1: Escaneo rápido individual y exportación a PDF (P1, SC-001, SC-003)

1. **Pasos**:
   - Abrir la aplicación Escaner.
   - Presionar el botón flotante **Escanear** en la pantalla principal.
   - Si es la primera vez, aceptar el permiso de cámara.
   - Encuadrar una factura física o recibo en una mesa bien iluminada.
   - Presionar el botón disparador central.
   - Verificar que se muestra la vista previa con recorte automático y filtro **Blanco y Negro** aplicado por defecto.
   - Presionar **Guardar PDF**.
2. **Resultado esperado**:
   - El proceso completo toma menos de 10 segundos desde el disparo hasta el guardado (SC-001).
   - Aparece mensaje de confirmación de guardado.
   - Abrir la aplicación "Mis Archivos" en el teléfono y navegar a `Almacenamiento interno > Documentos > Escaner`. El archivo `Escaner_AAAAMMDD_HHMMSS.pdf` está presente, visible y abre correctamente con textos nítidos y sellos legibles (SC-003).

---

### Escenario 2: Operación 100% Fuera de Línea en Modo Avión (SC-002, Principio III)

1. **Pasos**:
   - En el teléfono, activar el **Modo Avión** (desconectar WiFi y Datos Móviles).
   - Abrir Escaner.
   - Realizar una captura completa, ajuste de bordes, cambio de filtro y pulsar **Guardar PDF**.
2. **Resultado esperado**:
   - La aplicación opera con total normalidad, sin advertencias de red, bloqueos ni errores (SC-002).
   - El PDF se guarda íntegramente en `Documentos/Escaner/`.

---

### Escenario 3: Historial, Apertura, Renombrado y Eliminación (P2, FR-009, FR-012, FR-015, FR-016)

1. **Pasos**:
   - Abrir la aplicación.
   - Observar la lista del historial en la pantalla principal: el documento recién generado aparece en la parte superior con fecha, hora y tamaño.
   - Tocar sobre el cuerpo del documento en la lista: verificar que se abre en el visor PDF del teléfono (FR-012).
   - Regresar a la app. Seleccionar la opción **Renombrar** del documento.
   - Cambiar el nombre a `Factura_Internet_Noviembre` y guardar. Verificar que en la lista se actualiza el nombre a `Factura_Internet_Noviembre.pdf`.
   - Seleccionar la opción **Eliminar** en otro documento de prueba.
   - En el diálogo de confirmación, presionar **Eliminar**.
2. **Resultado esperado**:
   - El archivo eliminado desaparece instantáneamente del historial y se borra físicamente de la carpeta `Documentos/Escaner/` (FR-015).
   - El archivo renombrado conserva la extensión `.pdf` y se renombra en el almacenamiento (FR-016).

---

### Escenario 4: Compartir mediante la Hoja Nativa de Android (FR-010, FR-011, SC-005)

1. **Pasos**:
   - En la lista del historial, presionar el botón **Compartir** de cualquier documento.
2. **Resultado esperado**:
   - Se despliega de forma inmediata (< 1 segundo, SC-005) el `Android Sharesheet` del sistema con opciones para WhatsApp, Gmail, Bluetooth, etc.
   - El archivo PDF va adjunto directamente como binario estándar sin marcas de agua ni enlaces publicitarios.

---

### Escenario 5: Escaneo Multipágina y Descarte de Página Defectuosa (P3, FR-004, FR-005, FR-006, SC-006)

1. **Pasos**:
   - Iniciar un nuevo escaneo y capturar la primera hoja de un recibo.
   - En la pantalla de revisión, presionar **Añadir página (+)**.
   - Capturar una segunda hoja de prueba moviendo intencionalmente la cámara para que quede borrosa.
   - Presionar nuevamente **Añadir página (+)** y capturar una tercera hoja nítida.
   - En la tira inferior de miniaturas de la pantalla de revisión, observar las 3 páginas numeradas (1, 2 y 3).
   - Tocar el icono de papelera sobre la miniatura 2 (la página borrosa).
   - Verificar que la página 2 se remueve y las miniaturas se reenumeran (1 y 2).
   - Presionar **Guardar PDF**.
2. **Resultado esperado**:
   - Se compila un único PDF de exactamente 2 páginas, correspondientes a la primera y tercera toma, en su orden cronológico exacto (FR-006).

---

### Escenario 6: Reajuste Manual de Bordes y Perspectiva (P4, FR-003)

1. **Pasos**:
   - Colocar una hoja blanca sobre una mesa clara de bajo contraste.
   - Capturar la fotografía.
   - Si la detección automática toma un borde incorrecto, presionar **Ajustar bordes**.
   - Arrastrar manualmente las 4 manijas táctiles a las esquinas exactas de la hoja física.
   - Presionar **Confirmar recorte**.
2. **Resultado esperado**:
   - La imagen resultante se proyecta y endereza perfectamente recta usando `Matrix.setPolyToPoly`, eliminando la superficie de la mesa sobrante.

---

### Escenario 7: Selector de Modos de Color y Rotación 90° (FR-017, FR-018)

1. **Pasos**:
   - En la pantalla de revisión, observar que por defecto se aplica **Blanco y Negro** (FR-002, FR-017).
   - Tocar la opción **Color**: la vista previa cambia instantáneamente a los colores originales optimizados.
   - Tocar la opción **Escala de Grises**: la vista previa se actualiza a monocromático.
   - Tocar el botón de **Rotación (90°)**: la hoja gira 90 grados en sentido de las agujas del reloj.
2. **Resultado esperado**:
   - La visualización cambia fluidamente sin latencia y el PDF resultante respeta el modo de color y la rotación seleccionados.

---

### Escenario 8: Control de Linterna en Cámara (FR-019)

1. **Pasos**:
   - Abrir la cámara en un ambiente con poca luz.
   - Tocar el botón de linterna en la barra superior.
2. **Resultado esperado**:
   - El flash continuo del dispositivo se enciende iluminando el documento. Al pulsar nuevamente, se apaga. Si el dispositivo de prueba carece de unidad de flash, el botón permanece deshabilitado u oculto sin errores.

---

### Escenario 9: Verificación de Cero Publicidad y Cero Intrusión (Principio II, SC-004)

1. **Pasos**:
   - Realizar 5 escaneos completos consecutivos, navegando entre pantallas, abriendo el historial y compartiendo archivos.
2. **Resultado esperado**:
   - Durante toda la sesión hay 0 anuncios emergentes, 0 banners, 0 pantallas de cobro o suscripción y 0 diálogos pidiendo calificar la app (SC-004).
