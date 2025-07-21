FROM gradle:8.4-jdk17 AS build
WORKDIR /app
# Copy just the build files first for better caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
# Pre-download dependencies
RUN gradle dependencies --no-daemon

# Now copy the source code
COPY src ./src
# Build the application
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
