# Base de datos · MongoDB

Fila Cero usa **MongoDB** (Atlas M0 en la nube, `mongo:7` en local con Docker). Spring Data MongoDB crea las colecciones y los índices al arrancar (`auto-index-creation: true`), y `config/DatosIniciales.java` carga los datos de prueba si la base está vacía.

## Colecciones

| Colección | Clase | Contenido | Índices |
|---|---|---|---|
| `usuarios` | `Usuario` (abstracta) → `Cliente`, `Asesor`, `Administrador` | Una sola colección para la jerarquía; MongoDB guarda la clase concreta en `_class` | `correo` único, `documento` único |
| `sucursales` | `Sucursal` | Nombre, dirección, horario (`horaApertura`, `horaCierre`) y `tramitesIds` | — |
| `tramites` | `Tramite` | Prefijo del código, nombre y `tiempoPromedioMin` | `prefijo` único |
| `modulos` | `Modulo` | Ventanilla de una sucursal: número, trámites que atiende, estado y asesor actual | — |
| `turnos` | `Turno` | Código, sucursal, trámite, cliente, prioridad, canal, estado, tiempos, calificación y `version` (bloqueo optimista) | `{sucursalId, tramiteId, fecha, estado}` |
| `citas` | `Cita` | Cliente, sucursal, trámite, `fechaHora`, estado y turno generado en el check-in | — |
| `notificaciones` | `Notificacion` | Avisos para el cliente generados por el observador de turnos | — |
| `contadores` | (documento simple) | Consecutivo diario por `sucursal:trámite:fecha`; se incrementa con `findAndModify` + `$inc` (atómico) | `_id` |

## Modelo documental

- Las relaciones se guardan **por id** (`sucursalId`, `tramiteId`, `clienteId`, `moduloId`) y los nombres que se muestran a menudo se **copian** en el documento (`sucursalNombre`, `tramiteNombre`, `asesorNombre`). Así la pantalla de llamado y el historial se leen con una sola consulta y el turno conserva cómo se llamaba el trámite cuando se atendió.
- El campo `fecha` del turno (`yyyy-MM-dd`) permite que la cola se reinicie cada día y que el tablero filtre por rango con un índice.
- Las horas se guardan en hora de Colombia (`America/Bogota`), también en Render, que corre en UTC.

## Ejemplo de documento `turnos`

```json
{
  "_id": "6742f1c2a1b2c3d4e5f60718",
  "version": 3,
  "codigo": "C-014",
  "sucursalId": "6742f0...", "sucursalNombre": "Sucursal Centro",
  "tramiteId": "6742f0...", "tramiteNombre": "Caja",
  "fecha": "2026-11-24",
  "clienteId": "6742f0...", "clienteNombre": "Carlos Benavides", "clienteDocumento": "1085100001",
  "prioridad": "GENERAL", "canal": "APP", "estado": "ATENDIDO",
  "moduloId": "6742f0...", "moduloNombre": "Caja 2",
  "asesorId": "6742f0...", "asesorNombre": "Andrés Mora",
  "vecesLlamado": 1,
  "horaCreacion": "2026-11-24T09:41:00", "horaLlamado": "2026-11-24T09:58:00",
  "horaInicioAtencion": "2026-11-24T09:59:00", "horaFin": "2026-11-24T10:04:00",
  "calificacion": 5, "comentario": "Muy amable",
  "_class": "com.filacero.model.Turno"
}
```

## Conexión

- **Local:** `mongodb://localhost:27017/filacero` (valor por defecto).
- **Nube:** variable de entorno `MONGODB_URI` en Render con la cadena de MongoDB Atlas. **Nunca** se escribe en el repositorio.
