package com.filacero.dto;

import com.filacero.model.Rol;

public record AuthResponse(String token, String id, String nombre, String correo, Rol rol, String perfil) {
}
