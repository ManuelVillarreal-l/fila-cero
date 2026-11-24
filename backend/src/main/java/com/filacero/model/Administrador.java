package com.filacero.model;

/** Gerente o administrador del banco: configura sucursales, trámites, módulos y asesores. */
public class Administrador extends Usuario {

    private String cargo;

    public Administrador() {
    }

    public Administrador(String nombre, String documento, String correo, String contrasena, String cargo) {
        super(nombre, documento, correo, contrasena, Rol.ADMINISTRADOR);
        this.cargo = cargo;
    }

    @Override
    public String descripcionPerfil() {
        return cargo == null ? "Administrador" : cargo;
    }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
}
