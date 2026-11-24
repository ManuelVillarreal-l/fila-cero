package com.filacero.dto;

import com.filacero.model.Sucursal;

import java.time.LocalTime;
import java.util.List;

public record SucursalResponse(String id, String nombre, String direccion, String ciudad, String telefono,
                               LocalTime horaApertura, LocalTime horaCierre, boolean activa, boolean abiertaAhora,
                               int totalEnEspera, List<EstadoFilaResponse> filas) {
    public static SucursalResponse desde(Sucursal s, boolean abierta, List<EstadoFilaResponse> filas) {
        int total = filas.stream().mapToInt(EstadoFilaResponse::enEspera).sum();
        return new SucursalResponse(s.getId(), s.getNombre(), s.getDireccion(), s.getCiudad(), s.getTelefono(),
                s.getHoraApertura(), s.getHoraCierre(), s.isActiva(), abierta, total, filas);
    }
}
