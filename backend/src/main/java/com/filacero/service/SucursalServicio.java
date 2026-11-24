package com.filacero.service;

import com.filacero.dto.SucursalRequest;
import com.filacero.dto.SucursalResponse;
import com.filacero.exception.RecursoNoEncontradoException;
import com.filacero.exception.ReglaNegocioException;
import com.filacero.model.Sucursal;
import com.filacero.repository.SucursalRepository;
import com.filacero.repository.TramiteRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalTime;
import java.util.List;

/** Sucursales y estado de sus filas (HU_003 y HU_007). */
@Service
public class SucursalServicio {

    private final SucursalRepository sucursales;
    private final TramiteRepository tramites;
    private final ColaServicio colas;
    private final Clock reloj;

    public SucursalServicio(SucursalRepository sucursales, TramiteRepository tramites, ColaServicio colas, Clock reloj) {
        this.sucursales = sucursales;
        this.tramites = tramites;
        this.colas = colas;
        this.reloj = reloj;
    }

    public List<SucursalResponse> listar(boolean incluirInactivas) {
        List<Sucursal> lista = incluirInactivas ? sucursales.findAll() : sucursales.findByActivaTrueOrderByNombre();
        return lista.stream().map(this::respuesta).toList();
    }

    public SucursalResponse detalle(String id) {
        return respuesta(obtener(id));
    }

    public Sucursal obtener(String id) {
        return sucursales.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", id));
    }

    public SucursalResponse crear(SucursalRequest req) {
        Sucursal s = new Sucursal(req.nombre(), req.direccion(), req.ciudad(), req.telefono(),
                req.horaApertura(), req.horaCierre(), req.tramitesIds());
        validar(s);
        return respuesta(sucursales.save(s));
    }

    public SucursalResponse actualizar(String id, SucursalRequest req) {
        Sucursal s = obtener(id);
        s.setNombre(req.nombre());
        s.setDireccion(req.direccion());
        s.setCiudad(req.ciudad());
        s.setTelefono(req.telefono());
        s.setHoraApertura(req.horaApertura());
        s.setHoraCierre(req.horaCierre());
        s.setTramitesIds(req.tramitesIds());
        s.setActiva(true);
        validar(s);
        return respuesta(sucursales.save(s));
    }

    public void desactivar(String id) {
        Sucursal s = obtener(id);
        s.setActiva(false);
        sucursales.save(s);
    }

    private void validar(Sucursal s) {
        if (!s.getHoraApertura().isBefore(s.getHoraCierre())) {
            throw new ReglaNegocioException("La hora de apertura debe ser anterior a la de cierre");
        }
        long existentes = tramites.findAllById(s.getTramitesIds()).size();
        if (existentes != s.getTramitesIds().size()) {
            throw new ReglaNegocioException("Uno o más trámites de la sucursal no existen");
        }
    }

    private SucursalResponse respuesta(Sucursal s) {
        return SucursalResponse.desde(s, s.estaAbierta(LocalTime.now(reloj)), colas.estadoFilas(s));
    }
}
