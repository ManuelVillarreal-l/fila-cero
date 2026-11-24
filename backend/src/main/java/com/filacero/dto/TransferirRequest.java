package com.filacero.dto;

import jakarta.validation.constraints.NotBlank;

public record TransferirRequest(@NotBlank(message = "El trámite destino es obligatorio") String tramiteId) {
}
