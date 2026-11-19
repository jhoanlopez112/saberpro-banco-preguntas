package co.edu.unicauca.saberpro.lifecycle.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara la misma topología que question-service (idempotente) para que el
 * consumidor pueda arrancar aunque el productor aún no haya publicado nada.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "saberpro.preguntas.exchange";
    public static final String COLA_REVISION = "pregunta.revisar.queue";
    public static final String ROUTING_KEY_REVISION = "pregunta.revisar";

    @Bean
    public DirectExchange preguntasExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue colaRevision() {
        return QueueBuilder.durable(COLA_REVISION).build();
    }

    @Bean
    public Binding bindingRevision(Queue colaRevision, DirectExchange preguntasExchange) {
        return BindingBuilder.bind(colaRevision).to(preguntasExchange).with(ROUTING_KEY_REVISION);
    }

    /** Convierte el JSON al tipo del parámetro del listener (ignora el __TypeId__ del productor). */
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
