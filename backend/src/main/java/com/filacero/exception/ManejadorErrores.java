package com.filacero.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Manejador global de errores: toda respuesta de error sale con el mismo formato y en español (RNF_006). */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    public record RespuestaError(LocalDateTime fecha, int estado, String error, String mensaje,
                                 Map<String, String> campos) {
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String mensaje, Map<String, String> campos) {
        return ResponseEntity.status(estado)
                .body(new RespuestaError(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), mensaje, campos));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage() == null ? "inválido" : e.getDefaultMessage(),
                        (a, b) -> a, LinkedHashMap::new));
        return responder(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", campos);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<RespuestaError> formato(Exception ex) {
        return responder(HttpStatus.BAD_REQUEST, "La solicitud tiene un formato inválido", null);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<RespuestaError> credenciales(CredencialesInvalidasException ex) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<RespuestaError> denegado(AccesoDenegadoException ex) {
        return responder(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaError> sinPermiso(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación", null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> noEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<RespuestaError> regla(ReglaNegocioException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(SucursalCerradaException.class)
    public ResponseEntity<RespuestaError> cerrada(SucursalCerradaException ex) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<RespuestaError> duplicado(DuplicateKeyException ex) {
        return responder(HttpStatus.CONFLICT, "Ya existe un registro con esos datos (correo, documento o prefijo)", null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<RespuestaError> concurrencia(OptimisticLockingFailureException ex) {
        return responder(HttpStatus.CONFLICT, "Otro asesor modificó este turno al mismo tiempo. Intenta de nuevo", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> general(Exception ex) {
        log.error("Error no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", null);
    }
}
