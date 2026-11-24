package com.filacero.model;

import java.time.LocalDate;
import java.time.Period;

/** Cliente del banco registrado en la app. */
public class Cliente extends Usuario {

    /** Edad desde la que un cliente se considera adulto mayor (RN3). */
    public static final int EDAD_ADULTO_MAYOR = 60;

    private String telefono;
    private LocalDate fechaNacimiento;
    private CondicionEspecial condicion = CondicionEspecial.NINGUNA;

    public Cliente() {
    }

    public Cliente(String nombre, String documento, String correo, String contrasena,
                   String telefono, LocalDate fechaNacimiento, CondicionEspecial condicion) {
        super(nombre, documento, correo, contrasena, Rol.CLIENTE);
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.condicion = condicion == null ? CondicionEspecial.NINGUNA : condicion;
    }

    /** GRASP Experto: el cliente conoce su edad y su condición, así que sabe si es preferencial. */
    public boolean esPreferencial(LocalDate hoy) {
        if (condicion != null && condicion != CondicionEspecial.NINGUNA) {
            return true;
        }
        return fechaNacimiento != null && Period.between(fechaNacimiento, hoy).getYears() >= EDAD_ADULTO_MAYOR;
    }

    public TipoPrioridad prioridadPara(LocalDate hoy) {
        return esPreferencial(hoy) ? TipoPrioridad.PREFERENCIAL : TipoPrioridad.GENERAL;
    }

    @Override
    public String descripcionPerfil() {
        return "Cliente" + (condicion != CondicionEspecial.NINGUNA ? " con atención preferencial" : "");
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public CondicionEspecial getCondicion() { return condicion; }
    public void setCondicion(CondicionEspecial condicion) { this.condicion = condicion; }
}
