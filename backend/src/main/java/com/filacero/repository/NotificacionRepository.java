package com.filacero.repository;

import com.filacero.model.Notificacion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificacionRepository extends MongoRepository<Notificacion, String> {
    List<Notificacion> findTop30ByClienteIdOrderByFechaDesc(String clienteId);
    List<Notificacion> findByClienteIdAndLeidaFalse(String clienteId);
    boolean existsByTurnoIdAndTipo(String turnoId, com.filacero.model.TipoEvento tipo);
}
