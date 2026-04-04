# 💰 FinTech Service — Finance Dashboard Backend

> A secure, role-driven **Finance Dashboard Backend** built with **Spring Boot 3.x** — featuring JWT authentication, four-tier role-based access control, full transaction management, and multi-scope dashboard analytics.

---

## ⚡ Tech Stack

| | |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x |
| **Security** | Spring Security + JWT (Stateless) |
| **ORM** | Spring Data JPA (Hibernate) |
| **Database** | H2 In-Memory |
| **Build** | Maven |
| **API Style** | RESTful JSON |

---

## ✨ Features

| Feature | Detail |
|---|---|
| 🔐 JWT Auth | Stateless token-based auth with email & password |
| 👥 4-Role RBAC | Admin · Analyst · User · Viewer with strict enforcement |
| 🧾 Transaction CRUD | Create, read, update, soft-delete with filtering & pagination |
| 📊 Dashboard Analytics | Summary, category totals, monthly trends — scoped by role |
| 📤 CSV Export | Filtered transaction data downloadable as `.csv` |
| ✅ Validation | Bean validation with regex & field-level error messages |
| ⚠️ Global Exception Handler | Centralized responses with correct HTTP status codes |
| 🔄 ACID Transactions | `@Transactional` on all financial write operations |
| 🧵 Java Streams | Used throughout service layer for aggregation & mapping |
| 📄 Pagination | All listing endpoints support `page` & `size` query params |
| 🗑️ Soft Delete | Transactions flagged `deleted=true` — never physically removed |
| 📝 Manual JPQL Queries | Custom `@Query` for aggregations, filters, and trend calculations |
| 🤖 Seeded Default Users | `CommandLineRunner` auto-creates default accounts on startup |
| 🎨 Design Patterns | Singleton · Builder · Proxy |

---

## 🏗️ Project Structure

```
com/fintech/fintech_service/
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── TransactionController.java
│   └── DashboardController.java
├── dto/
│   ├── auth/
│   │   ├── AuthRequest.java
│   │   └── AuthResponse.java
│   ├── dashboard/
│   │   ├── CategoryTotal.java
│   │   ├── DashboardSummary.java
│   │   ├── ExportRequest.java
│   │   └── MonthlyTrend.java
│   ├── roleandstatus/
│   │   ├── UpdateRoleRequest.java
│   │   └── UpdateStatusRequest.java
│   ├── transaction/
│   │   ├── TransactionRequest.java
│   │   └── TransactionResponse.java
│   └── user/
│       ├── UserRequest.java
│       └── UserResponse.java
├── entity/
│   ├── Transaction.java
│   └── User.java
├── enums/
│   ├── Role.java
│   ├── Status.java
│   └── TransactionType.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceAlreadyExistsException.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── TransactionRepository.java
│   └── UserRepository.java
├── security/
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── filter/
│   │   └── JwtAuthFilter.java
│   ├── service/
│   │   ├── CustomInitializer.java
│   │   └── CustomUserDetailsService.java
│   └── util/
│       ├── JWTUtil.java
│       └── SecurityUtil.java
├── service/
│   ├── DashboardService.java
│   ├── TransactionService.java
│   └── UserService.java
└── FintechServiceApplication.java
```

---

## ⚙️ Setup & Run

```bash
# 1. Clone
git clone https://github.com/your-username/fintech-service.git
cd fintech-service

# 2. Build & Run
mvn clean install
mvn spring-boot:run
```

Server starts at **`http://localhost:8080`**

**`application.properties`** — no changes needed for dev:
```properties
spring.datasource.url=jdbc:h2:mem:fintechdb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
jwt.secret=your-256-bit-secret-key
jwt.expiration=86400000
```

> H2 Console → `http://localhost:8080/h2-console` · JDBC URL: `jdbc:h2:mem:fintechdb` · User: `sa` · Password: *(blank)*

---

## 👤 Default Users *(Auto-seeded on startup)*

| Name | Mobile | Password | Role |
|---|---|---|---|
| Admin | 9353177331 | Admin@123 | ADMIN |
| Analyst | 6778986543 | Analyst@123 | ANALYST |
|  User | 6363622640 | User@123 | USER |
| Viewer | 8788899765 | Viewer@123 | VIEWER |

> Login directly with these credentials — no registration needed.

---

## 🔐 Authentication

