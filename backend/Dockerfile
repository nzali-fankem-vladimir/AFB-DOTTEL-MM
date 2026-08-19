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
# declarees via ENV : une valeur vide RESOUDRAIT le placeholder Spring au
# lieu de le laisser echouer, transformant une erreur de configuration
# explicite en panne opaque au premier appel.
#
# Liste alignee sur les variables reellement lues par
# backend/src/main/resources/application*.yml (verifiee au Sprint MM.14).
#
#   -- ConfigMap (k8s/configmap.yaml) --
#   SPRING_PROFILES_ACTIVE              prod
#   DB_URL                              jdbc:postgresql://<service>:5432/<base>
#   APP_CORS_ALLOWED_ORIGINS            origine exacte du frontend, jamais *
#   DOTTEL_KEYCLOAK_ISSUER_URI          URL du realm -- A CONFIRMER DSI (MM.15).
#                                       Sans repli : le demarrage echoue si absente.
#   KAFKA_BOOTSTRAP_SERVERS             <service-kafka>:9092
#   DOTTEL_DOCUMENTS_CHEMIN_STOCKAGE    repertoire du PDF de l'etat mensuel
#   DOTTEL_DOCUMENTS_CHAPITRE_DEFAUT    repli chapitre comptable (defaut 37210100)
#   DOTTEL_NOTIFICATIONS_ENABLED        true|false (false = stub journalisant)
#   DOTTEL_NOTIFICATIONS_EXPEDITEUR     adresse d'envoi -- A CONFIRMER DSI (MM.17)
#   DOTTEL_MAIL_HOST                    serveur SMTP -- A CONFIRMER DSI (MM.17)
#   DOTTEL_MAIL_PORT                    587 par defaut
#
#   -- Secret (k8s/secret.yaml) --
#   DB_USER, DB_PASSWORD
#   DOTTEL_MAIL_USERNAME, DOTTEL_MAIL_PASSWORD
#
# DOTTEL_JWT_SECRET a ete retiree au Sprint MM.14 : Keycloak est l'unique
# emetteur de jetons depuis MM.7, le backend valide via JWKS et ne lit plus
# aucun secret partage (JwtUtil.java n'existe plus).

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]