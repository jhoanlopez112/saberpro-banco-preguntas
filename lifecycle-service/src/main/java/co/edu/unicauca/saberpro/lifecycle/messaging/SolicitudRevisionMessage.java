package co.edu.unicauca.saberpro.lifecycle.messaging;

/**
 * Contrato del mensaje recibido desde la cola pregunta.revisar.queue.
 * (Cada Bounded Context mantiene su propia copia del contrato: no se comparten clases.)
 */
public record SolicitudRevisionMessage(
        Long preguntaId,
        String enunciado,
        String estado,
        int numeroOpciones,
        String fechaEnvio) {
}
