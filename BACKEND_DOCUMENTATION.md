# StreamFlix Backend Architecture & Technical Reference Manual
## Secure Enterprise Billing, Invoicing, and Transactional Revenue Engine

---

### 1. Architectural & Gateway Security Config

#### SecurityConfig.java
*   **Role:** Main Security and Identity Access Control configuration class utilizing Spring Security's `@EnableWebSecurity` annotation.
*   **Logical Operations & Details:** It secures endpoints, manages cookie-based stateful authentication, and blocks CSRF threats while keeping CORS accessible. It designates specific public endpoints such as `/api/customer/login`, `/api/customer/register`, and `/api/customer/plans/all` as unrestricted, while securing `/api/**` with a strict `authenticated()` requirement. It customizes the HTTP 401 response JSON to ensure unauthorized API queries get an elegant JSON structure instead of a generic browser popup.
*   **Uncommon Library Components & Security Filters:**
    *   `DaoAuthenticationProvider`: Seamlessly bridges user login credential requests with database-backed user records by loading properties from the user details service.
    *   `BCryptPasswordEncoder`: Encodes raw passwords with a secure, GPU-resistant, dynamically-salted BCrypt hashing algorithm before comparison.
    *   `SessionCreationPolicy.IF_REQUIRED`: Enforces that the backend server only creates servlet sessions when required by stateful login operations, keeping REST queries light.
    *   `sessionFixation().migrateSession()`: Prevents Session Fixation Attacks by creating a brand-new Servlet Session and copying old session attributes whenever a user logs in.
    *   `CorsConfigurationSource` & `UrlBasedCorsConfigurationSource`: Declares precise source patterns (such as `localhost:3000`) and headers to allow cookie-bearing AJAX queries from the frontend.

---

### 2. Controller Routing APIs

Controllers are designed as `@RestController` entities utilizing Spring MVC `@RequestMapping("/api")` annotations.

#### AdminController.java
*   **Role:** Platform administration REST endpoint router layer.
*   **Logical Operations:** Handles admin calls, tracking active MRR, ARR, and subscriber churn. It provides creation, update, and toggle endpoints for active plans, coupons, and add-ons. It also facilitates staff registry setups and manages customer listing lookups.

#### AuthController.java
*   **Role:** Secure entry and identity access REST endpoint.
*   **Logical Operations:** Receives login credentials for customers and staff, validates profiles, maps cookie sessions, and handles safe, complete session terminations during logout actions. It also supports instant session validations on page refreshes via `/api/auth/me`.

#### CustomerBillingController.java
*   **Role:** Customer ledger retrieval and transactions controller.
*   **Logical Operations:** Coordinates user ledger details, handles historical invoice lists, processes credit notes, validates applied coupon codes, and converts invoice rows into formatted, streamable PDF binaries.

#### CustomerController.java
*   **Role:** Personal customer profile router.
*   **Logical Operations:** Updates names, contact details, default billing currencies, and shipping destinations. It supports secure address corrections and filters public, non-restricted plans.

#### CustomerPaymentController.java
*   **Role:** Billing source and card tokenization router.
*   **Logical Operations:** Registers credit cards and UPI accounts. It processes safe token entries, establishes default card symbols, and deletes expired credentials from the database.

#### CustomerSubscriptionController.java
*   **Role:** Active subscription adjustments and pricing tier router.
*   **Logical Operations:** Intercepts upgrade requests, triggers trial transfers, handles instant billing changes, processes pause dates, and handles subscription reactivations.

#### CustomerSupportController.java
*   **Role:** Technical support desk router.
*   **Logical Operations:** Intercepts contact-us inquiries, registers tech requests, and distributes catalogued FAQs based on active categorizations.

---

### 3. Business Service Layer

The service layer implements the core business logic. All classes use interfaces alongside implementations (`ServiceImpl`) to wrap database transactions in robust Spring transaction proxies (`@Transactional`).

#### AdminDashboardService.java & AdminDashboardServiceImpl.java
*   **Role:** Administrative reporting aggregator service.
*   **Logical Operations:** Compiles active subscription levels and plan metrics. It aggregates multi-currency revenues (USD, EUR, GBP) into Unified INR, and calculates MRR, ARR, and active subscription stats.

#### AuthService.java & AuthServiceImpl.java
*   **Role:** Secure authentication context manager service.
*   **Logical Operations:** Matches credentials with the database, verifies password hashes using BCrypt, maps sessions in Spring Security, and executes invalidations upon logout.

#### CustomerBillingService.java & CustomerBillingServiceImpl.java
*   **Role:** Billing accounting and invoice ledger service.
*   **Logical Operations:** Generates invoice PDFs, computes tax structures, validates and applies discount coupons, processes outstanding balances, and manages credit records.

#### CustomerPaymentService.java & CustomerPaymentServiceImpl.java
*   **Role:** Payment gateway orchestrator service.
*   **Logical Operations:** Registers payment cards and UPI links, designates default card configurations, verifies credit sources, and logs transactional records.

#### CustomerService.java & CustomerServiceImpl.java
*   **Role:** Customer account and profile management service.
*   **Logical Operations:** Validates personal contact fields, secures database records, handles address changes, and maintains currency preferences.

#### CustomerSubscriptionService.java & CustomerSubscriptionServiceImpl.java
*   **Role:** Central subscription manager and proration calculator service.
*   **Logical Operations:** Calculates trial date changes, adjusts active plan items, handles standard cancellations, and executes active mid-period proration calculations.

