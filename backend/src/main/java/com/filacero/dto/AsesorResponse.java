package com.filacero.dto;

import com.filacero.model.Asesor;

public record AsesorResponse(String id, String nombre, String documento, String correo, String sucursalId,
                             String sucursalNombre, String moduloId, boolean activo) {
    public static AsesorResponse desde(Asesor a, String sucursalNombre) {
        return new AsesorResponse(a.getId(), a.getNombre(), a.getDocumento(), a.getCorreo(), a.getSucursalId(),
                sucursalNombre, a.getModuloId(), a.isActivo());
    }
}
