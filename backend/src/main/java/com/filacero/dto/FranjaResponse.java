package com.filacero.dto;

import java.time.LocalDateTime;

public record FranjaResponse(LocalDateTime fechaHora, int cupos, int ocupados, boolean disponible) {
}
