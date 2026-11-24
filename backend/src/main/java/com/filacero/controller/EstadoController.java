package com.filacero.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Tag(name = "00. Estado")
public class EstadoController {

    private final Clock reloj;

    public EstadoController(Clock reloj) {
        this.reloj = reloj;
    }

    @Operation(summary = "Comprobar que la API está viva")
    @GetMapping("/api/estado")
    public Map<String, Object> estado() {
        return Map.of("servicio", "Fila Cero API", "estado", "OK", "hora", LocalDateTime.now(reloj).toString());
    }
}
