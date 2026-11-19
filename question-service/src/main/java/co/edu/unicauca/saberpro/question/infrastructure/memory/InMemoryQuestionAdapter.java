package co.edu.unicauca.saberpro.question.infrastructure.memory;

import co.edu.unicauca.saberpro.question.domain.model.Question;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptador secundario de persistencia en memoria (HashMap).
 * Implementa el mismo puerto que JpaQuestionAdapter: es la prueba de
 * intercambiabilidad de la Arquitectura Hexagonal.
 */
public class InMemoryQuestionAdapter implements PreguntaRepositoryPort {

    private final Map<Long, Question> almacen = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public Question guardar(Question pregunta) {
        if (pregunta.getId() == null) {
            pregunta.asignarId(secuencia.incrementAndGet());
        }
        Question copia = copiar(pregunta);
        almacen.put(copia.getId(), copia);
        return copiar(copia);
    }

    @Override
    public Optional<Question> buscarPorId(Long id) {
        return Optional.ofNullable(almacen.get(id)).map(InMemoryQuestionAdapter::copiar);
    }

    @Override
    public List<Question> listarTodas() {
        List<Question> resultado = new ArrayList<>();
        almacen.values().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .forEach(q -> resultado.add(copiar(q)));
        return resultado;
    }

    /** Se guarda una copia para que los cambios fuera del repositorio no se filtren sin llamar a guardar(). */
    private static Question copiar(Question q) {
        return Question.reconstituir(q.getId(), q.getEnunciado(), q.getJustificacion(),
                q.getOpciones(), q.getEstado());
    }
}
