package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import co.edu.unicauca.saberpro.question.domain.model.AnswerOption;
import co.edu.unicauca.saberpro.question.domain.model.Enunciado;
import co.edu.unicauca.saberpro.question.domain.model.Justificacion;
import co.edu.unicauca.saberpro.question.domain.model.Question;

/**
 * Traductor (patrón Mapper) entre el agregado de dominio puro y el modelo JPA.
 */
public class QuestionDomainMapper {

    public QuestionRecord toRecord(Question pregunta) {
        return new QuestionRecord(
                pregunta.getId(),
                pregunta.getEnunciado().getTexto(),
                pregunta.getJustificacion().getTexto(),
                pregunta.getEstado(),
                pregunta.getOpciones().stream()
                        .map(o -> new QuestionRecord.AnswerOptionRecord(o.getTexto(), o.isCorrecta()))
                        .toList());
    }

    public Question toDomain(QuestionRecord record) {
        return Question.reconstituir(
                record.getId(),
                new Enunciado(record.getEnunciado()),
                new Justificacion(record.getJustificacion()),
                record.getOpciones().stream()
                        .map(o -> new AnswerOption(o.getTexto(), o.isCorrecta()))
                        .toList(),
                record.getEstado());
    }
}
