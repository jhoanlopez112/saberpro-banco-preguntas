package co.edu.unicauca.saberpro.question.domain.ports.outbound;

import co.edu.unicauca.saberpro.question.domain.model.Question;

import java.util.List;

/**
 * Puerto de salida (driven port) hacia el Bounded Context de Validación.
 * Implementado por ValidationServiceClient (REST síncrono contra validation-service :8082).
 */
public interface ValidacionPort {

    ResultadoValidacion validar(Question pregunta);

    record ResultadoValidacion(boolean valida, List<String> errores) {

        public ResultadoValidacion {
            errores = errores == null ? List.of() : List.copyOf(errores);
        }
    }
}
