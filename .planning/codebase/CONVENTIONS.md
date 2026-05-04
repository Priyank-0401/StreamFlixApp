# Coding Conventions

## Overview
StreamFlixApp follows common React + Spring Boot conventions with some project-specific patterns documented here.

---

## Frontend Conventions

### Component Patterns

#### Function Components Only
All React components use function components with hooks. No class components anywhere:
```tsx
export const RoleGuard: React.FC<RoleGuardProps> = ({ children, allowedRoles, redirectTo }) => {
  const { isAuthenticated, user, isLoading } = useAuthContext();
  // ...
};
```

#### Named Exports
Components use **named exports** (not default exports), except `App.tsx` which uses `export default App`:
```tsx
// Pattern used everywhere
export const CustomerLayout: React.FC<...> = () => { ... };
export const AdminDashboardPage = () => { ... };
```

#### Props via Interface
Component props defined as TypeScript interfaces:
```tsx
interface RoleGuardProps {
  children: React.ReactNode;
  allowedRoles: ROLES[];
  redirectTo: string;
}
```

### State Management

#### Context API Pattern
Global state uses React Context with Provider + custom hook:
```tsx
// 1. Create context
const AuthContext = createContext<AuthContextType | null>(null);

// 2. Provider component
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [authState, setAuthState] = useState<AuthState>({ ... });
  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
};

// 3. Custom hook
export const useAuthContext = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuthContext must be used within an AuthProvider');
  return context;
};
```

#### Local State with useState
Page-level state uses `useState` with explicit types:
```tsx
const [isCustomer, setIsCustomer] = useState<boolean>(() => {
  const cached = sessionStorage.getItem('isCustomer');
  return cached === 'true';
});
```

### API Service Pattern

#### Centralized Fetch Wrapper
All authenticated API calls flow through `fetchWithSession()` in `authService.ts`:
```tsx
export const fetchWithSession = async (endpoint: string, options: RequestInit = {}) => {
  const defaultOptions: RequestInit = {
    ...options,
    credentials: 'include',  // JSESSIONID cookie
    headers: { 'Content-Type': 'application/json', ...options.headers },
  };
  const response = await fetch(url, defaultOptions);
  if (!response.ok) { /* parse error, throw */ }
  return response;
};
```

#### Service Object Pattern
Auth service uses an object literal:
```tsx
export const authService = {
  customerLogin: async (request: LoginRequest): Promise<any> => { ... },
  managerLogin: async (request: LoginRequest): Promise<any> => { ... },
  checkSession: async (): Promise<any> => { ... },
};
```

#### Exported Function Pattern
Customer and admin services export individual functions:
```tsx
export const getCustomerProfile = async (): Promise<CustomerProfile> => {
  const response = await fetchWithSession(`${API_BASE}/me`);
  if (!response.ok) throw new Error('Failed to fetch profile');
  return response.json();
};
```

#### Admin Service Generic Wrapper
Admin service has its own typed wrapper:
```tsx
async function adminFetch<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${endpoint}`, { ...options, credentials: 'include' });
  // error handling...
  const text = await res.text();
  return text ? JSON.parse(text) : ({} as T);
}
```

### Type Definitions

#### Co-located Types
Customer types live directly in `customerService.ts` (552 lines file contains both types and functions). Admin types are in a separate `adminTypes.ts` file.

#### Enum-like Constants
Roles use `as const` assertion pattern:
```tsx
export const ROLES = {
  CUSTOMER: 'CUSTOMER',
  ADMIN: 'ADMIN',
  FINANCE: 'FINANCE',
  SUPPORT: 'SUPPORT'
} as const;

