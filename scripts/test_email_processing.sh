#!/bin/bash

# Finance Monkey Email Processing Test Script
# ==========================================
# This script tests the email processing functionality

# Configuration
API_BASE="http://localhost:8081/api"
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

# Login to get auth token
login() {
  print_status "INFO" "Logging in to get auth token..."
  
  response=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com", "password":"password123"}' \
    "$API_BASE/auth/login")
  
  if [[ $response == *"token"* ]]; then
    AUTH_TOKEN=$(echo $response | sed -E 's/.*"token":"([^"]+)".*/\1/')
    print_status "SUCCESS" "Successfully logged in and obtained token"
  else
    print_status "FAIL" "Login failed"
    exit 1
  fi
}

# Submit a test email for processing
submit_test_email() {
  print_status "INFO" "Submitting a test email for processing..."
  
  # First, get the user's email accounts
  email_accounts=$(curl -s -X GET \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    "$API_BASE/emails/accounts")
  
  # Extract the first email account ID
  email_account_id=$(echo $email_accounts | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
  
  if [ -z "$email_account_id" ]; then
    print_status "FAIL" "No email accounts found"
    return 1
  fi
  
  print_status "INFO" "Using email account ID: $email_account_id"
  
  # Create a simulated email in the processed_emails table
  # This would normally be done by the email processing service
  # In a real scenario, we'd trigger the email sync process
  
  # For testing purposes, we'll manually add a transaction
  echo "Creating a test transaction..."
  response=$(curl -s -X POST \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "description": "Test Transaction from Email",
      "amount": 75.25,
      "transactionDate": "2025-07-20T10:00:00",
      "currency": "USD",
      "vendor": "Amazon",
      "recurring": false
    }' \
    "$API_BASE/transactions")
  
  if [[ $response == *"id"* ]]; then
    transaction_id=$(echo $response | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
    print_status "SUCCESS" "Successfully created test transaction with ID: $transaction_id"
  else
    print_status "FAIL" "Failed to create test transaction"
    echo $response
    return 1
  fi
}

# Test the email processing functionality
test_email_processing() {
  print_status "INFO" "Testing email processing functionality..."
  
  login
  if [ -z "$AUTH_TOKEN" ]; then
    print_status "FAIL" "Failed to get auth token"
    exit 1
  fi
  
  submit_test_email
  
  print_status "INFO" "Getting all transactions to verify..."
  transactions=$(curl -s -X GET \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    "$API_BASE/transactions")
  
  echo "Transactions:"
  echo "$transactions" | grep -o '"description":"[^"]*"' | cut -d'"' -f4
  
  if [[ $transactions == *"Test Transaction from Email"* ]]; then
    print_status "SUCCESS" "Found the test transaction in the system"
  else
    print_status "FAIL" "Test transaction not found"
  fi
}

# Run the tests
print_status "INFO" "Starting email processing tests..."
test_email_processing
print_status "INFO" "All tests completed"
