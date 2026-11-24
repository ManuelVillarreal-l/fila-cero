package com.filacero.controller;

import com.filacero.dto.ModuloRequest;
import com.filacero.dto.ModuloResponse;
import com.filacero.service.ModuloServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/modulos")
@Tag(name = "04. Módulos", description = "HU_005")
public class ModuloController {

    private final ModuloServicio servicio;

    public ModuloController(ModuloServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Listar módulos de una sucursal (administrador o asesor)")
    @GetMapping
    public List<ModuloResponse> listar(@RequestParam String sucursalId) {
        return servicio.listar(sucursalId);
    }

    @Operation(summary = "HU_005 Crear módulo (administrador)")
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuloResponse crear(@Valid @RequestBody ModuloRequest req) {
        return servicio.crear(req);
    }

    @Operation(summary = "HU_005 Actualizar módulo (administrador)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ModuloResponse actualizar(@PathVariable String id, @Valid @RequestBody ModuloRequest req) {
        return servicio.actualizar(id, req);
    }

    @Operation(summary = "HU_005 Eliminar módulo cerrado (administrador)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        servicio.eliminar(id);
    }
}
