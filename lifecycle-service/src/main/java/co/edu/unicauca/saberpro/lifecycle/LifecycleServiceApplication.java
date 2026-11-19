package co.edu.unicauca.saberpro.lifecycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Ciclo de Vida (:8084): recibe de forma asíncrona (AMQP) las
 * solicitudes de revisión publicadas por question-service.
 */
@SpringBootApplication
public class LifecycleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifecycleServiceApplication.class, args);
    }
}
