package co.edu.unicauca.saberpro.question.api;

import co.edu.unicauca.saberpro.question.api.dto.ErrorResponse;
import co.edu.unicauca.saberpro.question.application.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.infrastructure.rest.ServicioValidacionNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Traduce las excepciones del dominio y de la infraestructura a códigos HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException e) {
        log.warn("[QUESTION-SERVICE] Regla de negocio violada: {}", e.getErrores());
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage(), e.getErrores());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> peticionMalFormada(Exception e) {
        return respuesta(HttpStatus.BAD_REQUEST, "La petición está mal formada", List.of(e.getMessage()));
    }

    @ExceptionHandler(PreguntaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> noEncontrada(PreguntaNoEncontradaException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(ServicioValidacionNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> validacionNoDisponible(ServicioValidacionNoDisponibleException e) {
        log.error("[QUESTION-SERVICE] {}", e.getMessage());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "validation-service no está disponible", List.of());
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ErrorResponse> brokerNoDisponible(AmqpException e) {
        log.error("[QUESTION-SERVICE] Error publicando en RabbitMQ: {}", e.getMessage());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no está disponible", List.of());
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje, List<String> detalles) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.de(status.value(), status.getReasonPhrase(), mensaje, detalles));
    }
}
