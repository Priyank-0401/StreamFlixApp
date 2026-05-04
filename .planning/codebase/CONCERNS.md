# Concerns

## Overview
This document catalogs technical debt, security issues, architectural concerns, and fragile areas in the StreamFlixApp codebase.

---

## 🔴 Critical Concerns

### 1. Hardcoded Database Credentials
**File**: `backend code/main/resources/application.properties`
```properties
spring.datasource.username=root
spring.datasource.password=root
```
- Root MySQL credentials hardcoded in source code
- No environment variable substitution or Spring profiles
- **Risk**: Credential exposure in version control

### 2. CSRF Disabled Without Mitigation
**File**: `backend code/main/java/com/infy/billing/config/SecurityConfig.java`
```java
.csrf(csrf -> csrf.disable()) // Typical for SPA
```
- CSRF protection disabled entirely
- While common for SPA + session-based auth, the `SameSite=Lax` cookie setting only partially mitigates CSRF for POST requests from cross-origin forms
- No alternative CSRF mitigation (e.g., custom header check, double-submit cookie)

### 3. Debug Logging in Production Code
**Files**: Throughout `frontend/src/context/AuthContext.tsx`, `frontend/src/services/auth/authService.ts`, `frontend/src/routes/RoleGuard.tsx`
```tsx
console.log('Login customer status FULL RESPONSE:', JSON.stringify(customerStatus));
console.log('AuthProvider rendering, isCustomer:', isCustomer, 'type:', typeof isCustomer);
console.log('RoleGuard check:', { isAuthenticated, userRole: user?.role, allowedRoles, isLoading });
```
- **50+ console.log statements** across auth-related frontend code
- Leaks user data (roles, auth status, full API responses) to browser console
- No log level management or conditional logging

### 4. No Test Coverage
- **Zero test files** in both frontend and backend
- No CI/CD pipeline to catch regressions
- Critical business logic (billing, subscriptions, payments) entirely untested
- See `TESTING.md` for details

---

## 🟡 Significant Concerns

### 5. Duplicate Component Structure
**Paths**: 
- `frontend/src/components/customer/pages/` — 6 page components
- `frontend/src/pages/customer/` — 8 page components (overlapping names)

Both directories contain components with the same names (e.g., `OverviewPage.tsx`, `SubscriptionPage.tsx`, `BillingPage.tsx`). The `pages/customer/` versions include per-page CSS files while `components/customer/pages/` does not. This suggests:
- An incomplete refactoring
- Unclear which components are actually being used by `App.tsx`
- `App.tsx` imports from `pages/customer/` — the `components/customer/pages/` versions may be stale

### 6. Loose TypeScript Typing
**File**: `frontend/src/context/AuthContext.tsx`
```tsx
user: any | null;
login: (user: any) => Promise<{ isCustomer: boolean }>;
```
- `any` type used extensively for user objects throughout auth layer
- `User` interface exists in `types/auth.types.ts` but is not used in `AuthContext`
- Defeats TypeScript's type safety benefits for the most critical data type in the app

### 7. Money as Plain Numbers
All monetary values stored as `BIGINT` in minor units but:
- No wrapper type or utility functions for money operations in frontend
- Division/multiplication for display scattered across components
- No currency formatting utilities
- Risk of precision errors in calculations

### 8. Inconsistent API Service Patterns
Three different patterns coexist:
1. **Object literal** (`authService`): `export const authService = { ... }`
2. **Exported functions** (`customerService`): `export const getProfile = async () => { ... }`
3. **Generic wrapper** (`adminService`): `async function adminFetch<T>()` with exported functions

This inconsistency makes it harder to establish conventions for new code.

### 9. No Environment Configuration
- Frontend: `publicService.ts` hardcodes `http://localhost:8765` directly
- Backend: No Spring profiles (dev/staging/prod)
- No `.env` file for frontend environment variables
- No Vite `import.meta.env` usage for configuration

### 10. Schema.sql as Only Database Migration
**File**: `backend code/main/resources/schema.sql` (967 lines)
- Single monolithic SQL file for all DDL + seed data
- No incremental migration tool (Flyway, Liquibase)
- Seed data (including admin passwords) mixed with schema
- Forward-references in the schema (e.g., `subscription` references `payment_method` which is defined later — relies on `SET FOREIGN_KEY_CHECKS = 0`)

