package co.edu.unicauca.saberpro.question.application;

import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.model.Question;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaCommand;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionPort;
import co.edu.unicauca.saberpro.question.infrastructure.memory.InMemoryQuestionAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Casos de uso probados sin Spring: los puertos de salida se satisfacen con
 * el adaptador en memoria y dobles de prueba (lambdas).
 */
class QuestionApplicationServiceTest {

    private final List<Question> notificadas = new ArrayList<>();
    private ValidacionPort.ResultadoValidacion resultadoValidacion;
    private QuestionApplicationService servicio;

    @BeforeEach
    void setUp() {
        notificadas.clear();
        resultadoValidacion = new ValidacionPort.ResultadoValidacion(true, List.of());
        servicio = new QuestionApplicationService(
                new InMemoryQuestionAdapter(),
                notificadas::add,
                pregunta -> resultadoValidacion);
    }

    static CrearPreguntaCommand comandoValido() {
        return new CrearPreguntaCommand(
                "¿Qué patrón separa el dominio de la infraestructura mediante puertos?",
                "La arquitectura hexagonal define puertos y adaptadores.",
                List.of(new CrearPreguntaCommand.Opcion("Hexagonal", true),
                        new CrearPreguntaCommand.Opcion("Singleton", false),
                        new CrearPreguntaCommand.Opcion("MVC", false),
                        new CrearPreguntaCommand.Opcion("Observer", false)));
    }

    @Test
    @DisplayName("Crear pregunta válida: se persiste con id y estado BORRADOR")
    void crearPregunta() {
        Question creada = servicio.crearPregunta(comandoValido());
        assertNotNull(creada.getId());
        assertEquals(EstadoPregunta.BORRADOR, creada.getEstado());
        assertEquals(1, servicio.listarPreguntas().size());
    }

    @Test
    @DisplayName("Si validation-service rechaza la pregunta no se persiste")
    void crearPreguntaRechazadaPorValidacion() {
        resultadoValidacion = new ValidacionPort.ResultadoValidacion(false, List.of("Opciones duplicadas"));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> servicio.crearPregunta(comandoValido()));
        assertTrue(e.getErrores().contains("Opciones duplicadas"));
        assertTrue(servicio.listarPreguntas().isEmpty());
    }

    @Test
    @DisplayName("Justificación corta: el dominio lanza ReglaNegocioException")
    void justificacionCorta() {
        CrearPreguntaCommand c = new CrearPreguntaCommand("Enunciado válido", "corta", comandoValido().opciones());
        assertThrows(ReglaNegocioException.class, () -> servicio.crearPregunta(c));
    }

    @Test
    @DisplayName("Enviar a revisión cambia el estado y notifica por el puerto de salida")
    void enviarARevision() {
        Question creada = servicio.crearPregunta(comandoValido());
        Question enviada = servicio.enviarARevision(creada.getId());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, enviada.getEstado());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, servicio.obtenerPregunta(creada.getId()).getEstado());
        assertEquals(1, notificadas.size());
    }

    @Test
    @DisplayName("Pregunta inexistente: PreguntaNoEncontradaException")
    void preguntaInexistente() {
        assertThrows(PreguntaNoEncontradaException.class, () -> servicio.enviarARevision(99L));
    }

    @Test
    @DisplayName("Con 3 opciones se crea en borrador pero no puede enviarse a revisión")
    void tresOpcionesNoSeEnvia() {
        CrearPreguntaCommand c = new CrearPreguntaCommand(comandoValido().enunciado(),
                comandoValido().justificacion(), comandoValido().opciones().subList(0, 3));
        Question creada = servicio.crearPregunta(c);
        assertThrows(ReglaNegocioException.class, () -> servicio.enviarARevision(creada.getId()));
        assertTrue(notificadas.isEmpty());
    }
}