#### CustomerSupportService.java & CustomerSupportServiceImpl.java
*   **Role:** Customer helpdesk service.
*   **Logical Operations:** Logs support requests, maps tickets to categories, and coordinates answers for regular FAQs.

#### SubscriptionFlowService.java & SubscriptionFlowServiceImpl.java
*   **Role:** Checkout workflow service.
*   **Logical Operations:** Coordinates multi-stage registries, validates card setups, handles initial discount coupons, and writes billing entries inside transactional boundaries.

---

### 4. Database Entities, Relationships, and Enums

Mapped using Jakarta Persistence (`jakarta.persistence.*`) annotations for automatic Hibernate DDL schema coordination.

#### Key Entities
1.  **User:** Core authentication record. Tracks username emails, salted BCrypt password hashes, and access permissions (`ROLE_CUSTOMER`, `ROLE_ADMIN`).
2.  **Customer:** Linked to User; contains billing details (physical address, country, default currency) and active platform status.
3.  **Plan:** Subscription packages. Declares standard minor unit pricing (paisa), billing intervals (`MONTHLY`, `YEARLY`), trial durations, and active tax rules.
4.  **AddOn:** Optional recurring modules (e.g., UHD streaming) with separate price plans.
5.  **Subscription:** Central contract record linking a Customer to a Plan, tracking active states (`ACTIVE`, `TRIALING`, `PAUSED`).
6.  **SubscriptionItem:** Maps individual line items (like the core Plan or active AddOns) inside an active Subscription.
7.  **Invoice:** Invoice history. Tracks total amounts, discounts, 18% GST calculations, and payment states (`PAID`, `OPEN`).
8.  **InvoiceLineItem:** Particular rows in an Invoice. Tracks charges for plans, add-ons, or proration credits.
9.  **Payment:** Logs transaction details (gateway references, idempotency keys, and transaction statuses).

#### Business Enums
*   `Status`: Unifies status states: `ACTIVE`, `INACTIVE`, `TRIALING`, `PAST_DUE`, `CANCELED`, `PAUSED`, `OPEN`, `PAID`, `SUCCESS`.
*   `BillingPeriod`: Represents subscription billing frequency: `MONTHLY`, `YEARLY`.
*   `ItemType`: Identifies item classifications: `PLAN`, `ADDON`, `METERED`.
*   `CouponType`: Identifies coupon calculation strategies: `PERCENT`, `FIXED`.

---

### 5. Repositories Layer

Leverages Spring Data JPA's `JpaRepository` interface, automatically translating method definitions into optimized SQL statements.

*   **UserRepository:** Handles user account database entries. It supports custom, transactional query loaders to retrieve security profiles by email.
*   **CustomerRepository:** Provides customer database selectors, enabling quick lookups of billing coordinates.
*   **PlanRepository:** Manages subscription plan configurations and active price indexes.
*   **SubscriptionRepository:** Filters active subscription records by customer IDs or current status ranges.
*   **SubscriptionItemRepository:** Locates subscription line items (like core plans or active add-ons) linked to a subscription ID.
*   **InvoiceRepository:** Performs billing ledger lookups, returning chronological invoices or unpaid invoice lists.
*   **InvoiceLineItemRepository:** Facilitates mass updates or deletes of line items inside active invoices.
*   **PaymentRepository:** Records payment histories, logging success codes, payment gateways, and charge timestamps.
*   **PaymentMethodRepository:** Stores card details and registered digital wallets linked to customer IDs.

---

### 6. Dynamic Proration & Billing Logic Details

#### Active Mid-Cycle Upgrades:
When upgrading plans mid-billing cycle, the system calculates proration immediately using:
$$\text{Days Remaining} = \text{Period End Date} - \text{Current Date}$$
$$\text{Old Refund Credit} = \text{Old Paid Total} \times \frac{\text{Days Remaining}}{\text{Total Period Days}}$$
$$\text{Net Immediate Charge} = \max\left(0, \text{New Plan Total Price} - \text{Old Refund Credit}\right)$$
The system creates a `PAID` invoice documenting this change, records the gateway payment, and instantly shifts the billing period to start fresh from today (`today` to `today.plusYears(1)` or `today.plusMonths(1)`).

#### Trial Upgrades:
When changing plans during active trials:
$$\text{Days Used} = \text{Current Date} - \text{Trial Start Date}$$
$$\text{New Trial Days Left} = \max\left(0, \text{New Plan Trial Days} - \text{Days Used}\right)$$
The system shifts `trialEndDate` by the remaining trial days from today. It then finds the scheduled future `OPEN` trial invoice, deletes all old line items, recalculates the subtotal based on the new plan + tax + coupon + addons, and updates its total and due date dynamically.

---

### 7. Custom Exception Handlers

#### CustomException.java
*   **Role:** Custom unchecked exception class extending `RuntimeException`.
*   **Logical Operations:** Captures specific error messages, HTTP status numbers, and validation errors to prevent leaking raw database stacks to the client.

#### GlobalExceptionHandler.java
*   **Role:** Global controller advice class using `@ControllerAdvice`.
*   **Logical Operations:** Catches validation errors, bad requests, and authentication exceptions, mapping them into clean, standardized JSON response bodies.
