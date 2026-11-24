# 02 · Descripción general

## 2.1 Perspectiva del producto
Sistema web con arquitectura en tres capas: **cliente** (React en Vercel, en celular, kiosco, TV y PC), **servidor de aplicaciones** (API REST Spring Boot en Render, en contenedor Docker) y **datos** (MongoDB Atlas). Se comunica con un **servicio de notificaciones** externo a través de una interfaz propia.

![Vista de contexto](https://github.com/ManuelVillarreal-l/fila-cero/raw/main/diagramas/vista-contexto.png)

## 2.2 Actores
| Actor | Tipo | Qué hace |
|---|---|---|
| Visitante | Persona sin cuenta | Consulta sucursales y filas, toma turno en el kiosco, ve la pantalla de llamado, se registra |
| Cliente | Usuario registrado | Toma turno virtual, sigue su turno, recibe avisos, cancela, agenda citas, califica |
| Asesor | Usuario registrado | Abre su módulo, llama, atiende, marca ausente y transfiere turnos |
| Administrador | Usuario registrado | Gestiona sucursales, trámites, módulos y asesores; consulta indicadores |
| Servicio de notificaciones | Sistema externo | Entrega los avisos del turno al cliente (simulado) |

`Cliente`, `Asesor` y `Administrador` heredan de la clase abstracta `Usuario`. El Visitante no es una clase porque no tiene cuenta.

## 2.3 Restricciones
- Backend en Java 17 + Spring Boot con MongoDB (requisito del curso).
- Diagramas en Visual Paradigm.
- Servicios gratuitos en la nube (Render duerme tras 15 minutos sin uso).

## 2.4 Supuestos y dependencias
- Las sucursales tienen conexión a internet para el kiosco y la pantalla.
- Los avisos se muestran en la app; un proveedor de SMS se conectaría implementando `NotificadorTurnos`.
