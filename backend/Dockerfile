# Stage 1: Development
FROM gradle:8-jdk17 as development
WORKDIR /app
COPY . .
RUN ./gradlew --no-daemon clean build
EXPOSE 8081
CMD ["./gradlew", "--no-daemon", "bootRun"]

# Stage 1: Build
FROM gradle:8-jdk17 as build
WORKDIR /app
COPY . .
RUN ./gradlew --no-daemon clean build

# Stage 2: Prod
FROM eclipse-temurin:17-jdk as prod
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
