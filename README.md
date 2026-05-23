# Personal Finance Manager API

A RESTful API built with **Spring Boot 3.x** and **Java 17** for managing personal finances — transactions, categories, savings goals, and reports.

## Tech Stack

| Component | Technology | Why |
|-----------|-----------|-----|
| Language | Java 17 | LTS, required by assignment |
| Framework | Spring Boot 3.2 | Industry standard, rapid REST API development |
| Security | Spring Security (session-based) | Assignment spec: session cookies |
| Database | H2 (in-memory) | Zero setup, sufficient for demo |
| Build | Maven | Standard enterprise Java |
| Tests | JUnit 5 + Mockito | Assignment requirement |

## Prerequisites

- **Java 17+** — https://adoptium.net/
- **Maven** (or use included `./mvnw` wrapper)

## Running Locally

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/personal-finance-manager.git
cd personal-finance-manager

# Run with Maven wrapper (no Maven install needed)
./mvnw spring-boot:run
```

Server starts at: `http://localhost:8080`

H2 Console (dev): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa`, Password: (empty)

## Running Tests

```bash
./mvnw test
```

Coverage report: `target/site/jacoco/index.html`

## API Documentation

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login (sets session cookie) |
| POST | `/api/auth/logout` | Logout (invalidates session) |

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions` | Create transaction |
| GET | `/api/transactions` | Get all (filter: `?startDate=&endDate=&categoryId=`) |
| PUT | `/api/transactions/{id}` | Update transaction |
| DELETE | `/api/transactions/{id}` | Delete transaction |

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | Get all categories |
| POST | `/api/categories` | Create custom category |
| DELETE | `/api/categories/{name}` | Delete custom category |

### Savings Goals

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/goals` | Create goal |
| GET | `/api/goals` | Get all goals |
| GET | `/api/goals/{id}` | Get specific goal |
| PUT | `/api/goals/{id}` | Update goal |
| DELETE | `/api/goals/{id}` | Delete goal |

### Reports

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reports/monthly/{year}/{month}` | Monthly report |
| GET | `/api/reports/yearly/{year}` | Yearly report |

## Design Decisions

1. **Session-based auth over JWT**: The assignment explicitly requires sessions with cookies. Sessions are server-side, making them easy to invalidate on logout.

2. **H2 in-memory DB**: No infrastructure overhead. Render's free tier works perfectly with this.

3. **BigDecimal for money**: Avoids IEEE 754 floating-point precision issues. `0.1 + 0.2 == 0.30000000000000004` in double — unacceptable for finance.

4. **Soft delete for categories**: Categories referenced by transactions can't be hard-deleted (would break transaction history). Marking `deleted=true` preserves history while hiding them from the UI.

5. **Dynamic progress calculation**: Savings goal progress is recalculated from live transaction data each request, so deleting transactions automatically updates goal progress.

## Running the Test Script

```bash
bash financial_manager_tests.sh https://YOUR-APP.onrender.com/api
```

## Deployment (Render)

1. Push code to GitHub (public repo)
2. Create account at render.com
3. New → Web Service → Connect GitHub repo
4. Build Command: `./mvnw clean package -DskipTests`
5. Start Command: `java -jar target/personal-finance-manager-0.0.1-SNAPSHOT.jar`
6. Environment: Java, Free tier