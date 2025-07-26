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

# Create entrypoint script with improved diagnostics
RUN echo '#!/bin/sh' > /app/entrypoint.sh && \
    echo 'set -e' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Print banner' >> /app/entrypoint.sh && \
    echo 'echo "==============================================="' >> /app/entrypoint.sh && \
    echo 'echo "   Finance Monkey Application - Render Deploy"' >> /app/entrypoint.sh && \
    echo 'echo "==============================================="' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Log system and environment information' >> /app/entrypoint.sh && \
    echo 'echo "Container hostname: $(hostname)"' >> /app/entrypoint.sh && \
    echo 'echo "Container IP: $(hostname -I || echo \"unavailable\")"' >> /app/entrypoint.sh && \
    echo 'echo "Java version: $(java -version 2>&1 | head -1)"' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Test DNS resolution' >> /app/entrypoint.sh && \
    echo 'echo "Testing DNS resolution..."' >> /app/entrypoint.sh && \
    echo 'echo "Google DNS: $(dig +short google.com || echo \"unavailable\")"' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Extract database host for testing' >> /app/entrypoint.sh && \
    echo 'if [ -n "$DATABASE_URL" ]; then' >> /app/entrypoint.sh && \
    echo '  masked_url=$(echo "$DATABASE_URL" | sed "s/:[^:@]*@/:*****@/g")' >> /app/entrypoint.sh && \
    echo '  echo "Database connection string format: $masked_url"' >> /app/entrypoint.sh && \
    echo '  # Try to extract and test the database hostname' >> /app/entrypoint.sh && \
    echo '  if echo "$DATABASE_URL" | grep -q "@"; then' >> /app/entrypoint.sh && \
    echo '    db_host=$(echo "$DATABASE_URL" | sed -E "s/^.*@([^:/]+).*$/\\1/")' >> /app/entrypoint.sh && \
    echo '    echo "Database hostname: $db_host"' >> /app/entrypoint.sh && \
    echo '    echo "Attempting to resolve database hostname..."' >> /app/entrypoint.sh && \
    echo '    if host "$db_host" > /dev/null 2>&1; then' >> /app/entrypoint.sh && \
    echo '      echo "✅ Database hostname resolved successfully: $(host $db_host | head -1)"' >> /app/entrypoint.sh && \
    echo '    else' >> /app/entrypoint.sh && \
    echo '      echo "❌ Failed to resolve database hostname: $db_host"' >> /app/entrypoint.sh && \
    echo '      echo "This is expected in Render Docker containers - the application will retry connections"' >> /app/entrypoint.sh && \
    echo '    fi' >> /app/entrypoint.sh && \
    echo '  fi' >> /app/entrypoint.sh && \
    echo 'else' >> /app/entrypoint.sh && \
    echo '  echo "⚠️ DATABASE_URL environment variable not set"' >> /app/entrypoint.sh && \
    echo 'fi' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Log network information' >> /app/entrypoint.sh && \
    echo 'echo "Network interfaces:"' >> /app/entrypoint.sh && \
    echo 'ip addr show | grep -E "inet|eth|lo" || echo "Unable to get network interfaces"' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Set app start time for diagnostics' >> /app/entrypoint.sh && \
    echo 'export JAVA_OPTS="-Dapp.startTime=$(date +%s) $JAVA_OPTS"' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo '# Start the application in resilient mode' >> /app/entrypoint.sh && \
    echo 'echo "Starting application with database-resilient mode enabled"' >> /app/entrypoint.sh && \
    echo 'echo "The application will retry database connections in the background"' >> /app/entrypoint.sh && \
    echo 'echo "==============================================="' >> /app/entrypoint.sh && \
    echo 'exec java $JAVA_OPTS -jar app.jar' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

# Adding comprehensive health check with retries
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:10000/actuator/health || exit 1

EXPOSE 10000
ENTRYPOINT ["/app/entrypoint.sh"]
