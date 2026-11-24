package com.filacero.service;

import com.filacero.dto.FinalizarRequest;
import com.filacero.dto.ModuloResponse;
import com.filacero.dto.PanelAsesorResponse;
import com.filacero.dto.TramiteResponse;
import com.filacero.dto.TransferirRequest;
import com.filacero.dto.TurnoResponse;
import com.filacero.exception.AccesoDenegadoException;
import com.filacero.exception.RecursoNoEncontradoException;
import com.filacero.exception.ReglaNegocioException;
import com.filacero.model.Asesor;
import com.filacero.model.EstadoModulo;
import com.filacero.model.EstadoTurno;
import com.filacero.model.EventoTurno;
import com.filacero.model.Modulo;
import com.filacero.model.Sucursal;
import com.filacero.model.TipoEvento;
import com.filacero.model.Tramite;
import com.filacero.model.Turno;
import com.filacero.repository.ContadorTurnosRepository;
import com.filacero.repository.ModuloRepository;
import com.filacero.repository.TramiteRepository;
import com.filacero.repository.TurnoRepository;
import com.filacero.repository.UsuarioRepository;
import com.filacero.security.UsuarioActual;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/** Trabajo del asesor en su módulo (HU_012 a HU_015). */
@Service
public class AtencionServicio {

    private static final int REINTENTOS = 3;

    private final TurnoRepository turnos;
    private final ModuloRepository modulos;
    private final UsuarioRepository usuarios;
    private final TramiteRepository tramites;
    private final ContadorTurnosRepository contador;
    private final SucursalServicio sucursales;
    private final ColaServicio colas;
    private final ApplicationEventPublisher eventos;
    private final Clock reloj;

