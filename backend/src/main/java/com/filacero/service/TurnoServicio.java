package com.filacero.service;

import com.filacero.dto.CalificacionRequest;
import com.filacero.dto.KioscoTurnoRequest;
import com.filacero.dto.TomarTurnoRequest;
import com.filacero.dto.TurnoResponse;
import com.filacero.exception.AccesoDenegadoException;
import com.filacero.exception.RecursoNoEncontradoException;
import com.filacero.exception.ReglaNegocioException;
import com.filacero.exception.SucursalCerradaException;
import com.filacero.model.CanalTurno;
import com.filacero.model.Cliente;
import com.filacero.model.EstadoTurno;
import com.filacero.model.EventoTurno;
import com.filacero.model.Sucursal;
import com.filacero.model.TipoEvento;
import com.filacero.model.TipoPrioridad;
import com.filacero.model.Tramite;
import com.filacero.model.Turno;
import com.filacero.model.Usuario;
import com.filacero.repository.ContadorTurnosRepository;
import com.filacero.repository.SucursalRepository;
import com.filacero.repository.TramiteRepository;
import com.filacero.repository.TurnoRepository;
import com.filacero.repository.UsuarioRepository;
import com.filacero.security.UsuarioActual;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Turnos desde el punto de vista del cliente (HU_008 a HU_011 y HU_019). */
@Service
public class TurnoServicio {

    private static final Set<EstadoTurno> ACTIVOS = EnumSet.of(EstadoTurno.EN_ESPERA, EstadoTurno.LLAMADO,
            EstadoTurno.EN_ATENCION);

    private final TurnoRepository turnos;
    private final SucursalRepository sucursales;
    private final TramiteRepository tramites;
    private final UsuarioRepository usuarios;
    private final ContadorTurnosRepository contador;
    private final ColaServicio colas;
    private final ApplicationEventPublisher eventos;
    private final Clock reloj;

