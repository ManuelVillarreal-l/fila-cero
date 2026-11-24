package com.filacero.service;

import com.filacero.dto.CitaRequest;
import com.filacero.dto.CitaResponse;
import com.filacero.dto.FranjaResponse;
import com.filacero.dto.TurnoResponse;
import com.filacero.exception.AccesoDenegadoException;
import com.filacero.exception.RecursoNoEncontradoException;
import com.filacero.exception.ReglaNegocioException;
import com.filacero.model.CanalTurno;
import com.filacero.model.Cita;
import com.filacero.model.Cliente;
import com.filacero.model.EstadoCita;
import com.filacero.model.EstadoTurno;
import com.filacero.model.Sucursal;
import com.filacero.model.TipoPrioridad;
import com.filacero.model.Tramite;
import com.filacero.model.Turno;
import com.filacero.repository.CitaRepository;
import com.filacero.repository.ModuloRepository;
import com.filacero.repository.TurnoRepository;
import com.filacero.repository.UsuarioRepository;
import com.filacero.security.UsuarioActual;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Citas agendadas y su check-in en la sucursal (HU_016 y HU_017). */
@Service
public class CitaServicio {

    /** RN9: se puede agendar con hasta 30 días de anticipación. */
    public static final int DIAS_MAX_ANTICIPACION = 30;
    private static final Set<EstadoCita> OCUPAN_CUPO = EnumSet.of(EstadoCita.PROGRAMADA, EstadoCita.REGISTRADA);

    private final CitaRepository citas;
    private final UsuarioRepository usuarios;
    private final ModuloRepository modulos;
    private final TurnoRepository turnos;
    private final SucursalServicio sucursales;
    private final TramiteServicio tramites;
    private final TurnoServicio turnoServicio;
    private final ColaServicio colas;
    private final Clock reloj;

    public CitaServicio(CitaRepository citas, UsuarioRepository usuarios, ModuloRepository modulos,
                        TurnoRepository turnos, SucursalServicio sucursales, TramiteServicio tramites,
                        TurnoServicio turnoServicio, ColaServicio colas, Clock reloj) {
        this.citas = citas;
        this.usuarios = usuarios;
        this.modulos = modulos;
        this.turnos = turnos;
        this.sucursales = sucursales;
        this.tramites = tramites;
        this.turnoServicio = turnoServicio;
        this.colas = colas;
        this.reloj = reloj;
    }

    /** Franjas de 30 minutos del día con sus cupos. Cupos = módulos que atienden el trámite. */
    public List<FranjaResponse> franjas(String sucursalId, String tramiteId, LocalDate fecha) {
        Sucursal sucursal = sucursales.obtener(sucursalId);
        Tramite tramite = tramiteOfrecido(sucursal, tramiteId);
        int cupos = cupos(sucursal, tramite);
        LocalDateTime inicio = fecha.atTime(sucursal.getHoraApertura());
        LocalDateTime fin = fecha.atTime(sucursal.getHoraCierre());
        Map<LocalDateTime, Long> ocupadas = citas.findBySucursalIdAndTramiteIdAndFechaHoraBetweenAndEstadoIn(
                        sucursalId, tramiteId, inicio, fin, OCUPAN_CUPO).stream()
                .collect(Collectors.groupingBy(Cita::getFechaHora, Collectors.counting()));
        LocalDateTime ahora = ahora();
        List<FranjaResponse> franjas = new ArrayList<>();
        for (LocalDateTime f = inicio; f.plusMinutes(Cita.MINUTOS_FRANJA).compareTo(fin) <= 0;
             f = f.plusMinutes(Cita.MINUTOS_FRANJA)) {
            int usados = ocupadas.getOrDefault(f, 0L).intValue();
            franjas.add(new FranjaResponse(f, cupos, usados, f.isAfter(ahora) && usados < cupos));
        }
        return franjas;
    }

