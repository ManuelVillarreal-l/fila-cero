package com.filacero.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tablero de indicadores de una sucursal (HU_020). GRASP Creador: el reporte agrega los turnos
 * y crea sus propias LineaIndicador por trámite, por asesor y por hora.
 */
public class ReporteIndicadores {

    private final String sucursalId;
    private final String sucursalNombre;
    private final String desde;
    private final String hasta;
    private final LineaIndicador resumen;
    private final double porcentajeAtendidos;
    private final List<LineaIndicador> porTramite;
    private final List<LineaIndicador> porAsesor;
    private final List<LineaIndicador> porHora;
    private final long enEsperaAhora;

    public ReporteIndicadores(String sucursalId, String sucursalNombre, String desde, String hasta,
                              List<Turno> turnos, LocalDateTime ahora) {
        this.sucursalId = sucursalId;
        this.sucursalNombre = sucursalNombre;
        this.desde = desde;
        this.hasta = hasta;
        this.resumen = crearLinea("Total", turnos, ahora);
        this.porcentajeAtendidos = turnos.isEmpty() ? 0
                : redondear(100.0 * resumen.atendidos() / resumen.total());
        this.porTramite = agrupar(turnos, Turno::getTramiteNombre, ahora);
        this.porAsesor = agrupar(turnos.stream().filter(t -> t.getAsesorNombre() != null).toList(),
                Turno::getAsesorNombre, ahora);
        this.porHora = agruparPorHora(turnos, ahora);
        this.enEsperaAhora = turnos.stream().filter(t -> t.getEstado() == EstadoTurno.EN_ESPERA).count();
    }

    private List<LineaIndicador> agrupar(List<Turno> turnos, Function<Turno, String> clave, LocalDateTime ahora) {
        Map<String, List<Turno>> grupos = turnos.stream()
                .collect(Collectors.groupingBy(clave, TreeMap::new, Collectors.toList()));
        List<LineaIndicador> lineas = new ArrayList<>();
        grupos.forEach((nombre, lista) -> lineas.add(crearLinea(nombre, lista, ahora)));
        return lineas;
    }

    private List<LineaIndicador> agruparPorHora(List<Turno> turnos, LocalDateTime ahora) {
        Map<Integer, List<Turno>> grupos = turnos.stream()
                .collect(Collectors.groupingBy(t -> t.getHoraCreacion().getHour(), TreeMap::new, Collectors.toList()));
        List<LineaIndicador> lineas = new ArrayList<>();
        grupos.forEach((hora, lista) -> lineas.add(crearLinea(String.format("%02d:00", hora), lista, ahora)));
        return lineas;
    }

    /** Creador: arma cada línea con sus promedios. */
    private LineaIndicador crearLinea(String nombre, Collection<Turno> turnos, LocalDateTime ahora) {
        long atendidos = contar(turnos, EstadoTurno.ATENDIDO);
        long ausentes = contar(turnos, EstadoTurno.AUSENTE);
        long cancelados = contar(turnos, EstadoTurno.CANCELADO);
        double espera = turnos.stream()
                .filter(t -> t.getHoraLlamado() != null)
                .mapToLong(t -> t.minutosEspera(ahora))
                .average().orElse(0);
        double atencion = turnos.stream()
                .map(Turno::minutosAtencion)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .average().orElse(0);
        Double calificacion = turnos.stream()
                .map(Turno::getCalificacion)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average().stream().map(ReporteIndicadores::redondear).boxed().findFirst().orElse(null);
        return new LineaIndicador(nombre, turnos.size(), atendidos, ausentes, cancelados,
                redondear(espera), redondear(atencion), calificacion);
    }

    private static long contar(Collection<Turno> turnos, EstadoTurno estado) {
        return turnos.stream().filter(t -> t.getEstado() == estado).count();
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }

    public String getSucursalId() { return sucursalId; }
    public String getSucursalNombre() { return sucursalNombre; }
    public String getDesde() { return desde; }
    public String getHasta() { return hasta; }
    public LineaIndicador getResumen() { return resumen; }
    public double getPorcentajeAtendidos() { return porcentajeAtendidos; }
    public List<LineaIndicador> getPorTramite() { return porTramite; }
    public List<LineaIndicador> getPorAsesor() { return porAsesor; }
    public List<LineaIndicador> getPorHora() { return porHora; }
    public long getEnEsperaAhora() { return enEsperaAhora; }
}
