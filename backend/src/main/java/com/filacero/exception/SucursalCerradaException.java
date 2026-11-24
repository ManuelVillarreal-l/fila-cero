package com.filacero.exception;

/** RN7: la sucursal está fuera de su horario de atención (HTTP 422). */
public class SucursalCerradaException extends RuntimeException {
    public SucursalCerradaException(String mensaje) {
        super(mensaje);
    }
}
