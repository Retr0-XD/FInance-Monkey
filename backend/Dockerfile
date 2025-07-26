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

# Create simple entrypoint script
RUN echo '#!/bin/sh' > /app/entrypoint.sh && \
    echo 'set -e' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo 'echo "==============================================="' >> /app/entrypoint.sh && \
    echo 'echo "   Finance Monkey Application - Render Deploy"' >> /app/entrypoint.sh && \
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
    echo '# Start the application' >> /app/entrypoint.sh && \
    echo 'echo "Starting Finance Monkey application..."' >> /app/entrypoint.sh && \
    echo 'exec java -jar app.jar' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

# Adding comprehensive health check with retries
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:10000/actuator/health || exit 1

EXPOSE 10000
ENTRYPOINT ["/app/entrypoint.sh"]