    public TurnoServicio(TurnoRepository turnos, SucursalRepository sucursales, TramiteRepository tramites,
                         UsuarioRepository usuarios, ContadorTurnosRepository contador, ColaServicio colas,
                         ApplicationEventPublisher eventos, Clock reloj) {
        this.turnos = turnos;
        this.sucursales = sucursales;
        this.tramites = tramites;
        this.usuarios = usuarios;
        this.contador = contador;
        this.colas = colas;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    /** HU_008: el cliente toma un turno virtual desde la app. */
    public TurnoResponse tomarTurno(UsuarioActual actual, TomarTurnoRequest req) {
        Cliente cliente = cliente(actual);
        Sucursal sucursal = sucursalAbierta(req.sucursalId());
        Tramite tramite = tramiteDe(sucursal, req.tramiteId());
        validarSinTurnoActivo(turnos.findByClienteIdAndFechaAndEstadoIn(cliente.getId(), hoy(), ACTIVOS));
        Turno turno = crear(sucursal, tramite, cliente.prioridadPara(LocalDate.now(reloj)), CanalTurno.APP,
                cliente.getId(), cliente.getNombre(), cliente.getDocumento());
        return colas.enriquecer(turno);
    }

    /** HU_009: turno presencial en el kiosco, con el documento y sin cuenta. */
    public TurnoResponse tomarTurnoKiosco(KioscoTurnoRequest req) {
        Sucursal sucursal = sucursalAbierta(req.sucursalId());
        Tramite tramite = tramiteDe(sucursal, req.tramiteId());
        validarSinTurnoActivo(turnos.findByClienteDocumentoAndSucursalIdAndFechaAndEstadoIn(
                req.documento(), sucursal.getId(), hoy(), ACTIVOS));
        Optional<Cliente> registrado = usuarios.findByDocumento(req.documento())
                .filter(Cliente.class::isInstance).map(Cliente.class::cast);
        TipoPrioridad prioridad = req.preferencial() ? TipoPrioridad.PREFERENCIAL
                : registrado.map(c -> c.prioridadPara(LocalDate.now(reloj))).orElse(TipoPrioridad.GENERAL);
        String nombre = registrado.map(Usuario::getNombre)
                .orElse(req.nombre() == null || req.nombre().isBlank() ? "Cliente" : req.nombre().trim());
        Turno turno = crear(sucursal, tramite, prioridad, CanalTurno.KIOSCO,
                registrado.map(Usuario::getId).orElse(null), nombre, req.documento());
        return colas.enriquecer(turno);
    }

    /** Crea el turno con su código del día (RN1) y avisa a los observadores. También lo usa el check-in de citas. */
    public Turno crear(Sucursal sucursal, Tramite tramite, TipoPrioridad prioridad, CanalTurno canal,
                       String clienteId, String clienteNombre, String documento) {
        String fecha = hoy();
        int consecutivo = contador.siguiente(sucursal.getId(), tramite.getId(), fecha);
        Turno turno = new Turno(tramite.codigoTurno(consecutivo), sucursal, tramite, fecha, prioridad, canal,
                LocalDateTime.now(reloj));
        turno.asignarCliente(clienteId, clienteNombre, documento);
        Turno guardado = turnos.save(turno);
        eventos.publishEvent(new EventoTurno(guardado, TipoEvento.TURNO_CREADO));
        return guardado;
    }

    /** HU_010: el turno activo del cliente con su posición y tiempo estimado. */
    public Optional<TurnoResponse> miTurnoActual(UsuarioActual actual) {
        return turnos.findByClienteIdAndFechaAndEstadoIn(actual.id(), hoy(), ACTIVOS).stream()
                .findFirst().map(colas::enriquecer);
    }

    public List<TurnoResponse> historial(UsuarioActual actual) {
        return turnos.findByClienteIdOrderByHoraCreacionDesc(actual.id()).stream().map(TurnoResponse::desde).toList();
    }

    /** Consulta pública del tiquete del kiosco por su id (el código QR del papel). */
    public TurnoResponse consultar(String id) {
        return colas.enriquecer(obtener(id));
    }

    /** HU_011 (RN11): solo se cancela un turno en espera. */
    public TurnoResponse cancelar(UsuarioActual actual, String id) {
        Turno turno = propio(actual, id);
        turno.cancelar(LocalDateTime.now(reloj));
        Turno guardado = turnos.save(turno);
        eventos.publishEvent(new EventoTurno(guardado, TipoEvento.TURNO_CANCELADO));
        return TurnoResponse.desde(guardado);
    }

    /** HU_019 (RN10). */
    public TurnoResponse calificar(UsuarioActual actual, String id, CalificacionRequest req) {
        Turno turno = propio(actual, id);
        turno.calificar(req.estrellas(), req.comentario());
        return TurnoResponse.desde(turnos.save(turno));
    }

    private Turno obtener(String id) {
        return turnos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Turno", id));
    }

    private Turno propio(UsuarioActual actual, String id) {
        Turno turno = obtener(id);
        if (!turno.esDe(actual.id())) {
            throw new AccesoDenegadoException("Este turno no es tuyo");
        }
        return turno;
    }

    private Cliente cliente(UsuarioActual actual) {
        return usuarios.findById(actual.id())
                .filter(Cliente.class::isInstance).map(Cliente.class::cast)
                .orElseThrow(() -> new AccesoDenegadoException("Solo los clientes pueden tomar turnos desde la app"));
    }

    /** RN7. */
    private Sucursal sucursalAbierta(String id) {
        Sucursal s = sucursales.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", id));
        if (!s.estaAbierta(LocalTime.now(reloj))) {
            throw new SucursalCerradaException("La sucursal " + s.getNombre() + " atiende de " + s.getHoraApertura()
                    + " a " + s.getHoraCierre() + ". Vuelve en ese horario o agenda una cita");
        }
        return s;
    }

    private Tramite tramiteDe(Sucursal sucursal, String tramiteId) {
        Tramite t = tramites.findById(tramiteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Trámite", tramiteId));
        if (!t.isActivo() || !sucursal.ofrece(tramiteId)) {
            throw new ReglaNegocioException("La sucursal " + sucursal.getNombre() + " no ofrece " + t.getNombre());
        }
        return t;
    }

    /** RN2: un turno activo a la vez. */
    private void validarSinTurnoActivo(List<Turno> activos) {
        if (!activos.isEmpty()) {
            Turno t = activos.get(0);
            throw new ReglaNegocioException("Ya tienes el turno " + t.getCodigo() + " activo en " + t.getSucursalNombre()
                    + ". Cancélalo o espera a ser atendido para tomar otro");
        }
    }

    private String hoy() {
        return LocalDate.now(reloj).toString();
    }
}
