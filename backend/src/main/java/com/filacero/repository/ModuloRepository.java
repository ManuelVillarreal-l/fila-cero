package com.filacero.repository;

import com.filacero.model.EstadoModulo;
import com.filacero.model.Modulo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ModuloRepository extends MongoRepository<Modulo, String> {
    List<Modulo> findBySucursalIdOrderByNumero(String sucursalId);
    List<Modulo> findBySucursalIdAndEstadoNot(String sucursalId, EstadoModulo estado);
    Optional<Modulo> findByAsesorId(String asesorId);
    boolean existsBySucursalIdAndNumero(String sucursalId, int numero);
}
