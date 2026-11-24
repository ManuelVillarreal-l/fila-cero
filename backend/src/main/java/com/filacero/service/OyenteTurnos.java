package com.filacero.service;

import com.filacero.integracion.NotificadorTurnos;
import com.filacero.model.ColaAtencion;
import com.filacero.model.EventoTurno;
import com.filacero.model.Notificacion;
import com.filacero.model.TipoEvento;
import com.filacero.model.Turno;
import com.filacero.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Observador de los eventos del turno (patrón Observer): los servicios publican el evento
 * sin saber quién lo escucha, y este oyente arma los avisos para el cliente.
 */
@Component
public class OyenteTurnos {

    /** RF_021: se avisa cuando faltan esta cantidad de turnos o menos. */
    public static final int AVISAR_CUANDO_FALTEN = 2;

    private static final Logger log = LoggerFactory.getLogger(OyenteTurnos.class);

    private final NotificadorTurnos notificador;
    private final NotificacionRepository notificaciones;
    private final ColaServicio colas;

    public OyenteTurnos(NotificadorTurnos notificador, NotificacionRepository notificaciones, ColaServicio colas) {
        this.notificador = notificador;
        this.notificaciones = notificaciones;
        this.colas = colas;
    }

    @EventListener
    public void alCambiarTurno(EventoTurno evento) {
        try {
            Turno t = evento.turno();
            switch (evento.tipo()) {
                case TURNO_CREADO -> avisar(t, evento.tipo(), "Turno " + t.getCodigo() + " confirmado",
                        "Estás en la fila de " + t.getTramiteNombre() + " en " + t.getSucursalNombre() + ".");
                case TURNO_LLAMADO -> {
                    avisar(t, evento.tipo(), "¡Es tu turno! " + t.getCodigo(),
                            "Acércate a " + t.getModuloNombre() + (t.getVecesLlamado() > 1
                                    ? " (llamado " + t.getVecesLlamado() + " de " + Turno.MAX_LLAMADOS + ")" : "") + ".");
                    avisarProximos(t);
                }
                case TURNO_ATENDIDO -> avisar(t, evento.tipo(), "Gracias por visitarnos",
                        "Tu turno " + t.getCodigo() + " fue atendido. Califica la atención en tu historial.");
                case TURNO_AUSENTE -> avisar(t, evento.tipo(), "Turno " + t.getCodigo() + " perdido",
                        "No te presentaste después de " + Turno.MAX_LLAMADOS + " llamados. Puedes tomar un turno nuevo.");
                case TURNO_TRANSFERIDO -> avisar(t, evento.tipo(), "Te enviamos a " + t.getTramiteNombre(),
                        "Tu nuevo turno es " + t.getCodigo() + " y conservas tu tiempo de espera.");
                case TURNO_CANCELADO -> log.info("Turno {} cancelado", t.getCodigo());
                default -> { }
            }
        } catch (RuntimeException e) {
            // Una falla al notificar nunca debe tumbar la atención del turno.
            log.warn("No se pudo notificar el evento {}: {}", evento.tipo(), e.getMessage());
        }
    }

    /** Avisa a los clientes que quedaron a 1 o 2 turnos de ser llamados. */
    private void avisarProximos(Turno llamado) {
        ColaAtencion cola = colas.cola(llamado.getSucursalId(), llamado.getTramiteId());
        List<Turno> proximos = cola.enOrden().stream().limit(AVISAR_CUANDO_FALTEN).toList();
        for (int i = 0; i < proximos.size(); i++) {
            Turno t = proximos.get(i);
            if (t.getClienteId() != null && !notificaciones.existsByTurnoIdAndTipo(t.getId(), TipoEvento.TURNO_PROXIMO)) {
                avisar(t, TipoEvento.TURNO_PROXIMO, "Tu turno " + t.getCodigo() + " está cerca",
                        (i == 0 ? "Eres el siguiente" : "Falta 1 persona antes que tú")
                                + " en " + t.getSucursalNombre() + ". Acércate a la sala de espera.");
            }
        }
    }

    private void avisar(Turno t, TipoEvento tipo, String titulo, String mensaje) {
        if (t.getClienteId() == null) {
            return; // Turnos de kiosco sin cuenta: no hay a quién notificar.
        }
        notificador.enviar(new Notificacion(t.getClienteId(), t.getId(), tipo, titulo, mensaje, colas.ahora()));
    }
}
