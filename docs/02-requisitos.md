# Requisitos de Fila Cero

Proyecto final de Diseño de Software · Manuel Alejandro Villarreal.

## Requisitos funcionales

| Código | Requisito | Historia |
|---|---|---|
| RF_001 | Registrar clientes con nombre, cédula, correo, contraseña, celular, fecha de nacimiento y condición especial. | HU_001 |
| RF_002 | Autenticar usuarios por correo y contraseña y emitir un token JWT con su rol. | HU_002 |
| RF_003 | Crear, consultar, actualizar y desactivar sucursales con horario y trámites ofrecidos. | HU_003 |
| RF_004 | Crear, consultar, actualizar y desactivar trámites con prefijo y tiempo promedio. | HU_004 |
| RF_005 | Crear, consultar, actualizar y eliminar módulos de atención por sucursal. | HU_005 |
| RF_006 | Crear, consultar, actualizar y desactivar cuentas de asesor asignadas a una sucursal. | HU_006 |
| RF_007 | Mostrar sucursales con su estado (abierta/cerrada) y, por trámite, personas en espera, módulos abiertos y tiempo estimado. | HU_007 |
| RF_008 | Emitir un turno virtual con código diario, prioridad según el cliente y validaciones de horario y turno único. | HU_008 |
| RF_009 | Emitir un turno presencial en el kiosco con la cédula, sin cuenta. | HU_009 |
| RF_010 | Mostrar al cliente su turno activo con posición, personas antes y tiempo estimado. | HU_010 |
| RF_011 | Cancelar un turno en espera por parte de su dueño. | HU_011 |
| RF_012 | Abrir, pausar, reanudar y cerrar el módulo de un asesor. | HU_012 |
| RF_013 | Llamar el siguiente turno según el puntaje de prioridad y los trámites del módulo. | HU_013 |
| RF_014 | Registrar inicio y fin de la atención, volver a llamar (máximo 3) y marcar ausente. | HU_014 |
| RF_015 | Transferir un turno en atención a la fila de otro trámite conservando su hora de llegada. | HU_015 |
| RF_016 | Mostrar franjas de citas con cupos y agendar citas. | HU_016 |
| RF_017 | Hacer check-in de una cita (genera turno con prioridad CITA), cancelarla y vencerla automáticamente. | HU_017 |
| RF_018 | Mostrar la pantalla pública de llamados de una sucursal. | HU_018 |
| RF_019 | Registrar la calificación (1 a 5) de un turno atendido. | HU_019 |
| RF_020 | Calcular indicadores por sucursal y rango de fechas: totales, promedios, por trámite, por asesor y por hora. | HU_020 |
| RF_021 | Notificar al cliente los cambios de su turno (creado, próximo, llamado, atendido, ausente, transferido) a través de un servicio de notificaciones. | HU_010 |
| RF_022 | Consultar el historial de turnos del cliente. | HU_010 |

## Requisitos no funcionales

| Código | Atributo | Requisito |
|---|---|---|
| RNF_001 | Tecnología | Backend en Java 17 con Spring Boot 3.3. |
| RNF_002 | Persistencia | Base de datos no relacional MongoDB (Atlas en la nube). |
| RNF_003 | Arquitectura | Arquitectura por capas Controller → Service → Repository con DTO; los controladores no acceden a repositorios. |
| RNF_004 | Seguridad | Contraseñas con BCrypt, JWT con expiración, permisos por rol y ninguna credencial en el repositorio. |
| RNF_005 | Mantenibilidad | Las reglas que cambian (bonificaciones, tiempos, llamados máximos) están en constantes con nombre y en estrategias intercambiables. |
| RNF_006 | Usabilidad de la API | Errores con código HTTP correcto (400, 401, 403, 404, 409, 422) y mensaje en español. |
| RNF_007 | Rendimiento | Consultar el estado de las filas responde en menos de 2 segundos con índices en MongoDB. |
| RNF_008 | Calidad | Pruebas unitarias JUnit 5 + Mockito y pruebas de API con Postman ejecutadas en CI (GitHub Actions). |
| RNF_009 | Portabilidad | Backend en contenedor Docker desplegado en la nube (Render) y frontend en Vercel. |
| RNF_010 | Documentación | Swagger/OpenAPI, README, wiki tipo SRS y diagramas UML en Visual Paradigm. |
| RNF_011 | Plataforma | Interfaz web responsive pensada primero para celular, más vistas de kiosco y pantalla de TV. |
| RNF_012 | Integración | El servicio de notificaciones externo se usa a través de una interfaz propia (NotificadorTurnos). |
| RNF_013 | Concurrencia | Los códigos de turno se generan de forma atómica y los turnos usan bloqueo optimista (@Version). |
| RNF_014 | Privacidad | El documento del cliente se muestra enmascarado en las respuestas públicas. |

