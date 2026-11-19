package co.edu.unicauca.saberpro.question.infrastructure.config;

import co.edu.unicauca.saberpro.question.application.QuestionApplicationService;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionPort;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.QuestionDomainMapper;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.SpringDataQuestionRepository;
import co.edu.unicauca.saberpro.question.infrastructure.memory.InMemoryQuestionAdapter;
import co.edu.unicauca.saberpro.question.infrastructure.messaging.RabbitMQPublisher;
import co.edu.unicauca.saberpro.question.infrastructure.rest.ValidationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuración de la Arquitectura Hexagonal: aquí (y solo aquí) se conectan los
 * puertos del dominio con sus adaptadores concretos.
 *
 * <p><b>Prueba de intercambiabilidad:</b> el adaptador de persistencia se elige con la
 * propiedad {@code saberpro.persistence.adapter} (variable de entorno
 * {@code SABERPRO_PERSISTENCE_ADAPTER}): {@code jpa} (por defecto) o {@code memory}.
 * Cambiar el {@code @Bean} no requiere tocar ni una línea del dominio ni de la aplicación.</p>
 */
@Configuration
public class HexagonalConfig {

    private static final Logger log = LoggerFactory.getLogger(HexagonalConfig.class);

    // ---------- Puerto de salida: persistencia (adaptadores intercambiables) ----------

    @Bean
    @ConditionalOnProperty(name = "saberpro.persistence.adapter", havingValue = "jpa", matchIfMissing = true)
    public PreguntaRepositoryPort jpaQuestionAdapter(SpringDataQuestionRepository repositorio) {
        log.info("[HEXAGONAL] PreguntaRepositoryPort -> JpaQuestionAdapter (Spring Data JPA)");
        return new JpaQuestionAdapter(repositorio, new QuestionDomainMapper());
    }

    @Bean
    @ConditionalOnProperty(name = "saberpro.persistence.adapter", havingValue = "memory")
    public PreguntaRepositoryPort inMemoryQuestionAdapter() {
        log.info("[HEXAGONAL] PreguntaRepositoryPort -> InMemoryQuestionAdapter (HashMap)");
        return new InMemoryQuestionAdapter();
    }

    // ---------- Puertos de salida: notificación y validación ----------

    @Bean
    public NotificacionPort notificacionPort(RabbitTemplate rabbitTemplate) {
        return new RabbitMQPublisher(rabbitTemplate);
    }

    @Bean
    public ValidacionPort validacionPort(RestClient.Builder builder,
                                         @Value("${saberpro.validation-service.url}") String url) {
        return new ValidationServiceClient(builder, url);
    }

    // ---------- Servicio de aplicación (implementa los puertos de entrada) ----------

    @Bean
    public QuestionApplicationService questionApplicationService(PreguntaRepositoryPort repositorio,
                                                                 NotificacionPort notificador,
                                                                 ValidacionPort validador) {
        return new QuestionApplicationService(repositorio, notificador, validador);
    }
}
