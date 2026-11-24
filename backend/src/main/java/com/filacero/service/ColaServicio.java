package com.filacero.service;

import com.filacero.dto.EstadoFilaResponse;
import com.filacero.dto.TurnoResponse;
import com.filacero.model.ColaAtencion;
import com.filacero.model.EstadoModulo;
import com.filacero.model.EstadoTurno;
import com.filacero.model.Modulo;
import com.filacero.model.Sucursal;
import com.filacero.model.Tramite;
import com.filacero.model.Turno;
import com.filacero.repository.ModuloRepository;
import com.filacero.repository.TramiteRepository;
import com.filacero.repository.TurnoRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Arma las colas de atención del día y calcula posiciones y tiempos estimados (RN3, RN4).
 * Lo usan los servicios de turnos, atención, sucursales y pantalla (alta cohesión).
 */
@Service
public class ColaServicio {

    private final TurnoRepository turnos;
    private final ModuloRepository modulos;
    private final TramiteRepository tramites;
    private final Clock reloj;

    public ColaServicio(TurnoRepository turnos, ModuloRepository modulos, TramiteRepository tramites, Clock reloj) {
        this.turnos = turnos;
        this.modulos = modulos;
        this.tramites = tramites;
        this.reloj = reloj;
    }

    public String hoy() {
        return LocalDate.now(reloj).toString();
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }

    public ColaAtencion cola(String sucursalId, String tramiteId) {
        return new ColaAtencion(
                turnos.findBySucursalIdAndTramiteIdAndFechaAndEstado(sucursalId, tramiteId, hoy(), EstadoTurno.EN_ESPERA),
                ahora());
    }

    /** Cola de un módulo: mezcla los trámites que atiende y aplica la misma regla de prioridad. */
    public ColaAtencion colaDeModulo(Modulo modulo) {
        return new ColaAtencion(
                turnos.findBySucursalIdAndFechaAndTramiteIdInAndEstado(modulo.getSucursalId(), hoy(),
                        modulo.getTramitesIds(), EstadoTurno.EN_ESPERA),
                ahora());
    }

    public int modulosAbiertos(String sucursalId, String tramiteId) {
        return (int) modulos.findBySucursalIdAndEstadoNot(sucursalId, EstadoModulo.CERRADO).stream()
                .filter(m -> m.atiende(tramiteId))
                .count();
    }

    /** Agrega posición y tiempo estimado si el turno está en espera. */
    public TurnoResponse enriquecer(Turno turno) {
        if (turno.getEstado() != EstadoTurno.EN_ESPERA || !hoy().equals(turno.getFecha())) {
            return TurnoResponse.desde(turno);
        }
        ColaAtencion cola = cola(turno.getSucursalId(), turno.getTramiteId());
        int minutos = tramites.findById(turno.getTramiteId()).map(Tramite::getTiempoPromedioMin).orElse(10);
        int abiertos = modulosAbiertos(turno.getSucursalId(), turno.getTramiteId());
        return TurnoResponse.desde(turno, cola.posicionDe(turno.getId()),
                cola.tiempoEstimadoPara(turno.getId(), minutos, abiertos));
    }

    /** Estado de todas las filas de una sucursal: personas en espera y tiempo estimado para un turno nuevo. */
    public List<EstadoFilaResponse> estadoFilas(Sucursal sucursal) {
        return estadoFilas(sucursal.getId(), sucursal.getTramitesIds());
    }

    public List<EstadoFilaResponse> estadoFilas(String sucursalId, List<String> tramitesIds) {
        Map<String, Tramite> porId = tramites.findAllById(tramitesIds).stream()
                .filter(Tramite::isActivo)
                .collect(Collectors.toMap(Tramite::getId, Function.identity()));
        Map<String, Long> enEspera = turnos.findBySucursalIdAndFechaAndEstado(sucursalId, hoy(), EstadoTurno.EN_ESPERA)
                .stream().collect(Collectors.groupingBy(Turno::getTramiteId, Collectors.counting()));
        List<Modulo> abiertos = modulos.findBySucursalIdAndEstadoNot(sucursalId, EstadoModulo.CERRADO);
        return tramitesIds.stream()
                .filter(porId::containsKey)
                .map(porId::get)
                .map(t -> {
                    int espera = enEspera.getOrDefault(t.getId(), 0L).intValue();
                    int nAbiertos = (int) abiertos.stream().filter(m -> m.atiende(t.getId())).count();
                    return new EstadoFilaResponse(t.getId(), t.getNombre(), t.getPrefijo(), espera, nAbiertos,
                            ColaAtencion.tiempoEstimado(espera, t.getTiempoPromedioMin(), nAbiertos));
                })
                .toList();
    }
}
