# syntax=docker/dockerfile:1

FROM gradle:8.8-jdk21 AS builder
WORKDIR /workspace
COPY . /workspace
RUN gradle --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
