package com.filacero.model.prioridad;

import com.filacero.model.TipoPrioridad;
import com.filacero.model.Turno;

import java.time.LocalDateTime;

/**
 * Patrón Strategy (GRASP Polimorfismo y Variaciones protegidas): cada tipo de prioridad
 * define cuántos minutos de ventaja le da a un turno en la cola.
 *
 * RN3: puntaje = minutos que lleva esperando + bonificación de la prioridad.
 * El turno con mayor puntaje se llama primero.
 */
public interface PoliticaPrioridad {

    TipoPrioridad tipo();

    /** Minutos de ventaja que la prioridad le suma al turno. */
    int bonificacionMinutos();

    String descripcion();

    default double puntaje(Turno turno, LocalDateTime ahora) {
        return turno.minutosEspera(ahora) + bonificacionMinutos();
    }
}