    public AtencionServicio(TurnoRepository turnos, ModuloRepository modulos, UsuarioRepository usuarios,
                            TramiteRepository tramites, ContadorTurnosRepository contador,
                            SucursalServicio sucursales, ColaServicio colas, ApplicationEventPublisher eventos,
                            Clock reloj) {
        this.turnos = turnos;
        this.modulos = modulos;
        this.usuarios = usuarios;
        this.tramites = tramites;
        this.contador = contador;
        this.sucursales = sucursales;
        this.colas = colas;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    public PanelAsesorResponse panel(UsuarioActual actual) {
        Asesor asesor = asesor(actual);
        Sucursal sucursal = sucursales.obtener(asesor.getSucursalId());
        Optional<Modulo> modulo = modulos.findByAsesorId(asesor.getId());
        TurnoResponse actualTurno = modulo.flatMap(this::turnoEnModulo).map(TurnoResponse::desde).orElse(null);
        List<String> tramitesModulo = modulo.map(Modulo::getTramitesIds).orElse(sucursal.getTramitesIds());
        long atendidos = turnos.findBySucursalIdAndFechaAndEstado(sucursal.getId(), colas.hoy(), EstadoTurno.ATENDIDO)
                .stream().filter(t -> asesor.getId().equals(t.getAsesorId())).count();
        List<TramiteResponse> tramitesSucursal = tramites.findAllById(sucursal.getTramitesIds()).stream()
                .filter(Tramite::isActivo).map(TramiteResponse::desde).toList();
        return new PanelAsesorResponse(asesor.getNombre(), sucursal.getId(), sucursal.getNombre(),
                modulo.map(ModuloResponse::desde).orElse(null), actualTurno,
                colas.estadoFilas(sucursal.getId(), tramitesModulo), atendidos,
                modulos.findBySucursalIdOrderByNumero(sucursal.getId()).stream().map(ModuloResponse::desde).toList(),
                tramitesSucursal);
    }

    /** HU_012: abrir el módulo para empezar a atender. */
    public PanelAsesorResponse abrirModulo(UsuarioActual actual, String moduloId) {
        Asesor asesor = asesor(actual);
        modulos.findByAsesorId(asesor.getId()).filter(m -> !m.getId().equals(moduloId)).ifPresent(m -> {
            throw new ReglaNegocioException("Ya tienes abierto el módulo " + m.getNombre() + ". Ciérralo primero");
        });
        Modulo modulo = modulos.findById(moduloId).orElseThrow(() -> new RecursoNoEncontradoException("Módulo", moduloId));
        modulo.abrir(asesor);
        modulos.save(modulo);
        asesor.setModuloId(modulo.getId());
        usuarios.save(asesor);
        return panel(actual);
    }

    public PanelAsesorResponse cerrarModulo(UsuarioActual actual) {
        Asesor asesor = asesor(actual);
        Modulo modulo = moduloAbierto(asesor);
        modulo.cerrar();
        modulos.save(modulo);
        asesor.setModuloId(null);
        usuarios.save(asesor);
        return panel(actual);
    }

    public PanelAsesorResponse pausar(UsuarioActual actual, boolean pausa) {
        Modulo modulo = moduloAbierto(asesor(actual));
        if (pausa) {
            modulo.pausar();
        } else {
            modulo.reanudar();
        }
        modulos.save(modulo);
        return panel(actual);
    }

    /** HU_013: llama al siguiente según la prioridad (RN3) y solo de los trámites del módulo (RN8). */
    public TurnoResponse llamarSiguiente(UsuarioActual actual) {
        Asesor asesor = asesor(actual);
        Modulo modulo = moduloAbierto(asesor);
        if (modulo.getEstado() == EstadoModulo.EN_PAUSA) {
            throw new ReglaNegocioException("El módulo está en pausa. Reanúdalo para llamar turnos");
        }
        turnoEnModulo(modulo).ifPresent(t -> {
            throw new ReglaNegocioException("Primero termina el turno " + t.getCodigo());
        });
        for (int intento = 1; intento <= REINTENTOS; intento++) {
            Turno siguiente = colas.colaDeModulo(modulo).siguiente()
                    .orElseThrow(() -> new ReglaNegocioException("No hay clientes en espera para los trámites de este módulo"));
            try {
                siguiente.llamar(modulo, asesor, ahora());
                Turno guardado = turnos.save(siguiente);
                modulo.ocupar();
                modulos.save(modulo);
                eventos.publishEvent(new EventoTurno(guardado, TipoEvento.TURNO_LLAMADO));
                return TurnoResponse.desde(guardado);
            } catch (OptimisticLockingFailureException e) {
                // Otro asesor llamó ese turno al mismo tiempo: se intenta con el siguiente.
                if (intento == REINTENTOS) {
                    throw e;
                }
            }
        }
        throw new ReglaNegocioException("No se pudo llamar el turno");
    }

    /** RN5: volver a llamar (máximo 3 veces). */
    public TurnoResponse volverALlamar(UsuarioActual actual, String turnoId) {
        EnModulo ctx = turnoDelModulo(actual, turnoId);
        ctx.turno().volverALlamar(ahora());
        Turno guardado = turnos.save(ctx.turno());
        eventos.publishEvent(new EventoTurno(guardado, TipoEvento.TURNO_LLAMADO));
        return TurnoResponse.desde(guardado);
    }

    public TurnoResponse iniciar(UsuarioActual actual, String turnoId) {
        EnModulo ctx = turnoDelModulo(actual, turnoId);
        ctx.turno().iniciarAtencion(ahora());
        return TurnoResponse.desde(turnos.save(ctx.turno()));
    }

    public TurnoResponse finalizar(UsuarioActual actual, String turnoId, FinalizarRequest req) {
        EnModulo ctx = turnoDelModulo(actual, turnoId);
        ctx.turno().finalizar(req == null ? null : req.observaciones(), ahora());
        return cerrarTurno(ctx, TipoEvento.TURNO_ATENDIDO);
    }

    public TurnoResponse marcarAusente(UsuarioActual actual, String turnoId) {
        EnModulo ctx = turnoDelModulo(actual, turnoId);
        ctx.turno().marcarAusente(ahora());
        return cerrarTurno(ctx, TipoEvento.TURNO_AUSENTE);
    }

    /** HU_015: enviar al cliente a la fila de otro trámite sin perder su tiempo de espera. */
    public TurnoResponse transferir(UsuarioActual actual, String turnoId, TransferirRequest req) {
        EnModulo ctx = turnoDelModulo(actual, turnoId);
        Sucursal sucursal = sucursales.obtener(ctx.turno().getSucursalId());
        Tramite destino = tramites.findById(req.tramiteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Trámite", req.tramiteId()));
        if (!destino.isActivo() || !sucursal.ofrece(destino.getId())) {
            throw new ReglaNegocioException("La sucursal no ofrece el trámite " + destino.getNombre());
        }
        String codigo = destino.codigoTurno(contador.siguiente(sucursal.getId(), destino.getId(), colas.hoy()));
        ctx.turno().transferir(destino, codigo);
        return cerrarTurno(ctx, TipoEvento.TURNO_TRANSFERIDO);
    }

    /** El turno sale del módulo: se guarda, el módulo queda disponible y se avisa a los observadores. */
    private TurnoResponse cerrarTurno(EnModulo ctx, TipoEvento evento) {
        Turno guardado = turnos.save(ctx.turno());
        ctx.modulo().liberar();
        modulos.save(ctx.modulo());
        eventos.publishEvent(new EventoTurno(guardado, evento));
        return TurnoResponse.desde(guardado);
    }

    /** Turno que está en el módulo del asesor, junto con ese módulo. */
    private record EnModulo(Modulo modulo, Turno turno) {
    }

    private EnModulo turnoDelModulo(UsuarioActual actual, String turnoId) {
        Modulo modulo = moduloAbierto(asesor(actual));
        Turno turno = turnos.findById(turnoId).orElseThrow(() -> new RecursoNoEncontradoException("Turno", turnoId));
        if (!modulo.getId().equals(turno.getModuloId())) {
            throw new AccesoDenegadoException("El turno " + turno.getCodigo() + " no está asignado a tu módulo");
        }
        return new EnModulo(modulo, turno);
    }

    private Optional<Turno> turnoEnModulo(Modulo modulo) {
        return turnos.findByModuloIdAndEstadoIn(modulo.getId(), EnumSet.of(EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION))
                .stream().findFirst();
    }

    private Modulo moduloAbierto(Asesor asesor) {
        return modulos.findByAsesorId(asesor.getId())
                .filter(Modulo::estaAbierto)
                .orElseThrow(() -> new ReglaNegocioException("Primero abre un módulo de atención"));
    }

    private Asesor asesor(UsuarioActual actual) {
        return usuarios.findById(actual.id())
                .filter(Asesor.class::isInstance).map(Asesor.class::cast)
                .orElseThrow(() -> new AccesoDenegadoException("Solo los asesores atienden turnos"));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }
}
