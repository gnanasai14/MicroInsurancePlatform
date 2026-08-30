# Multi-stage build - self-contained module (vendors its own copy of
# `common`), so the build context is just this directory.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml pom.xml
COPY src src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/pricing-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
