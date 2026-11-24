package com.filacero.exception;

/** El usuario está autenticado pero no puede hacer esta operación (HTTP 403). */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
