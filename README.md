# 🛡️ SGMS Backend

Security Guard Management System - Spring Boot REST API

## 🚀 Tech Stack

- **Framework**: Spring Boot 3.4.2
- **Language**: Java 17
- **Database**: PostgreSQL (Railway)
- **Security**: JWT Authentication, BCrypt
- **ORM**: Spring Data JPA + Hibernate
- **Migrations**: Flyway
- **Build Tool**: Maven
- **Deployment**: Railway

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+ (or use Railway)
- Git

## 🔧 Environment Variables

Create `.env` file in `backend/` directory:

```bash
# Database Configuration
DATABASE_URL=postgresql://user:password@host:port/database

# JWT Secret (minimum 32 characters)
APP_SECURITY_JWT_SECRET=your_super_secret_jwt_key_minimum_32_chars

# CORS Allowed Origins
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,https://your-frontend.netlify.app

# Server Port (optional, defaults to 8080)
SERVER_PORT=8080

# Active Profile
SPRING_PROFILES_ACTIVE=prod
```

See `.env.example` for full configuration.

## 📦 Installation

```bash
# Navigate to backend directory
cd backend

# Install dependencies
mvn clean install

# Run database migrations (auto-runs on startup)
# Migrations are in src/main/resources/db/migration/

# Start the server
mvn spring-boot:run
```

Server runs on `http://localhost:8080`

## 🏗️ Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/sgms/
│   │   │   ├── auth/          # Authentication (Login, Register, JWT)
│   │   │   ├── user/          # User & Role entities
│   │   │   ├── guard/         # Guard management
│   │   │   ├── client/        # Client accounts
│   │   │   ├── site/          # Site & Post management
│   │   │   ├── assignment/    # Guard assignments
│   │   │   ├── attendance/    # Check-in/out tracking
│   │   │   ├── security/      # JWT, UserDetails, SecurityConfig
│   │   │   └── config/        # App configuration
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/  # Flyway SQL migrations
│   └── test/                  # Unit & Integration tests
├── pom.xml
└── README.md
```

## 🔐 API Endpoints

### Authentication
- `POST /api/auth/login` - Login (public)
- `POST /api/auth/register` - Register (public/admin)
- `GET /api/auth/me` - Get current user (authenticated)

### Guards (ADMIN, SUPERVISOR)
- `GET /api/guards` - List all guards
- `GET /api/guards/{id}` - Get guard by ID
- `POST /api/guards` - Create guard (ADMIN only)
- `PUT /api/guards/{id}` - Update guard (ADMIN only)
- `DELETE /api/guards/{id}` - Delete guard (ADMIN only)

### Sites (ADMIN)
- `GET /api/sites` - List all sites
- `GET /api/sites?clientId={id}` - Filter by client
- `POST /api/sites` - Create site
- `DELETE /api/sites/{id}` - Delete site

### Clients (ADMIN)
- `GET /api/clients` - List all clients
- `POST /api/clients` - Create client
- `DELETE /api/clients/{id}` - Delete client

### Assignments (ADMIN, SUPERVISOR)
- `GET /api/assignments` - List active assignments
- `POST /api/assignments` - Create assignment
- `GET /api/assignments/shift-types` - Get shift types

### Attendance (ADMIN, SUPERVISOR, GUARD)
- `POST /api/attendance/check-in` - Guard check-in
- `POST /api/attendance/check-out` - Guard check-out
- `GET /api/attendance/guard/{id}` - Guard attendance history
- `GET /api/attendance/today-summary` - Today's summary

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn clean test jacoco:report
```

## 🚀 Deployment

### Railway Deployment

1. Create Railway project
2. Add PostgreSQL database
3. Set environment variables:
   ```
   DATABASE_URL (auto-set by Railway)
   APP_SECURITY_JWT_SECRET
   APP_CORS_ALLOWED_ORIGINS
   ```
4. Connect GitHub repository
5. Deploy automatically on push

### Manual Deployment

```bash
# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/sgms-backend-1.0.0.jar
```

## 📊 Database Schema

11 tables total:
- `roles` - User roles (ADMIN, SUPERVISOR, CLIENT, GUARD)
- `users` - User accounts
- `user_roles` - Many-to-many user-role mapping
- `guards` - Guard profiles
- `client_accounts` - Client organizations
- `sites` - Client sites
- `site_posts` - Guard positions at sites
- `shift_types` - Shift schedules
- `guard_assignments` - Guard deployments
- `attendance_logs` - Check-in/out records
- `supervisor_site_mapping` - Supervisor access control
- `client_site_access` - Client access control

All tables use soft deletes (`deleted_at` timestamp).

## 🔒 Security Features

- JWT-based stateless authentication
- BCrypt password hashing
- Role-based access control (@PreAuthorize)
- CORS whitelist configuration
- SQL injection protection (JPA/Hibernate)
- Input validation (Jakarta Validation)
- Secure headers (Spring Security defaults)

## 🐛 Debugging

```bash
# Enable debug logs
export LOGGING_LEVEL_COM_SGMS=DEBUG
mvn spring-boot:run

# Check health endpoint
curl http://localhost:8080/actuator/health

# Test authentication
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sgms.com","password":"admin123"}'
```

## 📝 Database Migrations

Flyway migrations in `src/main/resources/db/migration/`:

```
V1__initial_schema.sql
V2__phase_a_sites_and_client_accounts.sql
V3__phase_a_site_posts_and_shifts.sql
V4__phase_b_guard_assignments.sql
V5__phase_b_attendance_logs.sql
V6__seed_shift_types.sql
V7__supervisor_site_mapping.sql
V8__client_site_access.sql
```

Migrations run automatically on startup.

## 📚 Documentation

- API Docs: Run server and visit `/swagger-ui.html` (if Swagger enabled)
- Actuator: `http://localhost:8080/actuator`
- Health: `http://localhost:8080/actuator/health`

## 🤝 Contributing

1. Create feature branch
2. Make changes
3. Run tests: `mvn test`
4. Commit with conventional commits
5. Push and create PR

## 📄 License

Proprietary - All rights reserved

## 👥 Team

Zpluse Security - Security Guard Management System

---

**Production URL**: https://sgms-backend-production.up.railway.app
