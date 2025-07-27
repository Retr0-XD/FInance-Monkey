# Finance Monkey - Improved System Design

## Project Overview

Finance Monkey is a personal finance management platform that automatically extracts financial data from users' emails, processes it, and presents insightful analytics through a web interface. This improved design document introduces alternative storage options and enhanced resilience features.

## Architecture

The system follows a three-tier architecture with flexibility in storage options:

1. **Backend (Spring Boot)**
   - Handles business logic, email processing, AI-based data extraction
   - Manages database operations and alternative storage
   - Provides RESTful APIs for the frontend

2. **Frontend (Next.js)**
   - User interface for viewing financial data
   - Dashboard with analytics and visualizations
   - Account management features

3. **Storage Options**
   - **Primary Storage (PostgreSQL)**: Traditional database storage
   - **Alternative Storage (Google Drive)**: Cloud-based JSON file storage
   - **Hybrid Mode**: Use both systems for redundancy

## Key Components

### 1. Email Processing Service
- **Email Fetching**: Connect to users' email accounts via OAuth or email forwarding
- **AI-Based Data Extraction**: Use Gemini AI to parse and extract payment information from emails
- **Classification System**: Categorize transactions (bills, subscriptions, one-time purchases)

### 2. Multi-Tier Storage Layer
- **User Management**: Store user profiles, preferences, and authentication details
- **Transaction Storage Options**:
  - **Database Storage**: Store processed transaction data in PostgreSQL with proper categorization
  - **Google Drive Storage**: Export transaction data as JSON files to Google Drive
  - **Periodic Synchronization**: Automatically export data monthly or on-demand
- **Email Metadata Storage**: Track which emails have been processed

### 3. Analytics Engine
- **Spending Analysis**: Calculate spending patterns and trends
- **Budget Tracking**: Compare spending against user-defined budgets
- **Forecasting**: Predict future expenses based on recurring payments
- **Works with both storage systems**: Can pull data from either PostgreSQL or Google Drive

### 4. User Interface
- **Dashboard**: Overview of financial status
- **Transaction Explorer**: Detailed view of all transactions
- **Storage Management**: Control where data is stored and export options
- **Reports**: Generate custom reports and visualizations

## Google Drive Storage Flow

1. **Data Collection**:
   - Backend processes emails as normal
   - Extracts transaction data with AI assistance
   
2. **Storage Options**:
   - Primary: Stores data in PostgreSQL database
   - Alternative: Exports data to Google Drive as JSON
   
3. **Automated Export**:
   - Monthly scheduled export of all transaction data
   - Creates dated JSON files in user's Google Drive
   
4. **Frontend Access**:
   - Frontend can fetch data directly from Google Drive API
   - Uses backend as proxy to maintain security
   
5. **User Control**:
   - Manual export option via the UI
   - View export history and access files directly

## Benefits of Hybrid Storage

1. **Resilience**: Continues functioning even if database is unavailable
2. **Cost Efficiency**: Reduces database hosting costs
3. **Data Ownership**: Users have direct access to their data files
4. **Flexibility**: Works in environments with limited database access
5. **Backup**: Automatic backup of all transaction data

## Technical Implementation

1. **Google Drive API Integration**:
   - OAuth 2.0 authentication
   - Automatic folder creation and management
   - JSON file generation and versioning

2. **Backend Controllers**:
   - Endpoints for triggering exports
   - Endpoints for fetching data from Drive
   - Admin endpoints for system-wide operations

3. **Frontend Integration**:
   - UI components for managing Drive storage
   - Transaction display from Drive data
   - Export controls and history view
   
4. **Scheduled Tasks**:
   - Monthly export job
   - Health checks for both storage systems
   - Retry mechanisms for failed operations
