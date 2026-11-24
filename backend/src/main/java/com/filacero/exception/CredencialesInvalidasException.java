package com.filacero.exception;

/** Correo o contraseña incorrectos (HTTP 401). */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
