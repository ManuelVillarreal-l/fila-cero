# 01 · Introducción

## 1.1 Propósito
Este documento especifica los requisitos y el diseño de **Fila Cero**, un sistema para que los clientes del Banco del Galeras tomen y sigan sus turnos sin hacer fila, para que los asesores atiendan en un orden justo y para que el banco mida la calidad de su atención.

## 1.2 Problema
En las oficinas bancarias de Pasto los clientes esperan de pie sin saber cuánto falta, las personas con prioridad legal dependen de que alguien las vea y el banco no tiene datos de cuánto se espera en cada trámite y hora. Fila Cero resuelve:
- **Incertidumbre:** el cliente ve antes de salir de casa cuánta gente hay y cuánto tardará.
- **Filas físicas:** el turno se toma desde el celular o en el kiosco y se sigue en vivo.
- **Prioridad justa:** una regla de puntaje atiende primero a quien corresponde sin relegar a nadie para siempre.
- **Falta de datos:** el tablero muestra esperas, horas pico, ausentismo y satisfacción.

## 1.3 Alcance
Incluye: cuentas de clientes, configuración de sucursales/trámites/módulos/asesores, turnos virtuales y de kiosco, cola por prioridad, atención en ventanilla (llamar, volver a llamar, ausente, transferir), pantalla de llamado, citas con check-in, notificaciones (simuladas), calificación y tablero de indicadores.

No incluye: operaciones bancarias reales, integración con el core del banco ni envío real de SMS.

## 1.4 Definiciones
| Término | Significado |
|---|---|
| Turno | Lugar en la fila de un trámite. Código = prefijo + consecutivo diario (C-014) |
| Trámite | Servicio que se atiende con turno: Caja, Asesoría, Créditos, Empresas, Servicio al cliente |
| Módulo | Ventanilla donde atiende un asesor |
| Prioridad | General, Preferencial (adulto mayor, embarazo, discapacidad) o Cita |
| Puntaje | Minutos de espera + bonificación de la prioridad (RN3) |
| Kiosco | Pantalla táctil de la sucursal para tomar turno con la cédula |

## 1.5 Referencias
- IEEE 830-1998, *Recommended Practice for Software Requirements Specifications*.
- Material del curso Diseño de Software: vistas de contexto, funcional, lógica, desarrollo y despliegue.
- Ley 1171 de 2007 (atención preferencial a adultos mayores en Colombia).
