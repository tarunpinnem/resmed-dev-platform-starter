# Healthcare Platform Starter Kit

[![CI Pipeline](https://github.com/tarunpinnem/resmed-dev-platform-starter/actions/workflows/ci.yml/badge.svg)](https://github.com/tarunpinnem/resmed-dev-platform-starter/actions)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.2-blue.svg)](https://react.dev/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A **cloud-native, secure, and developer-friendly** healthcare service starter kit. Spin up a full-stack production-ready application in under 2 minutes with best practices baked in.

## ✨ Features

| Feature | Description |
|---------|-------------|
| **React Frontend** | Modern UI with TypeScript, Tailwind CSS, React Query |
| **JWT Authentication** | Secure stateless authentication with role-based access |
| **Patient CRUD API** | Complete RESTful API for healthcare entity management |
| **OpenAPI/Swagger** | Interactive API documentation at `/swagger-ui.html` |
| **Rate Limiting** | Token bucket algorithm to prevent API abuse |
| **Correlation IDs** | Request tracing for distributed debugging |
| **Structured Logging** | JSON logs ready for ELK/Splunk/CloudWatch |
| **Health Probes** | Kubernetes-ready `/health` and `/ready` endpoints |
| **Docker Ready** | Multi-stage Dockerfile + docker-compose |
| **CI/CD Pipeline** | GitHub Actions with build, test, and security scanning |

## 🚀 Quick Start (< 2 minutes)

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local development)

### One-Command Setup

```bash
# Clone and run
git clone https://github.com/tarunpinnem/resmed-dev-platform-starter.git
cd resmed-dev-platform-starter
docker compose up -d
```

**That's it!** The application is now running:
- **Frontend UI**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Verify It's Working

```bash
# Health check
curl http://localhost:8080/api/v1/health

# Expected response:
# {"status":"UP","timestamp":"2026-01-30T...","liveness":"CORRECT"}
```

### Frontend Screenshots

The React frontend includes:
- **Login Page** - Demo credentials displayed for easy testing
- **Dashboard** - System health status and recent patients
- **Patients List** - Paginated table with search
- **Patient Details** - Full patient information view
- **Patient Form** - Create and edit patients

## 📖 API Documentation

Once running, access the interactive Swagger UI:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔐 Authentication

### Demo Credentials

| Username | Password | Roles |
|----------|----------|-------|
| `admin` | `admin123` | ADMIN, USER |
| `user` | `user123` | USER |
| `doctor` | `doctor123` | DOCTOR, USER |
| `nurse` | `nurse123` | NURSE, USER |

### Get a Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Response:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "username": "admin",
    "roles": ["ADMIN", "USER"]
  }
}
```

### Use the Token

```bash
export TOKEN="your-jwt-token-here"

curl http://localhost:8080/api/v1/patients \
  -H "Authorization: Bearer $TOKEN"
```

## 📋 API Examples

### Create a Patient

```bash
curl -X POST http://localhost:8080/api/v1/patients \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-05-15",
    "email": "john.doe@example.com",
    "phone": "+14155551234",
    "address": "123 Healthcare Ave, Medical City, MC 12345"
  }'
```

### Get Patient by ID

```bash
curl http://localhost:8080/api/v1/patients/{id} \
  -H "Authorization: Bearer $TOKEN"
```

### List All Patients (with pagination)

```bash
curl "http://localhost:8080/api/v1/patients?page=0&size=20&search=John" \
  -H "Authorization: Bearer $TOKEN"
```

### Update a Patient

```bash
curl -X PUT http://localhost:8080/api/v1/patients/{id} \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "dateOfBirth": "1990-05-15"
  }'
```

### Delete a Patient

```bash
curl -X DELETE http://localhost:8080/api/v1/patients/{id} \
  -H "Authorization: Bearer $TOKEN"
