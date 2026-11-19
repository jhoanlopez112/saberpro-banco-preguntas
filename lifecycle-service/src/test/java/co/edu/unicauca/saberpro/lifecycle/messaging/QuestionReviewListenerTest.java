package co.edu.unicauca.saberpro.lifecycle.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionReviewListenerTest {

    @Test
    void registraLaSolicitudRecibida() {
        QuestionReviewListener listener = new QuestionReviewListener();
        listener.recibirSolicitudRevision(
                new SolicitudRevisionMessage(7L, "¿Qué es Docker?", "PENDIENTE_REVISION", 4, "2026-11-18T10:00"));
        assertEquals(1, listener.getRecibidas().size());
        assertEquals(7L, listener.getRecibidas().get(0).solicitud().preguntaId());
    }

    @Test
    void convierteElJsonDelProductorAunqueTengaOtroTypeId() throws Exception {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setHeader("__TypeId__",
                "co.edu.unicauca.saberpro.question.infrastructure.messaging.SolicitudRevisionEvent");
        props.setInferredArgumentType(SolicitudRevisionMessage.class);
        Message message = new Message("""
                {"preguntaId":3,"enunciado":"Enunciado","estado":"PENDIENTE_REVISION",
                 "numeroOpciones":4,"fechaEnvio":"2026-11-18T10:00"}""".getBytes(StandardCharsets.UTF_8), props);

        Object convertido = new RabbitMQConfig().jsonMessageConverter().fromMessage(message);

        assertEquals(new SolicitudRevisionMessage(3L, "Enunciado", "PENDIENTE_REVISION", 4, "2026-11-18T10:00"),
                convertido);
    }
}
