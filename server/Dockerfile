FROM eclipse-temurin:17-jre

WORKDIR /app
COPY server/build/libs/taskmanagement-server.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]