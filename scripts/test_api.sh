#!/bin/bash

# Finance Monkey API Test Script
# =============================
# This script tests the various API endpoints of the Finance Monkey application
# It includes tests for authentication, email connections, transactions, and more

# Configuration
# Check if we're running against Docker containers or local instance
if [ -n "$DOCKER_TEST" ]; then
  API_BASE="http://localhost:8080/api"
  echo "Testing against Docker container at $API_BASE"
else
  API_BASE="http://localhost:8080/api"
  echo "Testing against local instance at $API_BASE"
fi
AUTH_TOKEN=""

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

# Function to check if the backend server is up
check_server() {
  print_status "INFO" "Checking if server is running at $API_BASE..."
  
  response=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$API_BASE/actuator/health" 2>/dev/null)
  
  if [ "$response" = "200" ] || [ "$response" = "403" ]; then
    # 403 is acceptable as it may mean the endpoint is secured
    print_status "SUCCESS" "Server is up and running"
    return 0
  else
    print_status "FAIL" "Server is not responding (status code: $response)"
    return 1
  fi
}

# Function to register a new user
test_registration() {
  print_status "INFO" "Testing user registration..."
  
  # First try without settings field to see the default behavior
  echo "Testing registration without settings field:"
  curl -v -X POST \
    -H "Content-Type: application/json" \
    -d '{"username":"testuser", "email":"test@example.com", "password":"password123"}' \
    "$API_BASE/auth/register" 2>&1
  echo -e "\n"
  
  # Then try with settings as proper JSON object
  echo "Testing registration with settings as JSON object:"
  curl -v -X POST \
    -H "Content-Type: application/json" \
    -d '{"username":"testuser2", "email":"test2@example.com", "password":"password123", "settings":{}}' \
    "$API_BASE/auth/register" 2>&1
  echo -e "\n"
  
  # Try with settings as a serialized JSON string (which may be how the entity is expecting it)
  echo "Testing registration with settings as JSON string:"
  curl -v -X POST \
    -H "Content-Type: application/json" \
    -d '{"username":"testuser3", "email":"test3@example.com", "password":"password123", "settings":"{}"}' \
    "$API_BASE/auth/register" 2>&1
  echo -e "\n"
}

# Function to test user login
test_login() {
  print_status "INFO" "Testing user login..."
  
  # Try login with the registered test user
  response=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com", "password":"password123"}' \
    "$API_BASE/auth/login")
  
  # Extract token from response if successful
  if [[ $response == *"token"* ]]; then
    AUTH_TOKEN=$(echo $response | sed -E 's/.*"token":"([^"]+)".*/\1/')
    print_status "SUCCESS" "Successfully logged in and obtained token"
    echo "Token: $AUTH_TOKEN"
  else
    print_status "FAIL" "Login failed"
    echo "Response: $response"
    
    # Try with admin credentials as fallback
    print_status "INFO" "Trying with admin credentials..."
    response=$(curl -s -X POST \
      -H "Content-Type: application/json" \
      -d '{"email":"admin@example.com", "password":"admin123"}' \
      "$API_BASE/auth/login")
    
    if [[ $response == *"token"* ]]; then
      AUTH_TOKEN=$(echo $response | sed -E 's/.*"token":"([^"]+)".*/\1/')
      print_status "SUCCESS" "Successfully logged in with admin credentials"
      echo "Token: $AUTH_TOKEN"
    else
      print_status "FAIL" "Admin login also failed"
      echo "Response: $response"
    fi
  fi
}

# Function to test email connection endpoints
test_email_connection() {
  if [ -z "$AUTH_TOKEN" ]; then
    print_status "SKIP" "Skipping email connection test (no auth token)"
    return
  fi
  
  print_status "INFO" "Testing email connection endpoints..."
  
  # Test getting email connections
  echo "Getting email connections:"
  curl -v -X GET \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    "$API_BASE/emails/accounts" 2>&1
  echo -e "\n"
  
  # Test adding a new email connection
  echo "Adding new email connection:"
  curl -v -X POST \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "emailAddress": "user@gmail.com",
      "accessToken": "dummy-token",
      "refreshToken": "dummy-refresh"
    }' \
    "$API_BASE/emails/connect" 2>&1
  echo -e "\n"
}

