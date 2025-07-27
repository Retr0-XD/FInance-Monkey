# Google Drive Storage for Finance Monkey

This feature enables storing processed transaction data in Google Drive instead of relying on a traditional database. This is useful for deployments where database access is limited or expensive.

## How It Works

1. The backend processes emails as before, extracting transaction data.
2. Instead of (or in addition to) storing in PostgreSQL, the processed data is exported to Google Drive as JSON files.
3. The frontend can fetch these JSON files directly from Google Drive when needed.
4. Data is exported automatically once a month, or manually when triggered by a user.

## Setup Instructions

### Backend Configuration

1. **Google API Credentials**
   - If you already have Google credentials in your environment variables (for Gmail access), you can reuse them
   - Just ensure the Google Drive API is enabled for your project in the [Google Cloud Console](https://console.cloud.google.com/)
   - The system will use the existing `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` variables by default
   - Alternatively, set the `GOOGLE_CREDENTIALS_JSON` environment variable with full JSON credentials

2. **Application Configuration**
   - The Google Drive integration is already configured in `application.yml`
   - You can adjust the export schedule if needed

3. **First Run Authentication**
   - On the first run, you'll need to authenticate with Google
   - The application will open a browser window or provide a URL to authenticate
   - This creates a token that is stored in the `tokens` directory for future use

### Frontend Integration

The frontend can access transaction data through the following endpoints:

- `GET /api/drive/transactions` - Get the latest transactions from Google Drive
- `GET /api/drive/transactions/history` - Get a list of all transaction files in Drive
- `POST /api/drive/export/transactions` - Manually trigger an export of transactions to Drive

## Usage

### For Users

1. Connect your email account as normal
2. The application will process emails and store transactions
3. Transactions will be automatically exported to Google Drive monthly
4. You can manually trigger an export at any time through the UI or API

### For Admins

1. The admin can trigger a full migration of all user data to Google Drive
2. This is useful for migrating away from a database or creating backups
3. Use the `POST /api/drive/admin/migrate-all` endpoint (requires ADMIN role)

## Benefits

- No need for a persistent database connection
- Lower hosting costs (free Google Drive storage)
- Data can be accessed directly by the frontend
- Automatic monthly backups of all transaction data
- User can access their data files directly in Google Drive

## Limitations

- Not suitable for real-time applications with frequent updates
- Authentication requires user interaction on first run
- Limited query capabilities compared to a full database

## Important Notes

- Make sure to enable the Google Drive API in your Google Cloud project
- This can be done in the [Google Cloud Console](https://console.cloud.google.com/apis/library/drive.googleapis.com)
- The same OAuth credentials (client ID and secret) can be used for both Gmail and Drive access
