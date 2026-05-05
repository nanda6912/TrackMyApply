# ApplyTrack - Job Application Tracking System

A comprehensive job application tracking system that helps students and job seekers manage their job applications across multiple platforms like LinkedIn, Naukri, and Indeed.

## Features

### Core Features
- **Application Dashboard**: View and manage all job applications in one place
- **Manual Entry**: Add applications manually with detailed information
- **Chrome Extension**: Save job applications directly from LinkedIn, Naukri, and Indeed
- **Email Sync**: Automatically capture job applications from Gmail
- **Reminder System**: Set reminders for interviews and follow-ups
- **Filtering & Search**: Filter applications by status, platform, company, or role

### Application Status Tracking
- Applied
- Online Assessment (OA)
- Interview
- Offer
- Rejected

### Platform Support
- LinkedIn
- Naukri
- Indeed
- Other platforms

## Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL
- **Authentication**: JWT + OAuth 2.0 (Gmail)
- **API**: RESTful APIs

### Frontend (React)
- **Framework**: React 18
- **Styling**: Tailwind CSS
- **UI Components**: Custom components with Lucide icons
- **State Management**: React hooks

### Chrome Extension
- **Manifest V3**: Latest Chrome extension standards
- **Content Scripts**: Auto-detect job information from job pages
- **Popup UI**: Quick job saving interface

### Data Sources
1. **Manual Entry**: Direct input through web interface
2. **Browser Extension**: Semi-automatic capture from job pages
3. **Email Sync**: Automatic extraction from Gmail

## Quick Start

### Prerequisites
- Java 17+
- Node.js 16+
- PostgreSQL 12+
- Maven 3.6+
- Chrome Browser (for extension)

### Database Setup
1. Install PostgreSQL
2. Create database:
   ```sql
   CREATE DATABASE applytrack;
   ```
3. Update database credentials in `backend/src/main/resources/application.properties`

### Backend Setup
1. Navigate to backend directory:
   ```bash
   cd backend
   ```
2. Install dependencies:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. API will be available at `http://localhost:8080`

### Frontend Setup
1. Navigate to frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start development server:
   ```bash
   npm start
   ```
4. Application will be available at `http://localhost:3000`

### Chrome Extension Setup
1. Navigate to chrome-extension directory
2. Open Chrome and go to `chrome://extensions/`
3. Enable "Developer mode"
4. Click "Load unpacked"
5. Select the chrome-extension directory
6. Extension will be available in Chrome toolbar

### Gmail Integration Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable Gmail API
4. Create OAuth 2.0 credentials
5. Download client_secret.json
6. Place it in `backend/src/main/resources/` or specify path in application.properties

## API Endpoints

### Applications
- `GET /api/applications` - Get all applications
- `GET /api/applications/{id}` - Get application by ID
- `POST /api/applications` - Create manual application
- `POST /api/applications/extension` - Create application from extension
- `POST /api/applications/email` - Create application from email
- `PUT /api/applications/{id}` - Update application
- `DELETE /api/applications/{id}` - Delete application

### Reminders
- `GET /api/reminders/application/{applicationId}` - Get reminders for application
- `GET /api/reminders/upcoming` - Get upcoming reminders
- `GET /api/reminders/pending` - Get pending reminders
- `POST /api/reminders` - Create reminder
- `PUT /api/reminders/{id}` - Update reminder
- `DELETE /api/reminders/{id}` - Delete reminder

### Gmail Integration
- `POST /api/gmail/sync` - Sync emails
- `GET /api/gmail/status` - Get Gmail connection status

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

### Applications Table
```sql
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    applied_date TIMESTAMP NOT NULL,
    source VARCHAR(50) NOT NULL,
    job_link TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL
);
```

### Reminders Table
```sql
CREATE TABLE reminders (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    reminder_date TIMESTAMP NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

### Emails Processed Table
```sql
CREATE TABLE emails_processed (
    id BIGSERIAL PRIMARY KEY,
    email_id VARCHAR(255) UNIQUE NOT NULL,
    processed_at TIMESTAMP NOT NULL
);
```

## Development

### Running Tests
```bash
# Backend tests
cd backend
mvn test

# Frontend tests
cd frontend
npm test
```

### Building for Production
```bash
# Backend
cd backend
mvn clean package

# Frontend
cd frontend
npm run build
```

## Configuration

### Environment Variables
- `JWT_SECRET`: Secret key for JWT token generation
- `JWT_EXPIRATION`: Token expiration time in milliseconds
- `GMAIL_CREDENTIALS_FILE`: Path to Gmail OAuth credentials file

### Database Configuration
Update `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/applytrack
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Chrome Extension Usage

1. **Auto-Detection**: Extension automatically detects job pages on LinkedIn, Naukri, and Indeed
2. **Manual Save**: Click the extension icon to manually save job information
3. **Form Autofill**: Company name and role are automatically extracted from the page
4. **Quick Save**: Save job with one click after verification

## Email Sync Features

- **Automatic Scanning**: Periodically scans Gmail for job-related emails
- **Smart Extraction**: Extracts company, role, and application date from emails
- **Duplicate Prevention**: Avoids creating duplicate applications
- **Keyword Matching**: Detects emails with phrases like "Application received" and "Thank you for applying"

## Security

- **JWT Authentication**: Secure API endpoints with JWT tokens
- **Password Encryption**: BCrypt encryption for user passwords
- **CORS Configuration**: Proper cross-origin resource sharing setup
- **Input Validation**: Comprehensive input validation and sanitization

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
1. Check the existing issues on GitHub
2. Create a new issue with detailed description
3. Include steps to reproduce any bugs

## Future Enhancements

- [ ] Mobile app (React Native)
- [ ] Advanced analytics and insights
- [ ] Resume parsing and matching
- [ ] Interview preparation tools
- [ ] Company reviews and ratings integration
- [ ] Salary tracking and comparison
- [ ] Network connections tracking
