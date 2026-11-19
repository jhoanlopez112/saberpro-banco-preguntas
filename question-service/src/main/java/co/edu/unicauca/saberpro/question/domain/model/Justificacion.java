package co.edu.unicauca.saberpro.question.domain.model;

import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;

import java.util.Objects;

/**
 * Objeto de Valor inmutable que explica por qué la clave correcta es correcta.
 * Invariante: longitud mínima de 15 caracteres (sin contar espacios extremos).
 */
public final class Justificacion {

    public static final int LONGITUD_MINIMA = 15;

    private final String texto;

    public Justificacion(String texto) {
        if (texto == null || texto.trim().length() < LONGITUD_MINIMA) {
            throw new ReglaNegocioException(
                    "La justificación debe tener al menos " + LONGITUD_MINIMA + " caracteres");
        }
        this.texto = texto.trim();
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Justificacion other)) return false;
        return texto.equals(other.texto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto);
    }

    @Override
    public String toString() {
        return texto;
    }
}
