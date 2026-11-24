package com.filacero.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/** Aviso que recibe un cliente sobre su turno (RF_021). */
@Document(collection = "notificaciones")
public class Notificacion {

    @Id
    private String id;
    private String clienteId;
    private String turnoId;
    private TipoEvento tipo;
    private String titulo;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;

    public Notificacion() {
    }

    public Notificacion(String clienteId, String turnoId, TipoEvento tipo, String titulo, String mensaje,
                        LocalDateTime fecha) {
        this.clienteId = clienteId;
        this.turnoId = turnoId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public void marcarLeida() { this.leida = true; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getTurnoId() { return turnoId; }
    public void setTurnoId(String turnoId) { this.turnoId = turnoId; }
    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
}
