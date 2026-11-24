package com.filacero.dto;

import jakarta.validation.constraints.Size;

public record FinalizarRequest(@Size(max = 300) String observaciones) {
}
