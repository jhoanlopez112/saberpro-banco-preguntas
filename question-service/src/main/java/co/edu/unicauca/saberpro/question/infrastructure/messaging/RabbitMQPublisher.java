package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import co.edu.unicauca.saberpro.question.domain.model.Question;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

/**
 * Adaptador secundario de mensajería: implementa NotificacionPort publicando
 * eventos AMQP en RabbitMQ.
 */
public class RabbitMQPublisher implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void notificarEnvioARevision(Question pregunta) {
        SolicitudRevisionEvent evento = new SolicitudRevisionEvent(
                pregunta.getId(),
                pregunta.getEnunciado().getTexto(),
                pregunta.getEstado().name(),
                pregunta.getOpciones().size(),
                LocalDateTime.now().toString());

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_REVISION, evento);
        log.info("[QUESTION-SERVICE] Solicitud de revisión de la pregunta #{} publicada en '{}'",
                pregunta.getId(), RabbitMQConfig.COLA_REVISION);
    }
}
