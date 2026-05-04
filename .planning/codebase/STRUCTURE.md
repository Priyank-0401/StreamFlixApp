# Project Structure

## Root Directory Layout

```
StreamFlixApp/
├── .git/                                    # Git repository
├── .planning/                               # GSD planning directory
│   └── codebase/                            # Codebase mapping docs (this folder)
├── frontend/                                # React + Vite + TypeScript SPA
├── backend code/                            # Spring Boot Java backend (source only, no build files)
│   └── main/
│       ├── java/com/infy/billing/           # Java source code
│       └── resources/                       # Config, schema, validation
├── *.txt                                    # Reference/guide documents
├── *.md                                     # Documentation (schema, signup flow, readme)
├── backend code.zip                         # Archived backend code
└── README.md                                # Project README
```

---

## Frontend Structure (`frontend/`)

```
frontend/
├── index.html                               # SPA entry HTML (695 bytes)
├── package.json                             # NPM config & dependencies
├── package-lock.json                        # Lockfile
├── vite.config.ts                           # Vite config (port 3000, API proxy)
├── tsconfig.json                            # TypeScript project references
├── tsconfig.app.json                        # App TypeScript config
├── tsconfig.node.json                       # Node TypeScript config
├── eslint.config.js                         # ESLint flat config
├── public/                                  # Static assets
├── node_modules/                            # Dependencies
├── src.zip                                  # Archived source
└── src/                                     # Application source
    ├── main.tsx                             # Entry point (React root, CSS imports)
    ├── App.tsx                              # Root component (Router, AuthProvider, Routes)
    ├── App.css                              # Vite default styles (mostly unused)
    ├── index.css                            # Global styles (fonts, auth layout, buttons)
    ├── setupProxy.js                        # Legacy CRA proxy (not used by Vite)
    ├── assets/                              # Static assets
    ├── constants/
    │   └── roles.ts                         # Role constants (CUSTOMER, ADMIN, FINANCE, SUPPORT)
    ├── types/
    │   └── auth.types.ts                    # User and AuthState interfaces
    ├── context/
    │   ├── AuthContext.tsx                   # Global auth state (login, logout, session)
    │   └── CustomerContext.tsx              # Customer status context
    ├── routes/
    │   └── RoleGuard.tsx                    # Role-based route protection component
    ├── services/
    │   ├── auth/
    │   │   ├── authService.ts              # Login, register, session check, fetchWithSession
    │   │   └── authTypes.ts                # LoginRequest, RegisterRequest types
    │   ├── admin/
    │   │   ├── adminService.ts             # All admin CRUD operations (130 lines)
    │   │   └── adminTypes.ts               # Admin DTO interfaces (130 lines)
    │   ├── customer/
    │   │   └── customerService.ts          # All customer API calls + types (552 lines)
    │   └── public/
    │       └── publicService.ts            # Public plan listing (44 lines)
    ├── styles/
    │   ├── LandingPage.css                 # Landing page styles (28KB)
    │   ├── admin.css                       # Admin panel styles (14KB)
    │   ├── admin-modal.css                 # Admin modal styles (6KB)
    │   ├── auth.css                        # Authentication page styles (6KB)
    │   └── bootstrap-custom.css            # Bootstrap theme overrides (11KB)
    ├── components/
    │   ├── admin/
    │   │   ├── layout/
    │   │   │   ├── AdminLayout.tsx         # Admin shell (sidebar + outlet)
    │   │   │   ├── Sidebar.tsx             # Admin sidebar navigation
    │   │   │   └── TopBar.tsx              # Admin top bar
    │   │   ├── dashboard/
    │   │   │   └── StatsCard.tsx           # Dashboard statistics card
    │   │   └── shared/
    │   │       ├── AdminModal.tsx           # Reusable admin modal
    │   │       ├── DataTable.tsx            # Generic data table
    │   │       ├── FormField.tsx            # Form field wrapper
    │   │       ├── PageHeader.tsx           # Page header with title
    │   │       └── StatusBadge.tsx          # Status indicator badge
    │   ├── auth/
    │   │   ├── customer/
    │   │   │   ├── CustomerLoginForm.tsx    # Customer login form
    │   │   │   └── CustomerSignupForm.tsx   # Customer registration form
    │   │   ├── management/
    │   │   │   └── ManagementLoginForm.tsx  # Admin/staff login form
    │   │   └── shared/
    │   │       ├── AuthLayout.tsx           # Auth page layout wrapper
    │   │       └── FormFields.tsx           # Reusable form field components
    │   └── customer/
    │       ├── layout/
    │       │   └── CustomerLayout.tsx       # Customer dashboard shell (13KB)
    │       ├── pages/
    │       │   ├── OverviewPage.tsx         # Dashboard overview (12KB)
    │       │   ├── SubscriptionPage.tsx     # Subscription management (18KB)
    │       │   ├── BillingPage.tsx          # Billing & invoices (9KB)
    │       │   ├── PaymentMethodsPage.tsx   # Payment methods (8KB)
    │       │   ├── ProfilePage.tsx          # User profile (6KB)
    │       │   └── SupportPage.tsx          # Support/contact (6KB)
    │       └── subscription/
    │           ├── CustomerDetailsStep.tsx  # Onboarding step 1 (10KB)
    │           ├── PaymentMethodStep.tsx    # Onboarding step 2 (14KB)
    │           └── MockPaymentStep.tsx      # Simulated payment step (8KB)
    └── pages/
        ├── public/
        │   ├── LandingPage.tsx             # Landing/hero page (17KB)
        │   └── PlansPage.tsx               # Plans comparison page (11KB)
        ├── auth/
        │   ├── CustomerAuthPage.tsx        # Customer auth page wrapper
        │   └── ManagementAuthPage.tsx      # Management auth page wrapper
        ├── customer/
        │   ├── OverviewPage.tsx            # Customer overview (with CSS)
        │   ├── SubscriptionPage.tsx        # Subscription page (with CSS)
        │   ├── SubscriptionFlow.tsx        # Multi-step subscription flow
        │   ├── SubscriptionCheckoutPage.tsx # Checkout page (with CSS)
        │   ├── BillingPage.tsx             # Billing page (with CSS)
        │   ├── PaymentMethodsPage.tsx      # Payment methods (with CSS)
        │   ├── ProfilePage.tsx             # Profile page (with CSS)
        │   ├── SupportPage.tsx             # Support page (with CSS)
        │   ├── *.css                       # Per-page CSS files
        └── admin/
            ├── AdminDashboardPage.tsx      # Admin dashboard (12KB)
            ├── catalog/
            │   ├── ProductPage.tsx         # Product management
            │   ├── PlansPage.tsx           # Plans management
            │   ├── AddOnsPage.tsx          # Add-ons management
            │   └── MeteredComponentsPage.tsx # Metered components
            ├── pricing/
            │   ├── PriceBooksPage.tsx      # Price book management
            │   ├── TaxRatesPage.tsx        # Tax rate management
            │   └── CouponsPage.tsx         # Coupon management
            ├── users/
            │   ├── CustomersPage.tsx       # Customer listing
            │   └── StaffAccountsPage.tsx   # Staff account management
            └── subscriptions/
                └── SubscriptionsPage.tsx   # Subscription overview
```

