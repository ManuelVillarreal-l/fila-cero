package com.filacero.model;

/** Eventos del ciclo de vida del turno que se publican a los observadores (patrón Observer). */
public enum TipoEvento {
    TURNO_CREADO,
    TURNO_PROXIMO,
    TURNO_LLAMADO,
    TURNO_ATENDIDO,
    TURNO_AUSENTE,
    TURNO_CANCELADO,
    TURNO_TRANSFERIDO
}
