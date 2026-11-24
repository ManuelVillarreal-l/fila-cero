package com.filacero.repository;

import com.filacero.model.Cita;
import com.filacero.model.EstadoCita;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CitaRepository extends MongoRepository<Cita, String> {
    List<Cita> findByClienteIdOrderByFechaHoraDesc(String clienteId);
    long countBySucursalIdAndTramiteIdAndFechaHoraAndEstadoIn(String sucursalId, String tramiteId,
                                                             LocalDateTime fechaHora, Collection<EstadoCita> estados);
    List<Cita> findBySucursalIdAndTramiteIdAndFechaHoraBetweenAndEstadoIn(String sucursalId, String tramiteId,
                                                                          LocalDateTime desde, LocalDateTime hasta,
                                                                          Collection<EstadoCita> estados);
    boolean existsByClienteIdAndFechaHoraAndEstado(String clienteId, LocalDateTime fechaHora, EstadoCita estado);
    List<Cita> findByEstadoAndFechaHoraBefore(EstadoCita estado, LocalDateTime limite);
}
