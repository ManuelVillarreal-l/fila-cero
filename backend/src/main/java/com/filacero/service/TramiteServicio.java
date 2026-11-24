package com.filacero.service;

import com.filacero.dto.TramiteRequest;
import com.filacero.dto.TramiteResponse;
import com.filacero.exception.RecursoNoEncontradoException;
import com.filacero.exception.ReglaNegocioException;
import com.filacero.model.Tramite;
import com.filacero.repository.TramiteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Catálogo de trámites del banco (HU_004). */
@Service
public class TramiteServicio {

    private final TramiteRepository tramites;

    public TramiteServicio(TramiteRepository tramites) {
        this.tramites = tramites;
    }

    public List<TramiteResponse> listar(boolean incluirInactivos) {
        List<Tramite> lista = incluirInactivos ? tramites.findAll() : tramites.findByActivoTrueOrderByNombre();
        return lista.stream().map(TramiteResponse::desde).toList();
    }

    public Tramite obtener(String id) {
        return tramites.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Trámite", id));
    }

    public TramiteResponse crear(TramiteRequest req) {
        if (tramites.existsByPrefijoIgnoreCase(req.prefijo())) {
            throw new ReglaNegocioException("Ya hay un trámite con el prefijo " + req.prefijo());
        }
        return TramiteResponse.desde(tramites.save(
                new Tramite(req.prefijo(), req.nombre(), req.descripcion(), req.tiempoPromedioMin())));
    }

    public TramiteResponse actualizar(String id, TramiteRequest req) {
        Tramite t = obtener(id);
        if (!t.getPrefijo().equalsIgnoreCase(req.prefijo()) && tramites.existsByPrefijoIgnoreCase(req.prefijo())) {
            throw new ReglaNegocioException("Ya hay un trámite con el prefijo " + req.prefijo());
        }
        t.setPrefijo(req.prefijo());
        t.setNombre(req.nombre());
        t.setDescripcion(req.descripcion());
        t.setTiempoPromedioMin(req.tiempoPromedioMin());
        t.setActivo(true);
        return TramiteResponse.desde(tramites.save(t));
    }

    /** Se desactiva en vez de borrar para no dañar el historial de turnos. */
    public void desactivar(String id) {
        Tramite t = obtener(id);
        t.setActivo(false);
        tramites.save(t);
    }
}
