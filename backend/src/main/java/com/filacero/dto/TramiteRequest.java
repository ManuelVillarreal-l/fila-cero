package com.filacero.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TramiteRequest(
        @NotBlank @Pattern(regexp = "[A-Z]", message = "El prefijo debe ser una letra mayúscula") String prefijo,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        String descripcion,
        @Min(value = 1, message = "El tiempo promedio debe ser al menos 1 minuto")
        @Max(value = 120, message = "El tiempo promedio no puede pasar de 120 minutos") int tiempoPromedioMin) {
}
