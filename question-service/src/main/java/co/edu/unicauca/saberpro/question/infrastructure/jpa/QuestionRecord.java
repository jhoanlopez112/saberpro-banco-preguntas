package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de persistencia JPA (tabla "preguntas"). Vive SOLO en infraestructura:
 * el dominio nunca ve esta clase; QuestionDomainMapper traduce entre ambos.
 */
@Entity
@Table(name = "preguntas")
public class QuestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String enunciado;

    @Column(nullable = false, length = 2000)
    private String justificacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPregunta estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "opciones_respuesta", joinColumns = @JoinColumn(name = "pregunta_id"))
    @OrderColumn(name = "posicion")
    private List<AnswerOptionRecord> opciones = new ArrayList<>();

    protected QuestionRecord() {
        // requerido por JPA
    }

    public QuestionRecord(Long id, String enunciado, String justificacion,
                          EstadoPregunta estado, List<AnswerOptionRecord> opciones) {
        this.id = id;
        this.enunciado = enunciado;
        this.justificacion = justificacion;
        this.estado = estado;
        this.opciones = new ArrayList<>(opciones);
    }

    public Long getId() {
        return id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public EstadoPregunta getEstado() {
        return estado;
    }

    public List<AnswerOptionRecord> getOpciones() {
        return opciones;
    }

    /** Opción de respuesta embebida en la tabla "opciones_respuesta". */
    @Embeddable
    public static class AnswerOptionRecord {

        @Column(nullable = false, length = 1000)
        private String texto;

        @Column(nullable = false)
        private boolean correcta;

        protected AnswerOptionRecord() {
            // requerido por JPA
        }

        public AnswerOptionRecord(String texto, boolean correcta) {
            this.texto = texto;
            this.correcta = correcta;
        }

        public String getTexto() {
            return texto;
        }

        public boolean isCorrecta() {
            return correcta;
        }
    }
}
