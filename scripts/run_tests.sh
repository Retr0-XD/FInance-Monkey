#!/bin/bash

# Finance Monkey Test Script
# =========================
# This script builds and starts the Finance Monkey backend with Docker Compose,
# then runs tests against the API endpoints

# Configuration
export COMPOSE_PROJECT_NAME=finance-monkey-test

# Text formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colorful status messages
print_status() {
  local status=$1
  local message=$2
  
  if [ "$status" = "SUCCESS" ]; then
    echo -e "${GREEN}[SUCCESS]${NC} $message"
  elif [ "$status" = "FAIL" ]; then
    echo -e "${RED}[FAIL]${NC} $message"
  else
    echo -e "${YELLOW}[$status]${NC} $message"
  fi
}

# Stop any existing containers and cleanup
cleanup() {
  print_status "INFO" "Cleaning up existing containers..."
  docker compose down -v --remove-orphans
}

# Build and start the services
start_services() {
  print_status "INFO" "Building and starting services..."
  docker compose up -d
  
  # Wait for services to be ready
  print_status "INFO" "Waiting for services to start..."
  sleep 15
  
  # Check if services are running
  if [ "$(docker compose ps --status running | grep -c backend)" -eq 0 ]; then
    print_status "FAIL" "Backend service failed to start."
    docker compose logs backend
    cleanup
    exit 1
  fi
  
  print_status "SUCCESS" "Services are up and running."
}

# Run API tests
run_tests() {
  print_status "INFO" "Running API tests..."
  
  # Execute the test script
  bash /workspaces/FInance-Monkey/scripts/test_api.sh
  
  # Check the exit code
  if [ $? -eq 0 ]; then
    print_status "SUCCESS" "API tests completed successfully."
  else
    print_status "FAIL" "API tests failed."
  fi
}

# Main execution
main() {
  print_status "INFO" "Starting Finance Monkey test suite..."
  
  # Clean up before starting
  cleanup
  
  # Start services
  start_services
  
  # Run tests
  run_tests
  
  # Final cleanup
  print_status "INFO" "Tests completed. Cleaning up..."
  cleanup
  
  print_status "INFO" "Test suite completed."
}

# Run the main function
main
