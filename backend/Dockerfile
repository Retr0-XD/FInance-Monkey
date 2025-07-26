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
# Install curl for health check and netcat for database connection checks
RUN apt-get update && apt-get install -y curl netcat-openbsd && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Create entrypoint script for proper startup
RUN echo '#!/bin/sh' > /app/entrypoint.sh && \
    echo 'set -e' >> /app/entrypoint.sh && \
    echo 'echo "Starting Finance Monkey application..."' >> /app/entrypoint.sh && \
    echo '# Log database connection info for troubleshooting' >> /app/entrypoint.sh && \
    echo 'masked_url=$(echo "$DATABASE_URL" | sed "s/:[^:@]*@/:*****@/g")' >> /app/entrypoint.sh && \
    echo 'echo "Database connection string format: $masked_url"' >> /app/entrypoint.sh && \
    echo '# Start the application without waiting for database' >> /app/entrypoint.sh && \
    echo 'echo "Starting application with database connection retry enabled"' >> /app/entrypoint.sh && \
    echo 'exec java -jar app.jar' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

# Adding health check with correct port
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:10000/api/actuator/health || exit 1
EXPOSE 10000
ENTRYPOINT ["/app/entrypoint.sh"]
