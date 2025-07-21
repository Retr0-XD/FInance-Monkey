# Finance Monkey Backend

This is the backend service for the Finance Monkey application, built with Spring Boot.

## Prerequisites

- JDK 17 or higher
- PostgreSQL database
- Gradle

## Configuration

Before running the application, you need to configure several environment variables:

### Database Configuration
```
DB_URL=jdbc:postgresql://localhost:5432/finance_monkey
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
```

### Security Configuration
```
JWT_SECRET=yourJwtSecretKey
```

### Gemini AI API
```
GEMINI_API_KEY=your-gemini-api-key
```

### Google OAuth Configuration (for Gmail API access)
```
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/oauth2/callback/google
```

## Running Locally

1. Make sure you have PostgreSQL running and create a database named `finance_monkey`:

```bash
createdb finance_monkey
```

2. Build the application:

```bash
cd backend
./gradlew build
```

3. Run the application:

```bash
./gradlew bootRun
```

The application will start on port 8080 by default, with the API accessible at `http://localhost:8080/api`

## API Documentation

Once the application is running, you can access the Swagger UI at:
```
http://localhost:8080/api/swagger-ui.html
```

The OpenAPI specification is available at:
```
http://localhost:8080/api/api-docs
```

## Development Workflow

### Database Migrations

The project uses Flyway for database migrations. Migration files are located in:
```
src/main/resources/db/migration
```

New migrations should follow the naming convention `V{version}__{description}.sql`

### Testing

Run tests with:

```bash
./gradlew test
```

## Getting OAuth and Gemini API Keys

### Google OAuth Setup

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Navigate to APIs & Services > Credentials
4. Click "Create Credentials" and select "OAuth client ID"
5. Configure the OAuth consent screen
6. Create an OAuth client ID for a Web application
7. Add authorized redirect URIs (e.g., `http://localhost:8080/api/auth/oauth2/callback/google`)
8. Note the Client ID and Client Secret

### Gemini API Key

1. Go to the [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create an API key
3. Copy the API key for use in your application
