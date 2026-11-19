package co.edu.unicauca.saberpro.question.domain.model;

import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del Agregado Pregunta: Java puro, sin Spring.
 */
class QuestionTest {

    private static Question pregunta(List<AnswerOption> opciones) {
        return Question.crear(
                new Enunciado("¿Cuál es la capital de Colombia?"),
                new Justificacion("Bogotá es la capital desde 1819."),
                opciones);
    }

    private static List<AnswerOption> cuatroOpcionesUnaCorrecta() {
        return List.of(
                new AnswerOption("Bogotá", true),
                new AnswerOption("Medellín", false),
                new AnswerOption("Cali", false),
                new AnswerOption("Popayán", false));
    }

    @Test
    @DisplayName("Una pregunta nueva nace en estado BORRADOR y sin id")
    void nuevaPreguntaEnBorrador() {
        Question q = pregunta(cuatroOpcionesUnaCorrecta());
        assertEquals(EstadoPregunta.BORRADOR, q.getEstado());
        assertEquals(null, q.getId());
    }

    @Test
    @DisplayName("Con 4 opciones y 1 clave correcta pasa a PENDIENTE_REVISION")
    void enviarARevisionValida() {
        Question q = pregunta(cuatroOpcionesUnaCorrecta());
        q.enviarARevision();
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, q.getEstado());
    }

    @Test
    @DisplayName("No se envía a revisión con menos de 4 opciones")
    void noEnviaConTresOpciones() {
        Question q = pregunta(cuatroOpcionesUnaCorrecta().subList(0, 3));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, q::enviarARevision);
        assertTrue(e.getMessage().contains("exactamente 4"));
        assertEquals(EstadoPregunta.BORRADOR, q.getEstado());
    }

    @Test
    @DisplayName("No se envía a revisión con 2 claves correctas")
    void noEnviaConDosClaves() {
        Question q = pregunta(List.of(
                new AnswerOption("A", true), new AnswerOption("B", true),
                new AnswerOption("C", false), new AnswerOption("D", false)));
        assertThrows(ReglaNegocioException.class, q::enviarARevision);
        assertEquals(EstadoPregunta.BORRADOR, q.getEstado());
    }

    @Test
    @DisplayName("No se envía a revisión sin clave correcta")
    void noEnviaSinClave() {
        Question q = pregunta(List.of(
                new AnswerOption("A", false), new AnswerOption("B", false),
                new AnswerOption("C", false), new AnswerOption("D", false)));
        assertThrows(ReglaNegocioException.class, q::enviarARevision);
    }

    @Test
    @DisplayName("No se permiten más de 4 opciones")
    void noMasDeCuatroOpciones() {
        List<AnswerOption> cinco = new ArrayList<>(cuatroOpcionesUnaCorrecta());
        cinco.add(new AnswerOption("Pasto", false));
        assertThrows(ReglaNegocioException.class, () -> pregunta(cinco));
    }

    @Test
    @DisplayName("Una pregunta pendiente no puede volver a enviarse a revisión")
    void noReenviaPendiente() {
        Question q = pregunta(cuatroOpcionesUnaCorrecta());
        q.enviarARevision();
        assertThrows(ReglaNegocioException.class, q::enviarARevision);
    }

    @Test
    @DisplayName("Solo una pregunta PENDIENTE_REVISION se aprueba o se rechaza")
    void aprobarYRechazar() {
        Question q = pregunta(cuatroOpcionesUnaCorrecta());
        assertThrows(ReglaNegocioException.class, q::aprobar);
        q.enviarARevision();
        q.rechazar();
        assertEquals(EstadoPregunta.RECHAZADA, q.getEstado());
        q.enviarARevision();
        q.aprobar();
        assertEquals(EstadoPregunta.APROBADA, q.getEstado());
    }

    @Test
    @DisplayName("Las opciones del agregado no pueden modificarse desde afuera")
    void opcionesEncapsuladas() {
        Question q = pregunta(cuatroOpcionesUnaCorrecta());
        assertThrows(UnsupportedOperationException.class,
                () -> q.getOpciones().add(new AnswerOption("Intrusa", true)));
    }
}
