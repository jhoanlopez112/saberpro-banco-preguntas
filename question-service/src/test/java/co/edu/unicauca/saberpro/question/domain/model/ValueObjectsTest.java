package co.edu.unicauca.saberpro.question.domain.model;

import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de los Objetos de Valor inmutables.
 */
class ValueObjectsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Enunciado rechaza texto nulo o vacío")
    void enunciadoInvalido(String texto) {
        assertThrows(ReglaNegocioException.class, () -> new Enunciado(texto));
    }

    @Test
    @DisplayName("Enunciado válido conserva el texto sin espacios extremos")
    void enunciadoValido() {
        assertEquals("¿Qué es DDD?", new Enunciado("  ¿Qué es DDD?  ").getTexto());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Muy corta", "14 caracteres!"})
    @DisplayName("Justificación con menos de 15 caracteres lanza ReglaNegocioException")
    void justificacionCorta(String texto) {
        assertThrows(ReglaNegocioException.class, () -> new Justificacion(texto));
    }

    @Test
    @DisplayName("Justificación de exactamente 15 caracteres es válida")
    void justificacionLimite() {
        assertDoesNotThrow(() -> new Justificacion("123456789012345"));
    }

    @Test
    @DisplayName("OpcionRespuesta exige texto y conserva la marca de clave correcta")
    void opcionRespuesta() {
        assertThrows(ReglaNegocioException.class, () -> new AnswerOption(" ", true));
        AnswerOption o = new AnswerOption("Bogotá", true);
        assertEquals(true, o.isCorrecta());
        assertEquals(new AnswerOption("Bogotá", true), o);
    }
}
