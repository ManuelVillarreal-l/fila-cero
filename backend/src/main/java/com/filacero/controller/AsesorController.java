package com.filacero.controller;

import com.filacero.dto.AsesorRequest;
import com.filacero.dto.AsesorResponse;
import com.filacero.service.AsesorServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/asesores")
@Tag(name = "05. Asesores", description = "HU_006 (administrador)")
public class AsesorController {

    private final AsesorServicio servicio;

    public AsesorController(AsesorServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "HU_006 Listar asesores, opcionalmente de una sucursal")
    @GetMapping
    public List<AsesorResponse> listar(@RequestParam(required = false) String sucursalId) {
        return servicio.listar(sucursalId);
    }

    @Operation(summary = "HU_006 Crear asesor")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsesorResponse crear(@Valid @RequestBody AsesorRequest req) {
        return servicio.crear(req);
    }

    @Operation(summary = "HU_006 Actualizar asesor (contraseña opcional)")
    @PutMapping("/{id}")
    public AsesorResponse actualizar(@PathVariable String id, @Valid @RequestBody AsesorRequest req) {
        return servicio.actualizar(id, req);
    }

    @Operation(summary = "HU_006 Desactivar asesor")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable String id) {
        servicio.desactivar(id);
    }
}
