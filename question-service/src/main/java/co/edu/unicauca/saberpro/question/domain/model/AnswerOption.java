package co.edu.unicauca.saberpro.question.domain.model;

import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;

import java.util.Objects;

/**
 * Objeto de Valor inmutable "OpcionRespuesta": texto de la opción y un booleano
 * que indica si es la clave correcta.
 * Invariante: el texto de la opción no puede ser nulo ni vacío.
 */
public final class AnswerOption {

    private final String texto;
    private final boolean correcta;

    public AnswerOption(String texto, boolean correcta) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El texto de una opción de respuesta no puede ser vacío");
        }
        this.texto = texto.trim();
        this.correcta = correcta;
    }

    public String getTexto() {
        return texto;
    }

    public boolean isCorrecta() {
        return correcta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnswerOption other)) return false;
        return correcta == other.correcta && texto.equals(other.texto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto, correcta);
    }

    @Override
    public String toString() {
        return (correcta ? "[x] " : "[ ] ") + texto;
    }
}
