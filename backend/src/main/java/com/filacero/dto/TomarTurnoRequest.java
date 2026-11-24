package com.filacero.dto;

import jakarta.validation.constraints.NotBlank;

public record TomarTurnoRequest(
        @NotBlank(message = "La sucursal es obligatoria") String sucursalId,
        @NotBlank(message = "El trámite es obligatorio") String tramiteId) {
}
