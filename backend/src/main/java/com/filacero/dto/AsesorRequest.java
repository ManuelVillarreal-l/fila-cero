package com.filacero.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Para crear un asesor. Al actualizar, la contraseña puede ir vacía para no cambiarla. */
public record AsesorRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank @Pattern(regexp = "\\d{6,12}", message = "El documento debe tener entre 6 y 12 dígitos") String documento,
        @NotBlank @Email(message = "El correo no es válido") String correo,
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String contrasena,
        @NotBlank(message = "La sucursal es obligatoria") String sucursalId) {
}
