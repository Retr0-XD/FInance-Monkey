# Finance Monkey

Finance Monkey is a personal finance management platform that automatica### Testing

Run the backend tests with:

```bash
cd backend
./gradlew test
```

## Deployment

### Deploying to Render

The application is configured for deployment on Render using the `render.yaml` file in the root directory.

1. **Prerequisites**
   - A Render account (https://render.com)
   - Repository pushed to GitHub

2. **Setup Steps**
   - Connect your GitHub repository to Render
   - Use the "Blueprint" deployment option and select the `render.yaml` file
   - Set the required environment variables in the Render dashboard:
     - JWT_SECRET
     - GEMINI_API_KEY
     - GOOGLE_CLIENT_ID
     - GOOGLE_CLIENT_SECRET
     - GOOGLE_REDIRECT_URI (update this to your production URL)

3. **Monitoring**
   - Once deployed, you can monitor your application in the Render dashboard
   - Logs are available for debugging

### CI/CD Pipeline

The project includes a GitHub Actions workflow for continuous integration and deployment:

- **Backend Testing**: Runs Gradle build and tests with a PostgreSQL test database
- **Frontend Testing**: Runs linting and build checks
- **Automatic Deployment**: Triggers a deploy to Render when changes are pushed to the main branch

To enable automatic deployment to Render:
1. Create a deploy hook URL in your Render dashboard
2. Add the URL as a secret named `RENDER_DEPLOY_HOOK` in your GitHub repository settingscts financial data from users' emails, processes it, and presents insightful analytics through a web interface. The system aims to help users track all their payments and financial activities in one centralized application.

## Architecture

The system follows a three-tier architecture:

1. **Backend (Spring Boot)**
   - Handles business logic, email processing, AI-based data extraction
   - Manages database operations
   - Provides RESTful APIs for the frontend

2. **Frontend (Next.js)** - *Coming soon*
   - User interface for viewing financial data
   - Dashboard with analytics and visualizations
   - Account management features

3. **Database (PostgreSQL)**
   - Stores user information
   - Stores processed financial transactions
   - Maintains email processing status

## Getting Started

### Prerequisites

- JDK 17 or higher
- Docker and Docker Compose (for easy setup)
- Gradle

### Running with Docker Compose

The easiest way to get started is using Docker Compose:

```bash
# Clone the repository
git clone https://github.com/Retr0-XD/FInance-Monkey.git
cd FInance-Monkey

# Create a .env file with your API keys
cat > .env << EOL
GEMINI_API_KEY=your-gemini-api-key
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/oauth2/callback/google
EOL

# Start the application
docker-compose up -d
```

The backend will be available at `http://localhost:8080/api`.

### Running Manually

To run the backend manually:

```bash
cd backend
./gradlew bootRun
```

See the [backend README](backend/README.md) for more detailed instructions.

## API Documentation

Once the backend is running, you can access the Swagger UI at:
```
http://localhost:8080/api/swagger-ui.html
```

## Configuration

### Required External API Keys

1. **Gemini API Key**
   - Used for AI-based email parsing
   - Get it from [Google AI Studio](https://makersuite.google.com/app/apikey)

2. **Google OAuth Client ID and Secret**
   - Used for connecting to Gmail
   - Set up in the [Google Cloud Console](https://console.cloud.google.com/)

## Development

### Backend Development

The backend is built with Spring Boot. See the [backend README](backend/README.md) for development instructions.

### Testing

Run the backend tests with:

```bash
cd backend
./gradlew test
```Inance-Monkey
Finance Monkey is a personal finance management platform that automatically extracts financial data from users’ emails
