# 🏢 SAMVAYA — Society Management System

> **Connected Living. Unified Residential Intelligence.**
> A production-grade Society Management System built with **Spring Boot REST API**, **Local MySQL (`samvaya`)**, and a **Multi-Role HTML5 / CSS3 / Vanilla JavaScript Frontend**.

---

## 🏛️ System Architecture

- **Backend**: Java 17+, Spring Boot 3.2.3, Spring Data JPA, Hibernate, MySQL Connector/J, Jakarta Validation, Lombok.
- **Database**: Local MySQL 8.x database named `samvaya`, managed via MySQL Workbench / CLI.
- **Frontend**: Pure HTML5, CSS3 (Connected Living Theme: Midnight Navy `#030725`, Heritage Teal `#006a63`, Sunset Coral `#e55d32`), Vanilla JavaScript ES6+ (Fetch API). **No React framework used.**
- **User Roles**: 3 Core Roles only:
  1. **Admin** (`ADMIN`): Society administration, units, resident registry, finance/billing, staff, notices, reports.
  2. **Resident** (`RESIDENT`): Owners & Tenants with home services, visitor passes, deliveries, complaints, amenity booking, dues payment.
  3. **Security** (`SECURITY_GUARD`): Gate operations, visitor check-in/out, deliveries, workers, FastTag RFID vehicle verification, incidents.

---

## 🗄️ Database Setup (Local MySQL Workbench)

1. Open **MySQL Workbench** or MySQL Command Line.
2. Execute the schema script:
   ```sql
   source database/schema.sql;
   ```
3. Seed default mock society data & credentials:
   ```sql
   source database/sample-data.sql;
   ```
4. Verify table creation and queries:
   ```sql
   source database/queries.sql;
   ```

---

## 🚀 Running the Application

### 1. Launch Backend (Port 8080)
Double-click `run-backend.bat` or run:
```bash
cd backend
.\mvnw spring-boot:run
```
*(Or `mvn spring-boot:run` in a fresh terminal)*
*API Base URL:* `http://localhost:8080/api`

### 2. Launch Frontend (Port 3000)
Double-click `run-frontend.bat` or run:
```bash
cd frontend
python -m http.server 3000
```
Open your browser at `http://localhost:3000` (or `http://localhost:3000/index.html`).

---

## 🔑 Default Test Accounts

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Full Society Governance |
| **Resident (Owner)** | `owner1` | `owner123` | Wing A-101 (Vikramaditya Singhania) |
| **Resident (Tenant)**| `tenant1` | `tenant123` | Wing B-202 (Ananya Sharma) |
| **Security Guard** | `guard1` | `guard123` | Main Gate 1 (Ramesh Kumar) |

---

## 📂 Project Structure

```
Samvaya/
├── database/
│   ├── schema.sql              # Complete normalized DDL (28 tables)
│   ├── sample-data.sql         # Seed data & test accounts
│   └── queries.sql             # Diagnostic queries
├── backend/
│   ├── pom.xml                 # Maven POM with Spring Boot & MySQL Connector
│   └── src/main/
│       ├── resources/
│       │   └── application.properties # Local MySQL config
│       └── java/com/samvaya/
│           ├── SamvayaApplication.java
│           ├── config/CorsConfig.java
│           ├── model/          # 28 JPA entity classes
│           ├── repository/     # 20+ Spring Data JPA Repositories
│           ├── dto/            # Request/Response DTOs
│           ├── exception/      # GlobalExceptionHandler & custom exceptions
│           ├── service/        # Business logic services
│           └── controller/     # REST API Controllers
├── frontend/
│   ├── index.html              # SAMVAYA Gateway & Portal Selector
│   ├── css/
│   │   ├── main.css            # Design tokens & Connected Living theme
│   │   ├── dashboard.css       # Sidebar, Topbar, and Table layout
│   │   ├── admin.css           # Admin module styling
│   │   ├── resident.css        # Resident module styling
│   │   └── security.css        # Security gate styling
│   ├── js/
│   │   ├── config.js           # API config & authentication state
│   │   ├── api/                # REST API Fetch clients
│   │   ├── components/         # Sidebar, Navbar, Modal, Table, Toast
│   │   └── pages/              # Login, Admin, Resident, Security controllers
│   └── pages/
│       ├── admin/              # 15 Admin HTML views
│       ├── resident/           # 13 Resident HTML views
│       └── security/           # 8 Security HTML views
├── run-backend.bat             # Backend launcher script
├── run-frontend.bat            # Frontend launcher script
└── README.md
```
