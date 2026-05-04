# Technology Stack

## Overview
StreamFlixApp is a **Subscription Billing & Revenue Management System** for a streaming platform called StreamFlix. It uses a Spring Boot (Java) backend with a React (TypeScript) frontend, connected via REST APIs over HTTP session-based authentication.

---

## Languages & Runtimes

| Layer | Language | Version |
|-------|----------|---------|
| Frontend | TypeScript | ~6.0.2 |
| Frontend | JavaScript (JSX/TSX) | ES2020+ |
| Backend | Java | (Spring Boot, implied JDK 17+) |
| Database | SQL (MySQL dialect) | MySQL |

---

## Frontend Stack

### Core Framework
- **React** `^19.2.5` — latest React with hooks and function components exclusively
- **React DOM** `^19.2.5`
- **Vite** `^8.0.9` — build tool and dev server
- **TypeScript** `~6.0.2`

### Routing
- **react-router-dom** `^7.14.2` — declarative routing with `BrowserRouter`, `Routes`, `Route`

### UI Framework
- **Bootstrap** `^5.3.8` — CSS framework (imported via `bootstrap/dist/css/bootstrap.min.css`)
- **Custom CSS** — extensive custom styles in `src/styles/` directory (LandingPage.css, admin.css, auth.css, bootstrap-custom.css, admin-modal.css)
- **Vanilla CSS** — no CSS-in-JS or Tailwind; all styling via CSS files

### Icons
- **lucide-react** `^1.8.0` — icon library

### Fonts (Google Fonts via CDN)
- **Outfit** — headings/brand font (weights: 300–800)
- **Inter** — body/UI font (weights: 400–600)
- **JetBrains Mono** — monospace/code font (weights: 400–500)

### Build & Dev Tools
- **@vitejs/plugin-react** `^6.0.1` — React support for Vite
- **ESLint** `^9.39.4` with:
  - `eslint-plugin-react-hooks` `^7.1.1`
  - `eslint-plugin-react-refresh` `^0.5.2`
  - `typescript-eslint` `^8.58.2`
- **http-proxy-middleware** `^2.0.6` — dev dependency for API proxying (legacy CRA approach, Vite proxy is primary)

### Dev Server Configuration
- **Port**: `3000` (configured in `frontend/vite.config.ts`)
- **API Proxy**: `/api` → `http://localhost:8765` (backend)
- **Logout Proxy**: `/logout` → `http://localhost:8765`

---

## Backend Stack

### Core Framework
- **Spring Boot** — `@SpringBootApplication` main class at `com.infy.billing.StreamFlixApplication`
- **Spring Security** — session-based authentication with `SecurityFilterChain`
- **Spring Data JPA** — repository pattern with `JpaRepository`

### Authentication
- **Session-based** (JSESSIONID cookie, NOT JWT)
- **BCrypt** password hashing via `BCryptPasswordEncoder`
- **DaoAuthenticationProvider** with custom `UserDetailsService`
- Session policy: `IF_REQUIRED`, max 1 session per user

### Database
- **MySQL** — `jdbc:mysql://localhost:3306/subscription_billing`
- **Hibernate/JPA** — ORM with SQL logging enabled (`spring.jpa.show-sql=true`)
- Schema initialization via `schema.sql` (23 tables with seed data)

### Server Configuration
- **Port**: `8765`
- **Session timeout**: 30 minutes
- **CORS**: Allows `http://localhost:3000` and `http://127.0.0.1:3000`
- **CSRF**: Disabled (typical for SPA)

---

## Database Schema (23 Tables)

| # | Table | Purpose |
|---|-------|---------|
| 1 | `user` | Core user accounts (CUSTOMER/SUPPORT/FINANCE/ADMIN) |
| 2 | `customer` | Customer profile extending user (1:1) |
| 3 | `product` | Streaming product catalog (single product: StreamFlix) |
| 4 | `plan` | Subscription plans (Basic/Premium × Monthly/Yearly) |
| 5 | `price_book_entry` | Region-specific pricing (IN/US/GB) |
| 6 | `add_on` | Optional add-ons (e.g., Ad-Free Experience) |
| 7 | `metered_component` | Usage-based components (e.g., Download Storage) |
| 8 | `subscription` | Active customer subscriptions |
| 9 | `subscription_item` | Line items within subscriptions |
| 10 | `subscription_coupon` | Applied coupons on subscriptions |
| 11 | `usage_record` | Metered usage tracking |
| 12 | `invoice` | Generated invoices |
| 13 | `invoice_line_item` | Invoice line items |
| 14 | `credit_note` | Refund/credit records |
| 15 | `billing_job` | Scheduled billing jobs |
| 16 | `dunning_retry_log` | Payment retry tracking |
| 17 | `payment` | Payment transactions |
| 18 | `payment_method` | Stored payment methods (CARD/UPI) |
| 19 | `tax_rate` | Region-based tax rates |
| 20 | `coupon` | Discount coupons |
| 21 | `notification` | Email/SMS notifications |
| 22 | `audit_log` | Audit trail |
| 23 | `revenue_snapshot` | Revenue metrics snapshots |

---

## Configuration Files

| File | Purpose |
|------|---------|
| `frontend/vite.config.ts` | Vite build config, dev server port, API proxy rules |
| `frontend/tsconfig.json` | TypeScript project references (app + node configs) |
| `frontend/tsconfig.app.json` | App TypeScript config |
| `frontend/tsconfig.node.json` | Node TypeScript config |
| `frontend/eslint.config.js` | ESLint flat config with React/TS rules |
| `frontend/package.json` | NPM dependencies and scripts |
| `backend code/main/resources/application.properties` | Spring Boot config (DB, server, security, session) |
| `backend code/main/resources/schema.sql` | Database DDL and seed data |
| `backend code/main/resources/ValidationMessages.properties` | Bean validation messages |

---

## NPM Scripts

| Script | Command |
|--------|---------|
| `dev` | `vite` — start dev server on port 3000 |
| `build` | `tsc -b && vite build` — type-check then bundle |
| `lint` | `eslint .` — lint all files |
| `preview` | `vite preview` — preview production build |
