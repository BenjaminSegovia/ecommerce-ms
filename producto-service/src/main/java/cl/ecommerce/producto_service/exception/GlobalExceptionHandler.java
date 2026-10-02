package cl.ecommerce.producto_service.exception;

import cl.ecommerce.producto_service.dto.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorDTO> handleRecursoNoEncontrado(
            RecursoNoEncontradoException e,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(construirError(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleNoResourceFound(
            NoResourceFoundException e,
            HttpServletRequest request) {

        String mensaje = String.format("La ruta '%s' no existe", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(construirError(HttpStatus.NOT_FOUND, mensaje, request.getRequestURI()));
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiErrorDTO> handleReglaNegocio(
            ReglaNegocioException e,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(construirError(HttpStatus.BAD_REQUEST, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidationExceptions(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        Map<String, String> campos = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            campos.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiErrorDTO error = construirError(HttpStatus.BAD_REQUEST, "Error de validación en los campos enviados", request.getRequestURI());
        error.setCampos(campos);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleDataIntegrityViolation(
            DataIntegrityViolationException e,
            HttpServletRequest request) {

        log.error("DataIntegrityViolationException: ", e);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(construirError(HttpStatus.CONFLICT, "Conflicto de integridad de datos (registro duplicado o restricción violada)", request.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDTO> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(construirError(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud (JSON) es inválido o está vacío", request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorDTO> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {

        String mensaje = String.format("El parámetro '%s' debe ser de tipo %s", e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "válido");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(construirError(HttpStatus.BAD_REQUEST, mensaje, request.getRequestURI()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorDTO> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {

        String mensaje = String.format("El método HTTP '%s' no está soportado para esta ruta", e.getMethod());

        HttpHeaders headers = new HttpHeaders();
        if (e.getSupportedHttpMethods() != null) {
            headers.setAllow(e.getSupportedHttpMethods());
        }

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(headers)
                .body(construirError(HttpStatus.METHOD_NOT_ALLOWED, mensaje, request.getRequestURI()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorDTO> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException e,
            HttpServletRequest request) {

        String mensaje = String.format("El Content-Type '%s' no está soportado", e.getContentType());

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(construirError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mensaje, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGlobalException(
            Exception e,
            HttpServletRequest request) {

        log.error("Unhandled Exception: ", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(construirError(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error interno en el servidor", request.getRequestURI()));
    }

    private ApiErrorDTO construirError(HttpStatus status, String mensaje, String path) {
        ApiErrorDTO error = new ApiErrorDTO();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(status.value());
        error.setError(status.getReasonPhrase());
        error.setMensaje(mensaje);
        error.setPath(path);
        return error;
    }
}