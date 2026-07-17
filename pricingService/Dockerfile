# Simple runtime image - expects the jar already built via:
#   mvn -pl <this-service> -am clean package -DskipTests
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
