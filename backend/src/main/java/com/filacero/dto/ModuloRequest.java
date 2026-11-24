package com.filacero.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ModuloRequest(
        @NotBlank(message = "La sucursal es obligatoria") String sucursalId,
        @Min(value = 1, message = "El número debe ser mayor que cero") int numero,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotEmpty(message = "El módulo debe atender al menos un trámite") List<String> tramitesIds) {
}
