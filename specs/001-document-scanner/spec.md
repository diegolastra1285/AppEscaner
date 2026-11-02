# Feature Specification: Escaner v1 - Escaneo de Documentos y Exportación a PDF

**Feature Branch**: `001-document-scanner`  
**Created**: 2026-11-02  
**Status**: Draft  
**Input**: User description: "Especificación: Escaner v1. Herramienta Android para escanear facturas, remisiones y documentos físicos mediante la cámara del teléfono, con corrección de perspectiva, contraste, compilación en PDF multipágina, persistencia local pública, historial, botón de compartir nativo y cero publicidad."

## Clarifications

### Session 2026-11-02

- Q: ¿Debe la pantalla de historial permitir eliminar archivos PDF directamente desde la aplicación? (FR-009) → A: Sí, permitir eliminar y además renombrar archivos PDF directamente desde el historial mediante acciones dedicadas con confirmación.
- Q: ¿Debe la pantalla de revisión permitir alternar entre distintos modos de color (Color, Blanco y Negro, Escala de grises) antes de confirmar cada página? (FR-002) → A: Sí, ofrecer un selector rápido con 3 modos (Color original optimizado, Blanco y Negro / Alto contraste, Escala de grises), aplicando Blanco y Negro por defecto.
- Q: ¿Cómo debe realizarse la captura de la fotografía en la cámara: mediante un botón de disparo manual o mediante disparo automático al detectar el documento? (FR-002) → A: Disparo exclusivamente manual mediante botón de captura visible en la cámara, procesando el recorte y perspectiva inmediatamente tras la toma.
- Q: ¿Debe la pantalla de revisión de cada página incluir una acción para rotar la imagen en incrementos de 90 grados antes de aprobarla? (FR-003) → A: Sí, incluir un botón de rotación de 90° (horario) en la pantalla de revisión de cada página.
- Q: ¿Debe la pantalla de la cámara incluir un botón para encender y apagar la linterna (flash continuo) del teléfono durante el encuadre del documento? (FR-001) → A: Sí, incluir un botón conmutable para encender o apagar la linterna en la pantalla de la cámara.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Captura individual, recorte automático y exportación a PDF (Priority: P1)

Como creador y usuario de la app, quiero abrir la cámara, apuntar a una factura o remisión física, presionar el botón de captura y obtener un archivo PDF limpio, enderezado y legible guardado directamente en mi teléfono, para poder digitalizar mis comprobantes de trabajo rápidamente y sin distracciones.

**Why this priority**: Es el núcleo funcional indispensable (MVP mínimo viable). Si solo se implementa esta historia, la aplicación ya entrega valor inmediato permitiendo sustituir otras herramientas comerciales invasivas.

**Independent Test**: Puede probarse de forma independiente colocando el teléfono en Modo Avión, abriendo la app, fotografiando un recibo sobre un escritorio, confirmando la captura y verificando que el archivo `.pdf` aparezca legible en la carpeta de documentos del teléfono.

**Acceptance Scenarios**:

1. **Given** que el usuario está en la pantalla principal y presiona el botón de escaneo, **When** apunta la cámara a un documento físico (con opción de activar la linterna si las condiciones de luz lo ameritan) y presiona el disparador, **Then** la app recorta automáticamente los bordes del fondo, corrige la perspectiva dejándola plana, aplica por defecto el modo Blanco y Negro (alto contraste) y muestra la vista previa del documento procesado con opción de alternar entre Color, Blanco y Negro o Escala de grises, así como un botón para rotar la imagen en 90 grados.
2. **Given** que la vista previa procesada es satisfactoria, **When** el usuario pulsa "Guardar", **Then** el sistema compila un archivo con formato `.pdf`, lo guarda en la carpeta pública `Documentos/Escaner/` con un nombre único basado en fecha y hora (ej. `Escaner_20261102_101530.pdf`), y muestra la confirmación de guardado con la opción de compartir.
3. **Given** la vista previa del documento procesado, **When** el usuario pulsa el botón de rotación, **Then** la imagen gira 90 grados en sentido horario y actualiza la visualización de inmediato conservando la corrección aplicada.

