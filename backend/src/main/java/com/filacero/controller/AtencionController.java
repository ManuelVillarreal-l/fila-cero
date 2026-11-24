package com.filacero.controller;

import com.filacero.dto.AbrirModuloRequest;
import com.filacero.dto.FinalizarRequest;
import com.filacero.dto.PanelAsesorResponse;
import com.filacero.dto.TransferirRequest;
import com.filacero.dto.TurnoResponse;
import com.filacero.security.UsuarioActual;
import com.filacero.service.AtencionServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/atencion")
@Tag(name = "08. Atención en ventanilla", description = "HU_012 a HU_015 (asesor)")
public class AtencionController {

    private final AtencionServicio servicio;

    public AtencionController(AtencionServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Panel del asesor: módulo, turno actual y filas")
    @GetMapping("/panel")
    public PanelAsesorResponse panel(@AuthenticationPrincipal UsuarioActual u) {
        return servicio.panel(u);
    }

    @Operation(summary = "HU_012 Abrir mi módulo")
    @PostMapping("/modulo/abrir")
    public PanelAsesorResponse abrir(@AuthenticationPrincipal UsuarioActual u, @Valid @RequestBody AbrirModuloRequest req) {
        return servicio.abrirModulo(u, req.moduloId());
    }

    @Operation(summary = "HU_012 Cerrar mi módulo")
    @PostMapping("/modulo/cerrar")
    public PanelAsesorResponse cerrar(@AuthenticationPrincipal UsuarioActual u) {
        return servicio.cerrarModulo(u);
    }

    @Operation(summary = "HU_012 Pausar mi módulo")
    @PostMapping("/modulo/pausar")
    public PanelAsesorResponse pausar(@AuthenticationPrincipal UsuarioActual u) {
        return servicio.pausar(u, true);
    }

    @Operation(summary = "HU_012 Reanudar mi módulo")
    @PostMapping("/modulo/reanudar")
    public PanelAsesorResponse reanudar(@AuthenticationPrincipal UsuarioActual u) {
        return servicio.pausar(u, false);
    }

    @Operation(summary = "HU_013 Llamar al siguiente turno según la prioridad")
    @PostMapping("/llamar-siguiente")
    public TurnoResponse llamarSiguiente(@AuthenticationPrincipal UsuarioActual u) {
        return servicio.llamarSiguiente(u);
    }

    @Operation(summary = "HU_014 Volver a llamar (máximo 3 veces)")
    @PostMapping("/turnos/{id}/rellamar")
    public TurnoResponse rellamar(@AuthenticationPrincipal UsuarioActual u, @PathVariable String id) {
        return servicio.volverALlamar(u, id);
    }

    @Operation(summary = "HU_014 Iniciar la atención (el cliente llegó a la ventanilla)")
    @PostMapping("/turnos/{id}/iniciar")
    public TurnoResponse iniciar(@AuthenticationPrincipal UsuarioActual u, @PathVariable String id) {
        return servicio.iniciar(u, id);
    }

    @Operation(summary = "HU_014 Finalizar la atención")
    @PostMapping("/turnos/{id}/finalizar")
    public TurnoResponse finalizar(@AuthenticationPrincipal UsuarioActual u, @PathVariable String id,
                                   @Valid @RequestBody(required = false) FinalizarRequest req) {
        return servicio.finalizar(u, id, req);
    }

    @Operation(summary = "HU_014 Marcar ausente (no se presentó)")
    @PostMapping("/turnos/{id}/ausente")
    public TurnoResponse ausente(@AuthenticationPrincipal UsuarioActual u, @PathVariable String id) {
        return servicio.marcarAusente(u, id);
    }

    @Operation(summary = "HU_015 Transferir el turno a otro trámite")
    @PostMapping("/turnos/{id}/transferir")
    public TurnoResponse transferir(@AuthenticationPrincipal UsuarioActual u, @PathVariable String id,
                                    @Valid @RequestBody TransferirRequest req) {
        return servicio.transferir(u, id, req);
    }
}
