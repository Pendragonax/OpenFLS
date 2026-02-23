# Stage 1: Development
# Zweck: Dev-Container mit Hot-Run (bootRun)
FROM gradle:9-jdk21 AS development
# Build-Args für lokalen Nutzer (Standard: 1000:1000)
ARG UID=1000
ARG GID=1000

# Arbeitsverzeichnis setzen
WORKDIR /app
USER ${UID}:${GID}

# Dev-Port freigeben
EXPOSE 8081
CMD ["./gradlew", "--no-daemon", "bootRun"]


# Stage 2: Build
# Zweck: Artifact-Build für das Prod-Image
FROM gradle:9-jdk21 AS build
# Build-Args für lokalen Nutzer (Standard: 1000:1000)
ARG UID=1000
ARG GID=1000
# Nutzer/Gruppe im Image anlegen, falls nicht vorhanden
RUN set -e; \
    if ! getent group "${GID}" >/dev/null 2>&1; then \
      groupadd -g "${GID}" app; \
    fi; \
    if ! getent passwd "${UID}" >/dev/null 2>&1; then \
      useradd -m -u "${UID}" -g "${GID}" app; \
    fi
# Arbeitsverzeichnis setzen und Quellcode kopieren (mit korrektem Owner)
WORKDIR /app
RUN mkdir -p /app && chown ${UID}:${GID} /app
COPY --chown=${UID}:${GID} . /app/
USER ${UID}:${GID}
# Jar bauen (JWT-Keys werden zur Laufzeit via Compose-Secrets injiziert)
RUN ./gradlew --no-daemon clean build

# Stage 3: Prod
# Zweck: Schlankes Runtime-Image mit JRE
FROM eclipse-temurin:21-jre AS prod
# Build-Args für lokalen Nutzer (Standard: 1000:1000)
ARG UID=1000
ARG GID=1000
# Arbeitsverzeichnis setzen und Jar aus Build-Stage kopieren
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# Log-Verzeichnisse vorbereiten (Owner per numerischer UID/GID)
RUN mkdir -p /logs /app/logs \
    && chown -R "${UID}:${GID}" /logs /app/logs
# Standard-Umgebungsvariablen für Runtime
ENV SESSION_TIMEOUT=36000 \
    MYSQL_DATABASE=openfls \
    MYSQL_HOST=localhost
EXPOSE 8081
# Anwendung nicht als root starten (numerische UID/GID vermeiden passwd-Abhängigkeit)
USER ${UID}:${GID}
CMD ["java", "-jar", "app.jar"]