---

### User Story 2 - Historial de documentos, gestión y compartir mediante la hoja nativa (Priority: P2)

Como usuario, quiero abrir la aplicación y ver una lista cronológica de todos los documentos PDF que he escaneado previamente, con opciones visibles para compartir, renombrar o eliminar cada archivo, para poder gestionarlos y enviarlos rápidamente por WhatsApp, correo electrónico u otras apps sin depender de un explorador de archivos externo.

**Why this priority**: Permite consultar, depurar y dar salida a los documentos generados en el día a día sin necesidad de recurrir a un explorador de archivos externo para compartirlos o borrarlos.

**Independent Test**: Puede probarse teniendo al menos un PDF generado previamente, abriendo la app, observando que aparece en el historial, pulsando el botón de compartir bajo el elemento y verificando que se despliega la hoja nativa de Android con el PDF adjunto listo para ser enviado por correo o mensajería; además, probar renombrar el archivo y eliminar un archivo con confirmación verificando su desaparición física en la carpeta.

**Acceptance Scenarios**:

1. **Given** que existen documentos previamente escaneados en el almacenamiento, **When** el usuario inicia la aplicación, **Then** la pantalla de inicio presenta la lista de dichos archivos ordenados cronológicamente (los más recientes primero), mostrando su nombre, fecha y acciones visibles de compartir, renombrar y eliminar.
2. **Given** un elemento de la lista del historial, **When** el usuario presiona el botón "Compartir", **Then** se abre inmediatamente la hoja nativa de compartir del sistema operativo (`Android Sharesheet`) con el archivo PDF físico adjunto.
3. **Given** un elemento de la lista del historial, **When** el usuario pulsa sobre el cuerpo del documento, **Then** el sistema delega la apertura del archivo PDF en el visor predeterminado del teléfono mediante el selector nativo del sistema.
4. **Given** un elemento de la lista del historial, **When** el usuario selecciona la opción de eliminar y confirma en el diálogo de confirmación, **Then** el sistema borra el archivo físico del almacenamiento y lo remueve inmediatamente del historial.
5. **Given** un elemento de la lista del historial, **When** el usuario selecciona la opción de renombrar e introduce un nuevo nombre válido, **Then** el sistema renombra el archivo físico en el almacenamiento y actualiza su visualización en el historial.

---

### User Story 3 - Escaneo de lote multipágina con eliminación de hojas defectuosas (Priority: P3)

Como usuario que maneja paquetes de facturas o documentos extensos, quiero agregar múltiples páginas sucesivas a una misma sesión de escaneo y tener la posibilidad de eliminar cualquier página que haya salido borrosa antes de generar un único archivo PDF que agrupe todas las hojas en el orden en que las tomé.

**Why this priority**: Es fundamental para la gestión de facturas compuestas por varias hojas o remisiones con anexos, evitando generar archivos sueltos y desorganizados.

**Independent Test**: Puede probarse capturando una primera hoja, pulsando "Añadir página", capturando una segunda y una tercera hoja, eliminando la segunda página por haber quedado borrosa y confirmando el guardado; el PDF resultante debe tener exactamente 2 páginas correspondientes a la primera y tercera toma.

**Acceptance Scenarios**:

1. **Given** que se ha capturado la primera página de un documento, **When** el usuario presiona el botón "Añadir página", **Then** la cámara se activa nuevamente para capturar la siguiente hoja sin perder la anterior.
2. **Given** una sesión con múltiples páginas capturadas, **When** el usuario revisa el lote, **Then** puede visualizar las miniaturas numeradas de las páginas en el orden secuencial en que fueron tomadas.
3. **Given** una miniatura de página defectuosa dentro del lote en curso, **When** el usuario pulsa el icono de eliminar sobre esa miniatura, **Then** el sistema remueve esa hoja del lote y recalcula la secuencia sin afectar las demás páginas.
4. **Given** un lote con múltiples páginas aprobadas, **When** el usuario presiona "Guardar PDF", **Then** se compila un único archivo `.pdf` que contiene todas las páginas restantes en estricto orden cronológico de captura.

