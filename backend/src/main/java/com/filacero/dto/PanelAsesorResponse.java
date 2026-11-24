package com.filacero.dto;

import java.util.List;

/** Todo lo que ve el asesor en su pantalla de trabajo. */
public record PanelAsesorResponse(String asesorNombre, String sucursalId, String sucursalNombre,
                                  ModuloResponse modulo, TurnoResponse turnoActual,
                                  List<EstadoFilaResponse> filas, long atendidosHoy,
                                  List<ModuloResponse> modulosSucursal, List<TramiteResponse> tramitesSucursal) {
}
