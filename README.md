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

## 🚀 Key Architectural Upgrades (V2)

### 1. 🅿️ Dynamic Parking Calculation & Management
- **Intelligent Slot Queries**: Query methods in backend repository: `countByIsOccupiedFalse()`, `countBySlotTypeAndIsOccupiedFalse(slotType)`, and `listAvailableSlots()`.
- **Classification**: Categorized into `2_WHEELER` (Bikes/Scooters) and `4_WHEELER` (Cars).
- **Dynamic Metric Cards**: Real-time counts on Admin and Security dashboards showing: Total Slots, Occupied Slots, and Available Slots (breakdown by 2-Wheeler and 4-Wheeler).
- **Interactive Modals**: Seamlessly assign available slots to flat numbers and vacate slots immediately upon resident departure.

### 2. 🧾 Standardized Maintenance Billing Engine (1 BHK vs 2 BHK)
- **Standard Carpet Areas**:
  - `1BHK`: **550 sq.ft**
  - `2BHK`: **900 sq.ft**
  - `3BHK`: **1450 sq.ft**
- **Billing Parity Model**:
  - **Fixed Society Charges (Identical for ALL units = ₹2,500/mo)**:
    - Security & Guard Services: ₹1,000.00
    - Lift & Common Area Electricity: ₹800.00
    - Sinking Fund Reserve: ₹500.00
    - Administrative & Office Fee: ₹200.00
  - **Variable Area-Proportional Charge**:
    - `Rate per sq.ft` × `Carpet Area sq.ft` (e.g., at ₹3.50/sq.ft):
      - 1 BHK (550 sq.ft) = ₹1,925.00 ➔ **Total = ₹4,425.00**
      - 2 BHK (900 sq.ft) = ₹3,150.00 ➔ **Total = ₹5,650.00**
  - **Batch Generation Endpoint**:
    - `POST /api/admin/bills/generate-monthly?month=YYYY-MM&ratePerSqFt=3.5`

### 3. 🧭 Unified 4-Workspace Navigation Architecture
Instead of 15+ scattered links, the user interface is decluttered into 4 unified tabbed workspaces:
1. **People & Units** (`pages/admin/people-units.html`): Flats, Residents, and Owners registry.
2. **Gate & Security** (`pages/admin/gate-security.html`): Parking slots (2W/4W metrics & allocation), Visitors, and Deliveries.
3. **Financials** (`pages/admin/financials.html`): Itemized maintenance invoices (1BHK vs 2BHK transparency), Payment history, and Automated monthly bill generator.
4. **Community & Desk** (`pages/admin/community-desk.html`): Helpdesk complaints, Society notices, and Amenity bookings.
- Features unified top-level breadcrumbs, floating quick action button (FAB), and floating toast notifications.

### 4. ☁️ Cloud & Non-Localhost Deployment Ready
- **Dynamic API Base Resolution**: Frontend auto-detects host (`localhost` / `127.0.0.1` ➔ `http://localhost:8080/api`; non-localhost / cloud reverse proxy ➔ `/api`).
- **Flexible CORS**: Dynamic origin configuration via `cors.allowed-origins` property or environment variable.
- **Environment-Driven Configuration**: Spring Boot `application.properties` supports standard environment variables:
  - `DB_URL` (default: `jdbc:mysql://localhost:3306/samvaya?...`)
  - `DB_USERNAME` (default: `root`)
  - `DB_PASSWORD` (default: `root`)
  - `CORS_ALLOWED_ORIGINS` (default: `http://localhost:3000,http://127.0.0.1:3000,...`)

---

## 🗄️ Database Setup & Migration

### Fresh Database Setup:
```bash
mysql -u root -p samvaya < database/schema.sql
mysql -u root -p samvaya < database/sample-data.sql
```

### Upgrading Existing Database to V2:
To apply the parking, billing, and flat carpet area schema updates without losing existing records:
```bash
mysql -u root -p samvaya < database/migration_v2.sql
```

---

## 🚀 Running the Application

### 1. Launch Backend (Port 8080)
Double-click `run-backend.bat` or run:
```bash
cd backend
.\mvnw spring-boot:run
```
*(Or `mvn test` to run test suites including `BillingEngineTest` and `ParkingServiceTest`)*

### 2. Launch Frontend (Port 3000)
Double-click `run-frontend.bat` or run:
```bash
cd frontend
python -m http.server 3000
```
Open your browser at `http://localhost:3000`.

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
│   ├── schema.sql              # Complete normalized DDL
│   ├── migration_v2.sql        # Step-by-step idempotent V2 upgrade script
│   ├── sample-data.sql         # Seed data & test accounts
│   └── queries.sql             # Diagnostic queries
├── backend/
│   ├── pom.xml                 # Maven POM with Spring Boot & MySQL Connector
│   └── src/
│       ├── test/java/com/samvaya/service/
│       │   ├── BillingEngineTest.java   # 1BHK vs 2BHK billing calculation tests
│       │   └── ParkingServiceTest.java # Parking stats & slot assignment tests
│       └── main/
│           ├── resources/
│           │   └── application.properties # Dynamic DB & CORS configuration
│           └── java/com/samvaya/
│               ├── config/CorsConfig.java
│               ├── controller/          # Includes ParkingController, AdminController
│               ├── service/             # Includes ParkingService, PaymentService
│               ├── model/               # Includes ParkingSlot, Flat, MaintenanceBill
│               ├── dto/                 # Includes ParkingSlotDTO, BillDTO, FlatDTO
│               └── repository/          # Includes ParkingSlotRepository
├── frontend/
│   ├── index.html              # SAMVAYA Gateway & Portal Selector
│   ├── css/
│   │   ├── main.css            # Design tokens & Connected Living theme
│   │   ├── dashboard.css       # Sidebar, Topbar, Workspace tabs, Breadcrumbs, FAB
│   │   ├── admin.css           # Admin module styling
│   │   ├── resident.css        # Resident module styling
│   │   └── security.css        # Security gate styling
│   ├── js/
│   │   ├── config.js           # Dynamic API Base URL detection
│   │   ├── api/                # REST API Fetch clients (adminApi, securityApi, etc.)
│   │   ├── components/         # Sidebar, Navbar, Modal, Table, Toast
│   │   └── pages/              # Admin, Resident, Security controllers
│   └── pages/
│       ├── admin/              # 4 Unified Workspaces + legacy redirects
│       │   ├── people-units.html
│       │   ├── gate-security.html
│       │   ├── financials.html
│       │   └── community-desk.html
│       ├── resident/           # Resident HTML views
│       └── security/           # Security HTML views
├── run-backend.bat             # Backend launcher script
├── run-frontend.bat            # Frontend launcher script
└── README.md
```
