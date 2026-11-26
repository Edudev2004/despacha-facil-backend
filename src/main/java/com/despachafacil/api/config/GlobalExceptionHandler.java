package com.despachafacil.api.config;

import com.despachafacil.api.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Capturador global de excepciones para toda la API de Despacha Fácil.
 * Imprime logs detallados e inequívocos en la terminal (consola / stdout) con stack traces legibles,
 * devolviendo respuestas JSON estandarizadas (RFC 7807 compatible).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String BORDER_LINE = "==========================================================================================";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String reason = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();

        logWarnBanner("RESPONSE_STATUS_EXCEPTION", status.value(), reason, request.getRequestURI());

        ApiErrorResponse body = ApiErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                reason,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        logWarnBanner("VALIDATION_FAILED", HttpStatus.BAD_REQUEST.value(),
                "Error de validación en campos: " + errors, request.getRequestURI());

        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Uno o más campos enviados son inválidos",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        logWarnBanner("SECURITY_BAD_CREDENTIALS", HttpStatus.UNAUTHORIZED.value(),
                "Credenciales incorrectas intentadas en: " + request.getRequestURI(), request.getRequestURI());

        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Credenciales de acceso incorrectas o cuenta inactiva",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        logWarnBanner("SECURITY_ACCESS_DENIED", HttpStatus.FORBIDDEN.value(),
                "Acceso no autorizado: " + ex.getMessage(), request.getRequestURI());

        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "No tienes permisos suficientes para realizar esta acción",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        logErrorBanner("DATABASE_INTEGRITY_VIOLATION", HttpStatus.CONFLICT.value(),
                "Violación de clave única o integridad referencial en BD: " + ex.getMostSpecificCause().getMessage(),
                request.getRequestURI(), ex);

        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "Conflicto de unicidad o integridad de datos en el servidor",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        logWarnBanner("ILLEGAL_ARGUMENT", HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getRequestURI());

        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtExceptions(
            Exception ex,
            HttpServletRequest request
    ) {
        logErrorBanner("UNEXPECTED_SERVER_ERROR [CRITICAL]", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(), request.getRequestURI(), ex);

        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ha ocurrido un error interno en el servidor. Por favor intenta nuevamente.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private void logWarnBanner(String category, int status, String detail, String uri) {
        System.err.println("\n" + BORDER_LINE);
        System.err.printf("⚠️  [DESPACHA-FACIL ALERTA HTTP %d] %s%n", status, category);
        System.err.printf("📍  Ruta afectada : %s%n", uri);
        System.err.printf("💬  Detalle       : %s%n", detail);
        System.err.println(BORDER_LINE + "\n");
        log.warn("[{}] HTTP {} en URI {}: {}", category, status, uri, detail);
    }

    private void logErrorBanner(String category, int status, String detail, String uri, Throwable ex) {
        System.err.println("\n" + BORDER_LINE);
        System.err.printf("❌ [DESPACHA-FACIL ERROR CRÍTICO %d] %s%n", status, category);
        System.err.printf("📍  Ruta afectada : %s%n", uri);
        System.err.printf("💬  Causa raíz    : %s%n", detail);
        System.err.println("📜  Stack trace a continuación:");
        ex.printStackTrace(System.err);
        System.err.println(BORDER_LINE + "\n");
        log.error("[{}] HTTP {} en URI {}: {}", category, status, uri, detail, ex);
    }
}
