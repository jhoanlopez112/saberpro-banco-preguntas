package co.edu.unicauca.saberpro.question.infrastructure.messaging;

/**
 * Mensaje (contrato de integración AMQP) publicado en la cola pregunta.revisar.queue
 * cuando una pregunta pasa a PENDIENTE_REVISION.
 */
public record SolicitudRevisionEvent(
        Long preguntaId,
        String enunciado,
        String estado,
        int numeroOpciones,
        String fechaEnvio) {
}
