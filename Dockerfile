FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/volleyball-community-backend-0.0.1.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
EXPOSE 8090
