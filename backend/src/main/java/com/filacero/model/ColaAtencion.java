package com.filacero.model;

import com.filacero.model.prioridad.FabricaPrioridades;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Cola de turnos en espera de un trámite en una sucursal. No se guarda en MongoDB:
 * se arma con los turnos del día y es la Experta en el orden de atención (RN3) y en el
 * tiempo estimado de espera (RN4). Usa las estrategias de PoliticaPrioridad (polimorfismo).
 */
public class ColaAtencion {

    private final List<Turno> turnos;
    private final LocalDateTime ahora;

    public ColaAtencion(List<Turno> turnosEnEspera, LocalDateTime ahora) {
        this.ahora = ahora;
        this.turnos = turnosEnEspera.stream()
                .filter(t -> t.getEstado() == EstadoTurno.EN_ESPERA)
                .sorted(ordenDeAtencion(ahora))
                .toList();
    }

    /** RN3: mayor puntaje primero; si empatan, el que llegó antes. */
    public static Comparator<Turno> ordenDeAtencion(LocalDateTime ahora) {
        Comparator<Turno> porPuntaje = Comparator.comparingDouble(
                t -> FabricaPrioridades.para(t.getPrioridad()).puntaje(t, ahora));
        return porPuntaje.reversed().thenComparing(Turno::getHoraCreacion);
    }

    public List<Turno> enOrden() {
        return turnos;
    }

    public Optional<Turno> siguiente() {
        return turnos.stream().findFirst();
    }

    public int tamano() {
        return turnos.size();
    }

    /** Posición 1 = es el próximo en ser llamado. Devuelve 0 si el turno no está en la cola. */
    public int posicionDe(String turnoId) {
        for (int i = 0; i < turnos.size(); i++) {
            if (turnos.get(i).getId().equals(turnoId)) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * RN4: tiempo estimado = ceil(personas antes x minutos promedio del trámite / módulos abiertos).
     * Si no hay módulos abiertos se calcula como si hubiera uno.
     */
    public static int tiempoEstimado(int personasAntes, int minutosPromedio, int modulosAbiertos) {
        int modulos = Math.max(1, modulosAbiertos);
        return (int) Math.ceil((double) personasAntes * minutosPromedio / modulos);
    }

    public int tiempoEstimadoPara(String turnoId, int minutosPromedio, int modulosAbiertos) {
        int posicion = posicionDe(turnoId);
        return posicion == 0 ? 0 : tiempoEstimado(posicion - 1, minutosPromedio, modulosAbiertos);
    }

    public int tiempoEstimadoNuevoTurno(int minutosPromedio, int modulosAbiertos) {
        return tiempoEstimado(turnos.size(), minutosPromedio, modulosAbiertos);
    }

    public LocalDateTime getAhora() {
        return ahora;
    }
}
