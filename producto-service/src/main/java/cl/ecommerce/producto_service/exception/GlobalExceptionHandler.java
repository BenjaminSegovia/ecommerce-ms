package cl.ecommerce.producto_service.exception;

import cl.ecommerce.producto_service.dto.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

        ApiErrorDTO error = new ApiErrorDTO();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        error.setMensaje(e.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiErrorDTO> handleReglaNegocio(
            ReglaNegocioException e,
            HttpServletRequest request) {

        ApiErrorDTO error = new ApiErrorDTO();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setMensaje(e.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidationExceptions(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        Map<String, String> campos = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            campos.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiErrorDTO error = new ApiErrorDTO();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setMensaje("Error de validación en los campos enviados");
        error.setPath(request.getRequestURI());
        error.setCampos(campos);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleDataIntegrityViolation(
            DataIntegrityViolationException e,
            HttpServletRequest request) {

        log.error("DataIntegrityViolationException: ", e);

        ApiErrorDTO error = new ApiErrorDTO();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setError(HttpStatus.CONFLICT.getReasonPhrase());
        error.setMensaje("Conflicto de integridad de datos (registro duplicado o restricción violada)");
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGlobalException(
            Exception e,
            HttpServletRequest request) {

        log.error("Unhandled Exception: ", e);

        ApiErrorDTO error = new ApiErrorDTO();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        error.setMensaje("Ha ocurrido un error interno en el servidor");
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}