package com.filacero.service;

import com.filacero.dto.NotificacionResponse;
import com.filacero.repository.NotificacionRepository;
import com.filacero.security.UsuarioActual;
import org.springframework.stereotype.Service;

import java.util.List;

/** Bandeja de avisos del cliente (RF_021). */
@Service
public class NotificacionServicio {

    private final NotificacionRepository notificaciones;

    public NotificacionServicio(NotificacionRepository notificaciones) {
        this.notificaciones = notificaciones;
    }

    public List<NotificacionResponse> mias(UsuarioActual actual) {
        return notificaciones.findTop30ByClienteIdOrderByFechaDesc(actual.id()).stream()
                .map(NotificacionResponse::desde).toList();
    }

    public void marcarLeidas(UsuarioActual actual) {
        var pendientes = notificaciones.findByClienteIdAndLeidaFalse(actual.id());
        pendientes.forEach(n -> n.marcarLeida());
        notificaciones.saveAll(pendientes);
    }
}