---

### User Story 4 - Reajuste manual de bordes y perspectiva (Priority: P4)

Como usuario que escanea en condiciones de baja iluminación o sobre superficies de color similar al documento (ej. papel blanco sobre mesa blanca), quiero poder ajustar manualmente las cuatro esquinas de corte si la detección automática no fue precisa, para garantizar que no se corte texto ni queden bordes no deseados.

**Why this priority**: Evita la frustración cuando la detección automática falla por condiciones ambientales adversas, permitiendo rescatar la toma sin tener que repetir la fotografía indefinidamente.

**Independent Test**: Puede probarse fotografiando un papel blanco sobre una superficie clara donde el recorte automático incluya parte de la mesa; al tocar "Ajustar bordes", se arrastran las esquinas a los límites reales del papel y se verifica que el resultado final corregido coincida exactamente con la selección manual.

**Acceptance Scenarios**:

1. **Given** que se ha procesado una captura automáticamente, **When** el usuario observa que los bordes cortaron información o incluyeron fondo de más, **Then** puede presionar la opción "Ajustar bordes" para visualizar la toma original con los cuatro puntos de las esquinas interactivos.
2. **Given** los puntos de corte en pantalla, **When** el usuario arrastra cualquiera de las cuatro esquinas a los vértices correctos del papel y confirma, **Then** el sistema recalcula la corrección de perspectiva y el contraste aplicando el nuevo polígono definido.

---

### Edge Cases

