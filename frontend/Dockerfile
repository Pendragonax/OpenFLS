# Stage 1: Development
# Zweck: Dev-Container
FROM node:22.12-alpine AS development
# Build-Args für lokalen Nutzer (Standard: 1000:1000)
ARG UID=1000
ARG GID=1000

# Entwicklungsmodus setzen
ENV NODE_ENV=development

# Arbeitsverzeichnis setzen und Abhängigkeiten installieren
WORKDIR /app
RUN npm install -g @angular/cli@21.1.0
USER ${UID}:${GID}

# Dev-Ports freigeben
EXPOSE 4200 49153
ENTRYPOINT ["sh", "entrypoint.sh"]


# Stage 2: Build
# Zweck: Angular-Build für das Prod-Image
FROM node:22.12-alpine AS build
# Build-Args für lokalen Nutzer (Standard: 1000:1000)
ARG UID=1000
ARG GID=1000

# Arbeitsverzeichnis setzen und Abhängigkeiten installieren
WORKDIR /app
COPY --chown=${UID}:${GID} package*.json package-lock.json ./
RUN npm ci
RUN npm install -g @angular/cli@21.1.0
# Quellcode kopieren (mit korrektem Owner)
COPY --chown=${UID}:${GID} . /app/
RUN chown -R ${UID}:${GID} /app
USER ${UID}:${GID}
# Prod-Build erzeugen
RUN ["ng", "build"]


# Stage 3: Prod
# Zweck: Statisches Nginx-Image für fertige Assets
FROM nginx:1.27.0-alpine-slim AS prod

# Default-HTML entfernen und Build-Output kopieren
RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /app/dist /usr/share/nginx/html

# Nginx im Vordergrund starten
ENTRYPOINT ["nginx", "-g", "daemon off;"]
