# Copilot Instructions for Finance Monkey

## Project Overview
Finance Monkey is a three-tier personal finance management platform. It extracts financial data from users' emails, processes it with AI, and presents analytics via a Next.js frontend. The backend is Spring Boot, and PostgreSQL is used for data storage.

## Architecture & Key Components
- **Backend (Spring Boot)**: Handles business logic, email processing (JavaMail, IMAP/POP3), AI-based extraction (Gemini API), and exposes REST APIs. Authentication uses JWT via Spring Security.
- **Frontend (Next.js)**: User dashboard, analytics, and account management. Uses Material-UI/Tailwind CSS, Redux/Context API, and NextAuth.js for authentication.
- **Database (PostgreSQL)**: Stores users, email accounts, transactions, and categories. Uses Hibernate/Spring Data JPA and Flyway/Liquibase for migrations.

## Data Flow
1. User connects email account (OAuth or forwarding).
2. Backend fetches emails, parses with AI, and stores transactions.
3. Frontend fetches processed data via REST APIs for display and analytics.

## Developer Workflows
- **Backend**: Build/run with Maven or Gradle. Test with JUnit. Use Swagger/OpenAPI for API docs.
- **Frontend**: Build/run with `npm run dev` or `yarn dev`. Test with Jest/React Testing Library.
- **Database**: Migrations via Flyway/Liquibase. Connection pooling with HikariCP.
- **DevOps**: Deploy on Render. Use Docker for containerization. CI/CD via GitHub Actions.

## Conventions & Patterns
- **API Endpoints**: RESTful, documented in Swagger. Example: `/api/transactions`, `/api/emails/connect`.
- **Authentication**: JWT for backend, NextAuth.js for frontend.
- **AI Integration**: Gemini API for email parsing. Transaction classification and recurring payment detection are handled server-side.
- **Error Handling**: Standard Spring Boot exception handling. Frontend displays user-friendly errors.
- **Security**: OAuth 2.0 for email, encryption at rest/in transit, GDPR compliance.

## Key Files & Directories
- `DESIGN_Improved.MD`: System design, architecture, and roadmap.
- `README.md`: Project summary.
- `.github/workflows/`: CI/CD pipelines (if present).
- `src/` (backend), `pages/` (frontend): Main code locations.

## External Integrations
- **Gemini API**: For AI-based email parsing.
- **Render**: Deployment platform.
- **Prometheus/Grafana/New Relic**: Monitoring (if configured).

## Example Patterns
- Email processing jobs run as background tasks in backend.
- Transaction categorization uses hierarchical categories (see `Categories` table).
- Frontend fetches analytics via `/api/transactions/stats`.

---
For questions about unclear conventions or missing details, ask the user for clarification or examples from the codebase.
