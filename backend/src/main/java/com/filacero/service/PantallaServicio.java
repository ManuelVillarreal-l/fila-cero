package com.filacero.service;

import com.filacero.dto.PantallaResponse;
import com.filacero.dto.TurnoResponse;
import com.filacero.model.Sucursal;
import com.filacero.repository.TurnoRepository;
import org.springframework.stereotype.Service;

/** Pantalla de llamado de la sucursal (HU_018). */
@Service
public class PantallaServicio {

    private final TurnoRepository turnos;
    private final SucursalServicio sucursales;
    private final ColaServicio colas;

    public PantallaServicio(TurnoRepository turnos, SucursalServicio sucursales, ColaServicio colas) {
        this.turnos = turnos;
        this.sucursales = sucursales;
        this.colas = colas;
    }

    public PantallaResponse pantalla(String sucursalId) {
        Sucursal s = sucursales.obtener(sucursalId);
        var llamados = turnos.findTop8BySucursalIdAndFechaAndHoraLlamadoNotNullOrderByHoraLlamadoDesc(s.getId(), colas.hoy())
                .stream().map(TurnoResponse::desde).toList();
        return new PantallaResponse(s.getId(), s.getNombre(), colas.ahora(), llamados, colas.estadoFilas(s));
    }
}
