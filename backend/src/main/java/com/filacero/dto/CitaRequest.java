package com.filacero.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CitaRequest(
        @NotBlank(message = "La sucursal es obligatoria") String sucursalId,
        @NotBlank(message = "El trámite es obligatorio") String tramiteId,
        @NotNull(message = "La fecha y hora son obligatorias") @Future(message = "La cita debe ser en el futuro")
        LocalDateTime fechaHora,
        @Size(max = 200) String motivo) {
}
