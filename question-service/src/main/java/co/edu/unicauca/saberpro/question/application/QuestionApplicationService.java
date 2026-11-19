package co.edu.unicauca.saberpro.question.application;

import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.domain.model.AnswerOption;
import co.edu.unicauca.saberpro.question.domain.model.Enunciado;
import co.edu.unicauca.saberpro.question.domain.model.Justificacion;
import co.edu.unicauca.saberpro.question.domain.model.Question;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.ConsultarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaCommand;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionPort;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de aplicación: orquesta los casos de uso. Implementa los puertos de
 * entrada y depende únicamente de puertos de salida (interfaces). No conoce
 * Spring, JPA, HTTP ni RabbitMQ: los adaptadores concretos se inyectan por
 * constructor desde la configuración de infraestructura.
 */
public class QuestionApplicationService
        implements CrearPreguntaUseCase, EvaluarPreguntaUseCase, ConsultarPreguntaUseCase {

    private final PreguntaRepositoryPort repositorio;
    private final NotificacionPort notificador;
    private final ValidacionPort validador;

    public QuestionApplicationService(PreguntaRepositoryPort repositorio,
                                      NotificacionPort notificador,
                                      ValidacionPort validador) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.notificador = Objects.requireNonNull(notificador);
        this.validador = Objects.requireNonNull(validador);
    }

    @Override
    public Question crearPregunta(CrearPreguntaCommand command) {
        if (command == null) {
            throw new ReglaNegocioException("La solicitud de creación no puede ser nula");
        }
        List<AnswerOption> opciones = command.opciones() == null ? List.of()
                : command.opciones().stream()
                .map(o -> {
                    if (o == null) {
                        throw new ReglaNegocioException("Las opciones de respuesta no pueden ser nulas");
                    }
                    return new AnswerOption(o.texto(), o.correcta());
                })
                .toList();

        // 1) Invariantes del dominio (Objetos de Valor + Agregado)
        Question pregunta = Question.crear(
                new Enunciado(command.enunciado()),
                new Justificacion(command.justificacion()),
                opciones);

        // 2) Validación síncrona en el Bounded Context de Validación (REST :8082)
        ValidacionPort.ResultadoValidacion resultado = validador.validar(pregunta);
        if (!resultado.valida()) {
            throw new ReglaNegocioException("La pregunta no superó la validación", resultado.errores());
        }

        // 3) Persistencia a través del puerto de salida (JPA o memoria)
        return repositorio.guardar(pregunta);
    }

    @Override
    public Question enviarARevision(Long preguntaId) {
        Question pregunta = buscar(preguntaId);
        pregunta.enviarARevision();              // regla de negocio dentro del agregado
        Question guardada = repositorio.guardar(pregunta);
        notificador.notificarEnvioARevision(guardada); // evento asíncrono (AMQP)
        return guardada;
    }

    @Override
    public Question aprobar(Long preguntaId) {
        Question pregunta = buscar(preguntaId);
        pregunta.aprobar();
        return repositorio.guardar(pregunta);
    }

    @Override
    public Question rechazar(Long preguntaId) {
        Question pregunta = buscar(preguntaId);
        pregunta.rechazar();
        return repositorio.guardar(pregunta);
    }

    @Override
    public List<Question> listarPreguntas() {
        return repositorio.listarTodas();
    }

    @Override
    public Question obtenerPregunta(Long preguntaId) {
        return buscar(preguntaId);
    }

    private Question buscar(Long id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new PreguntaNoEncontradaException(id));
    }
}