    public CitaResponse agendar(UsuarioActual actual, CitaRequest req) {
        Cliente cliente = cliente(actual);
        Sucursal sucursal = sucursales.obtener(req.sucursalId());
        Tramite tramite = tramiteOfrecido(sucursal, req.tramiteId());
        LocalDateTime fechaHora = req.fechaHora().withSecond(0).withNano(0);
        LocalDateTime ahora = ahora();
        if (!fechaHora.isAfter(ahora)) {
            throw new ReglaNegocioException("La cita debe ser en el futuro");
        }
        if (fechaHora.isAfter(ahora.plusDays(DIAS_MAX_ANTICIPACION))) {
            throw new ReglaNegocioException("Solo se pueden agendar citas con " + DIAS_MAX_ANTICIPACION + " días de anticipación");
        }
        if (fechaHora.getMinute() % Cita.MINUTOS_FRANJA != 0) {
            throw new ReglaNegocioException("Las citas son en franjas de " + Cita.MINUTOS_FRANJA + " minutos (por ejemplo 9:00 o 9:30)");
        }
        if (fechaHora.toLocalTime().isBefore(sucursal.getHoraApertura())
                || fechaHora.toLocalTime().plusMinutes(Cita.MINUTOS_FRANJA).isAfter(sucursal.getHoraCierre())) {
            throw new ReglaNegocioException("La hora está fuera del horario de la sucursal");
        }
        if (citas.existsByClienteIdAndFechaHoraAndEstado(cliente.getId(), fechaHora, EstadoCita.PROGRAMADA)) {
            throw new ReglaNegocioException("Ya tienes una cita a esa hora");
        }
        long ocupados = citas.countBySucursalIdAndTramiteIdAndFechaHoraAndEstadoIn(sucursal.getId(), tramite.getId(),
                fechaHora, OCUPAN_CUPO);
        if (ocupados >= cupos(sucursal, tramite)) {
            throw new ReglaNegocioException("No quedan cupos en esa franja. Elige otra hora");
        }
        Cita cita = citas.save(new Cita(cliente, sucursal, tramite, fechaHora, req.motivo(), ahora));
        return CitaResponse.desde(cita, ahora);
    }

    public List<CitaResponse> misCitas(UsuarioActual actual) {
        LocalDateTime ahora = ahora();
        return citas.findByClienteIdOrderByFechaHoraDesc(actual.id()).stream()
                .peek(c -> {
                    if (c.estaVencida(ahora)) {
                        c.vencer();
                        citas.save(c);
                    }
                })
                .map(c -> CitaResponse.desde(c, ahora)).toList();
    }

    public CitaResponse cancelar(UsuarioActual actual, String id) {
        Cita cita = propia(actual, id);
        cita.cancelar(ahora());
        return CitaResponse.desde(citas.save(cita), ahora());
    }

    /** HU_017: el cliente llega a la sucursal y confirma su cita; recibe un turno con prioridad CITA. */
    public TurnoResponse checkIn(UsuarioActual actual, String id) {
        Cita cita = propia(actual, id);
        LocalDateTime ahora = ahora();
        if (!cita.puedeHacerCheckIn(ahora)) {
            cita.registrarLlegada(null, ahora); // lanza el mensaje adecuado (temprano o vencida)
        }
        if (!turnos.findByClienteIdAndFechaAndEstadoIn(actual.id(), colas.hoy(),
                EnumSet.of(EstadoTurno.EN_ESPERA, EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION)).isEmpty()) {
            throw new ReglaNegocioException("Ya tienes un turno activo. Termínalo o cancélalo antes del check-in");
        }
        Sucursal sucursal = sucursales.obtener(cita.getSucursalId());
        Tramite tramite = tramites.obtener(cita.getTramiteId());
        Turno turno = turnoServicio.crear(sucursal, tramite, TipoPrioridad.CITA, CanalTurno.CITA,
                cita.getClienteId(), cita.getClienteNombre(), cita.getClienteDocumento());
        turno.setCitaId(cita.getId());
        turnos.save(turno);
        cita.registrarLlegada(turno.getId(), ahora);
        citas.save(cita);
        return colas.enriquecer(turno);
    }

    /** Cada 5 minutos marca como vencidas las citas a las que nadie llegó. */
    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void vencerCitas() {
        LocalDateTime limite = ahora().minusMinutes(Cita.CHECKIN_MINUTOS_DESPUES);
        citas.findByEstadoAndFechaHoraBefore(EstadoCita.PROGRAMADA, limite).forEach(c -> {
            c.vencer();
            citas.save(c);
        });
    }

    private int cupos(Sucursal sucursal, Tramite tramite) {
        return (int) Math.max(1, modulos.findBySucursalIdOrderByNumero(sucursal.getId()).stream()
                .filter(m -> m.atiende(tramite.getId())).count());
    }

    private Tramite tramiteOfrecido(Sucursal sucursal, String tramiteId) {
        Tramite t = tramites.obtener(tramiteId);
        if (!sucursal.ofrece(tramiteId) || !t.isActivo()) {
            throw new ReglaNegocioException("La sucursal no ofrece el trámite " + t.getNombre());
        }
        return t;
    }

    private Cita propia(UsuarioActual actual, String id) {
        Cita cita = citas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cita", id));
        if (!cita.esDe(actual.id())) {
            throw new AccesoDenegadoException("Esta cita no es tuya");
        }
        return cita;
    }

    private Cliente cliente(UsuarioActual actual) {
        return usuarios.findById(actual.id())
                .filter(Cliente.class::isInstance).map(Cliente.class::cast)
                .orElseThrow(() -> new AccesoDenegadoException("Solo los clientes pueden agendar citas"));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }
}
