package com.filacero.model;

import java.time.LocalDateTime;
import java.util.List;

/** Objetos de prueba. */
final class Fabrica {

    private Fabrica() {
    }

    static Sucursal sucursal() {
        Sucursal s = new Sucursal("Sucursal Centro", "Calle 18", "Pasto", "1",
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(16, 0), List.of("caja", "asesoria"));
        s.setId("centro");
        return s;
    }

    static Tramite caja() {
        Tramite t = new Tramite("C", "Caja", "Depósitos", 5);
        t.setId("caja");
        return t;
    }

    static Turno turno(String codigo, TipoPrioridad prioridad, LocalDateTime creado) {
        Turno t = new Turno(codigo, sucursal(), caja(), creado.toLocalDate().toString(), prioridad,
                CanalTurno.APP, creado);
        t.setId(codigo);
        return t;
    }

    static Modulo modulo() {
        Modulo m = new Modulo("centro", 1, "Caja 1", List.of("caja"));
        m.setId("m1");
        return m;
    }

    static Asesor asesor() {
        Asesor a = new Asesor("Laura", "123456", "laura@x.co", "hash", "centro");
        a.setId("a1");
        return a;
    }
}
