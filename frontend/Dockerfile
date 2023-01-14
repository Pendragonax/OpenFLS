FROM node:16-alpine AS DEV

WORKDIR /app

COPY package*.json ./

RUN npm ci

RUN npm install -g @angular/cli

COPY . /app/

EXPOSE 4200 49153

ENTRYPOINT ["sh", "entrypoint.sh"]


FROM node:16-alpine AS BUILD

WORKDIR /app
COPY package*.json package-lock.json ./
RUN npm ci
RUN npm install -g @angular/cli

COPY . /app/

RUN ["ng", "build"]


FROM nginx:1.17.1-alpine AS PROD

RUN rm -rf /usr/share/nginx/html/*
COPY --from=BUILD /app/dist /usr/share/nginx/html

ENTRYPOINT ["nginx", "-g", "daemon off;"]
