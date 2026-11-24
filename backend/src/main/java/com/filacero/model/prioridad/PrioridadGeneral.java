package com.filacero.model.prioridad;

import com.filacero.model.TipoPrioridad;

/** Turno sin prioridad: se atiende por orden de llegada. */
public class PrioridadGeneral implements PoliticaPrioridad {

    public static final int BONIFICACION = 0;

    @Override
    public TipoPrioridad tipo() { return TipoPrioridad.GENERAL; }

    @Override
    public int bonificacionMinutos() { return BONIFICACION; }

    @Override
    public String descripcion() { return "Orden de llegada"; }
}
