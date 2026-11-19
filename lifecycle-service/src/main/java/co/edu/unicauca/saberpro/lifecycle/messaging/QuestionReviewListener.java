package co.edu.unicauca.saberpro.lifecycle.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Consumidor AMQP (@RabbitListener) de la cola pregunta.revisar.queue.
 * Registra cada solicitud de revisión recibida y la deja disponible para consulta
 * en GET /api/revisiones.
 */
@Component
public class QuestionReviewListener {

    private static final Logger log = LoggerFactory.getLogger(QuestionReviewListener.class);

    public record RevisionRecibida(SolicitudRevisionMessage solicitud, String recibidaEn) {
    }

    private final List<RevisionRecibida> recibidas = Collections.synchronizedList(new ArrayList<>());

    @RabbitListener(queues = RabbitMQConfig.COLA_REVISION)
    public void recibirSolicitudRevision(SolicitudRevisionMessage solicitud) {
        log.info("[LIFECYCLE-SERVICE] Mensaje recibido de '{}' -> pregunta #{} | estado={} | opciones={} | enunciado=\"{}\"",
                RabbitMQConfig.COLA_REVISION, solicitud.preguntaId(), solicitud.estado(),
                solicitud.numeroOpciones(), solicitud.enunciado());
        recibidas.add(new RevisionRecibida(solicitud, LocalDateTime.now().toString()));
        log.info("[LIFECYCLE-SERVICE] Pregunta #{} asignada a la bandeja de revisión del comité académico "
                + "(total en bandeja: {})", solicitud.preguntaId(), recibidas.size());
    }

    public List<RevisionRecibida> getRecibidas() {
        synchronized (recibidas) {
            return List.copyOf(recibidas);
        }
    }
}