export type ROLES = keyof typeof ROLES;
```

### Styling Conventions

#### CSS Approach
- **Bootstrap 5** as base (imported globally)
- **Bootstrap-custom.css** for theme overrides (dark theme, custom variables)
- **Per-domain CSS files** in `src/styles/` (admin.css, auth.css, LandingPage.css)
- **Per-page CSS files** co-located with page components in `pages/customer/`
- No CSS-in-JS, no Tailwind, no CSS Modules

#### Class Naming
Mix of Bootstrap utility classes and custom CSS:
```tsx
<div className="min-vh-100 d-flex justify-content-center align-items-center bg-dark">
```

### Logging Convention
**Extensive `console.log` debugging throughout** — production code contains many debug logs:
```tsx
console.log('RoleGuard check:', { isAuthenticated, userRole: user?.role });
console.log('Login customer status FULL RESPONSE:', JSON.stringify(customerStatus));
console.log('AuthProvider rendering, isCustomer:', isCustomer, 'type:', typeof isCustomer);
```

---

## Backend Conventions

### Package Structure
Standard Spring Boot layered architecture under `com.infy.billing`:
```
config/       → Spring configuration classes
controller/   → REST API controllers
service/      → Business logic (Interface + Impl)
repository/   → Data access layer
entity/       → JPA domain entities
dto/          → Data transfer objects (by domain)
enums/        → Java enums
exception/    → Custom exceptions + global handler
request/      → Request body DTOs
```

### Service Pattern (Interface + Impl)
Every service has an interface and implementation:
```java
// Interface
public interface CustomerService {
    CustomerProfileDTO getProfile(Long userId);
    CustomerProfileDTO updateProfile(Long userId, UpdateProfileRequest request);
}

// Implementation
@Service
public class CustomerServiceImpl implements CustomerService {
    @Override
    public CustomerProfileDTO getProfile(Long userId) { ... }
}
```

### Entity Conventions
- JPA `@Entity` annotations
- `BIGINT` primary keys with `AUTO_INCREMENT`
- Monetary values in **minor units** (e.g., 19900 = ₹199.00)
- Status fields as MySQL `ENUM` strings
- Timestamp fields: `created_at`, `updated_at` with auto-timestamps

### Error Handling
Centralized via `GlobalExceptionHandler` with `@ControllerAdvice`:
- Catches `CustomException` and standard Spring exceptions
- Returns structured JSON error with `ErrorInfo` DTO
- Supports field-level validation errors (`validationErrors`)

### Security Conventions
- CSRF disabled (SPA pattern)
- URL-pattern based authorization in `SecurityFilterChain`
- Public endpoints explicitly listed (`permitAll()`)
- All `/api/**` endpoints require authentication
- Session-based with `JSESSIONID` cookie

### Naming Conventions (Backend)
- Controllers: `{Domain}Controller.java`
- Services: `{Domain}Service.java` (interface) + `{Domain}ServiceImpl.java`
- Repositories: `{Entity}Repository.java`
- DTOs: `{Entity}Response.java`, `{Action}Request.java`
- Entities: match database table names in PascalCase

---

## Cross-Cutting Conventions

### Money Handling
All monetary values use **minor units** (smallest currency denomination):
- Frontend: `priceMinor`, `amountMinor`, `totalMinor` field names
- Backend: `BIGINT` columns named `*_minor`
- Conversion: `amount / 100` for display (INR paisa → rupees)

### Date/Time
- Backend: `TIMESTAMP` and `DATE` MySQL types
- Frontend: ISO date strings (`string` type in interfaces)
- No dedicated date library (no moment.js, date-fns, etc.)

### Error Pattern
```
Backend → throws CustomException → GlobalExceptionHandler → JSON ErrorInfo
Frontend → fetchWithSession catches → parses ErrorInfo → throws Error with message
Component → try/catch → setState error message → render error UI
```

### Authentication Flow Pattern
```
fetch(url, { credentials: 'include' })  ← sends JSESSIONID cookie
   ↓
Spring Security validates session
   ↓
Controller accesses Principal/SecurityContext
   ↓
Response + Set-Cookie header (if new session)
```
