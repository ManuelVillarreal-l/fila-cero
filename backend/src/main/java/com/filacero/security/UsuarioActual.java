package com.filacero.security;

import com.filacero.model.Rol;

/** Datos del usuario autenticado que viajan en el token JWT. */
public record UsuarioActual(String id, String nombre, Rol rol) {
}
