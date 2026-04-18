FROM openjdk:17-jdk-slim
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY *.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EXPOSE 8090