---

## Backend Structure (`backend code/main/`)

```
backend code/main/
├── java/com/infy/billing/
│   ├── StreamFlixApplication.java           # Spring Boot main class
│   ├── config/
│   │   └── SecurityConfig.java              # Spring Security config (5.6KB)
│   ├── controller/
│   │   ├── AuthController.java              # Authentication endpoints (3.6KB)
│   │   ├── AdminController.java             # Admin CRUD endpoints (8.6KB)
│   │   ├── CustomerController.java          # Customer profile/status (4.7KB)
│   │   ├── CustomerSubscriptionController.java # Subscription lifecycle (3.6KB)
│   │   ├── CustomerBillingController.java   # Invoices/payments (2.8KB)
│   │   ├── CustomerPaymentController.java   # Payment methods (2KB)
│   │   └── CustomerSupportController.java   # Support/FAQ (1.2KB)
│   ├── service/
│   │   ├── AuthService.java + Impl          # Authentication (470B + 4KB)
│   │   ├── AdminDashboardService.java + Impl # Admin operations (2.4KB + 14KB)
│   │   ├── CustomerService.java + Impl      # Customer management (545B + 7KB)
│   │   ├── CustomerSubscriptionService.java + Impl # Subscriptions (814B + 13KB)
│   │   ├── CustomerBillingService.java + Impl # Billing (511B + 7.5KB)
│   │   ├── CustomerPaymentService.java + Impl # Payments (498B + 5.8KB)
│   │   ├── CustomerSupportService.java + Impl # Support (293B + 3.6KB)
│   │   └── SubscriptionFlowService.java + Impl # Onboarding flow (548B + 11.5KB)
│   ├── repository/                          # 18 JPA repositories (one per entity)
│   ├── entity/                              # 18 JPA entity classes
│   ├── dto/
│   │   ├── admin/                           # Admin-specific DTOs
│   │   ├── auth/                            # Auth DTOs
│   │   ├── customer/                        # Customer DTOs
│   │   └── error/                           # Error response DTOs
│   ├── enums/                               # 10 enum types
│   ├── exception/
│   │   ├── CustomException.java             # Custom exception class
│   │   └── GlobalExceptionHandler.java      # Centralized error handling
│   └── request/                             # 5 request DTOs
│       ├── AddPaymentMethodRequest.java
│       ├── ApplyCouponRequest.java
│       ├── CreateSubscriptionRequest.java
│       ├── PauseSubscriptionRequest.java
│       └── UpgradeSubscriptionRequest.java
└── resources/
    ├── application.properties               # Server config, DB connection, session
    ├── schema.sql                           # Full DB schema + seed data (967 lines)
    └── ValidationMessages.properties        # Bean validation messages
```

