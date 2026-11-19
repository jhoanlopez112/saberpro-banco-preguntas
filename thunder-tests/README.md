# Pruebas integradas de la API

- **Thunder Client (VS Code):** icono del rayo → *Collections* → menú `≡` → *Import* → `thunder-collection_saberpro.json`.
- La petición **c)** guarda el `id` creado en la variable `{{preguntaId}}`, que usa la petición **d)**.
  Si su versión de Thunder Client no guarda la variable, reemplace `{{preguntaId}}` por el `id` devuelto.

Orden sugerido (con `docker compose up -d --build` ya ejecutado):

| Petición | Esperado |
|---|---|
| b) GET /api/preguntas | 200 OK |
| c) POST /api/preguntas | 201 Created |
| c') POST justificación corta | 400 Bad Request (dominio) |
| c'') POST opciones repetidas | 400 Bad Request (validation-service) |
| d) POST /api/preguntas/{id}/enviar-revision | 202 Accepted |
| e) GET http://localhost:8084/api/revisiones | 200 OK con la revisión recibida por RabbitMQ |

Luego abra `http://localhost:15672` (guest / guest) → *Queues* → `pregunta.revisar.queue`.
