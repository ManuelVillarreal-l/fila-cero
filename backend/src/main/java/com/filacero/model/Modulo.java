package com.filacero.model;

import com.filacero.exception.ReglaNegocioException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/** Módulo o ventanilla de atención de una sucursal (por ejemplo "Caja 2"). */
@Document(collection = "modulos")
public class Modulo {

    @Id
    private String id;
    private String sucursalId;
    private int numero;
    private String nombre;
    private List<String> tramitesIds = new ArrayList<>();
    private EstadoModulo estado = EstadoModulo.CERRADO;
    private String asesorId;
    private String asesorNombre;

    public Modulo() {
    }

    public Modulo(String sucursalId, int numero, String nombre, List<String> tramitesIds) {
        this.sucursalId = sucursalId;
        this.numero = numero;
        this.nombre = nombre;
        this.tramitesIds = tramitesIds == null ? new ArrayList<>() : new ArrayList<>(tramitesIds);
    }

    /** El asesor abre el módulo y queda disponible para llamar turnos (HU_012). */
    public void abrir(Asesor asesor) {
        if (estado != EstadoModulo.CERRADO && !asesor.getId().equals(asesorId)) {
            throw new ReglaNegocioException("El módulo " + nombre + " ya está abierto por " + asesorNombre);
        }
        if (!asesor.perteneceA(sucursalId)) {
            throw new ReglaNegocioException("El módulo no pertenece a la sucursal del asesor");
        }
        this.asesorId = asesor.getId();
        this.asesorNombre = asesor.getNombre();
        if (estado == EstadoModulo.CERRADO) {
            this.estado = EstadoModulo.DISPONIBLE;
        }
    }

    public void cerrar() {
        if (estado == EstadoModulo.OCUPADO) {
            throw new ReglaNegocioException("Finaliza la atención en curso antes de cerrar el módulo");
        }
        this.estado = EstadoModulo.CERRADO;
        this.asesorId = null;
        this.asesorNombre = null;
    }

    public void pausar() {
        if (estado == EstadoModulo.OCUPADO) {
            throw new ReglaNegocioException("Finaliza la atención en curso antes de pausar el módulo");
        }
        this.estado = EstadoModulo.EN_PAUSA;
    }

    public void reanudar() {
        if (estado == EstadoModulo.EN_PAUSA) {
            this.estado = EstadoModulo.DISPONIBLE;
        }
    }

    public void ocupar() { this.estado = EstadoModulo.OCUPADO; }

    public void liberar() {
        if (estado == EstadoModulo.OCUPADO) {
            this.estado = EstadoModulo.DISPONIBLE;
        }
    }

    public boolean estaAbierto() {
        return estado != EstadoModulo.CERRADO;
    }

    /** RN8: el módulo solo llama turnos de los trámites que atiende. */
    public boolean atiende(String tramiteId) {
        return tramitesIds.contains(tramiteId);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSucursalId() { return sucursalId; }
    public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<String> getTramitesIds() { return tramitesIds; }
    public void setTramitesIds(List<String> tramitesIds) { this.tramitesIds = tramitesIds; }
    public EstadoModulo getEstado() { return estado; }
    public void setEstado(EstadoModulo estado) { this.estado = estado; }
    public String getAsesorId() { return asesorId; }
    public void setAsesorId(String asesorId) { this.asesorId = asesorId; }
    public String getAsesorNombre() { return asesorNombre; }
    public void setAsesorNombre(String asesorNombre) { this.asesorNombre = asesorNombre; }
}
