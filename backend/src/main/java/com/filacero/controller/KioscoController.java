package com.filacero.controller;

import com.filacero.dto.KioscoTurnoRequest;
import com.filacero.dto.TurnoResponse;
import com.filacero.service.TurnoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kiosco")
@Tag(name = "07. Kiosco", description = "HU_009 (visitante sin cuenta)")
public class KioscoController {

    private final TurnoServicio servicio;

    public KioscoController(TurnoServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "HU_009 Tomar turno presencial con el documento")
    @PostMapping("/turnos")
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoResponse tomar(@Valid @RequestBody KioscoTurnoRequest req) {
        return servicio.tomarTurnoKiosco(req);
    }

    @Operation(summary = "HU_009 Consultar el tiquete del kiosco (posición y tiempo estimado)")
    @GetMapping("/turnos/{id}")
    public TurnoResponse consultar(@PathVariable String id) {
        return servicio.consultar(id);
    }
}
