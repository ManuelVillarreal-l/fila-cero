package com.filacero.dto;

import com.filacero.model.Tramite;

public record TramiteResponse(String id, String prefijo, String nombre, String descripcion, int tiempoPromedioMin,
                              boolean activo) {
    public static TramiteResponse desde(Tramite t) {
        return new TramiteResponse(t.getId(), t.getPrefijo(), t.getNombre(), t.getDescripcion(),
                t.getTiempoPromedioMin(), t.isActivo());
    }
}