- **Historial vacío (primer uso)**: Cuando la app se abre por primera vez o no hay documentos escaneados en la carpeta, la pantalla principal no debe quedar en blanco; debe mostrar un mensaje amigable (ej. *"Aún no tienes documentos escaneados"*) acompañado del botón destacado para iniciar la cámara.
- **Rechazo o revocación de permisos de cámara o almacenamiento**: Si el usuario niega o revoca los permisos del sistema operativo, la app no debe cerrarse abruptamente ni quedar en negro. Debe desplegar un mensaje claro en español explicando la necesidad del permiso y un botón directo para concederlo o abrir la configuración del sistema.
- **Fondo sin contraste o fallo de detección de bordes**: Si el algoritmo de recorte no distingue los bordes de la hoja, debe tomar por defecto la totalidad del encuadre sin recortar información y ofrecer visiblemente el ajuste manual de esquinas.
- **Salida involuntaria durante un escaneo multipágina**: Si el usuario lleva capturadas varias páginas de un lote y pulsa el botón de retroceso de Android o intenta salir de la pantalla de escaneo, el sistema debe presentar un diálogo de confirmación (ej. *¿Deseas descartar las páginas capturadas o continuar escaneando?*) para impedir la pérdida accidental del trabajo.
- **Archivo borrado externamente**: Si el usuario borra un archivo PDF desde el explorador de archivos del teléfono y luego abre la app e intenta abrirlo o compartirlo desde el historial, el sistema debe detectar la ausencia física del archivo, notificar que el documento ya no existe, actualizar la lista del historial y evitar fallos de cierre forzoso.
- **Espacio de almacenamiento insuficiente**: Si la memoria del dispositivo no cuenta con espacio libre suficiente al compilar o guardar el PDF, el sistema debe atrapar el error, advertir al usuario sobre la falta de espacio y retener temporalmente la sesión de escaneo para que pueda liberar memoria sin perder las tomas realizadas.
- **Interrupción repentina por llamada entrante o cambio de aplicación**: Si entra una llamada telefónica o el usuario cambia momentáneamente de aplicación durante una sesión de captura o lote, el ciclo de vida de la app debe preservar el estado de las páginas tomadas para que al regresar continúe en el mismo punto.
- **Intento de renombrar con nombre inválido o existente**: Si el usuario intenta renombrar un archivo con un nombre vacío, caracteres no permitidos por el sistema de archivos o idéntico a otro archivo existente en la carpeta `Documentos/Escaner/`, el sistema debe alertar con un mensaje descriptivo y mantener el nombre original sin aplicar cambios.
- **Dispositivo sin flash de cámara**: Si el teléfono o la cámara seleccionada no cuenta con unidad de flash de hardware, el botón de linterna debe ocultarse o permanecer deshabilitado de forma transparente sin provocar fallos ni bloquear la captura.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE presentar en la pantalla de inicio un botón visible y prioritario para iniciar el proceso de escaneo y abrir la cámara de manera inmediata.
- **FR-002**: El sistema DEBE capturar la fotografía del documento físico exclusivamente mediante la pulsación manual del botón de obturador por parte del usuario, sometiendo la imagen capturada inmediatamente a detección de bordes, corrección de perspectiva (enderezado plano) y procesamiento visual inicial en Blanco y Negro (alto contraste) para maximizar la legibilidad de textos y sellos.
- **FR-003**: El sistema DEBE ofrecer en la pantalla de revisión de la captura una opción para reajustar manualmente las cuatro esquinas de corte sobre la imagen original antes de confirmar el procesamiento.
- **FR-004**: El sistema DEBE permitir agregar imágenes sucesivas a la sesión actual mediante una acción explícita ("Añadir página"), conformando un lote de documentos para un único archivo final.
- **FR-005**: El sistema DEBE permitir visualizar las páginas que componen el lote activo y eliminar de forma individual cualquier página defectuosa antes de compilar el documento final.
- **FR-006**: El sistema DEBE compilar las páginas aprobadas en una sesión exclusivamente en formato estándar `.pdf`, respetando rigurosamente el orden cronológico en que fueron añadidas.
- **FR-007**: El sistema DEBE almacenar físicamente los archivos PDF en una ruta pública y visible del almacenamiento local del teléfono (carpeta `Documentos/Escaner/`), garantizando que sean accesibles desde cualquier explorador de archivos externo.
- **FR-008**: El sistema DEBE asignar automáticamente a cada PDF generado un nombre único estructurado por fecha y hora (formato `Escaner_AAAAMMDD_HHMMSS.pdf`), previniendo sobreescrituras accidentales entre escaneos sucesivos.
- **FR-009**: El sistema DEBE mostrar en la pantalla principal un historial ordenado cronológicamente con todos los archivos PDF existentes en la carpeta de almacenamiento de la app.
- **FR-010**: El sistema DEBE incluir un botón de compartir tanto en la pantalla de confirmación de un nuevo escaneo como en cada fila del historial de documentos.
- **FR-011**: El sistema DEBE delegar la acción de compartir exclusivamente a través de la hoja nativa del sistema operativo (`Android Sharesheet`), enviando la referencia al archivo físico `.pdf`.
- **FR-012**: El sistema DEBE delegar la apertura y lectura de los documentos del historial en los visores externos de PDF instalados en el dispositivo mediante una intención nativa del sistema operativo.
- **FR-013**: El sistema DEBE operar de forma 100% local y fuera de línea, sin requerir conexión a internet, registro de cuentas, inicios de sesión ni validaciones con servidores externos.
- **FR-014**: El sistema DEBE estar totalmente libre de anuncios publicitarios, videos emergentes, banners comerciales, solicitudes de calificación en tiendas y pantallas de pago o suscripción.
- **FR-015**: El sistema DEBE permitir eliminar cualquier archivo PDF del historial mediante una acción explícita con diálogo de confirmación previa, borrando físicamente el archivo del almacenamiento del teléfono.
- **FR-016**: El sistema DEBE permitir renombrar cualquier archivo PDF del historial mediante un diálogo de edición, validando que el nuevo nombre no esté vacío, no use caracteres inválidos ni coincida con un archivo existente.
- **FR-017**: El sistema DEBE permitir en la pantalla de revisión de cada página alternar entre tres modos de color (Blanco y Negro / Alto contraste, Color original optimizado y Escala de grises), actualizando la previsualización al instante antes de aprobar la página.
- **FR-018**: El sistema DEBE incluir en la pantalla de revisión de cada página un botón para rotar la imagen procesada en incrementos de 90 grados en el sentido de las agujas del reloj antes de confirmarla o guardarla.
- **FR-019**: El sistema DEBE proveer en la pantalla de captura de la cámara un botón interactivo para alternar la linterna (flash continuo encendido / apagado) del dispositivo para iluminar documentos en baja luz.

