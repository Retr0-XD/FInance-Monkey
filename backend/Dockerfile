FROM gradle:8.4-jdk17 AS build
WORKDIR /app
# Copy entire project
COPY . .
# Check if gradlew exists, if not generate it
RUN if [ ! -f "gradlew" ]; then \
        gradle wrapper --gradle-version 8.4; \
    fi
# Make sure the gradle wrapper is executable
RUN chmod +x gradlew || echo "gradlew not found, trying to proceed anyway"
# Build the application with gradle directly if wrapper fails
RUN ./gradlew bootJar --no-daemon || gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# Adding health check
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/api/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
