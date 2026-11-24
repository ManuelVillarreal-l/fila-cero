package com.filacero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Turno presencial tomado en el kiosco de la sucursal, sin cuenta (HU_009). */
public record KioscoTurnoRequest(
        @NotBlank(message = "La sucursal es obligatoria") String sucursalId,
        @NotBlank(message = "El trámite es obligatorio") String tramiteId,
        @NotBlank @Pattern(regexp = "\\d{6,12}", message = "El documento debe tener entre 6 y 12 dígitos") String documento,
        String nombre,
        boolean preferencial) {
}
