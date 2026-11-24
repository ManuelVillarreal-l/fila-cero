package com.filacero.controller;

import com.filacero.dto.CalificacionRequest;
import com.filacero.dto.TomarTurnoRequest;
import com.filacero.dto.TurnoResponse;
import com.filacero.security.UsuarioActual;
import com.filacero.service.TurnoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@Tag(name = "06. Turnos del cliente", description = "HU_008, HU_010, HU_011 y HU_019 (cliente)")
public class TurnoController {

    private final TurnoServicio servicio;

    public TurnoController(TurnoServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "HU_008 Tomar turno virtual")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoResponse tomar(@AuthenticationPrincipal UsuarioActual usuario, @Valid @RequestBody TomarTurnoRequest req) {
        return servicio.tomarTurno(usuario, req);
    }

    @Operation(summary = "HU_010 Mi turno activo con posición y tiempo estimado (204 si no tengo)")
    @GetMapping("/actual")
    public ResponseEntity<TurnoResponse> actual(@AuthenticationPrincipal UsuarioActual usuario) {
        return servicio.miTurnoActual(usuario).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "HU_010 Historial de mis turnos")
    @GetMapping("/historial")
    public List<TurnoResponse> historial(@AuthenticationPrincipal UsuarioActual usuario) {
        return servicio.historial(usuario);
    }

    @Operation(summary = "HU_011 Cancelar mi turno (solo en espera)")
    @PatchMapping("/{id}/cancelar")
    public TurnoResponse cancelar(@AuthenticationPrincipal UsuarioActual usuario, @PathVariable String id) {
        return servicio.cancelar(usuario, id);
    }

    @Operation(summary = "HU_019 Calificar la atención de un turno atendido")
    @PostMapping("/{id}/calificacion")
    public TurnoResponse calificar(@AuthenticationPrincipal UsuarioActual usuario, @PathVariable String id,
                                   @Valid @RequestBody CalificacionRequest req) {
        return servicio.calificar(usuario, id, req);
    }
}
