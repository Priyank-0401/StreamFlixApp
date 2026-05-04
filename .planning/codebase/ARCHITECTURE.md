# Architecture

## Architectural Pattern
**Monolithic Client-Server SPA** — React single-page application communicating with a Spring Boot monolith via REST APIs, using server-side session authentication.

---

## System Architecture

```
┌─────────────────────────────────────────────┐
│                 Browser                      │
│  React SPA (Vite dev server :3000)          │
│  ┌──────────────────────────────────────┐   │
│  │  BrowserRouter                        │   │
│  │  ├── AuthProvider (global state)      │   │
│  │  │   ├── Public Routes               │   │
│  │  │   ├── Customer Routes (RoleGuard) │   │
│  │  │   └── Admin Routes (RoleGuard)    │   │
│  └──────────────────────────────────────┘   │
└──────────────┬──────────────────────────────┘
               │ fetch() with credentials:'include'
               │ JSESSIONID cookie
               ▼
┌──────────────────────────────────────────────┐
│  Vite Dev Proxy (/api → :8765)              │
└──────────────┬──────────────────────────────┘
               ▼
┌──────────────────────────────────────────────┐
│  Spring Boot Backend (:8765)                 │
│  ┌──────────────────────────────────────┐   │
│  │  SecurityFilterChain                  │   │
│  │  ├── CORS config                     │   │
│  │  ├── Session management              │   │
│  │  └── URL-based authorization         │   │
│  ├──────────────────────────────────────┤   │
│  │  Controllers (REST API)              │   │
│  │  ├── AuthController                  │   │
│  │  ├── AdminController                 │   │
│  │  ├── CustomerController              │   │
│  │  ├── CustomerSubscriptionController  │   │
│  │  ├── CustomerBillingController       │   │
│  │  ├── CustomerPaymentController       │   │
│  │  └── CustomerSupportController       │   │
│  ├──────────────────────────────────────┤   │
│  │  Services (Business Logic)           │   │
│  │  ├── Interface + Impl pattern        │   │
│  │  └── Transaction management          │   │
│  ├──────────────────────────────────────┤   │
│  │  Repositories (Data Access)          │   │
│  │  └── Spring Data JPA                 │   │
│  ├──────────────────────────────────────┤   │
│  │  Entities (Domain Model)             │   │
│  │  └── JPA @Entity classes             │   │
│  └──────────────────────────────────────┘   │
└──────────────┬──────────────────────────────┘
               ▼
┌──────────────────────────────────────────────┐
│  MySQL Database (:3306)                      │
│  Database: subscription_billing              │
│  23 tables + seed data                       │
└──────────────────────────────────────────────┘
```

---

## Backend Layers

### 1. Controller Layer (`controller/`)
Controllers handle HTTP requests and delegate to services. Each controller maps to a domain area:

| Controller | Path Prefix | Responsibility |
|-----------|-------------|----------------|
| `AuthController` | `/api/customer`, `/api/manager`, `/api/auth` | Login, register, session check |
| `AdminController` | `/api/admin` | All admin CRUD operations |
| `CustomerController` | `/api/customer` | Profile, plans, status |
| `CustomerSubscriptionController` | `/api/customer/subscription` | Subscription lifecycle |
| `CustomerBillingController` | `/api/customer/invoices`, `/payments`, `/credit-notes` | Billing & invoices |
| `CustomerPaymentController` | `/api/customer/payment-methods` | Payment method management |
| `CustomerSupportController` | `/api/customer/support` | Contact form, FAQs |

### 2. Service Layer (`service/`)
All services follow **Interface + Implementation** pattern (e.g., `CustomerService` interface → `CustomerServiceImpl` class):

| Service | Purpose |
|---------|---------|
| `AuthService` / `AuthServiceImpl` | Authentication logic |
| `AdminDashboardService` / `Impl` | Admin dashboard stats, CRUD for all entities |
| `CustomerService` / `Impl` | Customer profile management |
| `CustomerSubscriptionService` / `Impl` | Subscription CRUD, upgrade, cancel, pause/resume |
| `CustomerBillingService` / `Impl` | Invoices, payments, credit notes |
| `CustomerPaymentService` / `Impl` | Payment method management |
| `CustomerSupportService` / `Impl` | Support tickets, FAQs |
| `SubscriptionFlowService` / `Impl` | Multi-step subscription onboarding flow |

### 3. Repository Layer (`repository/`)
18 Spring Data JPA repositories, one per entity. All extend `JpaRepository`. Custom query methods use Spring Data naming conventions.

