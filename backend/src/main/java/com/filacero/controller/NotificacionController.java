package com.filacero.controller;

import com.filacero.dto.NotificacionResponse;
import com.filacero.security.UsuarioActual;
import com.filacero.service.NotificacionServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "12. Notificaciones", description = "RF_021 (cliente)")
public class NotificacionController {

    private final NotificacionServicio servicio;

    public NotificacionController(NotificacionServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Mis notificaciones (las 30 más recientes)")
    @GetMapping
    public List<NotificacionResponse> mias(@AuthenticationPrincipal UsuarioActual u) {
        return servicio.mias(u);
    }

    @Operation(summary = "Marcar todas como leídas")
    @PostMapping("/leidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leidas(@AuthenticationPrincipal UsuarioActual u) {
        servicio.marcarLeidas(u);
    }
}
