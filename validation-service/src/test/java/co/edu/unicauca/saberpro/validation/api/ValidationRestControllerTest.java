package co.edu.unicauca.saberpro.validation.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ValidationRestController.class)
class ValidationRestControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void preguntaValidaRetorna200() throws Exception {
        mvc.perform(post("/api/validaciones/preguntas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"enunciado":"¿Cuál es la capital de Colombia?",
                         "justificacion":"Bogotá es la capital desde 1819.",
                         "opciones":[{"texto":"Bogotá","correcta":true},{"texto":"Cali","correcta":false},
                                     {"texto":"Medellín","correcta":false},{"texto":"Pasto","correcta":false}]}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valida").value(true));
    }

    @Test
    void opcionesDuplicadasRetorna400() throws Exception {
        mvc.perform(post("/api/validaciones/preguntas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"enunciado":"¿Cuál es la capital de Colombia?",
                         "justificacion":"Bogotá es la capital desde 1819.",
                         "opciones":[{"texto":"Bogotá","correcta":true},{"texto":"bogotá ","correcta":false},
                                     {"texto":"Medellín","correcta":false},{"texto":"Pasto","correcta":false}]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valida").value(false))
                .andExpect(jsonPath("$.errores[0]").exists());
    }

    @Test
    void enunciadoCortoRetorna400() throws Exception {
        mvc.perform(post("/api/validaciones/preguntas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"enunciado":"¿Qué?","justificacion":"Justificación suficientemente larga",
                         "opciones":[{"texto":"A","correcta":true}]}
                        """))
                .andExpect(status().isBadRequest());
    }
}
