FROM gradle:8.4-jdk17 AS build
WORKDIR /app
# Copy gradle configuration files first for better layer caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
# Copy the gradle wrapper
COPY gradlew ./
# Make sure the gradle wrapper is executable
RUN chmod +x gradlew
# Copy source code
COPY src ./src
# Build the application
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# Adding health check
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/api/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
