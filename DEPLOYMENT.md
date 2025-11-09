# Finance Monkey Deployment Guide

This guide walks you through deploying Finance Monkey to production on Render.com.

## Prerequisites

1. A Render account (sign up at https://render.com)
2. Your Finance Monkey repository pushed to GitHub
3. The following API keys and credentials:
   - **Gemini API Key**: Get from [Google AI Studio](https://makersuite.google.com/app/apikey)
   - **Google OAuth Client ID & Secret**: Set up in [Google Cloud Console](https://console.cloud.google.com/)
   - **JWT Secret**: Generate a secure random string (at least 256 bits)
   - **NextAuth Secret**: Generate a secure random string for session management

## Deployment Steps

### 1. Connect Repository to Render

1. Log in to your Render dashboard
2. Click "New +" and select "Blueprint"
3. Connect your GitHub repository containing Finance Monkey
4. Render will automatically detect the `render.yaml` file

### 2. Configure Environment Variables

After the blueprint is detected, you'll need to set the following environment variables in the Render dashboard:

#### Backend Service (finance-monkey-backend)

- **JWT_SECRET**: Your secure JWT secret key
  ```
  Example: developmentSecretKeyWithMoreThan256BitsToSatisfyJWTRequirements
  ```

- **GEMINI_API_KEY**: Your Gemini AI API key
  ```
  Example: AIzaSy...
  ```

- **GOOGLE_CLIENT_ID**: Your Google OAuth Client ID
  ```
  Example: 123456789-abcdefghijk.apps.googleusercontent.com
  ```

- **GOOGLE_CLIENT_SECRET**: Your Google OAuth Client Secret
  ```
  Example: GOCSPX-...
  ```

- **GOOGLE_REDIRECT_URI**: OAuth callback URL
  ```
  Example: https://finance-monkey-backend.onrender.com/api/auth/oauth2/callback/google
  ```

#### Frontend Service (finance-monkey-frontend)

- **NEXTAUTH_URL**: Your frontend URL
  ```
  Example: https://finance-monkey-frontend.onrender.com
  ```

- **NEXTAUTH_SECRET**: Secure secret for NextAuth.js
  ```
  Example: generate with: openssl rand -base64 32
  ```

- **NEXT_PUBLIC_API_URL**: Backend API URL (already set in render.yaml)
  ```
  Default: https://finance-monkey-backend.onrender.com/api
  ```

### 3. Database Setup

The database will be automatically created via the render.yaml configuration:
- Database Name: `finance_monkey`
- Type: PostgreSQL
- Plan: Free tier

The database URL is automatically injected into the backend service.

### 4. Deploy

1. Click "Apply" to create the services
2. Render will build and deploy:
   - PostgreSQL database
   - Backend service (Spring Boot)
   - Frontend service (Next.js)

3. Monitor the deployment logs in each service's dashboard

### 5. Post-Deployment Configuration

#### Enable Google Drive API (Optional)

If you plan to use Google Drive storage:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Enable the Google Drive API for your project
3. Use the same OAuth credentials already configured

#### CORS Configuration

The backend is configured to allow requests from:
- Local development: `http://localhost:3000`
- Production: Update `CORS_ALLOWED_ORIGINS` in render.yaml to match your frontend URL

### 6. Verify Deployment

1. **Backend Health Check**:
   ```
   https://finance-monkey-backend.onrender.com/api/actuator/health
   ```
   Should return: `{"status":"UP"}`

2. **API Documentation**:
   ```
   https://finance-monkey-backend.onrender.com/api/swagger-ui.html
   ```

3. **Frontend**:
   ```
   https://finance-monkey-frontend.onrender.com
   ```

## Troubleshooting

### Service Won't Start

1. Check the logs in Render dashboard
2. Verify all environment variables are set correctly
3. Ensure database is healthy before backend starts

### Database Connection Issues

1. Check if DATABASE_URL is properly set
2. Verify PostgreSQL service is running
3. Review database migration logs

### Frontend Can't Connect to Backend

1. Verify `NEXT_PUBLIC_API_URL` is correct
2. Check CORS configuration in backend
3. Ensure backend service is healthy

### OAuth Not Working

1. Verify redirect URI in Google Cloud Console matches your Render URL
2. Check `GOOGLE_REDIRECT_URI` environment variable
3. Ensure OAuth consent screen is properly configured

## Updating the Application

### Automatic Deployment

If `autoDeploy: true` is set in render.yaml (default):
- Push changes to your GitHub repository
- Render automatically builds and deploys updates

### Manual Deployment

1. Go to service dashboard in Render
2. Click "Manual Deploy"
3. Select branch and click "Deploy"

## Scaling

### Free Tier Limitations

- Services spin down after 15 minutes of inactivity
- First request after spin-down may be slow
- Database has 1GB storage limit

### Upgrading

To upgrade for better performance:
1. Go to service settings in Render
2. Change plan from "Free" to "Starter" or higher
3. Adjust instance type and autoscaling as needed

## Monitoring

### Health Checks

- Backend: `/api/actuator/health`
- Frontend: Health check via HTTP GET to port 3000

### Logs

Access logs via:
1. Render dashboard → Service → Logs
2. Or use Render CLI: `render logs <service-name>`

### Metrics

Render provides basic metrics:
- CPU usage
- Memory usage
- Request count
- Response times

## Security Best Practices

1. **Environment Variables**: Never commit secrets to Git
2. **JWT Secret**: Use a strong, randomly generated secret
3. **Database**: Enable SSL connections in production
4. **API Keys**: Rotate keys periodically
5. **CORS**: Restrict to specific domains, not wildcard

## Backup

### Database Backups

Render automatically backs up databases:
- Free tier: Daily backups, retained for 7 days
- Paid tiers: More frequent backups with longer retention

### Manual Backup

```bash
# Export database
pg_dump $DATABASE_URL > backup.sql

# Restore database
psql $DATABASE_URL < backup.sql
```

## Support

For issues:
1. Check Render documentation: https://render.com/docs
2. Review application logs
3. Open an issue on the Finance Monkey GitHub repository
