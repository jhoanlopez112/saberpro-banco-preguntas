# ---------- Etapa 1: compilación del JAR ejecutable (equivale a ./mvnw clean package -DskipTests) ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa 2: imagen liviana de ejecución (Java 17) ----------
# Nota: la imagen openjdk:17-jdk-slim de la guía fue retirada de Docker Hub;
# eclipse-temurin:17-jre es la distribución oficial equivalente de OpenJDK 17.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
