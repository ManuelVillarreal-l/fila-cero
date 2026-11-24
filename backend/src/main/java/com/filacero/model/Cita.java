package com.filacero.model;

import com.filacero.exception.ReglaNegocioException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/** Cita agendada por un cliente para una franja horaria (HU_016 y HU_017). */
@Document(collection = "citas")
public class Cita {

    /** RN9: duración de cada franja de citas en minutos. */
    public static final int MINUTOS_FRANJA = 30;
    /** RN9: el check-in se puede hacer desde 15 minutos antes... */
    public static final int CHECKIN_MINUTOS_ANTES = 15;
    /** ...hasta 10 minutos después de la hora de la cita. */
    public static final int CHECKIN_MINUTOS_DESPUES = 10;
    /** RN12: la cita se cancela hasta 1 hora antes. */
    public static final int CANCELAR_HORAS_ANTES = 1;

    @Id
    private String id;
    private String clienteId;
    private String clienteNombre;
    private String clienteDocumento;
    private String sucursalId;
    private String sucursalNombre;
    private String tramiteId;
    private String tramiteNombre;
    private LocalDateTime fechaHora;
    private EstadoCita estado = EstadoCita.PROGRAMADA;
    private String turnoId;
    private String motivo;
    private LocalDateTime fechaCreacion;

    public Cita() {
    }

    public Cita(Cliente cliente, Sucursal sucursal, Tramite tramite, LocalDateTime fechaHora, String motivo,
                LocalDateTime ahora) {
        this.clienteId = cliente.getId();
        this.clienteNombre = cliente.getNombre();
        this.clienteDocumento = cliente.getDocumento();
        this.sucursalId = sucursal.getId();
        this.sucursalNombre = sucursal.getNombre();
        this.tramiteId = tramite.getId();
        this.tramiteNombre = tramite.getNombre();
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.fechaCreacion = ahora;
    }

    /** RN9 (Experto): la cita sabe si el cliente llegó dentro de la ventana de check-in. */
    public boolean puedeHacerCheckIn(LocalDateTime ahora) {
        return estado == EstadoCita.PROGRAMADA
                && !ahora.isBefore(fechaHora.minusMinutes(CHECKIN_MINUTOS_ANTES))
                && !ahora.isAfter(fechaHora.plusMinutes(CHECKIN_MINUTOS_DESPUES));
    }

    public boolean estaVencida(LocalDateTime ahora) {
        return estado == EstadoCita.PROGRAMADA && ahora.isAfter(fechaHora.plusMinutes(CHECKIN_MINUTOS_DESPUES));
    }

    public void registrarLlegada(String idTurno, LocalDateTime ahora) {
        if (estaVencida(ahora)) {
            throw new ReglaNegocioException("La cita venció: el check-in era hasta "
                    + CHECKIN_MINUTOS_DESPUES + " minutos después de la hora agendada");
        }
        if (!puedeHacerCheckIn(ahora)) {
            throw new ReglaNegocioException("El check-in se habilita " + CHECKIN_MINUTOS_ANTES
                    + " minutos antes de la cita");
        }
        this.estado = EstadoCita.REGISTRADA;
        this.turnoId = idTurno;
    }

    /** RN12: se cancela solo si está programada y falta más de 1 hora. */
    public void cancelar(LocalDateTime ahora) {
        if (estado != EstadoCita.PROGRAMADA) {
            throw new ReglaNegocioException("Solo se puede cancelar una cita programada");
        }
        if (!ahora.isBefore(fechaHora.minusHours(CANCELAR_HORAS_ANTES))) {
            throw new ReglaNegocioException("La cita solo se puede cancelar hasta "
                    + CANCELAR_HORAS_ANTES + " hora antes");
        }
        this.estado = EstadoCita.CANCELADA;
    }

    public void vencer() {
        if (estado == EstadoCita.PROGRAMADA) {
            this.estado = EstadoCita.VENCIDA;
        }
    }

    public boolean esDe(String idCliente) {
        return clienteId != null && clienteId.equals(idCliente);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    public String getSucursalId() { return sucursalId; }
    public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
    public String getSucursalNombre() { return sucursalNombre; }
    public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }
    public String getTramiteId() { return tramiteId; }
    public void setTramiteId(String tramiteId) { this.tramiteId = tramiteId; }
    public String getTramiteNombre() { return tramiteNombre; }
    public void setTramiteNombre(String tramiteNombre) { this.tramiteNombre = tramiteNombre; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
    public String getTurnoId() { return turnoId; }
    public void setTurnoId(String turnoId) { this.turnoId = turnoId; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
