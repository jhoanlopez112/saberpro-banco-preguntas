package co.edu.unicauca.saberpro.question.infrastructure.messaging;

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
 * Topología AMQP: exchange directo + cola de revisión + binding, y serialización JSON.
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

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
