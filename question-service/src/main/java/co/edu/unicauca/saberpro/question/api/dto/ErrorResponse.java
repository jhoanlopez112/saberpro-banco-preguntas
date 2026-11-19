package co.edu.unicauca.saberpro.question.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cuerpo JSON uniforme para las respuestas de error de la API.
 */
public record ErrorResponse(int status, String error, String mensaje, List<String> detalles, String timestamp) {

    public static ErrorResponse de(int status, String error, String mensaje, List<String> detalles) {
        return new ErrorResponse(status, error, mensaje, detalles, LocalDateTime.now().toString());
    }
}