---

## 🟠 Moderate Concerns

### 11. Security Config Typo
**File**: `backend code/main/java/com/infy/billing/config/SecurityConfig.java`
```java
response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authetication required\"}");
```
- Typo: "Authetication" instead of "Authentication"

### 12. Legacy CRA Proxy File
**File**: `frontend/src/setupProxy.js`
- Uses `require('http-proxy-middleware')` — CommonJS syntax in a Vite project
- `http-proxy-middleware` is a devDependency that's not used by Vite
- Vite proxy is configured in `vite.config.ts` — this file is dead code

### 13. App.css Contains Vite Boilerplate
**File**: `frontend/src/App.css` (185 lines)
- Contains Vite's default scaffold CSS (`.hero`, `.counter`, `#center`, `.ticks`)
- None of these classes appear to be used in the actual app
- Should be cleaned up or removed

### 14. Session-Only State Management
- Auth state cached in `sessionStorage` for cross-tab sync
- `window.location.reload()` triggered on cross-tab storage events
- Full page reload is a blunt approach — could cause UX issues
- `window.location.href = '/'` on logout forces hard navigation

### 15. Large Component Files
Several components exceed reasonable size for maintainability:
| File | Lines | Concern |
|------|-------|---------|
| `customerService.ts` | 552 | Types + API calls mixed in single file |
| `SubscriptionPage.tsx` (components) | ~18KB | Complex subscription management UI |
| `SubscriptionCheckoutPage.tsx` | ~15KB | Complex checkout flow |
| `AdminDashboardServiceImpl.java` | ~14KB | All admin CRUD in one service |
| `CustomerSubscriptionServiceImpl.java` | ~13KB | Complex subscription logic |
| `CustomerLayout.tsx` | ~13KB | Large layout component |
| `AdminDashboardPage.tsx` | ~12KB | Dashboard with all stats |

### 16. No Input Validation on Frontend
- Form data sent directly to backend without client-side validation
- Backend has `ValidationMessages.properties` and bean validation
- Missing frontend validation for: email format, password strength, required fields
- Only server-side errors displayed after round-trip

### 17. Zip Files in Repository
- `frontend/src.zip` (165KB) — archived source code inside the repo
- `backend code.zip` (90KB) — archived backend code inside the repo
- These should not be in version control

---

## 🔵 Low Priority / Improvement Opportunities

### 18. No Loading/Error States Pattern
- No global error boundary
- No shared loading spinner component
- Each page implements its own loading/error UI
- Could benefit from a shared `useAsync` hook or SWR/React Query

### 19. No API Response Caching
- Every page load re-fetches all data
- No React Query, SWR, or any client-side caching
- Admin dashboard re-fetches stats on every mount
- Could improve UX with stale-while-revalidate

### 20. Hardcoded Seed Passwords
**File**: `backend code/main/resources/schema.sql`
```sql
INSERT INTO user (full_name, email, password_hash, role, status) VALUES
('System Admin', 'admin@streamflix.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'ADMIN', 'ACTIVE'),
```
- BCrypt hash for `Password123!` committed in source
- Same password hash used for all seeded users
- Should use environment-specific seeds

### 21. No Pagination
- All list endpoints return full datasets (`getAllPlans()`, `getCustomers()`, etc.)
- Will become a performance issue as data grows
- No pagination UI in admin tables

### 22. Backend Structure Is Source-Only
- The `backend code/` directory contains only `main/java/` and `main/resources/`
- No `pom.xml` or `build.gradle` file present in the repository
- No `test/` directory
- Makes it impossible to build/run the backend from this repository alone

---

## Summary Priority Matrix

| Priority | Count | Issues |
|----------|-------|--------|
| 🔴 Critical | 4 | Hardcoded credentials, CSRF, debug logging, no tests |
| 🟡 Significant | 6 | Duplicate components, loose types, no env config |
| 🟠 Moderate | 7 | Legacy code, large files, no validation, typos |
| 🔵 Low | 5 | Caching, pagination, patterns, improvement opportunities |
