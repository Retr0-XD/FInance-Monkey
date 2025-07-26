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
    echo 'echo "Waiting for database to be ready..."' >> /app/entrypoint.sh && \
    echo 'DATABASE_HOST=$(echo ${DATABASE_URL} | sed -n "s/^.*@\\([^:]*\\).*$/\\1/p")' >> /app/entrypoint.sh && \
    echo 'DATABASE_PORT=$(echo ${DATABASE_URL} | sed -n "s/^.*:\\([0-9]*\\)\\/.*$/\\1/p")' >> /app/entrypoint.sh && \
    echo 'if [ -z "$DATABASE_PORT" ]; then DATABASE_PORT=5432; fi' >> /app/entrypoint.sh && \
    echo 'echo "Checking connection to $DATABASE_HOST:$DATABASE_PORT"' >> /app/entrypoint.sh && \
    echo 'count=0' >> /app/entrypoint.sh && \
    echo 'max=30' >> /app/entrypoint.sh && \
    echo 'while ! nc -z $DATABASE_HOST $DATABASE_PORT && [ $count -lt $max ]; do' >> /app/entrypoint.sh && \
    echo '  echo "Waiting for database connection... ($count/$max)"' >> /app/entrypoint.sh && \
    echo '  sleep 2' >> /app/entrypoint.sh && \
    echo '  count=$((count+1))' >> /app/entrypoint.sh && \
    echo 'done' >> /app/entrypoint.sh && \
    echo 'if [ $count -eq $max ]; then' >> /app/entrypoint.sh && \
    echo '  echo "Database connection failed"' >> /app/entrypoint.sh && \
    echo '  exit 1' >> /app/entrypoint.sh && \
    echo 'fi' >> /app/entrypoint.sh && \
    echo 'echo "Database is ready, starting application"' >> /app/entrypoint.sh && \
    echo 'exec java -jar app.jar' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

# Adding health check with correct port
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:10000/api/actuator/health || exit 1
EXPOSE 10000
ENTRYPOINT ["/app/entrypoint.sh"]
