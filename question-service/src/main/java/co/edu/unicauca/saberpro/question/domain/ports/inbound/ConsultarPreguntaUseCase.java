package co.edu.unicauca.saberpro.question.domain.ports.inbound;

import co.edu.unicauca.saberpro.question.domain.model.Question;

import java.util.List;

/**
 * Puerto de entrada (driving port): consultas sobre el banco de preguntas.
 */
public interface ConsultarPreguntaUseCase {

    List<Question> listarPreguntas();

    Question obtenerPregunta(Long preguntaId);
}
