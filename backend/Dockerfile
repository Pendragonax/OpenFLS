# Stage 1: Development
FROM maven:latest as DEVELOPMENT
WORKDIR /app
COPY . .
RUN mvn clean install
EXPOSE 8081
CMD mvn spring-boot:run

# Stage 1: Build
FROM maven:latest AS BUILD
WORKDIR /app
COPY . .
RUN mvn clean install

# Stage 2: Prod
FROM openjdk:23-jdk-slim as PROD
WORKDIR /app
COPY --from=BUILD /app/target/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]