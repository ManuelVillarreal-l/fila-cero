package com.filacero.repository;

import com.filacero.model.Tramite;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TramiteRepository extends MongoRepository<Tramite, String> {
    List<Tramite> findByActivoTrueOrderByNombre();
    boolean existsByPrefijoIgnoreCase(String prefijo);
}
