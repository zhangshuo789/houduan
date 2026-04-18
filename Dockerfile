FROM openjdk:17-jdk-slim
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY target/volleyball-community-backend-0.0.1.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EXPOSE 8090
