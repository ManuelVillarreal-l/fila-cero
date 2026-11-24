package com.filacero.dto;

import jakarta.validation.constraints.NotBlank;

public record AbrirModuloRequest(@NotBlank(message = "El módulo es obligatorio") String moduloId) {
}
