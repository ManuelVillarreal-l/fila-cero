package com.filacero.service;

import com.filacero.exception.ReglaNegocioException;
import com.filacero.model.ReporteIndicadores;
import com.filacero.model.Sucursal;
import com.filacero.repository.TurnoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Tablero de indicadores de atención para el administrador (HU_020). */
@Service
public class TableroServicio {

    public static final int DIAS_MAX_RANGO = 92;

    private final TurnoRepository turnos;
    private final SucursalServicio sucursales;
    private final ColaServicio colas;

    public TableroServicio(TurnoRepository turnos, SucursalServicio sucursales, ColaServicio colas) {
        this.turnos = turnos;
        this.sucursales = sucursales;
        this.colas = colas;
    }

    public ReporteIndicadores indicadores(String sucursalId, LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha final debe ser posterior a la inicial");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > DIAS_MAX_RANGO) {
            throw new ReglaNegocioException("El rango máximo es de " + DIAS_MAX_RANGO + " días");
        }
        Sucursal s = sucursales.obtener(sucursalId);
        // Las fechas yyyy-MM-dd se comparan bien como texto; Between es exclusivo, por eso se amplía un día.
        var lista = turnos.findBySucursalIdAndFechaBetween(s.getId(), desde.minusDays(1).toString(),
                hasta.plusDays(1).toString());
        return new ReporteIndicadores(s.getId(), s.getNombre(), desde.toString(), hasta.toString(), lista, colas.ahora());
    }
}