```
POST /api/v1/auth/register  →  Account created with VIEWER role (default)
POST /api/v1/auth/login     →  Returns JWT token
All other requests          →  Authorization: Bearer <token>
```

> Self-registered users always start as **VIEWER**. Only an **Admin** can promote roles via `PUT /api/v1/users/{id}/role`. This is intentional by design — role assignment is a privileged operation.

---

## 🛡️ Role & Access Matrix

| Capability | Viewer | User | Analyst | Admin |
|---|:---:|:---:|:---:|:---:|
| Login & register | ✅ | ✅ | ✅ | ✅ |
| Create own transactions | ❌ | ✅ | ❌ | ❌ |
| View own transactions (`/my`) | ✅ | ✅ | ✅ | ✅ |
| Own dashboard — summary, categories, trends, recent | ✅ | ✅ | ✅ | ✅ |
| View ALL transactions | ❌ | ❌ | ✅ | ✅ |
| Overall dashboard (all users combined) | ❌ | ❌ | ✅ | ✅ |
| Export CSV | ❌ | ❌ | ✅ | ✅ |
| Update / delete any transaction | ❌ | ❌ | ❌ | ✅ |
| Per-user dashboard (`/user/{id}/*`) | ❌ | ❌ | ❌ | ✅ |
| Full user management | ❌ | ❌ | ❌ | ✅ |

---

## 📡 API Reference

### 🔐 Auth — `/api/v1/auth` *(Public)*

| Method | Endpoint | Description |
|---|---|---|
| POST | `/register` | Register — always assigned VIEWER role |
| POST | `/login` | Authenticate and receive JWT token |

```json
// POST /login
{ "email": "alice@fintech.com", "password": "Admin@123" }

// Response
{ "token": "eyJhbGci...", "type": "Bearer", "userId": 1, "name": "Alice Admin", "role": "ADMIN" }
```

---

### 👤 User Management — `/api/v1/users` *(Admin only)*

| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | List all users |
| GET | `/{id}` | Get user by ID |
| POST | `/` | Create user directly |
| PUT | `/{id}` | Update name / email |
| PUT | `/{id}/role` | Change role |
| PATCH | `/{id}/status` | Activate or deactivate account |
| DELETE | `/{id}` | Delete user permanently |

```json
// PUT /{id}/role  →  allowed values: "VIEWER" | "USER" | "ANALYST" | "ADMIN"
{ "role": "USER" }

// PATCH /{id}/status
{ "active": false }
```

---

### 💳 Transactions — `/api/v1/transactions`

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/` | Analyst, Admin | All transactions — paginated & filterable |
| GET | `/{id}` | Analyst, Admin | Single transaction by ID |
| GET | `/user/{userId}` | Admin | Transactions for a specific user |
| GET | `/my` | **All roles** | Logged-in user's own transactions |
| POST | `/` | **User**, Admin | Create new transaction |
| PUT | `/{id}` | Admin | Update transaction |
| DELETE | `/{id}` | Admin | Soft delete (sets `deleted=true`) |

```json
// POST /  or  PUT /{id}
{
  "amount": 60000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-04-01",
  "notes": "April monthly salary"
}
```

**Query params for `GET /` and `GET /my`:**

| Param | Example | Notes |
|---|---|---|
| `type` | `INCOME` | `INCOME` or `EXPENSE` |
| `category` | `Salary` | Exact match |
| `from` | `2026-01-01` | ISO 8601 |
| `to` | `2026-04-30` | ISO 8601 |
| `page` | `0` | Default 0 |
| `size` | `10` | Default 10 |

---

### 📊 Dashboard — `/api/v1/dashboard`

#### Overall *(Analyst + Admin — all users combined)*

| Method | Endpoint | Description |
|---|---|---|
| GET | `/summary` | Total income, expenses, net balance |
| GET | `/categories` | Category-wise totals |
| GET | `/trends` | Monthly income vs expense breakdown |

#### My Data *(All roles — own data only)*

| Method | Endpoint | Description |
|---|---|---|
| GET | `/my-summary` | Own income, expenses, net balance |
| GET | `/my-categories` | Own category-wise totals |
| GET | `/my-trends` | Own monthly trends |
| GET | `/my-recent` | Own last 10 transactions |

#### By User ID *(Admin only)*

| Method | Endpoint | Description |
|---|---|---|
| GET | `/user/{id}/summary` | Specific user's summary |
| GET | `/user/{id}/categories` | Specific user's categories |
| GET | `/user/{id}/trends` | Specific user's monthly trends |

#### Export *(Analyst + Admin)*

| Method | Endpoint | Description |
|---|---|---|
| POST | `/export` | Download filtered transactions as CSV |

```json
// POST /export  →  all fields optional, send {} to export everything
{ "type": "EXPENSE", "from": "2026-04-01", "to": "2026-04-30" }
```

> **Postman:** Send → `Save Response → Save to file` → `transactions.csv`

**Sample dashboard responses:**
```json
// /summary  or  /my-summary
{ "totalIncome": 157000.00, "totalExpenses": 27500.00, "netBalance": 129500.00 }

