package com.filacero.controller;

import com.filacero.dto.TramiteRequest;
import com.filacero.dto.TramiteResponse;
import com.filacero.service.TramiteServicio;
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
@RequestMapping("/api/tramites")
@Tag(name = "03. Trámites", description = "HU_004")
public class TramiteController {

    private final TramiteServicio servicio;

    public TramiteController(TramiteServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Listar trámites (público)")
    @GetMapping
    public List<TramiteResponse> listar(@RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return servicio.listar(incluirInactivos);
    }

    @Operation(summary = "HU_004 Crear trámite (administrador)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TramiteResponse crear(@Valid @RequestBody TramiteRequest req) {
        return servicio.crear(req);
    }

    @Operation(summary = "HU_004 Actualizar trámite (administrador)")
    @PutMapping("/{id}")
    public TramiteResponse actualizar(@PathVariable String id, @Valid @RequestBody TramiteRequest req) {
        return servicio.actualizar(id, req);
    }

    @Operation(summary = "HU_004 Desactivar trámite (administrador)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable String id) {
        servicio.desactivar(id);
    }
}
