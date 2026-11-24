package com.filacero.model;

import com.filacero.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ciclo de vida del turno (RN5, RN6, RN10, RN11) con el patrón State. */
class TurnoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 24, 10, 0);

    private Turno enEspera() {
        return Fabrica.turno("C-001", TipoPrioridad.GENERAL, AHORA.minusMinutes(12));
    }

    @Test
    void flujoCompletoDeAtencion() {
        Turno t = enEspera();
        t.llamar(Fabrica.modulo(), Fabrica.asesor(), AHORA);
        assertEquals(EstadoTurno.LLAMADO, t.getEstado());
        assertEquals("Caja 1", t.getModuloNombre());
        assertEquals(12, t.minutosEspera(AHORA.plusMinutes(30)), "la espera se congela al ser llamado");
        t.iniciarAtencion(AHORA.plusMinutes(1));
        t.finalizar("Depósito", AHORA.plusMinutes(6));
        assertEquals(EstadoTurno.ATENDIDO, t.getEstado());
        assertEquals(5L, t.minutosAtencion());
        assertFalse(t.estaActivo());
    }

    @Test
    void noSePuedeFinalizarUnTurnoQueNoEstaEnAtencion() {
        Turno t = enEspera();
        assertThrows(ReglaNegocioException.class, () -> t.finalizar(null, AHORA));
    }

    @Test
    void seVuelveALlamarHastaTresVecesYLuegoSeMarcaAusente() {
        Turno t = enEspera();
        t.llamar(Fabrica.modulo(), Fabrica.asesor(), AHORA);
        t.volverALlamar(AHORA.plusMinutes(1));
        t.volverALlamar(AHORA.plusMinutes(2));
        assertEquals(Turno.MAX_LLAMADOS, t.getVecesLlamado());
        assertThrows(ReglaNegocioException.class, () -> t.volverALlamar(AHORA.plusMinutes(3)));
        t.marcarAusente(AHORA.plusMinutes(3));
        assertEquals(EstadoTurno.AUSENTE, t.getEstado());
    }

    @Test
    void soloSeCancelaEnEspera() {
        Turno t = enEspera();
        t.cancelar(AHORA);
        assertEquals(EstadoTurno.CANCELADO, t.getEstado());

        Turno llamado = enEspera();
        llamado.llamar(Fabrica.modulo(), Fabrica.asesor(), AHORA);
        assertThrows(ReglaNegocioException.class, () -> llamado.cancelar(AHORA));
    }

    @Test
    void transferirDevuelveElTurnoALaColaConservandoSuHoraDeLlegada() {
        Turno t = enEspera();
        LocalDateTime llegada = t.getHoraCreacion();
        t.llamar(Fabrica.modulo(), Fabrica.asesor(), AHORA);
        t.iniciarAtencion(AHORA);
        Tramite asesoria = new Tramite("A", "Asesoría", "", 15);
        asesoria.setId("asesoria");
        t.transferir(asesoria, "A-004");
        assertEquals(EstadoTurno.EN_ESPERA, t.getEstado());
        assertEquals("A-004", t.getCodigo());
        assertEquals("Caja", t.getTramiteOrigenNombre());
        assertEquals(llegada, t.getHoraCreacion());
        assertNull(t.getModuloId());
    }

    @Test
    void laCalificacionEsDeUnoACincoYUnaSolaVez() {
        Turno t = enEspera();
        assertThrows(ReglaNegocioException.class, () -> t.calificar(5, null), "no atendido");
        t.llamar(Fabrica.modulo(), Fabrica.asesor(), AHORA);
        t.iniciarAtencion(AHORA);
        t.finalizar(null, AHORA.plusMinutes(4));
        assertThrows(ReglaNegocioException.class, () -> t.calificar(6, null));
        t.calificar(5, "Excelente");
        assertThrows(ReglaNegocioException.class, () -> t.calificar(4, null));
        assertEquals(5, t.getCalificacion());
    }

    @Test
    void losEstadosFinalesNoTienenSalida() {
        assertTrue(EstadoTurno.ATENDIDO.esFinal());
        assertTrue(EstadoTurno.AUSENTE.esFinal());
        assertTrue(EstadoTurno.CANCELADO.esFinal());
        assertTrue(EstadoTurno.EN_ESPERA.esActivo());
        assertTrue(EstadoTurno.LLAMADO.puedePasarA(EstadoTurno.LLAMADO));
        assertFalse(EstadoTurno.EN_ESPERA.puedePasarA(EstadoTurno.ATENDIDO));
    }
}
