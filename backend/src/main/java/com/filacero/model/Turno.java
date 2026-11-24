package com.filacero.model;

import com.filacero.exception.ReglaNegocioException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Turno de atención. Es la clase central del dominio: conoce su estado y valida
 * cada cambio con EstadoTurno (patrón State). Usa @Version (bloqueo optimista)
 * para que dos asesores no llamen el mismo turno a la vez.
 */
@Document(collection = "turnos")
@CompoundIndex(name = "cola_idx", def = "{'sucursalId': 1, 'tramiteId': 1, 'fecha': 1, 'estado': 1}")
public class Turno {

    /** RN5: número de llamados sin presentarse antes de marcar el turno como ausente. */
    public static final int MAX_LLAMADOS = 3;
    /** RN10: rango de la calificación. */
    public static final int CALIFICACION_MIN = 1;
    public static final int CALIFICACION_MAX = 5;

    @Id
    private String id;
    @Version
    private Long version;

    private String codigo;
    private String sucursalId;
    private String sucursalNombre;
    private String tramiteId;
    private String tramiteNombre;
    /** Día del turno en formato yyyy-MM-dd: la cola se reinicia cada día (RN1). */
    private String fecha;

    private String clienteId;
    private String clienteNombre;
    private String clienteDocumento;

    private TipoPrioridad prioridad = TipoPrioridad.GENERAL;
    private CanalTurno canal = CanalTurno.APP;
    private EstadoTurno estado = EstadoTurno.EN_ESPERA;
    private String citaId;

    private String moduloId;
    private String moduloNombre;
    private String asesorId;
    private String asesorNombre;
    private int vecesLlamado;

    private LocalDateTime horaCreacion;
    private LocalDateTime horaLlamado;
    private LocalDateTime horaInicioAtencion;
    private LocalDateTime horaFin;

    private String observaciones;
    private String tramiteOrigenNombre;
    private Integer calificacion;
    private String comentario;

    public Turno() {
    }

    public Turno(String codigo, Sucursal sucursal, Tramite tramite, String fecha, TipoPrioridad prioridad,
                 CanalTurno canal, LocalDateTime ahora) {
        this.codigo = codigo;
        this.sucursalId = sucursal.getId();
        this.sucursalNombre = sucursal.getNombre();
        this.tramiteId = tramite.getId();
        this.tramiteNombre = tramite.getNombre();
        this.fecha = fecha;
        this.prioridad = prioridad;
        this.canal = canal;
        this.horaCreacion = ahora;
    }

    public void asignarCliente(String id, String nombre, String documento) {
        this.clienteId = id;
        this.clienteNombre = nombre;
        this.clienteDocumento = documento;
    }

    // ---------- Transiciones (patrón State) ----------

    private void cambiarA(EstadoTurno destino) {
        if (!estado.puedePasarA(destino)) {
            throw new ReglaNegocioException("El turno " + codigo + " está " + estado + " y no puede pasar a " + destino);
        }
        this.estado = destino;
    }

    /** Un módulo llama el turno (HU_013). */
    public void llamar(Modulo modulo, Asesor asesor, LocalDateTime ahora) {
        cambiarA(EstadoTurno.LLAMADO);
        this.moduloId = modulo.getId();
        this.moduloNombre = modulo.getNombre();
        this.asesorId = asesor.getId();
        this.asesorNombre = asesor.getNombre();
        this.vecesLlamado = 1;
        this.horaLlamado = ahora;
    }

    /** RN5: se vuelve a llamar; al superar MAX_LLAMADOS hay que marcarlo ausente. */
    public void volverALlamar(LocalDateTime ahora) {
        if (vecesLlamado >= MAX_LLAMADOS) {
            throw new ReglaNegocioException("El turno " + codigo + " ya se llamó " + MAX_LLAMADOS
                    + " veces: márcalo como ausente");
        }
        cambiarA(EstadoTurno.LLAMADO);
        this.vecesLlamado++;
        this.horaLlamado = ahora;
    }

    public void iniciarAtencion(LocalDateTime ahora) {
        cambiarA(EstadoTurno.EN_ATENCION);
        this.horaInicioAtencion = ahora;
    }

    public void finalizar(String observaciones, LocalDateTime ahora) {
        cambiarA(EstadoTurno.ATENDIDO);
        this.observaciones = observaciones;
        this.horaFin = ahora;
    }

    public void marcarAusente(LocalDateTime ahora) {
        cambiarA(EstadoTurno.AUSENTE);
        this.horaFin = ahora;
    }

    /** RN11: solo se cancela un turno que sigue en espera. */
    public void cancelar(LocalDateTime ahora) {
        cambiarA(EstadoTurno.CANCELADO);
        this.horaFin = ahora;
    }

