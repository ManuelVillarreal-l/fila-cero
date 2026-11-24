package com.filacero.controller;

import com.filacero.dto.CitaRequest;
import com.filacero.dto.CitaResponse;
import com.filacero.dto.FranjaResponse;
import com.filacero.dto.TurnoResponse;
import com.filacero.security.UsuarioActual;
import com.filacero.service.CitaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@Tag(name = "09. Citas", description = "HU_016 y HU_017 (cliente)")
public class CitaController {

    private final CitaServicio servicio;

    public CitaController(CitaServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "HU_016 Franjas de 30 minutos disponibles para un día")
    @GetMapping("/franjas")
    public List<FranjaResponse> franjas(@RequestParam String sucursalId, @RequestParam String tramiteId,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return servicio.franjas(sucursalId, tramiteId, fecha);
    }

    @Operation(summary = "HU_016 Agendar una cita")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse agendar(@AuthenticationPrincipal UsuarioActual u, @Valid @RequestBody CitaRequest req) {
        return servicio.agendar(u, req);
    }

    @Operation(summary = "HU_016 Mis citas")
    @GetMapping
    public List<CitaResponse> mias(@AuthenticationPrincipal UsuarioActual u) {
        return servicio.misCitas(u);
    }

    @Operation(summary = "HU_017 Cancelar una cita (hasta 1 hora antes)")
    @PatchMapping("/{id}/cancelar")
    public CitaResponse cancelar(@AuthenticationPrincipal UsuarioActual u, @PathVariable String id) {
        return servicio.cancelar(u, id);
    }

    @Operation(summary = "HU_017 Check-in: llegué a la sucursal (de 15 min antes a 10 min después)")
    @PostMapping("/{id}/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoResponse checkIn(@AuthenticationPrincipal UsuarioActual u, @PathVariable String id) {
        return servicio.checkIn(u, id);
    }
}
