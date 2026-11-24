package com.filacero.repository;

import com.filacero.model.Rol;
import com.filacero.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    Optional<Usuario> findByDocumento(String documento);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByDocumento(String documento);
    List<Usuario> findByRol(Rol rol);
}
