package com.filacero.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CalificacionRequest(
        @Min(value = 1, message = "La calificación mínima es 1") @Max(value = 5, message = "La calificación máxima es 5")
        int estrellas,
        @Size(max = 300, message = "El comentario admite máximo 300 caracteres") String comentario) {
}
