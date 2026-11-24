package com.filacero.model;

import com.filacero.model.prioridad.FabricaPrioridades;
import com.filacero.model.prioridad.PrioridadCita;
import com.filacero.model.prioridad.PrioridadGeneral;
import com.filacero.model.prioridad.PrioridadPreferencial;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** RN3: puntaje = minutos esperando + bonificación (General 0, Preferencial 15, Cita 30). */
class PrioridadTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 24, 10, 0);

    @Test
    void laFabricaEntregaLaEstrategiaDeCadaTipo() {
        assertInstanceOf(PrioridadGeneral.class, FabricaPrioridades.para(TipoPrioridad.GENERAL));
        assertInstanceOf(PrioridadPreferencial.class, FabricaPrioridades.para(TipoPrioridad.PREFERENCIAL));
        assertInstanceOf(PrioridadCita.class, FabricaPrioridades.para(TipoPrioridad.CITA));
        assertInstanceOf(PrioridadGeneral.class, FabricaPrioridades.para(null));
    }

    @Test
    void elPuntajeSumaLaEsperaYLaBonificacion() {
        Turno t = Fabrica.turno("C-001", TipoPrioridad.GENERAL, AHORA.minusMinutes(10));
        assertEquals(10, FabricaPrioridades.para(TipoPrioridad.GENERAL).puntaje(t, AHORA));
        assertEquals(25, FabricaPrioridades.para(TipoPrioridad.PREFERENCIAL).puntaje(t, AHORA));
        assertEquals(40, FabricaPrioridades.para(TipoPrioridad.CITA).puntaje(t, AHORA));
    }

    @Test
    void lasBonificacionesSonLasDeLaReglaDeNegocio() {
        assertEquals(0, PrioridadGeneral.BONIFICACION);
        assertEquals(15, PrioridadPreferencial.BONIFICACION);
        assertEquals(30, PrioridadCita.BONIFICACION);
    }
}
