package co.edu.unicauca.saberpro.question.infrastructure.rest;

/**
 * El microservicio de validación no respondió (caído o inalcanzable en la red).
 */
public class ServicioValidacionNoDisponibleException extends RuntimeException {

    public ServicioValidacionNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
