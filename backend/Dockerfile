# Stage 1: Development
FROM gradle:9-jdk21 AS development
WORKDIR /app
COPY . .
RUN ./gradlew --no-daemon clean build
EXPOSE 8081
CMD ["./gradlew", "--no-daemon", "bootRun"]

# Stage 1: Build
FROM gradle:9-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew --no-daemon clean build

# Stage 2: Prod
FROM eclipse-temurin:21-jre AS prod
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