### 4. Entity Layer (`entity/`)
18 JPA entity classes mapping to database tables. Uses Lombok annotations (implied), JPA annotations (`@Entity`, `@Table`, `@ManyToOne`, etc.).

### 5. DTO Layer (`dto/`)
Organized by domain:
- `dto/admin/` — Admin-specific DTOs
- `dto/auth/` — Authentication DTOs
- `dto/customer/` — Customer-facing DTOs
- `dto/error/` — Error response DTOs

### 6. Supporting Layers
- `enums/` — 10 enum types (BillingPeriod, Status, UserRole, PaymentType, etc.)
- `exception/` — `CustomException` + `GlobalExceptionHandler` (centralized error handling)
- `request/` — Request DTOs for write operations
- `config/` — `SecurityConfig` (single configuration class)

---

## Frontend Architecture

### State Management
- **React Context API** — no Redux or external state management
  - `AuthContext` — global authentication state, session management
  - `CustomerContext` — customer-specific state within dashboard

### Routing Architecture
Three route zones with role-based guards:

1. **Public Routes** (`/`, `/login`, `/register`, `/management/login`)
   - No authentication required
   - Landing page, auth forms

2. **Customer Routes** (`/dashboard/*`, `/plans`, `/subscribe`, `/checkout`)
   - Protected by `RoleGuard` with `ROLES.CUSTOMER`
   - Wrapped in `CustomerProvider` for dashboard
   - Uses `CustomerLayout` with sidebar navigation

3. **Admin Routes** (`/admin/*`)
   - Protected by `RoleGuard` with `ROLES.ADMIN`
   - Uses `AdminLayout` with sidebar + topbar

### Component Architecture

Components follow a **domain-first** organization:

```
components/
├── admin/
│   ├── layout/      → AdminLayout, Sidebar, TopBar
│   ├── dashboard/   → StatsCard
│   └── shared/      → AdminModal, DataTable, FormField, PageHeader, StatusBadge
├── auth/
│   ├── customer/    → CustomerLoginForm, CustomerSignupForm
│   ├── management/  → ManagementLoginForm
│   └── shared/      → AuthLayout, FormFields
└── customer/
    ├── layout/      → CustomerLayout
    ├── pages/       → Overview, Subscription, Billing, Payment, Profile, Support
    └── subscription/ → CustomerDetailsStep, PaymentMethodStep, MockPaymentStep
```

### Data Flow Pattern (Frontend)
1. Page component mounts
2. `useEffect` calls service function (e.g., `customerService.getInvoices()`)
3. Service function calls `fetchWithSession()` wrapper
4. `fetchWithSession()` sends `fetch()` with `credentials: 'include'` for JSESSIONID
5. Response parsed, state updated via `useState`
6. Error handling: service throws → component catches → displays error UI

---

## Authentication Flow

### Login Flow
1. User submits credentials to `CustomerLoginForm` / `ManagementLoginForm`
2. Frontend calls `/api/customer/login` or `/api/manager/login` via `authService`
3. Backend authenticates via `DaoAuthenticationProvider` + BCrypt
4. Spring Security creates session, sets `JSESSIONID` cookie
5. Frontend receives user object, calls `AuthContext.login(user)`
6. `login()` checks customer status via `/customer/status`
7. Sets `isAuthenticated=true`, stores in sessionStorage for cross-tab sync

### Session Verification (Page Reload)
1. `AuthProvider` mounts, calls `authService.checkSession()` → `GET /api/auth/me`
2. If valid session, user data returned, auth state restored
3. If 401, auth state cleared

### Authorization
- **Backend**: URL-based Spring Security rules in `SecurityConfig`
- **Frontend**: `RoleGuard` component checks `user.role` against `allowedRoles`
- Roles: `CUSTOMER`, `ADMIN`, `FINANCE`, `SUPPORT` (defined in `constants/roles.ts`)

---

## Key Design Decisions

1. **Session-based auth over JWT** — `JSESSIONID` cookies with `credentials: 'include'` on every fetch
2. **Interface+Impl service pattern** — all backend services use interface abstraction
3. **Money in minor units** — all monetary values stored as `BIGINT` (e.g., 19900 = ₹199.00)
4. **Single product model** — StreamFlix is the only product; plans are tied to it
5. **Multi-region pricing** — Price Book Entries support region/currency-specific pricing
6. **Separate customer & user tables** — 1:1 relationship, customer extends user with billing info
7. **Dual component structure** — `components/customer/pages/` duplicates page-level components alongside `pages/customer/`, suggesting a refactoring in progress
