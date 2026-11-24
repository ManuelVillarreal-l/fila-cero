package com.filacero.model;

/** Fila del tablero de indicadores: un trámite, un asesor o una hora del día. */
public record LineaIndicador(
        String nombre,
        long total,
        long atendidos,
        long ausentes,
        long cancelados,
        double esperaPromedioMin,
        double atencionPromedioMin,
        Double calificacionPromedio) {
}
