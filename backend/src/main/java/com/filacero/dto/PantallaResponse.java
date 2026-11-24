package com.filacero.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Datos de la pantalla de llamado (TV) de una sucursal (HU_018). */
public record PantallaResponse(String sucursalId, String sucursalNombre, LocalDateTime hora,
                               List<TurnoResponse> llamados, List<EstadoFilaResponse> filas) {
}
