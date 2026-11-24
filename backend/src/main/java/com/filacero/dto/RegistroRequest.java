package com.filacero.dto;

import com.filacero.model.CondicionEspecial;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
        @NotBlank(message = "El documento es obligatorio")
        @Pattern(regexp = "\\d{6,12}", message = "El documento debe tener entre 6 y 12 dígitos") String documento,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") String correo,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String contrasena,
        @Pattern(regexp = "3\\d{9}", message = "El celular debe tener 10 dígitos y empezar por 3") String telefono,
        @Past(message = "La fecha de nacimiento debe estar en el pasado") LocalDate fechaNacimiento,
        CondicionEspecial condicion) {
}
