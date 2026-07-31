# syntax=docker/dockerfile:1

# =====================================================================
# Stage 1 : build
# =====================================================================
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src/ src/

RUN ./mvnw clean package -DskipTests

# =====================================================================
# Stage 2 : runtime
# =====================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S dottel && adduser -S dottel -G dottel

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

RUN chown dottel:dottel app.jar
USER dottel

EXPOSE 8080

# Variables d'environnement attendues au runtime, injectees par le
# ConfigMap et le Secret Kubernetes (voir k8s/). Volontairement NON
# declarees via ENV : une valeur vide resoudrait le placeholder Spring au
# lieu de le laisser echouer, et DOTTEL_JWT_SECRET vide produirait une
# WeakKeyException opaque plutot qu'une erreur de configuration explicite.
#
#   SPRING_PROFILES_ACTIVE      prod
#   DB_URL, DB_USER, DB_PASSWORD
#   DOTTEL_JWT_SECRET           >= 32 caracteres (HS384)
#   APP_CORS_ALLOWED_ORIGINS    origine exacte du frontend
#   KAFKA_BOOTSTRAP_SERVERS

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]