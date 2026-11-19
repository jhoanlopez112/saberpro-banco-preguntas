# Banco de Preguntas Saber PRO — Taller 13 (Entrega Final, Fase II)

**Laboratorio de Ingeniería de Software II — Universidad del Cauca (2026.2)**
**Autor:** Jhoan Santiago Lopez

Ecosistema de microservicios Spring Boot (Java 17) con **DDD táctico**, **Arquitectura Hexagonal**
(puertos y adaptadores), mensajería **AMQP con RabbitMQ** y despliegue con **Dockerfile + Docker Compose**.

| Contenedor | Puerto | Responsabilidad |
|---|---|---|
| `saberpro-validation-service` | 8082 | Validación síncrona (REST) de la redacción de las preguntas (pipeline Pipes & Filters) |
| `saberpro-question-service` | 8083 | Microservicio principal: dominio puro, casos de uso, adaptadores JPA / memoria / REST / AMQP |
| `saberpro-lifecycle-service` | 8084 | Consumidor `@RabbitListener` de la cola `pregunta.revisar.queue` |
| `saberpro-rabbitmq` | 5672 / 15672 | Broker AMQP + consola de administración (`guest` / `guest`) |
| `saberpro-postgres` | interno (5432) | Base de datos de `question-service` dentro de Docker |

Todos los contenedores se comunican por la red bridge **`saberpro-net`** usando el nombre del servicio como DNS.

---

## 1. Ejecución con Docker Compose

Requisito: Docker Desktop (con WSL2 en Windows).

```bash
docker compose up -d --build          # construye las imágenes y levanta todo en segundo plano
docker ps                             # verifica los 5 contenedores
docker logs -f saberpro-question-service
docker logs -f saberpro-lifecycle-service
docker compose down                   # detiene el ecosistema (agregue -v para borrar la BD)
```

Cada `Dockerfile` es **multi-etapa**: la primera etapa ejecuta `mvn clean package -DskipTests`
dentro de una imagen Maven y la segunda copia el JAR (`COPY ... *.jar app.jar`) a una imagen liviana
de Java 17. Así `docker compose up -d --build` funciona sin tener Maven ni Java instalados.

> La guía sugiere `openjdk:17-jdk-slim`; esa imagen fue **retirada de Docker Hub**, por lo que se usa
> `eclipse-temurin:17-jre`, la distribución oficial equivalente de OpenJDK 17.

### Ejecución local sin Docker (opcional)

```bash
cd validation-service && ./mvnw spring-boot:run      # :8082
cd question-service   && ./mvnw spring-boot:run      # :8083 (H2 en memoria; requiere RabbitMQ en localhost)
cd lifecycle-service  && ./mvnw spring-boot:run      # :8084
```

Generar solo el JAR: `./mvnw clean package -DskipTests` (en Windows: `mvnw.cmd`).

---

## 2. Pruebas de la API (Thunder Client / Postman)

| # | Petición | Resultado esperado |
|---|---|---|
| b | `GET http://localhost:8083/api/preguntas` | **200 OK** con la lista |
| c | `POST http://localhost:8083/api/preguntas` (cuerpo válido) | **201 Created** + `id` asignado |
| c' | `POST http://localhost:8083/api/preguntas` (justificación < 15 caracteres u opciones repetidas) | **400 Bad Request** con los errores |
| d | `POST http://localhost:8083/api/preguntas/{id}/enviar-revision` | **202 Accepted** — mensaje enviado a RabbitMQ |
| d' | `POST .../{id}/enviar-revision` con una pregunta de 3 opciones o 2 claves | **400 Bad Request** (invariante del agregado) |
| e | `http://localhost:15672` (guest/guest) → Queues → `pregunta.revisar.queue` | Mensaje publicado y consumido |
| e' | `GET http://localhost:8084/api/revisiones` | Solicitudes de revisión recibidas por lifecycle-service |

Cuerpo de ejemplo para crear una pregunta:

