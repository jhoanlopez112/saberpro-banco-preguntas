package co.edu.unicauca.saberpro.question.application;

/**
 * Se lanza cuando se consulta o se opera sobre una pregunta que no existe.
 */
public class PreguntaNoEncontradaException extends RuntimeException {

    public PreguntaNoEncontradaException(Long id) {
        super("No existe una pregunta con id " + id);
    }
}
