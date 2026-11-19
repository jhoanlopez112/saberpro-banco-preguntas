package co.edu.unicauca.saberpro.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Validación (:8082). Aplica las reglas de calidad de redacción
 * (pipeline de validación heredado de la HU03 del Corte I) antes de que una
 * pregunta ingrese al banco.
 */
@SpringBootApplication
public class ValidationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidationServiceApplication.class, args);
    }
}
