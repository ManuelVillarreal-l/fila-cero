package com.filacero.model.prioridad;

import com.filacero.model.TipoPrioridad;

/** Adulto mayor, embarazo o discapacidad: 15 minutos de ventaja (RN3). */
public class PrioridadPreferencial implements PoliticaPrioridad {

    public static final int BONIFICACION = 15;

    @Override
    public TipoPrioridad tipo() { return TipoPrioridad.PREFERENCIAL; }

    @Override
    public int bonificacionMinutos() { return BONIFICACION; }

    @Override
    public String descripcion() { return "Atención preferencial"; }
}
