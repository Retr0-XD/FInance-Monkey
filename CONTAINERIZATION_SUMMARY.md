# Finance Monkey - Containerization Project

## Overview
This document summarizes the containerization work performed on the Finance Monkey project to enable local testing and deployment.

## Files Created/Modified

### Docker Configuration
- `frontend/Dockerfile` - Created a multi-stage Docker build for the Next.js frontend
- `backend/Dockerfile` - Updated with Google Drive integration and better error handling
- `docker-compose.yml` - Enhanced with frontend service, health checks, and volumes for Google Drive

### Configuration Files
- `backend/src/main/resources/application-docker.yml` - Docker-specific Spring Boot configuration
- `.env.example` - Updated with all environment variables needed for the containerized setup

### Documentation
- `CONTAINERIZED_SETUP.md` - Comprehensive guide for the containerized environment
- `run-local.sh` - Interactive script to build, test, and run the application

## Key Features

### Containerized Services
- **PostgreSQL Database** - Data persistence with volume mapping
- **Spring Boot Backend** - JVM-based API server with Google Drive integration
- **Next.js Frontend** - React UI with server-side rendering capabilities

### Integration Points
- Google Drive API for file storage
- Database connection between Spring Boot and PostgreSQL
- API communication between frontend and backend
- JWT authentication across services

### Health Monitoring
- Health checks for all services in docker-compose.yml
- Actuator endpoints exposed for backend status monitoring
- Automatic container restart configuration

### Local Development Support
- Volume mounts for credentials and tokens
- Comprehensive logging configuration
- Easy-to-use startup script with environment validation

## Testing Instructions

1. Copy your Google API credentials to `backend/src/main/resources/credentials/credentials.json`
2. Copy `.env.example` to `.env` and update values as needed
3. Run `./run-local.sh` to build and start all services
4. Access the application at http://localhost:3000
5. Access the API documentation at http://localhost:8080/api/swagger-ui/index.html

## Known Limitations

- Google authentication requires browser interaction on first run
- Database data is persisted in a Docker volume, not externally
- Long initial build time due to dependency downloads
- Environment variables need to be managed manually

## Next Steps

- Implement automated testing in the CI/CD pipeline
- Add data backup/restore functionality for Google Drive storage
- Configure production-ready security settings
- Implement monitoring and metrics collection