    /**
     * El asesor envía al cliente a otro trámite (HU_015). El turno vuelve a la cola del nuevo trámite
     * conservando su hora de creación, así no pierde el tiempo que ya esperó.
     */
    public void transferir(Tramite destino, String nuevoCodigo) {
        if (destino.getId().equals(tramiteId)) {
            throw new ReglaNegocioException("El turno ya pertenece al trámite " + destino.getNombre());
        }
        cambiarA(EstadoTurno.EN_ESPERA);
        this.tramiteOrigenNombre = this.tramiteNombre;
        this.tramiteId = destino.getId();
        this.tramiteNombre = destino.getNombre();
        this.codigo = nuevoCodigo;
        this.moduloId = null;
        this.moduloNombre = null;
        this.asesorId = null;
        this.asesorNombre = null;
        this.vecesLlamado = 0;
        this.horaLlamado = null;
        this.horaInicioAtencion = null;
    }

    /** RN10: solo se califica un turno atendido, una vez, de 1 a 5 estrellas. */
    public void calificar(int estrellas, String comentario) {
        if (estado != EstadoTurno.ATENDIDO) {
            throw new ReglaNegocioException("Solo se puede calificar un turno atendido");
        }
        if (calificacion != null) {
            throw new ReglaNegocioException("Este turno ya fue calificado");
        }
        if (estrellas < CALIFICACION_MIN || estrellas > CALIFICACION_MAX) {
            throw new ReglaNegocioException("La calificación debe estar entre " + CALIFICACION_MIN + " y " + CALIFICACION_MAX);
        }
        this.calificacion = estrellas;
        this.comentario = comentario;
    }

    // ---------- Consultas (Experto) ----------

    public long minutosEspera(LocalDateTime ahora) {
        LocalDateTime fin = horaLlamado != null ? horaLlamado : ahora;
        return Math.max(0, Duration.between(horaCreacion, fin).toMinutes());
    }

    public Long minutosAtencion() {
        if (horaInicioAtencion == null || horaFin == null) {
            return null;
        }
        return Duration.between(horaInicioAtencion, horaFin).toMinutes();
    }

    public boolean estaActivo() {
        return estado.esActivo();
    }

    public boolean esDe(String idCliente) {
        return clienteId != null && clienteId.equals(idCliente);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getSucursalId() { return sucursalId; }
    public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
    public String getSucursalNombre() { return sucursalNombre; }
    public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }
    public String getTramiteId() { return tramiteId; }
    public void setTramiteId(String tramiteId) { this.tramiteId = tramiteId; }
    public String getTramiteNombre() { return tramiteNombre; }
    public void setTramiteNombre(String tramiteNombre) { this.tramiteNombre = tramiteNombre; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    public TipoPrioridad getPrioridad() { return prioridad; }
    public void setPrioridad(TipoPrioridad prioridad) { this.prioridad = prioridad; }
    public CanalTurno getCanal() { return canal; }
    public void setCanal(CanalTurno canal) { this.canal = canal; }
    public EstadoTurno getEstado() { return estado; }
    public void setEstado(EstadoTurno estado) { this.estado = estado; }
    public String getCitaId() { return citaId; }
    public void setCitaId(String citaId) { this.citaId = citaId; }
    public String getModuloId() { return moduloId; }
    public void setModuloId(String moduloId) { this.moduloId = moduloId; }
    public String getModuloNombre() { return moduloNombre; }
    public void setModuloNombre(String moduloNombre) { this.moduloNombre = moduloNombre; }
    public String getAsesorId() { return asesorId; }
    public void setAsesorId(String asesorId) { this.asesorId = asesorId; }
    public String getAsesorNombre() { return asesorNombre; }
    public void setAsesorNombre(String asesorNombre) { this.asesorNombre = asesorNombre; }
    public int getVecesLlamado() { return vecesLlamado; }
    public void setVecesLlamado(int vecesLlamado) { this.vecesLlamado = vecesLlamado; }
    public LocalDateTime getHoraCreacion() { return horaCreacion; }
    public void setHoraCreacion(LocalDateTime horaCreacion) { this.horaCreacion = horaCreacion; }
    public LocalDateTime getHoraLlamado() { return horaLlamado; }
    public void setHoraLlamado(LocalDateTime horaLlamado) { this.horaLlamado = horaLlamado; }
    public LocalDateTime getHoraInicioAtencion() { return horaInicioAtencion; }
    public void setHoraInicioAtencion(LocalDateTime horaInicioAtencion) { this.horaInicioAtencion = horaInicioAtencion; }
    public LocalDateTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalDateTime horaFin) { this.horaFin = horaFin; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getTramiteOrigenNombre() { return tramiteOrigenNombre; }
    public void setTramiteOrigenNombre(String tramiteOrigenNombre) { this.tramiteOrigenNombre = tramiteOrigenNombre; }
    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
