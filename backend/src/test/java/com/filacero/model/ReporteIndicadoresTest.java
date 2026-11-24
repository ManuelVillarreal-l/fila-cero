package com.filacero.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReporteIndicadoresTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 24, 12, 0);

    @Test
    void calculaTotalesPromediosYPorcentajes() {
        Turno a = Fabrica.turno("C-001", TipoPrioridad.GENERAL, AHORA.minusMinutes(60));
        a.llamar(Fabrica.modulo(), Fabrica.asesor(), AHORA.minusMinutes(50)); // espera 10
        a.iniciarAtencion(AHORA.minusMinutes(50));
        a.finalizar(null, AHORA.minusMinutes(44)); // atención 6
        a.calificar(4, null);

        Turno b = Fabrica.turno("C-002", TipoPrioridad.GENERAL, AHORA.minusMinutes(40));
        b.llamar(Fabrica.modulo(), Fabrica.asesor(), AHORA.minusMinutes(20)); // espera 20
        b.marcarAusente(AHORA.minusMinutes(18));

        Turno c = Fabrica.turno("C-003", TipoPrioridad.GENERAL, AHORA.minusMinutes(5));

        ReporteIndicadores r = new ReporteIndicadores("centro", "Centro", "2026-11-24", "2026-11-24",
                List.of(a, b, c), AHORA);
        assertEquals(3, r.getResumen().total());
        assertEquals(1, r.getResumen().atendidos());
        assertEquals(1, r.getResumen().ausentes());
        assertEquals(15.0, r.getResumen().esperaPromedioMin());
        assertEquals(6.0, r.getResumen().atencionPromedioMin());
        assertEquals(4.0, r.getResumen().calificacionPromedio());
        assertEquals(33.3, r.getPorcentajeAtendidos());
        assertEquals(1, r.getEnEsperaAhora());
        assertEquals(1, r.getPorTramite().size());
        assertEquals("Laura", r.getPorAsesor().get(0).nombre());
    }
}
