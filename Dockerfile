# ==============================================================================
# Multi-Stage Dockerfile para Despacha Fácil API REST
# Optimizado para Dokploy con Eclipse Temurin 21 (JRE Alpine)
# ==============================================================================

# Stage 1: Build de la aplicación con Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilación del código fuente
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Imagen final ligera de ejecución (JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el jar compilado desde el builder
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
