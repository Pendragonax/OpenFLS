FROM node:22-alpine AS DEV

WORKDIR /app
COPY package*.json ./
RUN npm ci
RUN npm install -g @angular/cli@16.2.14
COPY . /app/
EXPOSE 4200 49153
ENTRYPOINT ["sh", "entrypoint.sh"]


FROM node:22-alpine AS BUILD

WORKDIR /app
COPY package*.json package-lock.json ./
RUN npm ci
RUN npm install -g @angular/cli@16.2.14
COPY . /app/
RUN ["ng", "build"]


FROM nginx:1.27.0-alpine-slim AS PROD

RUN rm -rf /usr/share/nginx/html/*
COPY --from=BUILD /app/dist /usr/share/nginx/html

ENTRYPOINT ["nginx", "-g", "daemon off;"]
