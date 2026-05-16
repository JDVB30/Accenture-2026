# ─── Etapa 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiamos el pom primero para aprovechar la caché de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y construimos el JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Etapa 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiamos solo el JAR generado
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Dspring.data.mongodb.host=franquicias-mongodb", \
  "-Dspring.data.mongodb.port=27017", \
  "-Dspring.data.mongodb.database=franquicias_db", \
  "-Dspring.data.mongodb.username=admin", \
  "-Dspring.data.mongodb.password=admin123", \
  "-Dspring.data.mongodb.authentication-database=admin", \
  "-jar", "app.jar"]