---

## Key File Locations

| What | Path |
|------|------|
| Frontend entry | `frontend/src/main.tsx` |
| Root React component | `frontend/src/App.tsx` |
| Global CSS | `frontend/src/index.css` |
| Auth context | `frontend/src/context/AuthContext.tsx` |
| API fetch wrapper | `frontend/src/services/auth/authService.ts` → `fetchWithSession()` |
| Customer API service | `frontend/src/services/customer/customerService.ts` |
| Admin API service | `frontend/src/services/admin/adminService.ts` |
| Route guard | `frontend/src/routes/RoleGuard.tsx` |
| Spring Boot main | `backend code/main/java/com/infy/billing/StreamFlixApplication.java` |
| Security config | `backend code/main/java/com/infy/billing/config/SecurityConfig.java` |
| DB schema | `backend code/main/resources/schema.sql` |

---

## Reference Documents (Root Level)

These `.txt` and `.md` files in the project root serve as **development guides**, not runnable code:

| File | Size | Purpose |
|------|------|---------|
| `README.md` | 35KB | Comprehensive project documentation |
| `Team_Project_Guide.txt` | 44KB | Team distribution & workflow guide |
| `Workflow_And_Distribution.txt` | 54KB | Detailed workflow documentation |
| `Complete_Customer_Frontend_Code.txt` | 81KB | Customer frontend reference code |
| `Complete_Customer_Backend_Code.txt` | 77KB | Customer backend reference code |
| `Admin_Dashboard_Frontend_Code.txt` | 65KB | Admin frontend reference code |
| `Complete_Updated_Repositories_and_Services.txt` | 54KB | Backend code reference |
| `Backend_Work_Guide.txt` | 41KB | Backend implementation guide |
| `Schema_Review.md` | 40KB | Schema review document |
| `Final_Merged_Schema.md` | 30KB | Final schema documentation |
| `Complete_Corrected_Entities.txt` | 35KB | Entity class reference |
| `Admin_Dashboard_Backend_Code.txt` | 36KB | Admin backend reference |
| `Complete_Backend_Code.txt` | 24KB | Backend code reference |
| `Team_Communication_Guide.txt` | 24KB | Team collaboration guide |
| `Backend_Implementation_Guide.txt` | 22KB | Backend implementation reference |
| `Customer_Signup_Flow.md` | 6KB | Customer onboarding flow spec |

---

## Naming Conventions

### Files
- **React components**: PascalCase (e.g., `CustomerLayout.tsx`, `AdminDashboardPage.tsx`)
- **Services**: camelCase (e.g., `authService.ts`, `customerService.ts`)
- **Types**: camelCase (e.g., `adminTypes.ts`, `authTypes.ts`)
- **CSS**: PascalCase or kebab-case (e.g., `LandingPage.css`, `bootstrap-custom.css`)
- **Java classes**: PascalCase (e.g., `CustomerController.java`, `AuthServiceImpl.java`)
- **Java packages**: lowercase (e.g., `com.infy.billing.controller`)

### Directories
- **Frontend**: lowercase (e.g., `components/`, `services/`, `pages/`)
- **Backend**: lowercase Java package convention (e.g., `controller/`, `service/`, `entity/`)
