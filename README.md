# JWT Authentication - Spring Boot

A JWT authentication backend built with Spring Boot, Spring Security, PostgreSQL, and RSA-signed JSON Web Tokens.

## Tech Stack

- Java 21
- Spring Boot 4.0.8
- Spring Security 7
- Spring Data JPA
- PostgreSQL
- H2 for integration tests
- Maven
- JWT with RSA public/private keys
- BCrypt password hashing
- Bean Validation
- JUnit / Spring Boot Test

## Features

- User registration
- BCrypt password hashing
- User login with JWT generation
- RSA-signed JWTs
- Persistent RSA keys stored in a PKCS12 keystore
- JWT signature and expiration validation
- Role-based authorization
- `USER` and `ADMIN` roles
- Current authenticated-user endpoint
- Input validation
- Duplicate username/email handling
- Centralized exception handling
- Integration tests using H2

## API Endpoints

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/test` | `USER` |
| GET | `/api/admin` | `ADMIN` |
| GET | `/api/users/me` | Authenticated users |

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "username": "adithya",
  "email": "adithya@example.com",
  "password": "password123"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "adithya",
  "password": "password123"
}
```

Response:

```json
{
  "token": "<JWT_TOKEN>"
}
```

### Authenticated Request

```http
GET /api/test
Authorization: Bearer <JWT_TOKEN>
```

### Current User

```http
GET /api/users/me
Authorization: Bearer <JWT_TOKEN>
```

Example response:

```json
{
  "id": 1,
  "username": "adithya",
  "email": "adithya@example.com",
  "role": "USER"
}
```

## Authentication Flow

```text
Register
Client
  -> AuthController
  -> AuthService
  -> BCrypt password hashing
  -> PostgreSQL

Login
Client
  -> AuthController
  -> AuthService
  -> Load user
  -> Verify BCrypt password
  -> Create JWT claims
  -> Sign JWT with RSA private key
  -> Return JWT

Protected Request
Client
  -> Authorization: Bearer <JWT>
  -> Spring Security
  -> Verify RSA signature
  -> Check expiration
  -> Extract role
  -> Protected Controller
```

## JWT Configuration

JWTs contain:

- `sub` - username
- `iat` - issued-at timestamp
- `exp` - expiration timestamp
- `role` - user's role

Tokens currently expire after **1 hour**.

The application uses RSA asymmetric cryptography:

- The private key signs JWTs.
- The public key verifies JWTs.
- Keys are stored in a PKCS12 (`.p12`) keystore.
- The keystore is excluded from Git.

## Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE jwt_authentication;
```

The application expects:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jwt_authentication
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
```

Set the database password using the `DB_PASSWORD` environment variable.

## JWT Keystore Setup

The application expects:

```properties
jwt.keystore.path=classpath:jwt-keystore.p12
jwt.keystore.password=${JWT_KEYSTORE_PASSWORD}
jwt.keystore.alias=jwt
```

Place the required keystore at:

```text
src/main/resources/jwt-keystore.p12
```

Set the keystore password using:

```text
JWT_KEYSTORE_PASSWORD
```

**Never commit the `.p12` keystore or its password to Git.**

## Running the Application

Clone the repository:

```bash
git clone https://github.com/AdithyaRaoK14/jwt-authentication.git
cd jwt-authentication
```

Set environment variables in PowerShell:

```powershell
$env:DB_PASSWORD="your-postgres-password"
$env:JWT_KEYSTORE_PASSWORD="your-keystore-password"
```

Run with Maven:

```bash
mvn spring-boot:run
```

Or Windows Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

## Running Tests

```bash
mvn clean test
```

Integration tests use H2 instead of the development PostgreSQL database.

The test suite covers:

- Registration
- Login
- JWT generation
- Authenticated requests
- Role-based authorization
- Unauthorized requests
- Invalid credentials
- Duplicate users
- Validation errors
- Invalid/malformed tokens
- Current-user endpoint

## Project Structure

```text
src/
├── main/
│   ├── java/com/example/jwt_authentication/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── application.properties
│       └── application-test.properties
└── test/
    ├── java/com/example/jwt_authentication/
    └── resources/
        └── application-test.properties
```

## Security

Sensitive files and configuration are excluded from Git:

```text
*.p12
*.jks
*.key
*.pem
.env
.env.*
.idea/
target/
```

Database and keystore passwords are supplied through environment variables.

For production deployment, additional security measures such as secret management, key rotation, HTTPS/TLS, refresh tokens, rate limiting, audit logging, and deployment-specific configuration should be considered.

## Git Workflow

```bash
git status
git add .
git commit -m "Describe your changes"
git push
```

## License

No license has been specified yet.
