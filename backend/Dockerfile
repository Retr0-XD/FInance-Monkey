FROM gradle:8.4-jdk17 AS build
WORKDIR /app
# Copy all files
COPY . /app/
# Make sure we have all directories
RUN mkdir -p /app/src/main/java /app/src/main/resources
# List files to debug
RUN ls -la /app && ls -la /app/src || echo "Error listing files, continuing anyway"
# Build directly with Gradle - more robust command
RUN cd /app && gradle bootJar --no-daemon --info

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# Adding health check
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/api/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
