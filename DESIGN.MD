# Finance Monkey - System Design Document

## Project Overview

Finance Monkey is a personal finance management platform that automatically extracts financial data from users' emails, processes it, and presents insightful analytics through a web interface. The system aims to help users track all their payments and financial activities in one centralized application.

## Architecture

The system will follow a three-tier architecture:

1. **Backend (Spring Boot)**
   - Handles business logic, email processing, AI-based data extraction
   - Manages database operations
   - Provides RESTful APIs for the frontend

2. **Frontend (Next.js)**
   - User interface for viewing financial data
   - Dashboard with analytics and visualizations
   - Account management features

3. **Database (PostgreSQL)**
   - Stores user information
   - Stores processed financial transactions
   - Maintains email processing status

## Key Components

### 1. Email Processing Service
- **Email Fetching**: Connect to users' email accounts via OAuth or email forwarding
- **AI-Based Data Extraction**: Use AI models to parse and extract payment information from emails
- **Classification System**: Categorize transactions (bills, subscriptions, one-time purchases)

### 2. Data Storage Layer
- **User Management**: Store user profiles, preferences, and authentication details
- **Transaction Storage**: Store processed transaction data with proper categorization
- **Email Metadata Storage**: Track which emails have been processed

### 3. Analytics Engine
- **Spending Analysis**: Calculate spending patterns and trends
- **Budget Tracking**: Compare spending against user-defined budgets
- **Forecasting**: Predict future expenses based on recurring payments

### 4. User Interface
- **Dashboard**: Overview of financial status
- **Transaction Explorer**: Detailed view of all transactions
- **Settings**: Email connection management, preferences
- **Reports**: Generate custom reports and visualizations

## Technical Stack

### Backend (Spring Boot)
- **Core**: Spring Boot 3.x
- **Authentication**: Spring Security with JWT
- **Email Integration**: JavaMail API, IMAP/POP3 protocols
- **AI Processing**: Gemini API integration for email parsing
- **API Documentation**: Swagger/OpenAPI

### Frontend (Next.js)
- **Framework**: Next.js 14+
- **State Management**: Redux or Context API
- **UI Components**: Material-UI or Tailwind CSS
- **Data Visualization**: D3.js or Chart.js
- **Authentication**: NextAuth.js

### Database (PostgreSQL)
- **ORM**: Hibernate/Spring Data JPA
- **Migrations**: Flyway or Liquibase
- **Connection Pooling**: HikariCP

### DevOps
- **Deployment Platform**: Render
- **Container Management**: Docker
- **CI/CD**: GitHub Actions
- **Monitoring**: Prometheus/Grafana or New Relic

## Data Flow

1. User connects email account to Finance Monkey
2. System periodically fetches new emails from connected accounts
3. AI service processes emails to extract payment information
4. Extracted data is validated and stored in PostgreSQL
5. Frontend requests processed data through REST APIs
6. User views organized financial data and analytics on the dashboard

## Database Schema (Key Tables)

### Users
- id (PK)
- email
- password_hash
- created_at
- updated_at
- settings (JSONB)

### EmailAccounts
- id (PK)
- user_id (FK)
- email_address
- access_token
- refresh_token
- last_sync_date
- sync_status

### Transactions
- id (PK)
- user_id (FK)
- email_id (FK)
- transaction_date
- amount
- currency
- vendor
- category
- description
- recurring (boolean)
- recurrence_pattern
- status

### Categories
- id (PK)
- name
- parent_category_id (FK, for hierarchical categories)
- icon
- color_code

## API Endpoints

### Authentication
- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/refresh-token
- POST /api/auth/logout

### Email Management
- POST /api/emails/connect
- GET /api/emails/accounts
- DELETE /api/emails/accounts/{id}
- POST /api/emails/sync

### Transactions
- GET /api/transactions
- GET /api/transactions/{id}
- PUT /api/transactions/{id}
- DELETE /api/transactions/{id}
- GET /api/transactions/stats
- GET /api/transactions/categories

### User Settings
- GET /api/user/settings
- PUT /api/user/settings
- GET /api/user/profile
- PUT /api/user/profile

## AI Integration

The AI component will:
1. Extract key information from email body:
   - Transaction amount
   - Date
   - Merchant/vendor
   - Transaction type
   - Currency
   - Reference numbers

2. Classify transactions into categories:
   - Bills (utilities, rent)
   - Subscriptions (streaming services, memberships)
   - Shopping
   - Travel
   - Food & Dining
   - etc.

3. Identify recurring payments and their patterns

## Deployment Architecture

### Render Deployment
- Backend Spring Boot application deployed as a Web Service
- Frontend Next.js application deployed as a Static Site
- PostgreSQL deployed as a managed database service
- Environment variables managed through Render dashboard

### Scaling Considerations
- Horizontal scaling for the backend services
- Database connection pooling
- Caching layer for frequently accessed data
- Background job processing for email analysis

## Development Roadmap

### Phase 1: Foundation (2-3 weeks)
- Set up basic project structure (Spring Boot, Next.js)
- Configure PostgreSQL database and basic schema
- Implement authentication system
- Set up CI/CD pipeline

### Phase 2: Core Features (3-4 weeks)
- Implement email connection and fetching
- Develop basic AI parsing for common email formats
- Create basic transaction storage and retrieval
- Develop minimal frontend dashboard

### Phase 3: Enhancement (3-4 weeks)
- Improve AI parsing accuracy
- Add transaction categorization
- Implement spending analytics
- Enhance frontend with visualizations

### Phase 4: Polish & Scale (2-3 weeks)
- Performance optimization
- UI/UX improvements
- Add more sophisticated analytics
- Implement user feedback mechanisms

## Security Considerations

- End-to-end encryption for sensitive data
- OAuth 2.0 for email account access
- Rate limiting to prevent abuse
- Regular security audits
- GDPR compliance for user data
- Sensitive data encryption at rest and in transit

## Monitoring & Maintenance

- Error tracking with Sentry
- Performance monitoring
- Regular database backups
- Automated testing pipeline
- Feature usage analytics

## Future Expansion Possibilities

- Mobile application
- Financial goal setting and tracking
- Integration with banking APIs
- Investment tracking
- Tax preparation assistance
- Budget recommendation engine
- Expense sharing features

This design document provides a comprehensive blueprint for building the Finance Monkey application. Implementation will follow the outlined phases, with regular reviews to ensure alignment with requirements and goals.