---

### Key Entities

- **DocumentoPDF**: Representa el archivo final generado y almacenado en el dispositivo. Atributos conceptuales: nombre del archivo (basado en timestamp), ruta física en el almacenamiento (`Documentos/Escaner/`), fecha y hora de creación, tamaño del archivo y número de páginas.
- **SesionLote**: Representa el contexto de trabajo temporal en curso mientras el usuario está capturando una o varias páginas. Atributos conceptuales: identificador de sesión, lista ordenada de páginas capturadas y estado de la sesión (en captura, en revisión, compilada o cancelada).
- **PaginaDocumento**: Representa una toma individual procesada dentro de una sesión de lote. Atributos conceptuales: índice de orden en el lote, imagen capturada original, coordenadas de las 4 esquinas de corte, orientación de rotación (0°, 90°, 180°, 270°), modo de color seleccionado (Blanco y Negro, Color o Escala de grises), imagen corregida final y estado de aprobación.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El usuario puede completar el escaneo de un documento de una sola página y tener el PDF generado y guardado en menos de 10 segundos desde el momento de presionar el disparador.
- **SC-002**: El 100% de las funcionalidades de captura, ajuste de bordes, compilación de PDF, consulta de historial y guardado operan con total normalidad con el teléfono en Modo Avión (sin conexión a internet ni datos móviles).
- **SC-003**: El 100% de los archivos PDF generados se almacenan como archivos planos en la carpeta pública `Documentos/Escaner/`, siendo inmediatamente visibles y legibles desde la aplicación nativa "Mis Archivos" de Android.
- **SC-004**: Cero anuncios comerciales, temporizadores de espera publicitaria, solicitudes de calificación o barreras de suscripción se muestran durante el uso de la aplicación (0 interrupciones).
- **SC-005**: Al presionar el botón "Compartir", la hoja nativa de Android se despliega en menos de 1 segundo con el archivo PDF real vinculado y listo para remitirse a través de cualquier aplicación compatible instalada.
- **SC-006**: La aplicación es capaz de compilar y generar un archivo PDF de un lote de hasta 20 páginas continuas sin presentar cierres inesperados ni pérdida de memoria en el dispositivo.

---

## Assumptions

- **Plataforma objetivo**: Dispositivo móvil con sistema operativo Android.
- **Visor de PDF**: Se asume que el teléfono cuenta con al menos una aplicación externa registrada en el sistema operativo capaz de abrir archivos PDF (como Google Drive PDF Viewer, visor preinstalado del fabricante o Adobe Reader).
- **Estructura de almacenamiento**: Se utilizará una carpeta única plana (`Documentos/Escaner/`), donde cada escaneo convive como un archivo `.pdf` independiente identificado por su marca temporal, sin anidar subcarpetas para cada escaneo individual.
- **Idioma y localización**: Todos los textos, botones, avisos y cuadros de diálogo estarán redactados en español claro y natural adaptado al contexto local (facturas, remisiones, recibos).
- **Exclusiones V1**: Conforme a la sección de fuera de alcance y la Constitución del proyecto, no se implementará OCR, categorización en subcarpetas dentro de la app, reordenamiento por arrastrar y soltar (drag and drop), firmas digitales, disparo automático por detección de estabilidad (auto-shutter) ni sincronización en la nube.
