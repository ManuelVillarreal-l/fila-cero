package com.filacero.dto;

import com.filacero.model.CanalTurno;
import com.filacero.model.EstadoTurno;
import com.filacero.model.TipoPrioridad;
import com.filacero.model.Turno;

import java.time.LocalDateTime;

/** Turno con su posición en la cola y el tiempo estimado (cuando está en espera). */
public record TurnoResponse(String id, String codigo, String sucursalId, String sucursalNombre, String tramiteId,
                            String tramiteNombre, String tramiteOrigenNombre, EstadoTurno estado,
                            TipoPrioridad prioridad, CanalTurno canal, String clienteNombre, String clienteDocumento,
                            String moduloNombre, String asesorNombre, int vecesLlamado, LocalDateTime horaCreacion,
                            LocalDateTime horaLlamado, LocalDateTime horaInicioAtencion, LocalDateTime horaFin,
                            Integer posicion, Integer personasAntes, Integer tiempoEstimadoMin, Integer calificacion,
                            String comentario, String observaciones) {

    public static TurnoResponse desde(Turno t) {
        return desde(t, null, null);
    }

    public static TurnoResponse desde(Turno t, Integer posicion, Integer tiempoEstimadoMin) {
        return new TurnoResponse(t.getId(), t.getCodigo(), t.getSucursalId(), t.getSucursalNombre(), t.getTramiteId(),
                t.getTramiteNombre(), t.getTramiteOrigenNombre(), t.getEstado(), t.getPrioridad(), t.getCanal(),
                t.getClienteNombre(), enmascarar(t.getClienteDocumento()), t.getModuloNombre(), t.getAsesorNombre(),
                t.getVecesLlamado(), t.getHoraCreacion(), t.getHoraLlamado(), t.getHoraInicioAtencion(), t.getHoraFin(),
                posicion, posicion == null ? null : Math.max(0, posicion - 1), tiempoEstimadoMin,
                t.getCalificacion(), t.getComentario(), t.getObservaciones());
    }

    /** Protección de datos: solo se muestran los últimos 4 dígitos del documento. */
    private static String enmascarar(String documento) {
        if (documento == null || documento.length() <= 4) {
            return documento;
        }
        return "****" + documento.substring(documento.length() - 4);
    }
}
