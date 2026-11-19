package co.edu.unicauca.saberpro.question.api;

import co.edu.unicauca.saberpro.question.api.dto.CrearPreguntaRequest;
import co.edu.unicauca.saberpro.question.api.dto.PreguntaResponse;
import co.edu.unicauca.saberpro.question.domain.model.Question;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.ConsultarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaCommand;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Adaptador primario REST. Solo conoce los puertos de entrada (casos de uso),
 * nunca los adaptadores de persistencia ni de mensajería.
 */
@RestController
@RequestMapping("/api/preguntas")
public class QuestionRestController {

    private final CrearPreguntaUseCase crearPregunta;
    private final EvaluarPreguntaUseCase evaluarPregunta;
    private final ConsultarPreguntaUseCase consultarPregunta;

    public QuestionRestController(CrearPreguntaUseCase crearPregunta,
                                  EvaluarPreguntaUseCase evaluarPregunta,
                                  ConsultarPreguntaUseCase consultarPregunta) {
        this.crearPregunta = crearPregunta;
        this.evaluarPregunta = evaluarPregunta;
        this.consultarPregunta = consultarPregunta;
    }

    /** b) Verificación de disponibilidad: 200 OK con la lista de preguntas. */
    @GetMapping
    public List<PreguntaResponse> listar() {
        return consultarPregunta.listarPreguntas().stream().map(PreguntaResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public PreguntaResponse obtener(@PathVariable Long id) {
        return PreguntaResponse.desde(consultarPregunta.obtenerPregunta(id));
    }

    /** c) Creación síncrona: 201 Created (o 400 Bad Request si se viola una regla). */
    @PostMapping
    public ResponseEntity<PreguntaResponse> crear(@RequestBody CrearPreguntaRequest request) {
        CrearPreguntaCommand command = new CrearPreguntaCommand(
                request.enunciado(),
                request.justificacion(),
                request.opciones() == null ? null : request.opciones().stream()
                        .map(o -> o == null ? null : new CrearPreguntaCommand.Opcion(o.texto(), o.correcta()))
                        .toList());
        Question creada = crearPregunta.crearPregunta(command);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(location).body(PreguntaResponse.desde(creada));
    }

    /** d) Envío a revisión asíncrona: 202 Accepted; el mensaje queda en RabbitMQ. */
    @PostMapping("/{id}/enviar-revision")
    public ResponseEntity<Map<String, Object>> enviarARevision(@PathVariable Long id) {
        Question pregunta = evaluarPregunta.enviarARevision(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "mensaje", "Solicitud de revisión despachada a la cola pregunta.revisar.queue",
                "preguntaId", pregunta.getId(),
                "estado", pregunta.getEstado().name()));
    }

    @PostMapping("/{id}/aprobar")
    public PreguntaResponse aprobar(@PathVariable Long id) {
        return PreguntaResponse.desde(evaluarPregunta.aprobar(id));
    }

    @PostMapping("/{id}/rechazar")
    public PreguntaResponse rechazar(@PathVariable Long id) {
        return PreguntaResponse.desde(evaluarPregunta.rechazar(id));
    }
}
