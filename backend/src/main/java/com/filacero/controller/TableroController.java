package com.filacero.controller;

import com.filacero.model.ReporteIndicadores;
import com.filacero.service.TableroServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tablero")
@Tag(name = "11. Tablero de indicadores", description = "HU_020 (administrador)")
public class TableroController {

    private final TableroServicio servicio;

    public TableroController(TableroServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "HU_020 Indicadores de una sucursal en un rango de fechas")
    @GetMapping
    public ReporteIndicadores indicadores(@RequestParam String sucursalId,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.indicadores(sucursalId, desde, hasta);
    }
}
