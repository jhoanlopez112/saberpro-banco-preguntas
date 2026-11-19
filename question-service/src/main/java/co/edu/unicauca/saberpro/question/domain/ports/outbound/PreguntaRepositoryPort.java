package co.edu.unicauca.saberpro.question.domain.ports.outbound;

import co.edu.unicauca.saberpro.question.domain.model.Question;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (driven port) de persistencia del agregado Pregunta.
 * Implementaciones intercambiables: JpaQuestionAdapter e InMemoryQuestionAdapter.
 */
public interface PreguntaRepositoryPort {

    /** Guarda el agregado y devuelve la versión persistida con su identidad asignada. */
    Question guardar(Question pregunta);

    Optional<Question> buscarPorId(Long id);

    List<Question> listarTodas();
}
