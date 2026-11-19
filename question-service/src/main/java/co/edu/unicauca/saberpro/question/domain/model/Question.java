package co.edu.unicauca.saberpro.question.domain.model;

import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del Agregado "Pregunta" del Banco de Preguntas Saber PRO.
 *
 * <p>Es Java puro: no tiene anotaciones de JPA ni de Spring. Encapsula sus
 * opciones de respuesta y protege las invariantes del negocio:</p>
 * <ul>
 *   <li>Una pregunta tiene como máximo {@value #NUMERO_OPCIONES} opciones de respuesta.</li>
 *   <li>Solo puede pasar a {@link EstadoPregunta#PENDIENTE_REVISION} si tiene exactamente
 *       {@value #NUMERO_OPCIONES} opciones y una sola clave marcada como correcta.</li>
 *   <li>Solo una pregunta pendiente de revisión puede aprobarse o rechazarse.</li>
 * </ul>
 */
public class Question {

    public static final int NUMERO_OPCIONES = 4;

    private Long id;
    private final Enunciado enunciado;
    private final Justificacion justificacion;
    private final List<AnswerOption> opciones;
    private EstadoPregunta estado;

    private Question(Long id, Enunciado enunciado, Justificacion justificacion,
                     List<AnswerOption> opciones, EstadoPregunta estado) {
        if (enunciado == null) {
            throw new ReglaNegocioException("La pregunta debe tener un enunciado");
        }
        if (justificacion == null) {
            throw new ReglaNegocioException("La pregunta debe tener una justificación");
        }
        if (opciones == null) {
            throw new ReglaNegocioException("La lista de opciones de respuesta no puede ser nula");
        }
        if (opciones.size() > NUMERO_OPCIONES) {
            throw new ReglaNegocioException(
                    "Una pregunta no puede tener más de " + NUMERO_OPCIONES + " opciones de respuesta");
        }
        if (opciones.stream().anyMatch(Objects::isNull)) {
            throw new ReglaNegocioException("Las opciones de respuesta no pueden ser nulas");
        }
        this.id = id;
        this.enunciado = enunciado;
        this.justificacion = justificacion;
        this.opciones = new ArrayList<>(opciones);
        this.estado = Objects.requireNonNull(estado, "estado");
    }

    /**
     * Fábrica: crea una pregunta nueva en estado BORRADOR.
     */
    public static Question crear(Enunciado enunciado, Justificacion justificacion, List<AnswerOption> opciones) {
        return new Question(null, enunciado, justificacion, opciones, EstadoPregunta.BORRADOR);
    }

    /**
     * Reconstruye una pregunta ya existente (usado por los adaptadores de persistencia).
     */
    public static Question reconstituir(Long id, Enunciado enunciado, Justificacion justificacion,
                                        List<AnswerOption> opciones, EstadoPregunta estado) {
        return new Question(id, enunciado, justificacion, opciones, estado);
    }

    /**
     * Cambia el estado a PENDIENTE_REVISION validando las invariantes del agregado.
     */
    public void enviarARevision() {
        if (estado != EstadoPregunta.BORRADOR && estado != EstadoPregunta.RECHAZADA) {
            throw new ReglaNegocioException(
                    "Solo una pregunta en BORRADOR o RECHAZADA puede enviarse a revisión (estado actual: " + estado + ")");
        }
        if (opciones.size() != NUMERO_OPCIONES) {
            throw new ReglaNegocioException(
                    "La pregunta debe tener exactamente " + NUMERO_OPCIONES
                            + " opciones de respuesta para enviarse a revisión (tiene " + opciones.size() + ")");
        }
        long clavesCorrectas = opciones.stream().filter(AnswerOption::isCorrecta).count();
        if (clavesCorrectas != 1) {
            throw new ReglaNegocioException(
                    "La pregunta debe tener exactamente 1 clave correcta para enviarse a revisión (tiene "
                            + clavesCorrectas + ")");
        }
        this.estado = EstadoPregunta.PENDIENTE_REVISION;
    }

    public void aprobar() {
        exigirPendienteDeRevision("aprobarse");
        this.estado = EstadoPregunta.APROBADA;
    }

    public void rechazar() {
        exigirPendienteDeRevision("rechazarse");
        this.estado = EstadoPregunta.RECHAZADA;
    }

    private void exigirPendienteDeRevision(String accion) {
        if (estado != EstadoPregunta.PENDIENTE_REVISION) {
            throw new ReglaNegocioException(
                    "Solo una pregunta PENDIENTE_REVISION puede " + accion + " (estado actual: " + estado + ")");
        }
    }

    /**
     * Asigna la identidad generada por el repositorio. Solo puede hacerse una vez.
     */
    public void asignarId(Long id) {
        if (this.id != null && !this.id.equals(id)) {
            throw new ReglaNegocioException("La identidad de una pregunta no puede cambiar");
        }
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Enunciado getEnunciado() {
        return enunciado;
    }

    public Justificacion getJustificacion() {
        return justificacion;
    }

    /** Copia inmodificable: nadie fuera del agregado puede alterar sus opciones. */
    public List<AnswerOption> getOpciones() {
        return List.copyOf(opciones);
    }

    public EstadoPregunta getEstado() {
        return estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }
}
