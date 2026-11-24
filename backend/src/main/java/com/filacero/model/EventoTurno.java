package com.filacero.model;

/** Evento de dominio que se publica cuando un turno cambia (patrón Observer). */
public record EventoTurno(Turno turno, TipoEvento tipo) {
}
