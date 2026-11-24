package com.filacero.model;

/** Asesor o cajero que atiende en un módulo de una sucursal. Su cuenta la crea el administrador. */
public class Asesor extends Usuario {

    private String sucursalId;
    /** Módulo que tiene abierto en este momento (null si no está atendiendo). */
    private String moduloId;

    public Asesor() {
    }

    public Asesor(String nombre, String documento, String correo, String contrasena, String sucursalId) {
        super(nombre, documento, correo, contrasena, Rol.ASESOR);
        this.sucursalId = sucursalId;
    }

    /** RN8: un asesor solo trabaja en su propia sucursal. */
    public boolean perteneceA(String idSucursal) {
        return sucursalId != null && sucursalId.equals(idSucursal);
    }

    @Override
    public String descripcionPerfil() {
        return "Asesor de ventanilla";
    }

    public String getSucursalId() { return sucursalId; }
    public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
    public String getModuloId() { return moduloId; }
    public void setModuloId(String moduloId) { this.moduloId = moduloId; }
}