## Reglas de negocio

| Código | Regla |
|---|---|
| RN1 | Código del turno = prefijo del trámite + consecutivo diario de 3 dígitos por sucursal y trámite (C-001). |
| RN2 | Un cliente (o una cédula en el kiosco) tiene como máximo un turno activo a la vez. |
| RN3 | Orden de atención por puntaje = minutos de espera + bonificación: General 0, Preferencial 15, Cita 30. Empate: llega primero. |
| RN4 | Tiempo estimado = ⌈personas antes × minutos promedio del trámite ÷ módulos abiertos⌉ (mínimo 1 módulo). |
| RN5 | Un turno llamado 3 veces sin presentarse se marca ausente. |
| RN6 | Estados del turno: En espera → Llamado → En atención → Atendido; Llamado → Ausente; En espera → Cancelado; En atención → En espera (transferido). |
| RN7 | Solo se toman turnos dentro del horario de la sucursal. |
| RN8 | Un asesor solo abre módulos de su sucursal y solo llama turnos de los trámites de su módulo. |
| RN9 | Citas en franjas de 30 minutos; cupos = módulos que atienden el trámite; check-in de 15 min antes a 10 min después. |
| RN10 | La calificación es de 1 a 5, una sola vez y solo para turnos atendidos. |
| RN11 | Solo se cancela un turno en espera. |
| RN12 | Una cita se cancela hasta 1 hora antes. |

### Ejemplo de la cola (RN3)

A las 10:00 hay tres turnos de Caja en espera:

| Turno | Prioridad | Llegó | Minutos esperando | Bonificación | Puntaje | Orden |
|---|---|---|---|---|---|---|
| C-001 | General | 9:40 | 20 | 0 | 20 | 3.º |
| C-002 | Preferencial | 9:52 | 8 | 15 | 23 | 2.º |
| C-003 | Cita | 9:58 | 2 | 30 | 32 | 1.º |

Como todos los turnos suman los mismos minutos con el paso del tiempo, el orden entre dos turnos no cambia mientras esperan: solo cambia cuando llega uno nuevo. Un turno general nunca queda relegado para siempre: un preferencial que llega 15 minutos o más después ya no lo supera.

### Ejemplo del tiempo estimado (RN4)

4 personas antes × 5 min (Caja) ÷ 2 cajas abiertas = 10 minutos.

## Matriz de trazabilidad HU → RF → componentes

| HU | RF | Controlador | Servicio | Clases de dominio |
|---|---|---|---|---|
| HU_001 | RF_001 | AuthController | AuthServicio | Cliente, Usuario |
| HU_002 | RF_002 | AuthController | AuthServicio, JwtServicio | Usuario |
| HU_003 | RF_003 | SucursalController | SucursalServicio | Sucursal |
| HU_004 | RF_004 | TramiteController | TramiteServicio | Tramite |
| HU_005 | RF_005 | ModuloController | ModuloServicio | Modulo |
| HU_006 | RF_006 | AsesorController | AsesorServicio | Asesor |
| HU_007 | RF_007 | SucursalController | SucursalServicio, ColaServicio | Sucursal, ColaAtencion |
| HU_008 | RF_008 | TurnoController | TurnoServicio, ColaServicio | Turno, Cliente, PoliticaPrioridad |
| HU_009 | RF_009 | KioscoController | TurnoServicio | Turno |
| HU_010 | RF_010, RF_021, RF_022 | TurnoController, NotificacionController | TurnoServicio, OyenteTurnos, NotificacionServicio | Turno, ColaAtencion, Notificacion |
| HU_011 | RF_011 | TurnoController | TurnoServicio | Turno, EstadoTurno |
| HU_012 | RF_012 | AtencionController | AtencionServicio | Modulo, Asesor |
| HU_013 | RF_013 | AtencionController | AtencionServicio, ColaServicio | ColaAtencion, PoliticaPrioridad, Turno |
| HU_014 | RF_014 | AtencionController | AtencionServicio | Turno, EstadoTurno |
| HU_015 | RF_015 | AtencionController | AtencionServicio | Turno, Tramite |
| HU_016 | RF_016 | CitaController | CitaServicio | Cita |
| HU_017 | RF_017 | CitaController | CitaServicio, TurnoServicio | Cita, Turno |
| HU_018 | RF_018 | PantallaController | PantallaServicio | Turno |
| HU_019 | RF_019 | TurnoController | TurnoServicio | Turno |
| HU_020 | RF_020 | TableroController | TableroServicio | ReporteIndicadores, LineaIndicador |
