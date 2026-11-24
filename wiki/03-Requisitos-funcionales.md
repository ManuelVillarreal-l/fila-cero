# 03 · Requisitos funcionales

| Código | Requisito | Historia |
|---|---|---|
| RF_001 | Registrar clientes con nombre, cédula, correo, contraseña, celular, fecha de nacimiento y condición especial. | [[HU_001]] |
| RF_002 | Autenticar usuarios por correo y contraseña y emitir un token JWT con su rol. | [[HU_002]] |
| RF_003 | Crear, consultar, actualizar y desactivar sucursales con horario y trámites ofrecidos. | [[HU_003]] |
| RF_004 | Crear, consultar, actualizar y desactivar trámites con prefijo y tiempo promedio. | [[HU_004]] |
| RF_005 | Crear, consultar, actualizar y eliminar módulos de atención por sucursal. | [[HU_005]] |
| RF_006 | Crear, consultar, actualizar y desactivar cuentas de asesor asignadas a una sucursal. | [[HU_006]] |
| RF_007 | Mostrar sucursales con su estado (abierta/cerrada) y, por trámite, personas en espera, módulos abiertos y tiempo estimado. | [[HU_007]] |
| RF_008 | Emitir un turno virtual con código diario, prioridad según el cliente y validaciones de horario y turno único. | [[HU_008]] |
| RF_009 | Emitir un turno presencial en el kiosco con la cédula, sin cuenta. | [[HU_009]] |
| RF_010 | Mostrar al cliente su turno activo con posición, personas antes y tiempo estimado. | [[HU_010]] |
| RF_011 | Cancelar un turno en espera por parte de su dueño. | [[HU_011]] |
| RF_012 | Abrir, pausar, reanudar y cerrar el módulo de un asesor. | [[HU_012]] |
| RF_013 | Llamar el siguiente turno según el puntaje de prioridad y los trámites del módulo. | [[HU_013]] |
| RF_014 | Registrar inicio y fin de la atención, volver a llamar (máximo 3) y marcar ausente. | [[HU_014]] |
| RF_015 | Transferir un turno en atención a la fila de otro trámite conservando su hora de llegada. | [[HU_015]] |
| RF_016 | Mostrar franjas de citas con cupos y agendar citas. | [[HU_016]] |
| RF_017 | Hacer check-in de una cita (genera turno con prioridad CITA), cancelarla y vencerla automáticamente. | [[HU_017]] |
| RF_018 | Mostrar la pantalla pública de llamados de una sucursal. | [[HU_018]] |
| RF_019 | Registrar la calificación (1 a 5) de un turno atendido. | [[HU_019]] |
| RF_020 | Calcular indicadores por sucursal y rango de fechas: totales, promedios, por trámite, por asesor y por hora. | [[HU_020]] |
| RF_021 | Notificar al cliente los cambios de su turno (creado, próximo, llamado, atendido, ausente, transferido) a través de un servicio de notificaciones. | [[HU_010]] |
| RF_022 | Consultar el historial de turnos del cliente. | [[HU_010]] |

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
| Turno | Prioridad | Minutos esperando | Bonificación | Puntaje | Orden |
|---|---|---|---|---|---|
| C-001 | General | 20 | 0 | 20 | 3.º |
| C-002 | Preferencial | 8 | 15 | 23 | 2.º |
| C-003 | Cita | 2 | 30 | 32 | 1.º |
