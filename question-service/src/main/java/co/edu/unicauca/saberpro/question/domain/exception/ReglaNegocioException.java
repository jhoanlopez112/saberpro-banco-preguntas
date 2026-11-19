package co.edu.unicauca.saberpro.question.domain.exception;

import java.util.List;

/**
 * Excepción del dominio que indica la violación de una regla de negocio (invariante).
 * Es Java puro: no depende de ningún framework.
 */
public class ReglaNegocioException extends RuntimeException {

    private final List<String> errores;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
        this.errores = List.of(mensaje);
    }

    public ReglaNegocioException(String mensaje, List<String> errores) {
        super(mensaje);
        this.errores = List.copyOf(errores);
    }

    public List<String> getErrores() {
        return errores;
    }
}
