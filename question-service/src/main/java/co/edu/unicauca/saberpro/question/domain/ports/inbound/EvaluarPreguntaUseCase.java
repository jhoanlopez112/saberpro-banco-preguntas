package co.edu.unicauca.saberpro.question.domain.ports.inbound;

import co.edu.unicauca.saberpro.question.domain.model.Question;

/**
 * Puerto de entrada (driving port): flujo de evaluación / revisión de una pregunta.
 */
public interface EvaluarPreguntaUseCase {

    /** Valida invariantes, cambia a PENDIENTE_REVISION y despacha la solicitud de revisión (asíncrona). */
    Question enviarARevision(Long preguntaId);

    Question aprobar(Long preguntaId);

    Question rechazar(Long preguntaId);
}
