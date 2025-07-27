# Google Drive Integration for Finance Monkey

## Overview

Finance Monkey now offers a Google Drive storage option that allows you to store transaction data in Google Drive instead of relying solely on a database. This document explains how to use and manage the Google Drive integration.

## Benefits

- **No Database Dependency**: Works even when the PostgreSQL database is unavailable
- **Cost Effective**: Uses free Google Drive storage instead of paid database hosting
- **User Controlled**: Users can directly access their data files in Google Drive
- **Automatic Backups**: Monthly exports ensure data is always backed up
- **Resilient Design**: System can operate even with intermittent connectivity

## Setup

### Prerequisites

1. Google Cloud account with Drive API enabled
2. Existing Google OAuth credentials (same as used for Gmail access)

### Configuration

The system is already configured to use your existing Google OAuth credentials. Simply ensure the Drive API is enabled in your Google Cloud Console project.

1. Go to [Google Cloud Console](https://console.cloud.google.com/apis/library/drive.googleapis.com)
2. Enable the Google Drive API for the same project you use for Gmail access
3. Make sure your `.env` file contains the Google credentials:
   ```
   GOOGLE_CLIENT_ID=your-google-client-id
   GOOGLE_CLIENT_SECRET=your-google-client-secret
   GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/oauth2/callback/google
   ```

## Usage

### Running the Application with Drive Integration

Use the provided startup script:

```bash
cd backend
./start-with-drive.sh
```

### First-time Authorization

On first run, you'll be prompted to authorize the application to access Google Drive. Follow these steps:

1. The application will open a browser window or display a URL
2. Log in with your Google account
3. Grant the requested permissions for Drive access
4. The authorization token will be stored in the `tokens` directory for future use

### APIs for Drive Storage

#### User APIs

- `GET /api/drive/transactions` - Get the latest transactions from Drive
- `GET /api/drive/transactions/history` - Get a list of all transaction files
- `POST /api/drive/export/transactions` - Manually trigger an export
- `GET /api/drive/status` - Check Google Drive connection status

#### Admin APIs

- `POST /api/drive/admin/migrate-all` - Migrate all user data to Drive
- `POST /api/drive/initialize` - Initialize the Drive folder structure

### Frontend Components

The following components are available for your frontend:

- `EnhancedDriveStorage.jsx` - Main component for user interaction with Drive storage
- `DriveStorageAdmin.jsx` - Admin dashboard for Drive storage management

### Automated Tasks

The system performs the following automated tasks:

- Monthly export of all transactions to Drive (configurable in `application.yml`)
- Automatic initialization of Drive folders on application startup

## Troubleshooting

### Connection Issues

If you encounter connection issues with Google Drive:

1. Check the status with `GET /api/drive/status`
2. Verify your Google Cloud project has Drive API enabled
3. Check the application logs for authorization errors
4. Try reinitializing the folder structure with `POST /api/drive/initialize`

### Authorization Problems

If you experience authorization problems:

1. Delete the `tokens` directory to force reauthorization
2. Verify your Google credentials in the `.env` file
3. Ensure your Google account has permission to use the application

## Data Storage Details

- Transaction data is stored as JSON files in Google Drive
- Files are organized in folders by data type
- File naming follows the pattern: `userId_dataType_timestamp.json`
- Monthly automatic exports ensure data is backed up regularly
