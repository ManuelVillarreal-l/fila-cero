package com.filacero.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Sucursal (oficina) del Banco del Galeras. */
@Document(collection = "sucursales")
public class Sucursal {

    @Id
    private String id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String telefono;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private List<String> tramitesIds = new ArrayList<>();
    private boolean activa = true;

    public Sucursal() {
    }

    public Sucursal(String nombre, String direccion, String ciudad, String telefono,
                    LocalTime horaApertura, LocalTime horaCierre, List<String> tramitesIds) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.telefono = telefono;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
        this.tramitesIds = tramitesIds == null ? new ArrayList<>() : new ArrayList<>(tramitesIds);
    }

    /** RN7 (Experto): la sucursal conoce su horario y sabe si está abierta. */
    public boolean estaAbierta(LocalTime hora) {
        return activa && !hora.isBefore(horaApertura) && hora.isBefore(horaCierre);
    }

    public boolean ofrece(String tramiteId) {
        return tramitesIds.contains(tramiteId);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public LocalTime getHoraApertura() { return horaApertura; }
    public void setHoraApertura(LocalTime horaApertura) { this.horaApertura = horaApertura; }
    public LocalTime getHoraCierre() { return horaCierre; }
    public void setHoraCierre(LocalTime horaCierre) { this.horaCierre = horaCierre; }
    public List<String> getTramitesIds() { return tramitesIds; }
    public void setTramitesIds(List<String> tramitesIds) { this.tramitesIds = tramitesIds; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
