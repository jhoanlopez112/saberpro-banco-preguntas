package co.edu.unicauca.saberpro.question.domain.ports.inbound;

import co.edu.unicauca.saberpro.question.domain.model.Question;

/**
 * Puerto de entrada (driving port): registrar una nueva pregunta en el banco.
 */
public interface CrearPreguntaUseCase {

    Question crearPregunta(CrearPreguntaCommand command);
}
