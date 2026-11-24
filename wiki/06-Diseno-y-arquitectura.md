# 06 · Diseño y arquitectura

Todos los diagramas se hicieron en **Visual Paradigm**. Cada elemento se puede rastrear a una historia de usuario y a un requisito (ver tabla al final y [diagramas/README.md](https://github.com/ManuelVillarreal-l/fila-cero/blob/main/diagramas/README.md)).

## Vista de contexto (nivel 0)
El sistema es un solo proceso; los actores y el sistema externo son rectángulos. Las flechas que entran son acciones; las que salen, información.
![](https://github.com/ManuelVillarreal-l/fila-cero/raw/main/diagramas/vista-contexto.png)

## Vista funcional (componentes)
![](https://github.com/ManuelVillarreal-l/fila-cero/raw/main/diagramas/vista-funcional.png)

## Vista lógica (clases)
![](https://github.com/ManuelVillarreal-l/fila-cero/raw/main/diagramas/vista-logica.png)

## Vista de desarrollo (clases por capas)
![](https://github.com/ManuelVillarreal-l/fila-cero/raw/main/diagramas/vista-desarrollo.png)

## Vista de despliegue
![](https://github.com/ManuelVillarreal-l/fila-cero/raw/main/diagramas/vista-despliegue.png)

## Diagramas complementarios
![](https://github.com/ManuelVillarreal-l/fila-cero/raw/main/diagramas/secuencia-llamar-siguiente.png)
![](https://github.com/ManuelVillarreal-l/fila-cero/raw/main/diagramas/secuencia-tomar-turno.png)
![](https://github.com/ManuelVillarreal-l/fila-cero/raw/main/diagramas/estados-turno.png)

## Patrones
| Patrón | Dónde |
|---|---|
| GRASP Controlador | `*Controller` delega en su servicio |
| GRASP Experto | `Turno`, `Cliente.esPreferencial`, `Sucursal.estaAbierta`, `Cita.puedeHacerCheckIn`, `ColaAtencion` |
| GRASP Creador | `ReporteIndicadores` crea `LineaIndicador`; `TurnoServicio.crear` crea el `Turno` |
| GRASP Polimorfismo + GoF Strategy | `PoliticaPrioridad`: General, Preferencial, Cita |
| GRASP Fabricación pura + Simple Factory | `FabricaPrioridades`, `ColaServicio`, repositorios |
| GoF State | `EstadoTurno` y las transiciones del `Turno` |
| GoF Observer | `EventoTurno` publicado por los servicios y escuchado por `OyenteTurnos` |
| Indirección / Variaciones protegidas | `NotificadorTurnos`, `PoliticaPrioridad` |
| Chain of Responsibility | `FiltroJwt` en la cadena de Spring Security |
| Repository, DTO, Inyección de dependencias | Toda la aplicación |
