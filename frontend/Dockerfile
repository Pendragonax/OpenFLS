# Stage 1: Development
# Zweck: Dev-Container
# node:22-alpine (statt 22.12), damit devDeps wie jsdom (>=22.13) die
# Engine-Anforderung erfüllen.
FROM node:22-alpine AS development
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
FROM node:22-alpine AS build
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

# Default-HTML entfernen und Build-Output kopieren.
# Der Angular application builder legt die Assets unter dist/openfls/browser ab;
# proxy/nginx.frontend.conf erwartet sie unter /usr/share/nginx/html/openfls.
RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /app/dist/openfls/browser /usr/share/nginx/html/openfls

# Nginx im Vordergrund starten
ENTRYPOINT ["nginx", "-g", "daemon off;"]
