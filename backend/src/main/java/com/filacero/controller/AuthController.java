package com.filacero.controller;

import com.filacero.dto.AuthResponse;
import com.filacero.dto.LoginRequest;
import com.filacero.dto.RegistroRequest;
import com.filacero.service.AuthServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** GRASP Controlador: recibe la petición y delega en AuthServicio. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "01. Cuenta", description = "HU_001 y HU_002")
public class AuthController {

    private final AuthServicio servicio;

    public AuthController(AuthServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "HU_001 Registrarse como cliente")
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return servicio.registrar(req);
    }

    @Operation(summary = "HU_002 Iniciar sesión (cliente, asesor o administrador)")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return servicio.iniciarSesion(req);
    }
}
