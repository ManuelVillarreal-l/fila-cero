package com.filacero.controller;

import com.filacero.dto.SucursalRequest;
import com.filacero.dto.SucursalResponse;
import com.filacero.service.SucursalServicio;
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
@RequestMapping("/api/sucursales")
@Tag(name = "02. Sucursales", description = "HU_003 y HU_007")
public class SucursalController {

    private final SucursalServicio servicio;

    public SucursalController(SucursalServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "HU_007 Consultar sucursales con el estado de sus filas (público)")
    @GetMapping
    public List<SucursalResponse> listar(@RequestParam(defaultValue = "false") boolean incluirInactivas) {
        return servicio.listar(incluirInactivas);
    }

    @Operation(summary = "HU_007 Detalle de una sucursal: horario, trámites, personas en espera y tiempo estimado")
    @GetMapping("/{id}")
    public SucursalResponse detalle(@PathVariable String id) {
        return servicio.detalle(id);
    }

    @Operation(summary = "HU_003 Crear sucursal (administrador)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SucursalResponse crear(@Valid @RequestBody SucursalRequest req) {
        return servicio.crear(req);
    }

    @Operation(summary = "HU_003 Actualizar sucursal (administrador)")
    @PutMapping("/{id}")
    public SucursalResponse actualizar(@PathVariable String id, @Valid @RequestBody SucursalRequest req) {
        return servicio.actualizar(id, req);
    }

    @Operation(summary = "HU_003 Desactivar sucursal (administrador)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable String id) {
        servicio.desactivar(id);
    }
}
