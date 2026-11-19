package co.edu.unicauca.saberpro.question.api.dto;

import co.edu.unicauca.saberpro.question.domain.model.Question;

import java.util.List;

/**
 * Representación JSON de una pregunta devuelta por la API REST.
 */
public record PreguntaResponse(Long id, String enunciado, String justificacion,
                               List<OpcionResponse> opciones, String estado) {

    public record OpcionResponse(String texto, boolean correcta) {
    }

    public static PreguntaResponse desde(Question q) {
        return new PreguntaResponse(
                q.getId(),
                q.getEnunciado().getTexto(),
                q.getJustificacion().getTexto(),
                q.getOpciones().stream().map(o -> new OpcionResponse(o.getTexto(), o.isCorrecta())).toList(),
                q.getEstado().name());
    }
}
