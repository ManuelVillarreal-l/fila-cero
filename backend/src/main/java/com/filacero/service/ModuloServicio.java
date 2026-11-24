package com.filacero.service;

import com.filacero.dto.ModuloRequest;
import com.filacero.dto.ModuloResponse;
import com.filacero.exception.RecursoNoEncontradoException;
import com.filacero.exception.ReglaNegocioException;
import com.filacero.model.EstadoModulo;
import com.filacero.model.Modulo;
import com.filacero.model.Sucursal;
import com.filacero.repository.ModuloRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Módulos o ventanillas de cada sucursal (HU_005). */
@Service
public class ModuloServicio {

    private final ModuloRepository modulos;
    private final SucursalServicio sucursales;

    public ModuloServicio(ModuloRepository modulos, SucursalServicio sucursales) {
        this.modulos = modulos;
        this.sucursales = sucursales;
    }

    public List<ModuloResponse> listar(String sucursalId) {
        return modulos.findBySucursalIdOrderByNumero(sucursalId).stream().map(ModuloResponse::desde).toList();
    }

    public Modulo obtener(String id) {
        return modulos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Módulo", id));
    }

    public ModuloResponse crear(ModuloRequest req) {
        Sucursal sucursal = sucursales.obtener(req.sucursalId());
        validarTramites(sucursal, req.tramitesIds());
        if (modulos.existsBySucursalIdAndNumero(req.sucursalId(), req.numero())) {
            throw new ReglaNegocioException("Ya existe el módulo número " + req.numero() + " en esta sucursal");
        }
        return ModuloResponse.desde(modulos.save(new Modulo(req.sucursalId(), req.numero(), req.nombre(), req.tramitesIds())));
    }

    public ModuloResponse actualizar(String id, ModuloRequest req) {
        Modulo m = obtener(id);
        if (!m.getSucursalId().equals(req.sucursalId())) {
            throw new ReglaNegocioException("Un módulo no se puede mover a otra sucursal");
        }
        validarTramites(sucursales.obtener(req.sucursalId()), req.tramitesIds());
        if (m.getNumero() != req.numero() && modulos.existsBySucursalIdAndNumero(req.sucursalId(), req.numero())) {
            throw new ReglaNegocioException("Ya existe el módulo número " + req.numero() + " en esta sucursal");
        }
        m.setNumero(req.numero());
        m.setNombre(req.nombre());
        m.setTramitesIds(req.tramitesIds());
        return ModuloResponse.desde(modulos.save(m));
    }

    public void eliminar(String id) {
        Modulo m = obtener(id);
        if (m.getEstado() != EstadoModulo.CERRADO) {
            throw new ReglaNegocioException("Solo se puede eliminar un módulo cerrado");
        }
        modulos.delete(m);
    }

    private void validarTramites(Sucursal sucursal, List<String> tramitesIds) {
        for (String id : tramitesIds) {
            if (!sucursal.ofrece(id)) {
                throw new ReglaNegocioException("La sucursal " + sucursal.getNombre() + " no ofrece el trámite " + id);
            }
        }
    }
}