# Function to test transaction endpoints
test_transactions() {
  if [ -z "$AUTH_TOKEN" ]; then
    print_status "SKIP" "Skipping transactions test (no auth token)"
    return
  fi
  
  print_status "INFO" "Testing transaction endpoints..."
  
  # Test getting all transactions
  echo "Getting all transactions:"
  curl -v -X GET \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    "$API_BASE/transactions" 2>&1
  echo -e "\n"
  
  # Test getting transaction stats
  echo "Getting transaction stats:"
  curl -v -X GET \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    "$API_BASE/transactions/stats" 2>&1
  echo -e "\n"
  
  # Test creating a transaction
  echo "Creating a new transaction:"
  TRANSACTION_RESPONSE=$(curl -s -X POST \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "description": "Test Transaction",
      "amount": 42.99,
      "transactionDate": "2025-07-20T10:00:00",
      "currency": "USD",
      "vendor": "Test Vendor",
      "categoryId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "recurring": false,
      "recurrencePattern": null
    }' \
    "$API_BASE/transactions")
    
  echo $TRANSACTION_RESPONSE
  
  # Extract the transaction ID
  TRANSACTION_ID=$(echo $TRANSACTION_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
  
  if [ -n "$TRANSACTION_ID" ]; then
    # Test updating the transaction
    echo "Updating transaction $TRANSACTION_ID:"
    curl -v -X PUT \
      -H "Authorization: Bearer $AUTH_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "description": "Updated Test Transaction",
        "amount": 45.99,
        "transactionDate": "2025-07-20T10:00:00",
        "currency": "USD",
        "vendor": "Updated Vendor",
        "categoryId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
        "recurring": false,
        "recurrencePattern": null
      }' \
      "$API_BASE/transactions/$TRANSACTION_ID" 2>&1
    echo -e "\n"
  else
    echo "Failed to create transaction or extract ID"
  fi
}

# Function to test category endpoints
test_categories() {
  if [ -z "$AUTH_TOKEN" ]; then
    print_status "SKIP" "Skipping categories test (no auth token)"
    return
  fi
  
  print_status "INFO" "Testing category endpoints..."
  
  # Test getting all categories
  echo "Getting all categories:"
  curl -v -X GET \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    "$API_BASE/transactions/categories" 2>&1
  echo -e "\n"
  
  # Test creating a new category
  echo "Creating a new category:"
  CATEGORY_RESPONSE=$(curl -s -X POST \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "Test Category",
      "parentCategoryId": null,
      "icon": "test",
      "colorCode": "#123456"
    }' \
    "$API_BASE/transactions/categories")
  
  echo $CATEGORY_RESPONSE
  
  # Extract the category ID
  CATEGORY_ID=$(echo $CATEGORY_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
  
  if [ -n "$CATEGORY_ID" ]; then
    # Test getting subcategories
    echo "Getting subcategories for category $CATEGORY_ID:"
    curl -v -X GET \
      -H "Authorization: Bearer $AUTH_TOKEN" \
      "$API_BASE/transactions/categories/$CATEGORY_ID/subcategories" 2>&1
    echo -e "\n"
  else
    echo "Failed to create category or extract ID"
  fi
}

# Main function to run all tests
run_all_tests() {
  print_status "INFO" "Starting Finance Monkey API tests..."
  
  # First check if server is running
  check_server
  if [ $? -ne 0 ]; then
    print_status "FAIL" "Server check failed. Make sure the backend is running."
    exit 1
  fi
  
  # Run all tests
  test_registration
  test_login
  test_email_connection
  test_transactions
  test_categories
  
  print_status "INFO" "All tests completed."
}

# Run the tests
run_all_tests
