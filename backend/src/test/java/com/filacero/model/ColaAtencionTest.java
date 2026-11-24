package com.filacero.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColaAtencionTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 24, 10, 0);

    @Test
    void sinPrioridadesSeAtiendePorOrdenDeLlegada() {
        Turno primero = Fabrica.turno("C-001", TipoPrioridad.GENERAL, AHORA.minusMinutes(20));
        Turno segundo = Fabrica.turno("C-002", TipoPrioridad.GENERAL, AHORA.minusMinutes(10));
        ColaAtencion cola = new ColaAtencion(List.of(segundo, primero), AHORA);
        assertEquals("C-001", cola.siguiente().orElseThrow().getCodigo());
    }

    @Test
    void elPreferencialPasaAdelanteSiLaDiferenciaEsMenorA15Minutos() {
        Turno general = Fabrica.turno("C-001", TipoPrioridad.GENERAL, AHORA.minusMinutes(20)); // 20
        Turno mayor = Fabrica.turno("C-002", TipoPrioridad.PREFERENCIAL, AHORA.minusMinutes(10)); // 10 + 15 = 25
        ColaAtencion cola = new ColaAtencion(List.of(general, mayor), AHORA);
        assertEquals("C-002", cola.siguiente().orElseThrow().getCodigo());
        assertEquals(2, cola.posicionDe("C-001"));
    }

    @Test
    void unGeneralQueEsperoMuchoNoQuedaRelegadoParaSiempre() {
        Turno general = Fabrica.turno("C-001", TipoPrioridad.GENERAL, AHORA.minusMinutes(40)); // 40
        Turno mayor = Fabrica.turno("C-002", TipoPrioridad.PREFERENCIAL, AHORA.minusMinutes(5)); // 20
        ColaAtencion cola = new ColaAtencion(List.of(mayor, general), AHORA);
        assertEquals("C-001", cola.siguiente().orElseThrow().getCodigo());
    }

    @Test
    void laCitaTieneMasPrioridadQueElPreferencial() {
        Turno mayor = Fabrica.turno("C-001", TipoPrioridad.PREFERENCIAL, AHORA.minusMinutes(10)); // 25
        Turno cita = Fabrica.turno("C-002", TipoPrioridad.CITA, AHORA.minusMinutes(1)); // 31
        assertEquals("C-002", new ColaAtencion(List.of(mayor, cita), AHORA).siguiente().orElseThrow().getCodigo());
    }

    @Test
    void soloEntranTurnosEnEspera() {
        Turno llamado = Fabrica.turno("C-001", TipoPrioridad.GENERAL, AHORA.minusMinutes(30));
        llamado.llamar(Fabrica.modulo(), Fabrica.asesor(), AHORA);
        Turno espera = Fabrica.turno("C-002", TipoPrioridad.GENERAL, AHORA.minusMinutes(5));
        ColaAtencion cola = new ColaAtencion(List.of(llamado, espera), AHORA);
        assertEquals(1, cola.tamano());
        assertEquals(0, cola.posicionDe("C-001"));
    }

    @Test
    void tiempoEstimadoDivideEntreLosModulosAbiertos() {
        // RN4: ceil(4 personas x 5 min / 2 módulos) = 10
        assertEquals(10, ColaAtencion.tiempoEstimado(4, 5, 2));
        assertEquals(8, ColaAtencion.tiempoEstimado(3, 5, 2)); // 7.5 -> 8
        assertEquals(15, ColaAtencion.tiempoEstimado(3, 5, 0)); // sin módulos abiertos se toma 1
        assertEquals(0, ColaAtencion.tiempoEstimado(0, 5, 1));
    }

    @Test
    void tiempoEstimadoDeUnTurnoCuentaSoloLosQueVanAntes() {
        Turno a = Fabrica.turno("C-001", TipoPrioridad.GENERAL, AHORA.minusMinutes(9));
        Turno b = Fabrica.turno("C-002", TipoPrioridad.GENERAL, AHORA.minusMinutes(6));
        Turno c = Fabrica.turno("C-003", TipoPrioridad.GENERAL, AHORA.minusMinutes(3));
        ColaAtencion cola = new ColaAtencion(List.of(a, b, c), AHORA);
        assertEquals(0, cola.tiempoEstimadoPara("C-001", 5, 1));
        assertEquals(10, cola.tiempoEstimadoPara("C-003", 5, 1));
        assertEquals(15, cola.tiempoEstimadoNuevoTurno(5, 1));
        assertTrue(cola.enOrden().get(0).getCodigo().equals("C-001"));
    }
}
