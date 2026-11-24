package com.filacero.model.prioridad;

import com.filacero.model.TipoPrioridad;

import java.util.EnumMap;
import java.util.Map;

/**
 * Simple Factory (GRASP Fabricación pura): entrega la estrategia de prioridad de cada tipo.
 * Para agregar un tipo nuevo: valor en TipoPrioridad + clase que implemente PoliticaPrioridad + un caso aquí.
 */
public final class FabricaPrioridades {

    private static final Map<TipoPrioridad, PoliticaPrioridad> POLITICAS = new EnumMap<>(TipoPrioridad.class);

    static {
        POLITICAS.put(TipoPrioridad.GENERAL, new PrioridadGeneral());
        POLITICAS.put(TipoPrioridad.PREFERENCIAL, new PrioridadPreferencial());
        POLITICAS.put(TipoPrioridad.CITA, new PrioridadCita());
    }

    private FabricaPrioridades() {
    }

    public static PoliticaPrioridad para(TipoPrioridad tipo) {
        PoliticaPrioridad politica = POLITICAS.get(tipo == null ? TipoPrioridad.GENERAL : tipo);
        if (politica == null) {
            throw new IllegalArgumentException("No hay política de prioridad para " + tipo);
        }
        return politica;
    }
}
