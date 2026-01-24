FROM node:22.12-alpine AS development
ENV NODE_ENV=development

WORKDIR /app
COPY package*.json ./
RUN npm ci
RUN npm install -g @angular/cli@21.1.0
COPY . /app/
EXPOSE 4200 49153
ENTRYPOINT ["sh", "entrypoint.sh"]


FROM node:22.12-alpine AS build

WORKDIR /app
COPY package*.json package-lock.json ./
RUN npm ci
RUN npm install -g @angular/cli@21.1.0
COPY . /app/
RUN ["ng", "build"]


FROM nginx:1.27.0-alpine-slim AS prod

RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /app/dist /usr/share/nginx/html

ENTRYPOINT ["nginx", "-g", "daemon off;"]
