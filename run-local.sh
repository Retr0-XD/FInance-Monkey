#!/bin/bash
# Finance Monkey - Build, Test and Run Script
# This script orchestrates the entire process of building, testing and running the Finance Monkey application

# Terminal colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Set error handling
set -e

# Print section header
print_section() {
    echo -e "\n${BLUE}======================================================${NC}"
    echo -e "${BLUE}   $1${NC}"
    echo -e "${BLUE}======================================================${NC}\n"
}

# Print success message
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Print warning message
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Print error message
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function to check if Docker is installed and running
check_docker() {
    print_section "Checking Docker Environment"
    
    # Check if Docker is installed
    if ! command -v docker &> /dev/null; then
        print_error "Docker not found. Please install Docker and try again."
        exit 1
    else
        print_success "Docker is installed."
    fi
    
    # Check if Docker is running
    if ! docker info &> /dev/null; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    else
        print_success "Docker is running."
    fi
    
    # Check if Docker Compose is installed
    if ! command -v docker compose &> /dev/null; then
        print_warning "Docker Compose not found as 'docker compose'. Checking for docker-compose..."
        if ! command -v docker-compose &> /dev/null; then
            print_error "Docker Compose not found. Please install Docker Compose and try again."
            exit 1
        else
            print_success "Docker Compose (legacy) is installed."
            DOCKER_COMPOSE="docker-compose"
        fi
    else
        print_success "Docker Compose is installed."
        DOCKER_COMPOSE="docker compose"
    fi
}

# Function to check Google Drive credentials
check_google_credentials() {
    print_section "Checking Google Credentials"
    
    CREDS_FILE="./backend/src/main/resources/credentials/credentials.json"
    
    if [ -f "$CREDS_FILE" ]; then
        print_success "Google credentials file found at $CREDS_FILE"
    else
        print_warning "Google credentials file not found at $CREDS_FILE"
        print_warning "Creating directory for credentials"
        mkdir -p ./backend/src/main/resources/credentials
        
        echo -e "You'll need to provide Google API credentials for Drive integration.\n"
        echo -e "Would you like to:"
        echo -e "1. Create a sample credentials file (for testing only)"
        echo -e "2. Skip this step (Drive features will not work)"
        
        read -p "Choose option (1/2): " option
        
        if [ "$option" == "1" ]; then
            echo '{
  "installed": {
    "client_id": "YOUR_CLIENT_ID",
    "project_id": "finance-monkey",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
    "client_secret": "YOUR_CLIENT_SECRET",
    "redirect_uris": ["urn:ietf:wg:oauth:2.0:oob", "http://localhost"]
  }
}' > "$CREDS_FILE"
            print_success "Sample credentials file created. Please update with real credentials before using Drive features."
        else
            print_warning "Skipping credentials. Drive features will not work."
            touch "$CREDS_FILE"
            echo '{"installed":{"client_id":"","project_id":"","auth_uri":"","token_uri":"","client_secret":""}}' > "$CREDS_FILE"
        fi
    fi
}

# Function to clean up existing containers and images
cleanup() {
    print_section "Cleaning Up Previous Deployments"
    
    echo "Stopping any existing Finance Monkey containers..."
    $DOCKER_COMPOSE down --remove-orphans
    
    echo "Removing dangling containers and images..."
    docker system prune -f
    
    print_success "Cleanup complete"
}

# Function to build the application
build_application() {
    print_section "Building Finance Monkey Application"
    
    echo "Building with Docker Compose..."
    $DOCKER_COMPOSE build --no-cache
    
    print_success "Build complete"
}

# Function to run tests
run_tests() {
    print_section "Running Tests"
    
    echo "Running backend tests..."
    $DOCKER_COMPOSE run --rm backend ./gradlew test
    
    echo "Running frontend tests..."
    # Uncomment when frontend tests are implemented
    # $DOCKER_COMPOSE run --rm frontend npm test
    
    print_success "Tests complete"
}

# Function to start the application
start_application() {
    print_section "Starting Finance Monkey Application"
    
    echo "Starting all services..."
    $DOCKER_COMPOSE up -d
    
    echo "Waiting for services to be ready..."
    sleep 10
    
    # Check if services are running
    backend_status=$(docker ps --filter "name=finance-monkey-backend" --format "{{.Status}}" | grep -q "Up" && echo "Running" || echo "Stopped")
    frontend_status=$(docker ps --filter "name=finance-monkey-frontend" --format "{{.Status}}" | grep -q "Up" && echo "Running" || echo "Stopped")
    postgres_status=$(docker ps --filter "name=finance-monkey-postgres" --format "{{.Status}}" | grep -q "Up" && echo "Running" || echo "Stopped")
    
    echo -e "\nService Status:"
    echo -e "- Backend:  ${backend_status}"
    echo -e "- Frontend: ${frontend_status}"
    echo -e "- Database: ${postgres_status}"
    
    if [ "$backend_status" == "Running" ] && [ "$frontend_status" == "Running" ] && [ "$postgres_status" == "Running" ]; then
        print_success "All services are running"
        print_section "Finance Monkey is ready!"
        echo "Access the application at http://localhost:3000"
        echo "Backend API available at http://localhost:8080/api"
        echo "API Documentation at http://localhost:8080/api/swagger-ui/index.html"
        echo -e "\nTo stop the application, run: $DOCKER_COMPOSE down"
    else
        print_error "Some services failed to start. Check the logs:"
        echo "Backend logs:  docker logs finance-monkey-backend"
        echo "Frontend logs: docker logs finance-monkey-frontend"
        echo "Database logs: docker logs finance-monkey-postgres"
    fi
}

# Main script execution
echo -e "${BLUE}╔════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                    ║${NC}"
echo -e "${BLUE}║               Finance Monkey Launcher              ║${NC}"
echo -e "${BLUE}║                                                    ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════╝${NC}"

check_docker
check_google_credentials
cleanup
build_application
run_tests
start_application
