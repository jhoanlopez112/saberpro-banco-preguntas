package co.edu.unicauca.saberpro.question.infrastructure.rest;

import co.edu.unicauca.saberpro.question.domain.model.Question;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Adaptador secundario REST: implementa ValidacionPort invocando de forma
 * síncrona a validation-service (POST /api/validaciones/preguntas).
 * validation-service responde 200 si la pregunta es válida y 400 con la lista de errores si no.
 */
public class ValidationServiceClient implements ValidacionPort {

    private static final Logger log = LoggerFactory.getLogger(ValidationServiceClient.class);

    private final RestClient restClient;

    public ValidationServiceClient(RestClient.Builder builder, String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public ResultadoValidacion validar(Question pregunta) {
        SolicitudValidacion solicitud = new SolicitudValidacion(
                pregunta.getEnunciado().getTexto(),
                pregunta.getJustificacion().getTexto(),
                pregunta.getOpciones().stream()
                        .map(o -> new SolicitudValidacion.Opcion(o.getTexto(), o.isCorrecta()))
                        .toList());
        try {
            RespuestaValidacion respuesta = restClient.post()
                    .uri("/api/validaciones/preguntas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .exchange((request, response) -> {
                        RespuestaValidacion cuerpo = response.bodyTo(RespuestaValidacion.class);
                        if (cuerpo == null) {
                            return new RespuestaValidacion(response.getStatusCode().is2xxSuccessful(), List.of());
                        }
                        return cuerpo;
                    });
            log.info("[QUESTION-SERVICE] validation-service respondió: valida={} errores={}",
                    respuesta.valida(), respuesta.errores());
            return new ResultadoValidacion(respuesta.valida(), respuesta.errores());
        } catch (ResourceAccessException e) {
            throw new ServicioValidacionNoDisponibleException(
                    "No fue posible contactar a validation-service: " + e.getMessage(), e);
        }
    }

    record SolicitudValidacion(String enunciado, String justificacion, List<Opcion> opciones) {
        record Opcion(String texto, boolean correcta) {
        }
    }

    record RespuestaValidacion(boolean valida, List<String> errores) {
    }
}
