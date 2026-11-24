package com.filacero.dto;

import com.filacero.model.Cita;
import com.filacero.model.EstadoCita;

import java.time.LocalDateTime;

public record CitaResponse(String id, String sucursalId, String sucursalNombre, String tramiteId, String tramiteNombre,
                           LocalDateTime fechaHora, EstadoCita estado, String motivo, String turnoId,
                           boolean puedeHacerCheckIn, boolean puedeCancelarse) {
    public static CitaResponse desde(Cita c, LocalDateTime ahora) {
        boolean cancelable = c.getEstado() == EstadoCita.PROGRAMADA
                && ahora.isBefore(c.getFechaHora().minusHours(Cita.CANCELAR_HORAS_ANTES));
        return new CitaResponse(c.getId(), c.getSucursalId(), c.getSucursalNombre(), c.getTramiteId(),
                c.getTramiteNombre(), c.getFechaHora(), c.getEstado(), c.getMotivo(), c.getTurnoId(),
                c.puedeHacerCheckIn(ahora), cancelable);
    }
}
