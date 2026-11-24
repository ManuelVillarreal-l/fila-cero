package com.filacero.repository;

import com.filacero.model.Sucursal;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SucursalRepository extends MongoRepository<Sucursal, String> {
    List<Sucursal> findByActivaTrueOrderByNombre();
}
