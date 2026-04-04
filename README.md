# 💰 FinTech Service — Finance Dashboard Backend

> A secure, role-driven **Finance Dashboard Backend** built with **Spring Boot 3.x** — featuring JWT authentication, four-tier role-based access control, full transaction management, and multi-scope dashboard analytics.

---

## ⚡ Tech Stack

| | |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 4.x |
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
//H2-InMemory-Database
spring.datasource.url=jdbc:h2:mem:financedb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=fintech
spring.datasource.password=

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

//SECURITY
jwt.secret=my-super-secret-key-that-is-long-enough
jwt.expiration=3600000
```

> H2 Console → `http://localhost:8080/h2-console` · JDBC URL: `jdbc:h2:mem:fintechdb` · User: `fintech` · Password: *(blank)*

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

## 🗂️ API Overview

| **Category**         | **API**                        | **Description**                                        |
| -------------------- | ------------------------------ | ------------------------------------------------------ |
| **Auth APIs**        | Login User                     | Authenticate with mobile + password; receive JWT token |
|                      | Register User                  | Create a new account (default role: VIEWER)            |
| **User APIs**        | Create User                    | Add a new user                                         |
|                      | Get All Users                  | List all users (with optional pagination)              |
|                      | Get User by ID                 | Retrieve specific user info                            |
|                      | Update User Role               | Change user role (Admin only)                          |
|                      | Update User Status             | Activate/deactivate account (Admin only)               |
|                      | Delete User                    | Soft delete user (Admin only)                          |
| **Transaction APIs** | Create Transaction             | Add new financial record (income/expense)              |
|                      | Get All Transactions           | List transactions with filters/pagination              |
|                      | Get Transaction by ID          | Fetch a specific transaction                           |
|                      | Get Transactions by User ID    | Transactions of a user                                 |
|                      | Get My Transactions            | Transactions of logged-in user                         |
|                      | Get Transactions by Date Range | Filter by fromDate / toDate                            |
|                      | Update Transaction             | Modify transaction details                             |
|                      | Soft Delete Transaction        | Mark transaction as deleted                            |
| **Dashboard APIs**   | Get Overall Summary            | System-wide aggregated statistics (Admin/Analyst)      |
|                      | Get Overall Category Totals    | Category-wise totals across system                     |
|                      | Get Overall Monthly Trends     | Monthly transaction trends (Admin/Analyst)             |
|                      | Get Summary by User            | Aggregated data for specific user                      |
|                      | Get Categories by User         | Category totals for a user                             |
|                      | Get Monthly Trend by User      | Monthly trends for a user                              |
|                      | Get My Summary                 | Personalized summary for logged-in user                |
|                      | Get My Categories              | Category totals for logged-in user                     |
|                      | Get My Trends                  | Monthly trends for logged-in user                      |
|                      | Get My Recent                  | Latest transactions for logged-in user                 |
|                      | Export CSV                     | Download transactions/summary as CSV                   |


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

---

## 🚀 Future Improvements

> Intentionally omitted to keep the submission clean and focused.

- **Redis Cache** — Cache dashboard aggregations for high-read performance
- **Structured Logging** — SLF4J + Logback with request tracing
- **Spring Profiles** — `dev` / `prod` configs with PostgreSQL in production
- **Spring AOP Programming — Cross-cutting concerns such as logging, transactions, and security via aspects.
- **API Versioning — Support for multiple API versions to maintain backward compatibility.
- **Unit & Integration Tests** — JUnit 5 + Mockito service and controller coverage
- **Swagger / OpenAPI** — Auto-generated interactive docs at `/swagger-ui.html`
- **Docker** — `Dockerfile` + `docker-compose` for one-command deployment

---

<div align="center">

**FinTech Service** &nbsp;·&nbsp; Java 17 &nbsp;·&nbsp; Spring Boot &nbsp;·&nbsp; Spring Security &nbsp;·&nbsp; JWT &nbsp;·&nbsp; H2

</div>
