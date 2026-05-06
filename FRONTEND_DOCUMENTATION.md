# StreamFlix Frontend Architecture & UI Reference Manual
## Client-Side User Portals, Role Guards, and State Orchestration

---

### 1. Global Application entry & Routing Context (`/src/`)

*   [main.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/main.tsx)
    *   *Role:* Mounts the application Virtual DOM to the `#root` element inside `index.html`.
    *   *Action:* Enwraps the application inside `<React.StrictMode>` and hooks up the custom bootstrap layout sheets.
*   [App.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/App.tsx)
    *   *Role:* Central React router configuration file.
    *   *Action:* Declares path matching routes, sets public layouts, customer portals, administrative pages, and hooks up the global session and profile context providers.
*   [App.css](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/App.css)
    *   *Role:* Global design stylesheet.
    *   *Action:* Declares baseline body settings, primary font-family overrides (`Inter`), and common animation transitions.
*   [index.css](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/index.css)
    *   *Role:* Design System setup stylesheet.
    *   *Action:* Declares custom CSS variables, custom root theme setups (glassmorphism settings, card shadows, etc.), and button defaults.
*   [setupProxy.js](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/setupProxy.js)
    *   *Role:* API routing middleware helper.
    *   *Action:* Configures local path mappings, proxying front-end requests to the active local Spring Boot backend.

---

### 2. Client-Side Global Context Providers (`/context/`)

*   [AuthContext.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/context/AuthContext.tsx)
    *   *Role:* Application-wide security session context.
    *   *Action:* Automatically recovers active session tokens on browser refresh via `fetchMe()`, holds the current logged-in user details, and controls redirection paths.
*   [CustomerContext.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/context/CustomerContext.tsx)
    *   *Role:* Logged-in customer state provider.
    *   *Action:* Holds profile data (billing coordinates, base currency) and provides functions to trigger immediate visual state re-renders upon updates.

---

### 3. Route Guard Protections (`/routes/`)

*   [RoleGuard.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/routes/RoleGuard.tsx)
    *   *Role:* Security wrapper protecting restricted UI paths.
    *   *Action:* Intercepts routing attempts; verifies if the current logged-in user role is authorized to view the requested component (e.g. checks for `ROLE_ADMIN` or `ROLE_CUSTOMER`), redirecting unauthorized users to public login gateways.

---

### 4. Public Pages Layout (`/pages/public/`)

*   [LandingPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/public/LandingPage.tsx)
    *   *Role:* Home index marketing page.
    *   *Action:* Features hero sliders using local assets (`/assets/image1.jpg`, etc.), and dynamically adjusts the Call-To-Action (CTA) depending on the active user session (e.g., "Go to Dashboard" for staff).
*   [PlansPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/public/PlansPage.tsx)
    *   *Role:* Public plans grid view.
    *   *Action:* Lists all available subscription plans, features transparent billing transparency cards, and directs users to signup or checkout pipelines.

---

### 5. Client Authentication Portals (`/pages/auth/`)

*   [CustomerAuthPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/auth/CustomerAuthPage.tsx)
    *   *Role:* Customer onboarding login/signup screen.
    *   *Action:* Modern interactive layout toggling between Login and Registration, with built-in validation messages.
*   [ManagementAuthPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/auth/ManagementAuthPage.tsx)
    *   *Role:* Administrator entry gateway.
    *   *Action:* Exposes a separate login layout specifically configured for staff and administrators, bypassing customer onboarding steps.

---

### 6. Customer Dashboard Portal Layout (`/pages/customer/`)

*   [OverviewPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/customer/OverviewPage.tsx)
    *   *Role:* Main user workspace page.
    *   *Action:* Details active subscription states, remaining trial duration, and lists a dynamic "Next Payment" card showing the calculated post-trial charges.
*   [BillingPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/customer/BillingPage.tsx)
    *   *Role:* Invoice ledger and receipt manager.
    *   *Action:* Displays past invoices, lists credit logs, handles discount coupons, and supports dynamic receipt downloads.
*   [PaymentMethodsPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/customer/PaymentMethodsPage.tsx)
    *   *Role:* User cards and UPI channel manager.
    *   *Action:* Integrates interactive payment card entry, manages default billing profiles, and processes payment source deletions.
*   [ProfilePage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/customer/ProfilePage.tsx)
    *   *Role:* Personal account manager view.
    *   *Action:* Handles name corrections, email setups, custom phone number entries, and billing location updates.
*   [SubscriptionPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/customer/SubscriptionPage.tsx)
    *   *Role:* Main plan modification and upgrade center.
    *   *Action:* Lists subscription metrics, shows plan comparative grids, and handles instant upgrades with inline checkout options.
*   [SubscriptionCheckoutPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/customer/SubscriptionCheckoutPage.tsx)
    *   *Role:* Multi-step purchase cart view.
    *   *Action:* Coordinates profile registries, credit card setup validations, coupon tests, and processes final subscription checkout purchases.
*   [SubscriptionFlow.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/customer/SubscriptionFlow.tsx)
    *   *Role:* Unified multi-step wrapper.
    *   *Action:* Maintains progress states for customer checkouts and coordinates page animations across steps.
*   [SupportPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/customer/SupportPage.tsx)
    *   *Role:* Assistance and ticket dispatch interface.
    *   *Action:* Sends contact forms to the support service, logs ticket submissions, and provides FAQ lists.

---

### 7. Administrative Portal Layout (`/pages/admin/`)

*   [AdminDashboardPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/admin/AdminDashboardPage.tsx)
    *   *Role:* Central platform metric overview screen.
    *   *Action:* Displays Monthly Recurring Revenue (MRR), ARR, subscriber trends, plan distributions, and recent transactional charts.
*   `catalog/` subfolder:
    *   `ProductPage.tsx`: Manages core software product listings.
    *   `PlansPage.tsx`: Exposes controls to adjust plan structures and toggle availability.
    *   `AddOnsPage.tsx`: Handles add-on catalog entries.
    *   `MeteredComponentsPage.tsx`: Configures usage metrics for dynamic component testing.
*   `pricing/` subfolder:
    *   [CouponsPage.tsx](file:///d:/Projects/Infosys%20Project/StreamFlixApp/frontend/src/pages/admin/pricing/CouponsPage.tsx): Standard coupon ledger where coupons can be created, edited, and toggled (e.g. `ACTIVE` or `DISABLED`).
    *   `PriceBooksPage.tsx`: Sets dynamic price indexes.
    *   `TaxRatesPage.tsx`: Configures tax rules (such as 18% GST).
*   `subscriptions/` subfolder:
    *   `SubscriptionsPage.tsx`: Central hub to inspect all customer plans across the platform.
*   `users/` subfolder:
    *   `CustomersPage.tsx`: Lists profile information of registered customer accounts.
    *   `StaffAccountsPage.tsx`: Creates and registers admin staff accounts.

---

### 8. Frontend API Services (`/services/`)

Wraps REST calls with TypeScript types using the global native `fetch` client.

*   `authService.ts`: Exports `loginCustomer`, `loginManager`, `register`, and `fetchMe` credentials verification calls.
*   `customerService.ts`: Controls dashboard interactions (such as `getCurrentSubscription`, `upgradeSubscription`, `getInvoices`, and `addPaymentMethod`).
*   `adminService.ts`: Manages administrative operations (such as `getMetrics`, `createPlan`, `togglePlanStatus`, and `toggleCouponStatus`).
*   `publicService.ts`: Coordinates marketing components and retrieves featured plans.
