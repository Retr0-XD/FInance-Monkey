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
# Install tools for diagnostics and troubleshooting
RUN apt-get update && \
    apt-get install -y curl netcat-openbsd dnsutils iputils-ping net-tools iproute2 && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Create directories for Google Drive integration
RUN mkdir -p /app/credentials /app/tokens && \
    chmod -R 777 /app/tokens

# Create enhanced entrypoint script with Google Drive support
RUN echo '#!/bin/sh' > /app/entrypoint.sh && \
    echo 'set -e' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo 'echo "==============================================="' >> /app/entrypoint.sh && \
    echo 'echo "   Finance Monkey Application - Local Deploy"' >> /app/entrypoint.sh && \
    echo 'echo "==============================================="' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Check if database URL is in PostgreSQL format and needs conversion' >> /app/entrypoint.sh && \
    echo 'if [ -n "$DATABASE_URL" ] && [ "$(echo $DATABASE_URL | cut -c1-12)" = "postgresql://" ]; then' >> /app/entrypoint.sh && \
    echo '  echo "Converting PostgreSQL URL to JDBC format"' >> /app/entrypoint.sh && \
    echo '  # Extract username:password@host:port/dbname' >> /app/entrypoint.sh && \
    echo '  DB_URI=${DATABASE_URL#postgresql://}' >> /app/entrypoint.sh && \
    echo '  # Convert to JDBC format' >> /app/entrypoint.sh && \
    echo '  export DATABASE_URL="jdbc:postgresql://$DB_URI"' >> /app/entrypoint.sh && \
    echo '  # Create masked URL for logging' >> /app/entrypoint.sh && \
    echo '  masked_url=$(echo "$DATABASE_URL" | sed "s/:[^:@]*@/:*****@/g")' >> /app/entrypoint.sh && \
    echo '  echo "Database URL converted: $masked_url"' >> /app/entrypoint.sh && \
    echo 'fi' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Check for Google Drive credentials' >> /app/entrypoint.sh && \
    echo 'if [ -f "$GOOGLE_APPLICATION_CREDENTIALS" ]; then' >> /app/entrypoint.sh && \
    echo '  echo "Google Drive credentials found at $GOOGLE_APPLICATION_CREDENTIALS"' >> /app/entrypoint.sh && \
    echo 'else' >> /app/entrypoint.sh && \
    echo '  echo "WARNING: Google Drive credentials not found at $GOOGLE_APPLICATION_CREDENTIALS"' >> /app/entrypoint.sh && \
    echo '  echo "Some features related to Google Drive may not work"' >> /app/entrypoint.sh && \
    echo 'fi' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Create tokens directory if it doesn\'t exist' >> /app/entrypoint.sh && \
    echo 'if [ ! -d "/app/tokens" ]; then' >> /app/entrypoint.sh && \
    echo '  mkdir -p /app/tokens' >> /app/entrypoint.sh && \
    echo '  chmod -R 777 /app/tokens' >> /app/entrypoint.sh && \
    echo 'fi' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Start the application' >> /app/entrypoint.sh && \
    echo 'echo "Starting Finance Monkey application..."' >> /app/entrypoint.sh && \
    echo 'exec java -jar app.jar' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

# Adding comprehensive health check with retries
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
