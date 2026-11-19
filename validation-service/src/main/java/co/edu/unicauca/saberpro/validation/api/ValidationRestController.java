package co.edu.unicauca.saberpro.validation.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * API REST de validación síncrona consumida por question-service.
 *
 * <p>Implementa un pipeline de filtros (Pipes &amp; Filters, heredado del Corte I):
 * cada filtro revisa una regla de redacción y acumula errores. Responde
 * <b>200 OK</b> si la pregunta es válida o <b>400 Bad Request</b> con la lista de errores.</p>
 */
@RestController
@RequestMapping("/api/validaciones")
public class ValidationRestController {

    private static final Logger log = LoggerFactory.getLogger(ValidationRestController.class);

    static final int LONGITUD_MINIMA_ENUNCIADO = 10;
    static final int MAXIMO_OPCIONES = 4;

    public record OpcionDto(String texto, boolean correcta) {
    }

    public record SolicitudValidacion(String enunciado, String justificacion, List<OpcionDto> opciones) {
    }

    public record ResultadoValidacion(boolean valida, List<String> errores) {
    }

    /** Pipeline de filtros de validación (Pipes & Filters). */
    private final List<BiConsumer<SolicitudValidacion, List<String>>> filtros = List.of(
            ValidationRestController::filtroEnunciado,
            ValidationRestController::filtroCantidadOpciones,
            ValidationRestController::filtroOpcionesNoVacias,
            ValidationRestController::filtroOpcionesDuplicadas,
            ValidationRestController::filtroJustificacionDistintaAlEnunciado);

    @PostMapping("/preguntas")
    public ResponseEntity<ResultadoValidacion> validarPregunta(@RequestBody SolicitudValidacion solicitud) {
        List<String> errores = new ArrayList<>();
        if (solicitud == null) {
            errores.add("La solicitud de validación está vacía");
        } else {
            filtros.forEach(f -> f.accept(solicitud, errores));
        }
        boolean valida = errores.isEmpty();
        log.info("[VALIDATION-SERVICE] Validación de pregunta -> valida={} errores={}", valida, errores);
        return ResponseEntity.status(valida ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(new ResultadoValidacion(valida, errores));
    }

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("servicio", "validation-service", "estado", "UP");
    }

    // ------------------------------- Filtros -------------------------------

    static void filtroEnunciado(SolicitudValidacion s, List<String> errores) {
        if (s.enunciado() == null || s.enunciado().trim().length() < LONGITUD_MINIMA_ENUNCIADO) {
            errores.add("El enunciado debe tener al menos " + LONGITUD_MINIMA_ENUNCIADO + " caracteres");
        }
    }

    static void filtroCantidadOpciones(SolicitudValidacion s, List<String> errores) {
        if (s.opciones() == null || s.opciones().isEmpty()) {
            errores.add("La pregunta debe incluir opciones de respuesta");
        } else if (s.opciones().size() > MAXIMO_OPCIONES) {
            errores.add("La pregunta no puede tener más de " + MAXIMO_OPCIONES + " opciones");
        }
    }

    static void filtroOpcionesNoVacias(SolicitudValidacion s, List<String> errores) {
        if (s.opciones() != null && s.opciones().stream()
                .anyMatch(o -> o == null || o.texto() == null || o.texto().isBlank())) {
            errores.add("Ninguna opción de respuesta puede estar vacía");
        }
    }

    static void filtroOpcionesDuplicadas(SolicitudValidacion s, List<String> errores) {
        if (s.opciones() == null) {
            return;
        }
        Set<String> vistos = new HashSet<>();
        for (OpcionDto o : s.opciones()) {
            if (o != null && o.texto() != null && !vistos.add(normalizar(o.texto()))) {
                errores.add("Las opciones de respuesta no pueden repetirse: '" + o.texto().trim() + "'");
                return;
            }
        }
    }

    static void filtroJustificacionDistintaAlEnunciado(SolicitudValidacion s, List<String> errores) {
        if (s.enunciado() != null && s.justificacion() != null
                && normalizar(s.enunciado()).equals(normalizar(s.justificacion()))) {
            errores.add("La justificación no puede ser igual al enunciado");
        }
    }

    private static String normalizar(String texto) {
        return texto.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