// /trends  or  /my-trends
[{ "year": 2026, "month": 4, "monthName": "April",
   "totalIncome": 85000.00, "totalExpenses": 25700.00, "net": 59300.00 }]

// /categories  or  /my-categories
[{ "category": "Salary", "total": 60000.00, "type": "INCOME"  },
 { "category": "Rent",   "total": 18000.00, "type": "EXPENSE" }]
```

---

## ⚠️ Error Handling

All errors follow a consistent format:

```json
{ "error": "Descriptive message here" }
```

| Status | Trigger |
|---|---|
| `400` | Validation failed — field-level messages returned |
| `401` | Missing or expired JWT token |
| `403` | Authenticated but role is insufficient |
| `404` | Resource not found |
| `409` | Duplicate resource (e.g. email already registered) |

```
GlobalExceptionHandler (@RestControllerAdvice)
├── MethodArgumentNotValidException  →  400  (field errors map)
├── ResourceNotFoundException        →  404
├── AccessDeniedException            →  403
├── BadCredentialsException          →  401
├── ResourceAlreadyExistException    →  409
└── Exception                        →  500  (catch-all)
```

---

## 🎨 Design Patterns

| Pattern | Where Used |
|---|---|
| **Singleton** | All `@Service`, `@Repository`, `@Component` beans — one instance per Spring context |
| **Builder** | DTO and entity construction via Lombok `@Builder` for clean, readable object creation |
| **Proxy** | Spring Security wraps controllers via AOP proxy — `@PreAuthorize` intercepts before execution |

---

## 🧪 Postman Testing Sequence

```
1.  Login as Alice Admin                →  copy ADMIN token
2.  Promote Dave → USER role            →  PUT /users/3/role  { "role": "USER" }
3.  Promote Carol → ANALYST             →  PUT /users/2/role  { "role": "ANALYST" }
4.  Seed 4 transactions (Admin token)   →  POST /transactions  ×4
5.  Login as Dave (USER token)
    ├── POST /transactions              →  ✅ 201 Created (User can create own)
    ├── GET  /transactions/my           →  ✅ 200 (own transactions)
    ├── GET  /dashboard/my-summary      →  ✅ 200 (own summary)
    ├── GET  /dashboard/my-recent       →  ✅ 200 (last 10)
    ├── GET  /dashboard/my-categories   →  ✅ 200 (category wise)
    ├── GET  /dashboard/my-trends       →  ✅ 200 (monthly trends)
    └── GET  /transactions              →  ❌ 403 Forbidden
6.  Login as Carol (ANALYST token)
    ├── GET  /dashboard/summary         →  ✅ 200 (overall data)
    └── POST /transactions              →  ❌ 403 Forbidden
7.  Login as Bob (VIEWER token)
    ├── GET  /dashboard/my-summary      →  ✅ 200
    └── POST /transactions              →  ❌ 403 Forbidden
8.  Export CSV (Admin/Analyst token)    →  POST /export  {}
```

---

## 🚀 Future Improvements

> Intentionally omitted to keep the submission clean and focused.

- **Redis Cache** — Cache dashboard aggregations for high-read performance
- **Structured Logging** — SLF4J + Logback with request tracing
- **Spring Profiles** — `dev` / `prod` configs with PostgreSQL in production
- **Unit & Integration Tests** — JUnit 5 + Mockito service and controller coverage
- **Swagger / OpenAPI** — Auto-generated interactive docs at `/swagger-ui.html`
- **Docker** — `Dockerfile` + `docker-compose` for one-command deployment

---

<div align="center">

**FinTech Service** &nbsp;·&nbsp; Java 17 &nbsp;·&nbsp; Spring Boot &nbsp;·&nbsp; Spring Security &nbsp;·&nbsp; JWT &nbsp;·&nbsp; H2

</div>