```json
{
  "enunciado": "¿Qué patrón arquitectónico aísla el dominio mediante puertos y adaptadores?",
  "justificacion": "La arquitectura hexagonal separa el núcleo de negocio de la infraestructura.",
  "opciones": [
    { "texto": "Arquitectura Hexagonal", "correcta": true },
    { "texto": "Singleton", "correcta": false },
    { "texto": "Modelo Vista Controlador", "correcta": false },
    { "texto": "Observer", "correcta": false }
  ]
}
```

En la carpeta [`thunder-tests/`](thunder-tests/) hay una colección importable en Thunder Client con todos los casos.

Otros endpoints: `GET /api/preguntas/{id}`, `POST /api/preguntas/{id}/aprobar`,
`POST /api/preguntas/{id}/rechazar`, `POST http://localhost:8082/api/validaciones/preguntas`.

---

## 3. Arquitectura Hexagonal de `question-service`

```
question-service/src/main/java/co/edu/unicauca/saberpro/question/
├── domain/                      NÚCLEO PURO (sin Spring ni JPA)
│   ├── model/                   Question (Raíz del Agregado), AnswerOption, Enunciado, Justificacion, EstadoPregunta
│   ├── exception/               ReglaNegocioException
│   └── ports/
│       ├── inbound/             CrearPreguntaUseCase, EvaluarPreguntaUseCase, ConsultarPreguntaUseCase
│       └── outbound/            PreguntaRepositoryPort, NotificacionPort, ValidacionPort
├── application/                 QuestionApplicationService (implementa los puertos de entrada)
├── infrastructure/              ADAPTADORES SECUNDARIOS
│   ├── jpa/                     QuestionRecord, QuestionDomainMapper, JpaQuestionAdapter
│   ├── memory/                  InMemoryQuestionAdapter (HashMap)
│   ├── messaging/               RabbitMQPublisher, RabbitMQConfig
│   ├── rest/                    ValidationServiceClient (cliente del validation-service)
│   └── config/                  HexagonalConfig (conexión puertos ↔ adaptadores con @Bean)
└── api/                         ADAPTADOR PRIMARIO: QuestionRestController, GlobalExceptionHandler
```

**Invariantes del dominio**

- `Enunciado`: no nulo ni vacío.
- `Justificacion`: mínimo 15 caracteres → si no, `ReglaNegocioException`.
- `AnswerOption` (OpcionRespuesta): texto obligatorio + booleano de clave correcta.
- `Question`: máximo 4 opciones; solo pasa a `PENDIENTE_REVISION` con **exactamente 4 opciones y 1 sola clave correcta**.

### Prueba de intercambiabilidad (InMemory vs JPA)

`HexagonalConfig` declara dos `@Bean` para `PreguntaRepositoryPort` y elige uno con la propiedad
`saberpro.persistence.adapter` (variable `SABERPRO_PERSISTENCE_ADAPTER`):

```powershell
# PowerShell: recrear solo question-service con el adaptador en memoria
$env:SABERPRO_PERSISTENCE_ADAPTER = "memory"; docker compose up -d question-service
docker logs saberpro-question-service | Select-String HEXAGONAL
# [HEXAGONAL] PreguntaRepositoryPort -> InMemoryQuestionAdapter (HashMap)

# Volver a JPA (PostgreSQL): las preguntas guardadas siguen ahí
$env:SABERPRO_PERSISTENCE_ADAPTER = "jpa"; docker compose up -d question-service
```

En Bash: `SABERPRO_PERSISTENCE_ADAPTER=memory docker compose up -d question-service`.

La prueba automática `IntercambiabilidadAdaptadoresTest` ejecuta el mismo escenario de casos de uso
con ambos adaptadores sin modificar el dominio:

```bash
cd question-service && ./mvnw test
```

---

## 4. Pruebas automatizadas

| Servicio | Pruebas |
|---|---|
| question-service | 28 (dominio, objetos de valor, casos de uso, intercambiabilidad JPA/memoria) |
| validation-service | 3 (200 válida / 400 inválida) |
| lifecycle-service | 2 (consumo y conversión del mensaje AMQP) |
