package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de Spring Data JPA usado internamente por JpaQuestionAdapter.
 */
public interface SpringDataQuestionRepository extends JpaRepository<QuestionRecord, Long> {
}
