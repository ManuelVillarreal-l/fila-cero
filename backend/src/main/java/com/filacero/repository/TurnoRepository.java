package com.filacero.repository;

import com.filacero.model.EstadoTurno;
import com.filacero.model.Turno;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface TurnoRepository extends MongoRepository<Turno, String> {
    List<Turno> findBySucursalIdAndTramiteIdAndFechaAndEstado(String sucursalId, String tramiteId, String fecha,
                                                               EstadoTurno estado);
    List<Turno> findBySucursalIdAndFechaAndEstado(String sucursalId, String fecha, EstadoTurno estado);
    List<Turno> findBySucursalIdAndFechaAndTramiteIdInAndEstado(String sucursalId, String fecha,
                                                                 Collection<String> tramitesIds, EstadoTurno estado);
    List<Turno> findByClienteIdAndFechaAndEstadoIn(String clienteId, String fecha, Collection<EstadoTurno> estados);
    List<Turno> findByClienteDocumentoAndSucursalIdAndFechaAndEstadoIn(String documento, String sucursalId, String fecha,
                                                                       Collection<EstadoTurno> estados);
    List<Turno> findByClienteIdOrderByHoraCreacionDesc(String clienteId);
    List<Turno> findByModuloIdAndEstadoIn(String moduloId, Collection<EstadoTurno> estados);
    List<Turno> findTop8BySucursalIdAndFechaAndHoraLlamadoNotNullOrderByHoraLlamadoDesc(String sucursalId, String fecha);
    List<Turno> findBySucursalIdAndFechaBetween(String sucursalId, String desde, String hasta);
}
