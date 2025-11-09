# Finance Monkey - Quick Start Guide

Get Finance Monkey up and running in 5 minutes!

## 🚀 Quick Start (Docker - Recommended)

### Prerequisites
- Docker and Docker Compose installed
- Git

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/Retr0-XD/FInance-Monkey.git
   cd FInance-Monkey
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env and add your API keys
   ```

3. **Start the application**
   ```bash
   docker compose up -d
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - API Documentation: http://localhost:8080/api/swagger-ui.html

That's it! 🎉

## 🛠️ Manual Setup (Without Docker)

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 14 or higher

### Steps

1. **Clone and setup**
   ```bash
   git clone https://github.com/Retr0-XD/FInance-Monkey.git
   cd FInance-Monkey
   ./setup.sh
   ```

2. **Start PostgreSQL**
   ```bash
   # Option 1: Using Docker
   docker compose up -d postgres
   
   # Option 2: Use your local PostgreSQL installation
   createdb finance_monkey
   ```

3. **Start the backend**
   ```bash
   cd backend
   ./gradlew bootRun
   ```

4. **Start the frontend** (in a new terminal)
   ```bash
   cd frontend
   npm run dev
   ```

5. **Access the application**
   - Frontend: http://localhost:3000
   - Backend: http://localhost:8080/api

## 🔑 Required API Keys

You'll need these API keys to use all features:

### 1. Gemini API Key (Required for AI email parsing)
- Get from: https://makersuite.google.com/app/apikey
- Free tier available
- Add to `.env` as `GEMINI_API_KEY`

### 2. Google OAuth Credentials (Required for Gmail integration)
- Get from: https://console.cloud.google.com/
- Create OAuth 2.0 Client ID
- Add to `.env`:
  - `GOOGLE_CLIENT_ID`
  - `GOOGLE_CLIENT_SECRET`
  - `GOOGLE_REDIRECT_URI`

### 3. JWT Secret (Required)
- Generate with: `openssl rand -base64 64`
- Add to `.env` as `JWT_SECRET`

### 4. NextAuth Secret (Required for frontend)
- Generate with: `openssl rand -base64 32`
- Add to `.env` as `NEXTAUTH_SECRET`

## 📝 First-Time User Guide

1. **Register an account**
   - Go to http://localhost:3000/register
   - Create your account

2. **Connect your email**
   - Navigate to Email Accounts
   - Click "Connect Email"
   - Authorize Finance Monkey to access your Gmail

3. **View your transactions**
   - Finance Monkey will automatically process your emails
   - Check the Dashboard for insights
   - Browse Transactions for details

4. **Organize with categories**
   - Create custom categories
   - Assign transactions to categories
   - View spending by category

## 🐛 Common Issues

### "Cannot connect to database"
```bash
# Check if PostgreSQL is running
docker compose ps postgres

# Restart PostgreSQL
docker compose restart postgres
```

### "Frontend can't reach backend"
- Check backend is running on http://localhost:8080
- Verify `NEXT_PUBLIC_API_URL` in `.env`
- Check for CORS issues in browser console

### "Gemini API not working"
- Verify `GEMINI_API_KEY` is correct
- Check API quota at https://makersuite.google.com
- Review backend logs for errors

### "OAuth redirect fails"
- Verify `GOOGLE_REDIRECT_URI` matches exactly in:
  - `.env` file
  - Google Cloud Console OAuth settings
- Check callback URL format

## 📚 Additional Resources

- **Full Documentation**: See [README.md](README.md)
- **Deployment Guide**: See [DEPLOYMENT.md](DEPLOYMENT.md)
- **API Documentation**: http://localhost:8080/api/swagger-ui.html
- **Architecture**: See [DESIGN_Improved.MD](DESIGN_Improved.MD)

## 🆘 Getting Help

- Check the [Troubleshooting section in DEPLOYMENT.md](DEPLOYMENT.md#troubleshooting)
- Review backend logs: `docker compose logs backend`
- Review frontend logs: `docker compose logs frontend`
- Open an issue on GitHub

## 🎯 What's Next?

- Explore the Dashboard to see your financial overview
- Set up categories to organize your spending
- Connect multiple email accounts
- Review transactions and add manual entries
- Export data to Google Drive (optional)

Enjoy using Finance Monkey! 🐵💰
