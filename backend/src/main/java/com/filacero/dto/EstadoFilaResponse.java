package com.filacero.dto;

/** Cómo está la fila de un trámite en una sucursal en este momento. */
public record EstadoFilaResponse(String tramiteId, String tramiteNombre, String prefijo, int enEspera,
                                 int modulosAbiertos, int tiempoEstimadoMin) {
}
