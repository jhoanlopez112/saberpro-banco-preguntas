package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import co.edu.unicauca.saberpro.question.domain.model.Question;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador secundario de persistencia basado en Spring Data JPA
 * (H2 en local, PostgreSQL dentro de Docker Compose).
 */
public class JpaQuestionAdapter implements PreguntaRepositoryPort {

    private final SpringDataQuestionRepository repositorio;
    private final QuestionDomainMapper mapper;

    public JpaQuestionAdapter(SpringDataQuestionRepository repositorio, QuestionDomainMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Question guardar(Question pregunta) {
        QuestionRecord guardado = repositorio.save(mapper.toRecord(pregunta));
        pregunta.asignarId(guardado.getId());
        return mapper.toDomain(guardado);
    }

    @Override
    public Optional<Question> buscarPorId(Long id) {
        return repositorio.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Question> listarTodas() {
        return repositorio.findAll(Sort.by("id")).stream().map(mapper::toDomain).toList();
    }
}
