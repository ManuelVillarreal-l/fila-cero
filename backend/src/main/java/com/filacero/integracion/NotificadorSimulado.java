package com.filacero.integracion;

import com.filacero.model.Notificacion;
import com.filacero.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementación simulada del servicio de notificaciones: guarda el aviso para que la app
 * lo muestre (bandeja del cliente) y deja en el log el "SMS" que se habría enviado.
 */
@Component
public class NotificadorSimulado implements NotificadorTurnos {

    private static final Logger log = LoggerFactory.getLogger(NotificadorSimulado.class);
    private final NotificacionRepository notificaciones;

    public NotificadorSimulado(NotificacionRepository notificaciones) {
        this.notificaciones = notificaciones;
    }

    @Override
    public void enviar(Notificacion notificacion) {
        notificaciones.save(notificacion);
        log.info("[SMS simulado] cliente={} | {} | {}", notificacion.getClienteId(), notificacion.getTitulo(),
                notificacion.getMensaje());
    }
}
