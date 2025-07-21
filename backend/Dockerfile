FROM gradle:8.4-jdk17 AS build
WORKDIR /app
# Copy the entire project
COPY . .
# Build the application
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
