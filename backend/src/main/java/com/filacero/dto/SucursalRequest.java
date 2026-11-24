package com.filacero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

public record SucursalRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "La dirección es obligatoria") String direccion,
        @NotBlank(message = "La ciudad es obligatoria") String ciudad,
        String telefono,
        @NotNull(message = "La hora de apertura es obligatoria (HH:mm)") LocalTime horaApertura,
        @NotNull(message = "La hora de cierre es obligatoria (HH:mm)") LocalTime horaCierre,
        @NotEmpty(message = "La sucursal debe ofrecer al menos un trámite") List<String> tramitesIds) {
}
