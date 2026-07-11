# syntax=docker/dockerfile:1
# Multi-stage build: compile with the JDK, ship on a slim JRE. One executable jar replaces the
# legacy four-EAR / WAF / EJB-container deployment (ADR-0005/0007).

# ---- build stage ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Prime the dependency cache on the (rarely-changing) pom before copying sources.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

COPY src/ src/
# Tests run in CI (they need Docker/embedded Mongo); the image build just packages the jar.
RUN ./mvnw -B -q clean package -DskipTests

# ---- runtime stage -------------------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as an unprivileged user.
RUN useradd -r -u 1001 -g root appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser

EXPOSE 8080

# Container-native health check hits the Actuator liveness endpoint.
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
    CMD ["sh", "-c", "wget -qO- http://localhost:8080/actuator/health/liveness || exit 1"]

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
