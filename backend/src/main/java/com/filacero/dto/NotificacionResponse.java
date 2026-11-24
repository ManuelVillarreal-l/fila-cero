package com.filacero.dto;

import com.filacero.model.Notificacion;
import com.filacero.model.TipoEvento;

import java.time.LocalDateTime;

public record NotificacionResponse(String id, String turnoId, TipoEvento tipo, String titulo, String mensaje,
                                   LocalDateTime fecha, boolean leida) {
    public static NotificacionResponse desde(Notificacion n) {
        return new NotificacionResponse(n.getId(), n.getTurnoId(), n.getTipo(), n.getTitulo(), n.getMensaje(),
                n.getFecha(), n.isLeida());
    }
}
