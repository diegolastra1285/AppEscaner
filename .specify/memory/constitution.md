<!--
SYNC IMPACT REPORT
==================
- Version change: Unratified Template -> 1.0.0
- Ratification Date: 2026-10-30
- Modified principles:
  - Replaced template placeholders with 7 project-defined core principles:
    1. I. Simplicidad ante todo
    2. II. Cero publicidad y cero intrusión
    3. III. Operación local y privacidad absoluta
    4. IV. Cero alcance fantasma (No Phantom Scope)
    5. V. Idioma y contexto local
    6. VI. Verificable por una persona no técnica
    7. VII. Respeto por el almacenamiento del teléfono
- Added sections:
  - Restricciones y Requisitos de Plataforma
  - Flujo de Desarrollo y Calidad
  - Governance
- Removed sections:
  - Generic template placeholder sections replaced by concrete project sections
- Follow-up TODOs:
  - None. All placeholders have been resolved.
-->

# Escaner Constitution

Esta constitución establece las leyes fundamentales que rigen el diseño y desarrollo de Escaner, una herramienta nativa para teléfono móvil orientada a digitalizar documentos físicos (facturas, remisiones, recibos) y guardarlos como archivos PDF en el dispositivo.

## Core Principles

### I. Simplicidad ante todo
Ante dos soluciones, se DEBE elegir siempre la más simple y directa. Para esta versión inicial (V1), se PROHÍBE cualquier complejidad anticipada, sobreingeniería o diseño especulativo.

### II. Cero publicidad y cero intrusión
La aplicación existe para ser una utilidad limpia y respetuosa. Queda TERMINANTEMENTE PROHIBIDO incluir anuncios comerciales, muros de pago (paywalls), pantallas de suscripción o solicitudes emergentes para calificar la app.

### III. Operación local y privacidad absoluta
Los documentos escaneados pertenecen exclusivamente al usuario. La aplicación DEBE operar 100% en el teléfono, sin requerir creación ni inicio de sesión en cuentas, y sin transferir documentos, metadatos o analíticas a servidores externos.

### IV. Cero alcance fantasma (No Phantom Scope)
Queda ESTRICTAMENTE PROHIBIDO implementar funcionalidades, ajustes o pantallas que no se encuentren explícitamente descritas en la `spec.md`. Cualquier idea nueva DEBE documentarse y discutirse previamente; no se programa sin especificación aprobada.

### V. Idioma y contexto local
Todo el producto, mensajes de interfaz, botones, diálogos y documentación DEBEN utilizar español de Colombia, con terminología clara para documentos cotidianos (remisiones, facturas, comprobantes, recibos).

### VI. Verificable por una persona no técnica
Cada criterio de éxito DEBE ser comprobable utilizando la aplicación en mano, mediante acciones cotidianas y sin necesidad de inspeccionar código ni registros técnicos.

### VII. Respeto por el almacenamiento del teléfono
Los archivos PDF generados DEBEN guardarse en ubicaciones estándar y visibles del almacenamiento del teléfono (p. ej., carpeta Documentos / Descargas). El usuario DEBE poder abrirlos, moverlos o compartirlos con cualquier otra aplicación sin depender de este escáner.

## Restricciones y Requisitos de Plataforma

- **Enfoque Nativo Móvil:** Diseñado para funcionamiento óptimo y fluido directamente en dispositivos móviles.
- **Flujo Central de Documentos:** Digitalización física directa a formato estándar PDF sin intermediarios propietarios.
- **Independencia de Red:** Capacidad de funcionamiento pleno sin conexión a internet ni dependencias de servicios en la nube.

## Flujo de Desarrollo y Calidad

- **Especificación Mandatoria:** Toda implementación debe estar respaldada por una especificación formal (`spec.md`), plan de implementación (`plan.md`) y tareas desglosadas (`tasks.md`).
- **Validación Práctica:** Las verificaciones de calidad priorizan la experiencia real en dispositivo sobre simulaciones abstractas.
- **Justificación de Dependencias:** Toda biblioteca o dependencia externa añadida debe justificar su necesidad frente a alternativas nativas más simples.

## Governance

Esta constitución rige de manera obligatoria todas las decisiones de diseño, arquitectura y desarrollo de Escaner. Prevalece sobre cualquier preferencia personal o práctica no documentada.

- **Cumplimiento:** Todas las especificaciones, planes y revisiones de código deben validar activamente el apego estricto a los 7 principios fundamentales.
- **Enmiendas:** Cualquier modificación a esta constitución requiere consenso documentado, registro en el historial de versiones y justificación del cambio.
- **Política de Versionado:**
  - **MAJOR (X.0.0):** Modificación o eliminación de principios fundamentales, o cambios de gobernanza retroincompatibles.
  - **MINOR (0.X.0):** Adición de nuevos principios o expansión material de las secciones de gobernanza.
  - **PATCH (0.0.X):** Refinamientos de redacción, corrección de erratas o aclaraciones no semánticas.

**Version**: 1.0.0 | **Ratified**: 2026-10-30 | **Last Amended**: 2026-10-30
