FROM eclipse-temurin:17-jre

WORKDIR /app
COPY cli/build/libs/taskmanagement-cli.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]