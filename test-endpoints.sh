#!/bin/bash

# Test swagger UI
echo "Testing Swagger UI..."
curl -v http://localhost:8080/swagger-ui/index.html
echo

# Test the health endpoint
echo "Testing health endpoint..."
curl -v http://localhost:8080/actuator/health
echo

# Test the public endpoints
echo "Testing public endpoints..."
echo "1. Register user:"
curl -v -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password123", "name": "Test User"}'
echo

echo "2. Login user:"
curl -v -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password123"}'
echo
