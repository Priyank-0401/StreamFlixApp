# Testing

## Current State

### No Tests Exist
The StreamFlixApp codebase currently has **zero test files** in both the frontend and backend:

- **Frontend**: No test framework configured, no test files, no `test` script in `package.json`
- **Backend**: The `backend code/` directory contains only `main/` sources — no `test/` directory exists

### No Test Infrastructure
- No testing libraries in `frontend/package.json` (no Jest, Vitest, Testing Library, Cypress, Playwright)
- No test configuration files (no `jest.config.ts`, `vitest.config.ts`, `cypress.config.ts`)
- No CI/CD pipeline configuration (no `.github/workflows/`, `Jenkinsfile`, etc.)

---

## Recommendations

### Frontend Testing Stack (Recommended)
Given the Vite + React + TypeScript stack:

1. **Unit/Component Testing**: Vitest + React Testing Library
   - Vitest integrates natively with Vite
   - React Testing Library for component testing
   - `@testing-library/user-event` for interaction simulation

2. **E2E Testing**: Playwright or Cypress
   - For testing auth flows, subscription flow, admin CRUD

3. **Priority Test Areas**:
   - `AuthContext` — session management, login/logout flows
   - `RoleGuard` — route protection logic
   - `fetchWithSession` — API error handling, credential passing
   - `customerService` — API contract validation
   - Subscription onboarding flow (multi-step form)

### Backend Testing Stack (Recommended)
Given the Spring Boot stack:

1. **Unit Testing**: JUnit 5 + Mockito
   - Service layer tests with mocked repositories
   - Controller tests with MockMvc

2. **Integration Testing**: `@SpringBootTest` + H2 in-memory DB
   - Full API endpoint testing
   - Security configuration testing

3. **Priority Test Areas**:
   - `SecurityConfig` — authentication/authorization rules
   - `SubscriptionFlowServiceImpl` — multi-step subscription creation
   - `AdminDashboardServiceImpl` — CRUD operations
   - `AuthServiceImpl` — login/registration
   - Entity validation (bean validation annotations)

---

## Build Scripts

| Script | Command | Tests Included? |
|--------|---------|-----------------|
| `dev` | `vite` | No |
| `build` | `tsc -b && vite build` | Type-checking only |
| `lint` | `eslint .` | Static analysis only |
| `preview` | `vite preview` | No |

No dedicated test scripts exist.

---

## Code Quality Tools

### Currently Active
- **TypeScript** `~6.0.2` — compile-time type checking
- **ESLint** `^9.39.4` — linting with:
  - `eslint-plugin-react-hooks` — React hooks rules
  - `eslint-plugin-react-refresh` — HMR compatibility
  - `typescript-eslint` — TypeScript-specific rules

### Not Configured
- No Prettier / code formatter
- No Husky / lint-staged (pre-commit hooks)
- No code coverage tool
- No Sonar / code quality gate
- No GitHub Actions / CI pipeline
