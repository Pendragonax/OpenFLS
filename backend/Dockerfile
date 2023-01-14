FROM maven:latest

WORKDIR /app
COPY . .
RUN mvn clean install

EXPOSE 8081

CMD mvn spring-boot:run