# Multi-stage build - builds common + user-service via the od-mip reactor,
# so `docker compose up --build` works standalone without needing Maven
# installed on the host. Build context must be od-mip/ (the reactor root),
# not user-service/ itself - see docker-compose.yml.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml pom.xml
COPY common common
COPY user-service user-service
RUN mvn -pl user-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/user-service/target/user-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
