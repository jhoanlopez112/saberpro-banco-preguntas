package co.edu.unicauca.saberpro.lifecycle.api;

import co.edu.unicauca.saberpro.lifecycle.messaging.QuestionReviewListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Permite verificar por HTTP qué solicitudes de revisión llegaron por RabbitMQ.
 */
@RestController
@RequestMapping("/api/revisiones")
public class RevisionRestController {

    private final QuestionReviewListener listener;

    public RevisionRestController(QuestionReviewListener listener) {
        this.listener = listener;
    }

    @GetMapping
    public List<QuestionReviewListener.RevisionRecibida> listar() {
        return listener.getRecibidas();
    }
}
