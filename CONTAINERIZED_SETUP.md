# Finance Monkey - Containerized Local Environment

## Overview
This document provides instructions for running Finance Monkey in a containerized local environment. The containerized setup includes:

- PostgreSQL database for data storage
- Spring Boot backend with Google Drive integration
- Next.js frontend with Material UI

## Prerequisites
- Docker and Docker Compose installed
- Git (to clone the repository)
- Google API credentials (for Drive integration features)

## Quick Start
To quickly build and run the entire application stack:

```bash
./run-local.sh
```

This script will:
1. Check Docker environment
2. Verify Google API credentials
3. Clean up any previous deployments
4. Build all containers
5. Run automated tests
6. Start the application stack
7. Verify that all services are running

## Accessing the Application
- **Frontend UI**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **API Documentation**: http://localhost:8080/api/swagger-ui/index.html

## Manual Setup

### 1. Configure Google Drive Integration (Optional)
Place your Google API credentials in:
```
backend/src/main/resources/credentials/credentials.json
```

### 2. Build and Run the Application Stack
```bash
docker-compose build
docker-compose up -d
```

### 3. Verify Services
```bash
docker-compose ps
```

### 4. Stop the Application
```bash
docker-compose down
```

## Architecture

### Backend (Spring Boot)
- **Port**: 8080
- **Context Path**: /api
- **Health Check**: http://localhost:8080/api/actuator/health
- **Database**: PostgreSQL via JPA/Hibernate
- **Authentication**: JWT
- **Email Processing**: JavaMail, IMAP/POP3
- **AI Text Analysis**: Gemini API
- **Storage**: PostgreSQL + Google Drive

### Frontend (Next.js)
- **Port**: 3000
- **Stack**: React, Material-UI/Tailwind CSS
- **State Management**: Redux/Context API
- **Authentication**: NextAuth.js
- **Data Fetching**: From backend API and Google Drive

### Database (PostgreSQL)
- **Port**: 5432
- **Database**: finance_monkey
- **Migrations**: Handled by Flyway

## Data Flow
1. User connects email account
2. Backend fetches emails and parses with AI
3. Transactions are stored in PostgreSQL and Google Drive
4. Frontend fetches processed data for display and analytics

## Troubleshooting

### Checking Logs
```bash
# Backend logs
docker logs finance-monkey-backend

# Frontend logs
docker logs finance-monkey-frontend

# Database logs
docker logs finance-monkey-postgres
```

### Restarting Services
```bash
# Restart a specific service
docker-compose restart backend
docker-compose restart frontend
docker-compose restart postgres

# Restart all services
docker-compose restart
```

### Common Issues

#### Backend Can't Connect to Database
Check if PostgreSQL is running and the connection string is correct:
```bash
docker logs finance-monkey-postgres
docker logs finance-monkey-backend
```

#### Google Drive Integration Not Working
Verify credentials are properly configured:
1. Check if credentials file exists
2. Check backend logs for authentication errors
3. Try re-authorizing by restarting the backend

#### Frontend Can't Connect to Backend
Check if the backend is running and accessible:
```bash
curl http://localhost:8080/api/actuator/health
```

## Development Workflow

### Rebuilding After Code Changes
```bash
docker-compose down
docker-compose build
docker-compose up -d
```

### Running Tests
```bash
# Backend tests
docker-compose run --rm backend ./gradlew test

# Frontend tests (when implemented)
docker-compose run --rm frontend npm test
```
