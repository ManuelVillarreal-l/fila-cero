package com.filacero.dto;

import com.filacero.model.EstadoModulo;
import com.filacero.model.Modulo;

import java.util.List;

public record ModuloResponse(String id, String sucursalId, int numero, String nombre, List<String> tramitesIds,
                             EstadoModulo estado, String asesorId, String asesorNombre) {
    public static ModuloResponse desde(Modulo m) {
        return new ModuloResponse(m.getId(), m.getSucursalId(), m.getNumero(), m.getNombre(), m.getTramitesIds(),
                m.getEstado(), m.getAsesorId(), m.getAsesorNombre());
    }
}
