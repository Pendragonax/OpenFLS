FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install necessary packages before switching users
RUN apt-get update && apt-get install -y \
    maven \
    git

CMD [ "sh" ]
