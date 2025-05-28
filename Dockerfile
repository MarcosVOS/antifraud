FROM openjdk:21-jdk-slim

WORKDIR /app

COPY . .

EXPOSE 8080

VOLUME /app

CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]