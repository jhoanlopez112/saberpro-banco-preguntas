package co.edu.unicauca.saberpro.question.domain.ports.inbound;

import java.util.List;

/**
 * Datos de entrada del caso de uso "Crear pregunta" (independiente del protocolo HTTP).
 */
public record CrearPreguntaCommand(String enunciado, String justificacion, List<Opcion> opciones) {

    public record Opcion(String texto, boolean correcta) {
    }
}
