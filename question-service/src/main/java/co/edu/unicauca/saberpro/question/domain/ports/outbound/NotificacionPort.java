package co.edu.unicauca.saberpro.question.domain.ports.outbound;

import co.edu.unicauca.saberpro.question.domain.model.Question;

/**
 * Puerto de salida (driven port) para notificar eventos del dominio a otros
 * Bounded Contexts. Implementado por RabbitMQPublisher (AMQP).
 */
public interface NotificacionPort {

    void notificarEnvioARevision(Question pregunta);
}
