#!/bin/bash

# Script to start Finance Monkey with Google Drive storage

# Kill any running Gradle processes
echo "Killing any running Gradle processes..."
pkill -f "gradle" || echo "No Gradle processes running"
pkill -f "java.*spring" || echo "No Spring applications running"

# Set environment variables for Google Drive access
echo "Setting up environment variables..."
export GOOGLE_APPLICATION_CREDENTIALS="src/main/resources/credentials/credentials.json"

# Check if the Google Drive API is enabled
echo "Checking Google Drive API status..."
echo "Note: If this is your first time running with Drive integration,"
echo "you will be prompted to authorize access in a browser window."

# Start the backend application
echo "Starting Finance Monkey backend..."
./gradlew bootRun --args='--spring.config.name=application --server.port=8086'
