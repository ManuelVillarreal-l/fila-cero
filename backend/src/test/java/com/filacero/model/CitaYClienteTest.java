package com.filacero.model;

import com.filacero.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitaYClienteTest {

    private static final LocalDateTime CITA = LocalDateTime.of(2026, 11, 24, 10, 0);

    private Cita cita() {
        Cliente c = new Cliente("Carlos", "1085100001", "c@x.co", "hash", "3001234567",
                LocalDate.of(1998, 1, 1), CondicionEspecial.NINGUNA);
        c.setId("cli");
        return new Cita(c, Fabrica.sucursal(), Fabrica.caja(), CITA, "CDT", CITA.minusDays(1));
    }

    @Test
    void elCheckInSeHabilitaDe15MinutosAntesA10Despues() {
        Cita c = cita();
        assertFalse(c.puedeHacerCheckIn(CITA.minusMinutes(16)));
        assertTrue(c.puedeHacerCheckIn(CITA.minusMinutes(15)));
        assertTrue(c.puedeHacerCheckIn(CITA.plusMinutes(10)));
        assertFalse(c.puedeHacerCheckIn(CITA.plusMinutes(11)));
        assertTrue(c.estaVencida(CITA.plusMinutes(11)));
    }

    @Test
    void registrarLlegadaTardeExplicaQueLaCitaVencio() {
        Cita c = cita();
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> c.registrarLlegada("t1", CITA.plusMinutes(30)));
        assertTrue(e.getMessage().contains("venció"));
        c.registrarLlegada("t1", CITA.minusMinutes(5));
        assertEquals(EstadoCita.REGISTRADA, c.getEstado());
    }

    @Test
    void laCitaSeCancelaHastaUnaHoraAntes() {
        Cita tarde = cita();
        assertThrows(ReglaNegocioException.class, () -> tarde.cancelar(CITA.minusMinutes(30)));
        Cita aTiempo = cita();
        aTiempo.cancelar(CITA.minusHours(2));
        assertEquals(EstadoCita.CANCELADA, aTiempo.getEstado());
    }

    @Test
    void elClienteEsPreferencialPorEdadOCondicion() {
        LocalDate hoy = LocalDate.of(2026, 11, 24);
        Cliente joven = new Cliente("A", "1", "a@x.co", "h", null, LocalDate.of(2000, 1, 1), null);
        Cliente mayor = new Cliente("B", "2", "b@x.co", "h", null, LocalDate.of(1966, 11, 24), null);
        Cliente embarazada = new Cliente("C", "3", "c@x.co", "h", null, LocalDate.of(1995, 1, 1),
                CondicionEspecial.EMBARAZO);
        assertEquals(TipoPrioridad.GENERAL, joven.prioridadPara(hoy));
        assertEquals(TipoPrioridad.PREFERENCIAL, mayor.prioridadPara(hoy), "cumple 60 hoy");
        assertEquals(TipoPrioridad.PREFERENCIAL, embarazada.prioridadPara(hoy));
    }

    @Test
    void laSucursalSabeSiEstaAbiertaYElTramiteArmaElCodigo() {
        Sucursal s = Fabrica.sucursal();
        assertTrue(s.estaAbierta(LocalTime.of(8, 0)));
        assertFalse(s.estaAbierta(LocalTime.of(16, 0)));
        assertFalse(s.estaAbierta(LocalTime.of(7, 59)));
        assertEquals("C-007", Fabrica.caja().codigoTurno(7));
        assertEquals("C-123", Fabrica.caja().codigoTurno(123));
    }

    @Test
    void elModuloSoloLoAbreUnAsesorDeSuSucursal() {
        Modulo m = Fabrica.modulo();
        Asesor otro = new Asesor("Otro", "999999", "o@x.co", "h", "torobajo");
        otro.setId("a2");
        assertThrows(ReglaNegocioException.class, () -> m.abrir(otro));
        m.abrir(Fabrica.asesor());
        assertEquals(EstadoModulo.DISPONIBLE, m.getEstado());
        m.ocupar();
        assertThrows(ReglaNegocioException.class, m::cerrar);
        m.liberar();
        m.cerrar();
        assertEquals(EstadoModulo.CERRADO, m.getEstado());
    }
}