```

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Applications                       │
│                    (Web, Mobile, Third-party)                    │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     API Gateway / Load Balancer                  │
│                    (Rate Limiting, SSL Termination)              │
└─────────────────────────────┬───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                  Healthcare Platform Service                     │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Security Layer                          │  │
│  │  ┌─────────────┐  ┌──────────────┐  ┌─────────────────┐   │  │
│  │  │ JWT Filter  │  │ Rate Limiter │  │ Correlation ID  │   │  │
│  │  └─────────────┘  └──────────────┘  └─────────────────┘   │  │
│  └───────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                   Controller Layer                         │  │
│  │  ┌──────────────┐  ┌────────────┐  ┌─────────────────┐    │  │
│  │  │ PatientCtrl  │  │ AuthCtrl   │  │ HealthCtrl      │    │  │
│  │  └──────────────┘  └────────────┘  └─────────────────┘    │  │
│  └───────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Service Layer                           │  │
│  │  ┌──────────────────────────────────────────────────────┐ │  │
│  │  │              Business Logic & Validation              │ │  │
│  │  └──────────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                  Repository Layer (JPA)                    │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     PostgreSQL Database                          │
│                    (Patient Records Store)                       │
└─────────────────────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
healthcare-platform-starter/
├── frontend/                     # React Frontend
│   ├── src/
│   │   ├── api/                  # API client
│   │   ├── components/           # React components
│   │   ├── context/              # Auth context
│   │   ├── pages/                # Page components
│   │   └── types/                # TypeScript types
│   ├── package.json
│   └── Dockerfile
├── src/                          # Spring Boot Backend
│   ├── main/
│   │   ├── java/com/healthcare/platform/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── exception/        # Exception handling
│   │   │   ├── filter/           # HTTP filters
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── security/         # JWT & security
│   │   │   └── service/          # Business logic
│   │   └── resources/
│   │       ├── application.yml   # Application config
│   │       └── logback-spring.xml
│   └── test/
│       └── java/com/healthcare/platform/
│           ├── controller/       # Controller tests
│           ├── integration/      # Integration tests
│           ├── security/         # Security tests
│           └── service/          # Service tests
├── .github/workflows/ci.yml      # CI/CD pipeline
├── docker-compose.yml            # Production compose
├── docker-compose.dev.yml        # Development compose
├── Dockerfile                    # Backend multi-stage build
└── pom.xml                       # Maven config
```

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Run Integration Tests

```bash
./mvnw verify
```

### Test Coverage Report

```bash
./mvnw test jacoco:report
# Open target/site/jacoco/index.html
```

## 📊 Metrics

| Metric | Value |
|--------|-------|
| One-command setup | < 2 minutes |
| Unit tests | 15+ tests |
| Integration tests | 9+ tests |
| Code coverage | ~80% |
| Docker image size | ~200MB |

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database name | `healthcare` |
| `DB_USERNAME` | Database user | `healthcare` |
| `DB_PASSWORD` | Database password | `healthcare123` |
| `JWT_SECRET` | JWT signing key | (default key) |
| `JWT_EXPIRATION_MS` | Token expiry (ms) | `86400000` (24h) |
| `RATE_LIMIT_ENABLED` | Enable rate limiting | `true` |
| `RATE_LIMIT_RPM` | Requests per minute | `60` |
| `SERVER_PORT` | Application port | `8080` |

### Development Mode

```bash
# Start only database
docker compose -f docker-compose.dev.yml up -d

# Run backend locally with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# In another terminal, run frontend
cd frontend
npm install
npm run dev
```

Frontend runs at http://localhost:3000 and proxies API requests to the backend.

## 📝 Response Format

All API responses follow a consistent format:

### Success Response
```json
{
  "success": true,
  "message": "Patient created successfully",
  "data": { ... },
  "timestamp": "2026-01-30T10:30:00Z",
  "correlationId": "abc123def456"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email must be valid",
      "rejectedValue": "invalid-email"
    }
  ],
  "timestamp": "2026-01-30T10:30:00Z",
  "correlationId": "abc123def456"
}
```

## 🔒 Security Features

1. **JWT Authentication**: Stateless token-based auth
2. **Password Encoding**: BCrypt hashing
3. **CORS Configuration**: Configurable allowed origins
4. **Rate Limiting**: Prevent brute force attacks
5. **Input Validation**: Bean validation on all inputs
6. **SQL Injection Prevention**: JPA parameterized queries
7. **Non-root Docker User**: Security hardened container

## 🚢 Deployment

### Docker

```bash
# Build image
docker build -t healthcare-platform .

# Run container
docker run -d -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e JWT_SECRET=your-production-secret \
  healthcare-platform
```

### Kubernetes (basic example)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: healthcare-platform
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: api
        image: healthcare-platform:latest
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /api/v1/health
            port: 8080
        readinessProbe:
          httpGet:
            path: /api/v1/ready
            port: 8080
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- The open-source community for the amazing tools

---

**Built with ❤️ for the developer community**
