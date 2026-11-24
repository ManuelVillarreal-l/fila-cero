package com.filacero.model.prioridad;

import com.filacero.model.TipoPrioridad;

/** Cliente que llegó a su cita agendada: 30 minutos de ventaja (RN3). */
public class PrioridadCita implements PoliticaPrioridad {

    public static final int BONIFICACION = 30;

    @Override
    public TipoPrioridad tipo() { return TipoPrioridad.CITA; }

    @Override
    public int bonificacionMinutos() { return BONIFICACION; }

    @Override
    public String descripcion() { return "Cita agendada"; }
}
