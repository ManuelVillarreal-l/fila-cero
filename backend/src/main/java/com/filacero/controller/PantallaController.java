package com.filacero.controller;

import com.filacero.dto.PantallaResponse;
import com.filacero.service.PantallaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pantalla")
@Tag(name = "10. Pantalla de llamado", description = "HU_018 (público)")
public class PantallaController {

    private final PantallaServicio servicio;

    public PantallaController(PantallaServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "HU_018 Últimos turnos llamados y filas de la sucursal")
    @GetMapping("/{sucursalId}")
    public PantallaResponse pantalla(@PathVariable String sucursalId) {
        return servicio.pantalla(sucursalId);
    }
}
