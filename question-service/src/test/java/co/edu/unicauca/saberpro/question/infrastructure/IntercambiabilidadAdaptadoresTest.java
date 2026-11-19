package co.edu.unicauca.saberpro.question.infrastructure;

import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.model.Question;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.ConsultarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaCommand;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionPort;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.question.infrastructure.memory.InMemoryQuestionAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PRUEBA DE INTERCAMBIABILIDAD (Arquitectura Hexagonal).
 *
 * <p>El mismo escenario de casos de uso se ejecuta dos veces, cambiando únicamente
 * la propiedad que decide qué {@code @Bean} implementa {@link PreguntaRepositoryPort}.
 * El dominio y la capa de aplicación no cambian en absoluto.</p>
 */
class IntercambiabilidadAdaptadoresTest {

    /** Escenario común: crear -> listar -> enviar a revisión. */
    abstract static class EscenarioCasosDeUso {

        @Autowired CrearPreguntaUseCase crear;
        @Autowired EvaluarPreguntaUseCase evaluar;
        @Autowired ConsultarPreguntaUseCase consultar;
        @Autowired PreguntaRepositoryPort repositorio;

        @MockBean ValidacionPort validacionPort;
        @MockBean NotificacionPort notificacionPort;

        @BeforeEach
        void validacionSiempreOk() {
            when(validacionPort.validar(any()))
                    .thenReturn(new ValidacionPort.ResultadoValidacion(true, List.of()));
        }

        Question ejecutarEscenario() {
            Question creada = crear.crearPregunta(new CrearPreguntaCommand(
                    "¿Qué contenedor expone el puerto 15672?",
                    "RabbitMQ expone su consola de administración en el 15672.",
                    List.of(new CrearPreguntaCommand.Opcion("RabbitMQ", true),
                            new CrearPreguntaCommand.Opcion("PostgreSQL", false),
                            new CrearPreguntaCommand.Opcion("question-service", false),
                            new CrearPreguntaCommand.Opcion("validation-service", false))));
            assertNotNull(creada.getId());
            assertEquals(EstadoPregunta.BORRADOR, consultar.obtenerPregunta(creada.getId()).getEstado());

            Question enviada = evaluar.enviarARevision(creada.getId());
            assertEquals(EstadoPregunta.PENDIENTE_REVISION, enviada.getEstado());
            assertEquals(EstadoPregunta.PENDIENTE_REVISION,
                    consultar.obtenerPregunta(creada.getId()).getEstado());
            verify(notificacionPort).notificarEnvioARevision(any());
            return enviada;
        }
    }

    @Nested
    @SpringBootTest(properties = "saberpro.persistence.adapter=jpa")
    @DisplayName("Con JpaQuestionAdapter (Spring Data JPA + H2)")
    class ConJpa extends EscenarioCasosDeUso {
        @Test
        void casosDeUsoFuncionanConJpa() {
            assertInstanceOf(JpaQuestionAdapter.class, repositorio);
            ejecutarEscenario();
        }
    }

    @Nested
    @SpringBootTest(properties = "saberpro.persistence.adapter=memory")
    @DisplayName("Con InMemoryQuestionAdapter (HashMap)")
    class ConMemoria extends EscenarioCasosDeUso {
        @Test
        void casosDeUsoFuncionanEnMemoria() {
            assertInstanceOf(InMemoryQuestionAdapter.class, repositorio);
            ejecutarEscenario();
        }
    }
}
