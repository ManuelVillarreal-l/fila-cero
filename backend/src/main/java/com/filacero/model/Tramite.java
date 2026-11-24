package com.filacero.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Trámite o servicio bancario que se atiende con turno (Caja, Asesoría, Créditos...). */
@Document(collection = "tramites")
public class Tramite {

    @Id
    private String id;
    /** Letra con que empieza el código del turno (RN1), por ejemplo "C" para Caja. */
    @Indexed(unique = true)
    private String prefijo;
    private String nombre;
    private String descripcion;
    /** Minutos promedio que dura la atención de este trámite (RN4). */
    private int tiempoPromedioMin;
    private boolean activo = true;

    public Tramite() {
    }

    public Tramite(String prefijo, String nombre, String descripcion, int tiempoPromedioMin) {
        this.prefijo = prefijo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tiempoPromedioMin = tiempoPromedioMin;
    }

    /** RN1 (Experto): el trámite arma el código visible del turno, por ejemplo C-007. */
    public String codigoTurno(int consecutivo) {
        return String.format("%s-%03d", prefijo, consecutivo);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String prefijo) { this.prefijo = prefijo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getTiempoPromedioMin() { return tiempoPromedioMin; }
    public void setTiempoPromedioMin(int tiempoPromedioMin) { this.tiempoPromedioMin = tiempoPromedioMin; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
