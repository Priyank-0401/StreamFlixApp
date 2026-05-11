# StreamFlix — User Story Analysis, SRS Contradictions & Implementation Review

> **Date:** May 11, 2026  
> **Project:** StreamFlix Subscription Billing & Revenue Management System  
> **Purpose:** Map current implementation to SRS user stories, identify contradictions, explain each story in detail, and document necessary changes.

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Implementation Status Matrix](#2-implementation-status-matrix)
3. [SRS Contradictions & Issues](#3-srs-contradictions--issues)
4. [Detailed User Story Analysis](#4-detailed-user-story-analysis)
   - [US-01: Subscribe to a Plan](#us-01--subscribe-to-a-plan)
   - [US-02: Automated Billing Cycles](#us-02--automated-billing-cycles)
   - [US-03: Catalog Management](#us-03--catalog-management)
   - [US-04: Taxes & Discounts](#us-04--taxes--discounts)
   - [US-05: Renewal Reminders](#us-05--renewal-reminders)
   - [US-06: Upgrades & Downgrades with Proration](#us-06--upgrades--downgrades-with-proration)
   - [US-07: Revenue Analytics](#us-07--revenue-analytics)
   - [US-08: Refunds & Credit Notes](#us-08--refunds--credit-notes)
   - [US-09: Dunning & Retry Strategies](#us-09--dunning--retry-strategies)
5. [Missing Roles & Portals](#5-missing-roles--portals)
6. [Recommended Action Items](#6-recommended-action-items)

---

## 1. Executive Summary

This document provides a comprehensive analysis of the StreamFlix codebase against the Software Requirements Specification (SRS). While the project has made substantial progress on customer-facing features and admin catalog management, several critical backend systems are either missing or incomplete — most notably the automated billing engine, notification service, dunning management, and revenue analytics.

Additionally, the SRS contains several contradictions and ambiguities regarding actor responsibilities (particularly around dunning, renewal reminders, and taxes) that must be resolved for consistent implementation.

**Key Finding:** The current implementation primarily covers **US-01 (Subscribe)**, **US-03 (Catalog Management)**, and **US-06 (Upgrades/Proration)**. **US-02 (Billing Cycles)**, **US-05 (Renewal Reminders)**, **US-07 (Revenue Analytics)**, **US-08 (Refunds)**, and **US-09 (Dunning)** are either entirely missing or significantly incomplete.

---

## 2. Implementation Status Matrix

| User Story | Status | Backend | Frontend | Notes |
|---|---|---|---|---|
| **US-01** Subscribe to a Plan | **~90% Complete** | `SubscriptionFlowServiceImpl`, `CustomerSubscriptionController`, checkout flow, invoice generation, payment recording | `SubscriptionCheckoutPage`, `SubscriptionFlow`, `PlansPage`, `OverviewPage` | Add-ons, coupons, taxes, trial handling all implemented. Missing: true payment gateway integration (mocked as designed). |
| **US-02** Automated Billing Cycles | **~5% Complete** | Invoice/Subscription entities exist. No scheduler, no cron, no billing engine. | No billing dashboard or job viewer. | **Critical Gap.** No `@Scheduled` jobs, no billing run logic. |
| **US-03** Catalog Management | **~95% Complete** | `AdminController`: full CRUD for Products, Plans, AddOns, MeteredComponents, PriceBooks, TaxRates, Coupons | `ProductPage`, `PlansPage`, `AddOnsPage`, `MeteredComponentsPage`, `PriceBooksPage`, `TaxRatesPage`, `CouponsPage` | Very well implemented. Missing: product-plan hierarchical linkage enforcement in UI. |
| **US-04** Taxes & Discounts | **~70% Complete** | TaxRate CRUD, Coupon CRUD, GST calculation in checkout. No inclusive/exclusive tax mode enforcement per line item. | Tax and coupon management pages exist. | Tax logic is hardcoded to 18% GST. Missing: region-based dynamic tax application, inclusive tax mode. |
| **US-05** Renewal Reminders | **~0% Complete** | No `Notification` entity usage. No reminder scheduler. No email/SMS service. | No notification preferences or reminder UI. | **Critical Gap.** No notification service exists at all. |
| **US-06** Upgrades & Downgrades | **~85% Complete** | Proration math implemented in `CustomerSubscriptionServiceImpl`. Trial transfer logic present. Invoice regeneration on upgrade works. | `SubscriptionPage` handles upgrades. | Well implemented. Missing: downgrade scenario handling (only upgrades supported). |
| **US-07** Revenue Analytics | **~30% Complete** | `AdminDashboardServiceImpl.getDashboardStats()` returns basic counts. No MRR/ARR/churn calculation engine. | `AdminDashboardPage` shows basic stats. | **Major Gap.** No analytics calculation service. No CSV/PDF export. |
| **US-08** Refunds & Credit Notes | **~40% Complete** | `CreditNote` entity exists. `CustomerBillingController` can return credit notes. No refund workflow. No support agent portal. | `BillingPage` shows credit notes. No refund request UI. | Missing: support agent role, refund approval workflow, automated credit note generation on refund. |
| **US-09** Dunning & Retry Strategies | **~0% Complete** | No dunning scheduler. No retry logic. No `past_due` status automation. | No dunning configuration UI. | **Critical Gap.** `PAST_DUE`, `ON_HOLD` statuses exist in enum but are never used. |

---

## 3. SRS Contradictions & Issues

### Issue 1: Dunning Ownership Contradiction

**Location:** US-09, Section 6.6, Section 10.4

**Contradiction:**
- US-09 states: *"As an **admin**, I want dunning & retry strategies so failed payments recover automatically."*
- Section 6.6 describes: *"Configurable retry schedule and max attempts; Customer notifications; status transitions on completion"*
- Section 10.4 describes an **automated system flow** (no human actor)

**Analysis:** Dunning is fundamentally a **system/automated process**. While an admin *configures* the strategy (retry schedule, max attempts, policy), the actual execution — detecting failed payments, scheduling retries, sending reminders, canceling after max retries — is performed by the **Billing Engine / System**.

**Resolution:**
- **Change US-09 actor from "Admin" to "System (Billing Engine)"**
- **Add a new configuration story:** "As an admin, I want to configure dunning & retry strategies..."
- This separates *configuration* (admin) from *execution* (system)

---

### Issue 2: Renewal Reminders Actor Mismatch

**Location:** US-05, Section 6.7

**Contradiction:**
- US-05 states: *"As a **customer**, I want renewal reminders so that I'm informed before charges."*
- The acceptance criteria describe system actions (T-7, T-1 reminders, email/SMS)

**Analysis:** A customer is the *beneficiary* of renewal reminders, not the actor who executes them. This is a system-initiated notification process. The customer does not trigger reminders; they receive them.

**Resolution:**
- **Change US-05 actor from "Customer" to "System (Notification Service)"**
- The customer remains the stakeholder/beneficiary
- Add a related story: "As a customer, I want to manage my notification preferences..."

---

### Issue 3: Taxes & Discounts Role Confusion

**Location:** US-04, Section 6.1, Section 6.4

**Contradiction:**
- US-04 states: *"As a **finance manager**, I want taxes and discounts applied correctly..."*
- However, tax rules and coupons are managed in the **Admin Console** (not Finance Dashboard)
- The actual *application* of taxes/discounts is done by the **Billing Engine** during checkout/renewal

**Analysis:** Three different actors are involved:
1. **Admin** — configures tax rules and coupon definitions
2. **System (Billing Engine)** — applies taxes and discounts during invoice generation
3. **Finance Manager** — reviews tax/discount reports and ensures compliance

**Resolution:**
- **Split US-04 into three distinct concerns:**
  - US-04a: "As an admin, I want to configure tax rules and coupons..."
  - US-04b: "As a system, I want to apply taxes and discounts correctly during billing..."
  - US-04c: "As a finance manager, I want to review tax and discount reports..."

---

### Issue 4: Role Implementation Mismatch

**Location:** Section 3 (Actors & Roles), Section 12 (Security)

**Contradiction:**
- SRS defines 4 roles: Customer, Admin, Finance Manager, Support Agent
- Actual implementation (`UserRole.java`) only has: `ROLE_CUSTOMER`, `ROLE_ADMIN`
- Finance Manager and Support Agent roles are completely absent

**Analysis:** The frontend has pages for Finance Dashboard and Support Console in the SRS description, but no corresponding routes or role guards exist in `App.tsx`.

**Resolution:**
- **Add `ROLE_FINANCE_MANAGER` and `ROLE_SUPPORT_AGENT` to `UserRole.java`**
- **Create `FinanceController` and `SupportController` endpoints**
- **Add frontend routes with role guards**
- Or, **consolidate roles** under Admin with sub-permissions (simpler for this project scope)

---

### Issue 5: Automated Billing vs. On-Demand Billing

**Location:** US-02, Section 6.3, Section 10.2

**Contradiction:**
- US-02 describes fully automated billing cycles via cron
- Section 6.3 mentions both "scheduler and on-demand trigger"
- The API design includes `POST /billing/run` (on-demand) but no scheduled implementation exists

**Analysis:** The system was designed to support both, but only the on-demand endpoint is partially possible (no actual billing run service exists). The SRS should clarify whether on-demand billing is a fallback or primary mechanism.

**Resolution:**
- **Primary:** Automated cron-based billing (daily at 00:00 UTC)
- **Secondary:** On-demand trigger (`POST /billing/run`) for manual corrections or testing
- Document both in the SRS

---

### Issue 6: Proration Behavior Ambiguity

**Location:** US-06, Section 6.2

**Contradiction:**
- US-06 says: *"Charge immediately or defer to next renewal"*
- The current implementation always charges immediately
- There is no configuration for "proration behavior" (immediate vs. deferred)

**Resolution:**
- **Add `prorationBehavior` field to `Plan` entity** (`IMMEDIATE_CHARGE` or `DEFER_TO_NEXT_CYCLE`)
- Update `CustomerSubscriptionServiceImpl` to respect this setting
- Default to `IMMEDIATE_CHARGE` for backward compatibility

---

### Issue 7: Invoice Status `UNCOLLECTIBLE` Never Used

**Location:** `Status.java`

**Contradiction:**
- `Status.java` defines `UNCOLLECTIBLE` for invoices
- No code path ever sets an invoice to `UNCOLLECTIBLE`
- This status should be applied after max dunning retries when the policy is "hold" rather than "cancel"

**Resolution:**
- Use `UNCOLLECTIBLE` when dunning maxes out and the policy is to place on hold (not cancel)
- Update dunning completion logic to set this status

---

### Issue 8: `Subscription` Missing `past_due` Status Transition Logic

**Location:** `Status.java`, `Subscription` entity

**Contradiction:**
- `Status.java` defines `PAST_DUE` and `ON_HOLD`
- `Subscription` entity status enum in DDL only shows: `ACTIVE`, `TRIALING`, `PAUSED`, `CANCELED`
- No code ever transitions a subscription to `PAST_DUE` or `ON_HOLD`

**Resolution:**
- Ensure `PAST_DUE` and `ON_HOLD` are valid enum values in the database schema
- Add transitions: `ACTIVE` → `PAST_DUE` (on payment failure), `PAST_DUE` → `ON_HOLD` (after max retries), `PAST_DUE` → `ACTIVE` (on successful retry)

---

## 4. Detailed User Story Analysis

### US-01 — Subscribe to a Plan *(Must Have)*

**Original SRS:**
> As a customer, I want to subscribe to a plan (with optional add-ons) so that I can start using the service.

**What is Implemented:**
- **Frontend:** Complete checkout wizard (`SubscriptionFlow.tsx` → `SubscriptionCheckoutPage.tsx`). Customer selects plan, add-ons, applies coupon, enters payment method, reviews order, confirms.
- **Backend:** `SubscriptionFlowServiceImpl.completeSubscriptionCheckout()` handles:
  - Customer lookup
  - Plan validation
  - Payment method tokenization & saving
  - Subscription creation (`TRIALING` or `ACTIVE`)
  - SubscriptionItem creation (plan + add-ons)
  - Invoice generation with GST tax calculation
  - Payment recording (mock gateway)
- **Status transitions:** `draft` → `trialing` (if trial days > 0) or `active` (immediate charge)

**What is Missing:**
- True payment gateway integration (mocked as per SRS assumption — acceptable)
- Subscription `DRAFT` status is never used; subscription is created directly in final state
- No handling for subscription creation failure rollback scenarios beyond basic `@Transactional`

**Recommended Changes:**
1. **No SRS change needed** — implementation matches story well
2. Add pre-subscription validation (e.g., check if customer already has active subscription for same product)
3. Consider adding a `DRAFT` subscription state during checkout that gets finalized on payment success

---

### US-02 — Automated Billing Cycles *(Must Have)*

**Original SRS:**
> As a system, I want to run automated billing cycles so that subscriptions are renewed on time.

**What is Implemented:**
- `Invoice`, `Subscription`, `Payment` entities exist
- `POST /billing/run` endpoint exists in API design but **no controller or service implements it**
- No `@Scheduled` cron jobs exist anywhere in the codebase
- No `BillingEngineService` or equivalent

**What is Missing (Critical):**
- **Cron/Scheduler:** A `@Scheduled` daily job that:
  1. Queries subscriptions where `current_period_end` is within next 24 hours
  2. Generates renewal invoices with proration, taxes, discounts
  3. Attempts payment via mock gateway
  4. On success: marks invoice `PAID`, sends receipt
  5. On failure: marks invoice `OPEN`, subscription `PAST_DUE`, enqueues retry
- **Idempotency:** No idempotency key mechanism for charges
- **Billing Job Tracking:** No `BillingJob` entity or job status tracking

**Recommended Changes:**
1. **Create `BillingEngineService.java`** with `@Scheduled(cron = "0 0 0 * * ?")` (daily at midnight UTC)
2. **Add `BillingRun` entity** to track each billing cycle execution (date, status, count processed, count failed)
3. **Implement idempotency:** Add `idempotencyKey` field to `Payment` entity (UUID generated per billing attempt)
4. **SRS Addition:** Clarify that billing runs daily at 00:00 UTC, processes subscriptions due in next 24h
5. **Add `POST /billing/run` endpoint** in `AdminController` for manual on-demand triggering (admin-only)

---

### US-03 — Catalog Management *(Must Have)*

**Original SRS:**
> As an admin, I want to configure products, plans, and pricing to offer flexible subscriptions.

**What is Implemented:**
- **Frontend:** Complete admin catalog pages:
  - `ProductPage`: Create/edit/toggle products
  - `PlansPage`: Create/edit/toggle plans with billing period, trial days, setup fee, tax mode
  - `AddOnsPage`: Manage recurring add-ons
  - `MeteredComponentsPage`: Manage usage-based components
  - `PriceBooksPage`: Region/currency-specific pricing
  - `TaxRatesPage`: GST/VAT rule management
  - `CouponsPage`: Discount code management
- **Backend:** Full CRUD in `AdminController` and `AdminDashboardServiceImpl`

**What is Missing:**
- Product-Plan hierarchy enforcement (plans should belong to a product, but UI allows orphan plans)
- Plan versioning/history (SRS mentions effective start/end dates, but no version control)
- Add-on and metered component linking to specific plans (they are global currently)

**Recommended Changes:**
1. **No major SRS change** — well implemented
2. Add `product_id` foreign key constraint enforcement in plan creation UI/API
3. Consider adding plan add-on linkage (which add-ons are available for which plan)

---

### US-04 — Taxes & Discounts *(Must Have)*

**Original SRS:**
> As a finance manager, I want taxes and discounts applied correctly for compliance and transparency.

**What is Implemented:**
- **TaxRate CRUD:** Admin can create region-based tax rates with inclusive/exclusive flag
- **Coupon CRUD:** Admin can create amount/percentage coupons with duration, expiry, usage limits
- **Checkout Logic:** `SubscriptionFlowServiceImpl` applies 18% GST calculation:
  ```java
  long tax = Math.round(taxableAmount * 0.18);
  ```
- **Invoice Display:** Line items show tax and discount breakdowns

**What is Missing:**
- Tax logic is **hardcoded to 18%** instead of looking up `TaxRate` by customer region
- `inclusive` vs `exclusive` tax mode is not actually enforced in calculation logic
- Coupon application at checkout exists, but coupon usage count is not incremented
- No finance manager role — all configuration is done by Admin

**Recommended Changes:**
1. **Change US-04 actor** from "Finance Manager" to "System (Billing Engine)" for the application part
2. **Add US-04a:** "As an admin, I want to configure tax rules and coupons..."
3. **Fix tax calculation:** Look up customer's region → find applicable `TaxRate` → apply `ratePercent` dynamically
4. **Implement inclusive tax mode:** If `taxRate.inclusive == true`, subtract tax from displayed price; if `false`, add tax to subtotal
5. **Increment `redeemed_count`** on `Coupon` when used

---

### US-05 — Renewal Reminders *(Must Have)*

**Original SRS:**
> As a customer, I want renewal reminders so that I'm informed before charges.

**What is Implemented:**
- `Notification` entity exists in data model
- `Channel.java` enum exists (`EMAIL`, `SMS`, `IN_APP`)
- **No actual notification service, scheduler, or sender**

**What is Missing (Critical):**
- No `NotificationService` class
- No scheduler to check for T-7 and T-1 reminder triggers
- No email/SMS mock service implementation
- No notification log/history for customers
- No customer notification preference management

**Recommended Changes:**
1. **Change US-05 actor** from "Customer" to "System (Notification Service)"
2. **Create `NotificationService.java`** with `@Scheduled(cron = "0 0 9 * * ?")` (daily at 9 AM)
3. **Reminder Logic:**
   - Query subscriptions where `current_period_end` is exactly 7 days away → send T-7 reminder
   - Query subscriptions where `current_period_end` is exactly 1 day away → send T-1 reminder
   - Include amount, date, and manage link in notification
4. **Create `NotificationLog` entity** to track sent notifications (prevent duplicates)
5. **Frontend:** Add notification bell/history in customer dashboard; add preferences page

---

### US-06 — Upgrades & Downgrades with Proration *(Must Have)*

**Original SRS:**
> As a customer, I want upgrades/downgrades mid-cycle with proration, so charges are fair.

**What is Implemented:**
- **Frontend:** `SubscriptionPage` allows plan comparison and upgrade selection
- **Backend:** `CustomerSubscriptionServiceImpl.upgradeSubscription()` implements:
  - Trial upgrade: transfers remaining trial days to new plan
  - Active upgrade: calculates proration credit/debit, charges immediately, resets billing period
  - Proration formulas documented in `DOCUMENTATION.md` and `SYNTACTICAL_CODE_EXECUTION_FLOWS.md`
- **Invoice Generation:** Creates `PAID` proration invoice on active upgrade

**What is Missing:**
- **Downgrades are not implemented** — only upgrades exist
- No option to "defer charge to next renewal" (always charges immediately)
- No handling for upgrades during the last X days of a billing period (some systems block this)

**Recommended Changes:**
1. **Add downgrade support:** Similar to upgrade but with credit generation instead of charge
2. **Add `prorationBehavior` to Plan:** `IMMEDIATE_CHARGE` or `DEFER_TO_NEXT_CYCLE`
3. **SRS Clarification:** Add acceptance criteria for downgrade scenarios
4. Consider adding a "preview proration" endpoint so customers can see the cost before confirming

---

### US-07 — Revenue Analytics *(Must Have)*

**Original SRS:**
> As a finance manager, I want revenue analytics so that I can track MRR/ARR and churn.

**What is Implemented:**
- `AdminDashboardServiceImpl.getDashboardStats()` returns:
  - `totalProducts`, `totalPlans`, `activeCoupons`, `totalAddOns`
  - `activeTaxRates`, `totalPriceBooks`, `totalCustomers`, `totalStaff`
- **No MRR, ARR, ARPU, Churn, or LTV calculations**

**What is Missing (Critical):**
- No analytics calculation engine
- No `MrrSnapshot` or `RevenueMetric` entity
- No scheduled job to calculate daily metrics
- No CSV/PDF export functionality
- No time-range filtering
- No plan/region breakdowns

**Recommended Changes:**
1. **Create `RevenueAnalyticsService.java`** with methods:
   - `calculateMRR(LocalDate date)` — sum of all active subscription plan prices
   - `calculateARR(LocalDate date)` — MRR × 12
   - `calculateChurnRate(LocalDate from, LocalDate to)` — (canceled / total at start) × 100
   - `calculateARPU(LocalDate date)` — MRR / active customer count
2. **Create `RevenueSnapshot` entity** with daily snapshots (date, mrr, arr, activeCustomers, newCustomers, churnedCustomers)
3. **Add `@Scheduled` daily snapshot job** at 00:30 UTC (after billing run)
4. **Add reporting endpoints:**
   - `GET /api/admin/reports/mrr?from=&to=`
   - `GET /api/admin/reports/arr?from=&to=`
   - `GET /api/admin/reports/churn?from=&to=`
   - `GET /api/admin/reports/export?format=csv|pdf`
5. **Frontend:** Enhance `AdminDashboardPage` with charts (MRR trend, churn rate, plan distribution)

---

### US-08 — Refunds & Credit Notes *(Must Have)*

**Original SRS:**
> As a support agent, I want to issue refunds/credit notes so that customer disputes are handled.

**What is Implemented:**
- `CreditNote` entity exists
- `CustomerBillingController.getCreditNotes()` returns customer's credit notes
- `BillingPage` displays credit notes
- `Payment` entity has `REFUNDED` and `PARTIALLY_REFUNDED` statuses

**What is Missing:**
- **No refund endpoint** — `POST /payments/{id}/refund` is in API design but not implemented
- **No support agent role or portal** — only `ROLE_ADMIN` and `ROLE_CUSTOMER` exist
- **No automated credit note generation** on refund
- **No audit log** for refund actions (despite `AuditLog` entity existing)
- **No refund approval workflow**

**Recommended Changes:**
1. **Add `ROLE_SUPPORT_AGENT`** to `UserRole.java`
2. **Implement `POST /api/admin/payments/{id}/refund`** in a new `FinanceController` or `AdminController`
3. **Refund Logic:**
   - Validate payment exists and is `SUCCESS`
   - Validate refund amount ≤ original payment amount
   - Create `CreditNote` linked to original invoice
   - Update payment status to `REFUNDED` or `PARTIALLY_REFUNDED`
   - Record audit log entry
4. **Frontend:** Create `SupportConsole` with customer lookup, refund workflow, and communication log
5. **SRS Change:** Rename actor from "Support Agent" to "Support Agent / Admin" (since no separate support role exists yet)

---

### US-09 — Dunning & Retry Strategies *(Must Have)*

**Original SRS:**
> As an admin, I want dunning & retry strategies so failed payments recover automatically.

**What is Implemented:**
- `Status.java` has `PAST_DUE` and `ON_HOLD` — but they are never used
- `Invoice` has `OPEN`, `PAID`, `VOID`, `UNCOLLECTIBLE` — but `UNCOLLECTIBLE` is never used
- No dunning service, scheduler, or configuration

**What is Missing (Critical):**
- **Dunning configuration entity** (retry schedule: 1d, 3d, 7d; max attempts; final action)
- **Dunning execution service** — no `@Scheduled` job to process `PAST_DUE` subscriptions
- **Payment retry logic** — no re-attempt mechanism
- **Customer notification on retry** — no "update your card" reminder
- **Final action logic** — cancel subscription or place `ON_HOLD` after max retries

**Recommended Changes:**
1. **Change US-09 actor** from "Admin" to "System (Billing Engine)"
2. **Add US-09a:** "As an admin, I want to configure dunning strategies..."
3. **Create `DunningConfig` entity:**
   - `retrySchedule` (e.g., "1,3,7" days after failure)
   - `maxRetryAttempts` (e.g., 3)
   - `finalAction` (`CANCEL` or `HOLD`)
   - `sendReminder` (boolean)
4. **Create `DunningService.java`** with `@Scheduled(cron = "0 0 6 * * ?")` (daily at 6 AM):
   - Find `PAST_DUE` subscriptions with retry attempts < max
   - If today matches retry schedule day → attempt payment again
   - On success → mark `ACTIVE`, send success notification
   - On final failure → apply `finalAction` (`CANCEL` or `ON_HOLD`)
5. **Create `DunningAttempt` entity** to track each retry (date, attempt number, result, failure reason)
6. **Frontend:** Add dunning configuration page in admin; add retry history in customer billing

---

## 5. Missing Roles & Portals

The SRS defines 5 actors, but the implementation only has 2 roles. Here is the gap analysis:

| Actor | Role in Code | Portal in Frontend | Status |
|---|---|---|---|
| **Customer** | `ROLE_CUSTOMER` | `/dashboard/*` | **Implemented** |
| **Admin** | `ROLE_ADMIN` | `/admin/*` | **Implemented** |
| **Finance Manager** | ❌ Not defined | ❌ No routes | **Missing** |
| **Support Agent** | ❌ Not defined | ❌ No routes | **Missing** |
| **System** | N/A | N/A | Partial (no schedulers) |

### Recommended Resolution

**Option A: Add Full Roles (Recommended for SRS compliance)**
- Add `ROLE_FINANCE_MANAGER` and `ROLE_SUPPORT_AGENT` to `UserRole.java`
- Create `FinanceController` with endpoints for: invoice aging, collections, revenue reports, refund approval
- Create `SupportController` with endpoints for: customer lookup, ticket management, refund processing
- Add frontend routes: `/finance/*`, `/support/*`

**Option B: Consolidate Under Admin (Simpler for current scope)**
- Keep only `ROLE_ADMIN`
- Add admin sub-permissions or sections for "Finance" and "Support" functions
- Update SRS to reflect this simplification
- **Note:** This contradicts the original SRS but may be pragmatic for project delivery

---

## 6. Recommended Action Items

### Priority 1 — Critical (Blocking Core Flow)

| # | Action | User Story | Effort |
|---|---|---|---|
| 1 | Implement `BillingEngineService` with `@Scheduled` daily billing run | US-02 | High |
| 2 | Implement `DunningService` with retry scheduler and final action logic | US-09 | High |
| 3 | Implement `NotificationService` with T-7/T-1 renewal reminders | US-05 | High |
| 4 | Add `ROLE_FINANCE_MANAGER` and `ROLE_SUPPORT_AGENT` roles | All | Medium |
| 5 | Implement `RevenueAnalyticsService` with MRR/ARR/Churn calculations | US-07 | High |

### Priority 2 — Important (Completeness)

| # | Action | User Story | Effort |
|---|---|---|---|
| 6 | Fix tax calculation to use dynamic `TaxRate` by region | US-04 | Medium |
| 7 | Add downgrade support with proration | US-06 | Medium |
| 8 | Implement refund endpoint with credit note generation | US-08 | Medium |
| 9 | Add notification preferences UI for customers | US-05 | Low |
| 10 | Add CSV/PDF export for analytics | US-07 | Medium |

### Priority 3 — Polish & Compliance

| # | Action | User Story | Effort |
|---|---|---|---|
| 11 | Add `prorationBehavior` to `Plan` entity | US-06 | Low |
| 12 | Implement inclusive vs. exclusive tax mode logic | US-04 | Low |
| 13 | Add idempotency keys to payment attempts | US-02 | Low |
| 14 | Create `AuditLog` entries for all monetary actions | US-08 | Medium |
| 15 | Update SRS document to resolve all contradictions listed in Section 3 | All | Low |

---

## Appendix A: Entity Status Usage Audit

The following audit shows which `Status` enum values are actually used in the codebase:

| Status Value | Used By | Actually Used? | Where |
|---|---|---|---|
| `ACTIVE` | Customer, Subscription, Plan, AddOn, Coupon | ✅ Yes | Throughout |
| `INACTIVE` | Plan, AddOn, Product, Customer | ✅ Yes | Toggle methods |
| `SUSPENDED` | Customer | ❌ No | Not referenced in code |
| `DRAFT` | Subscription | ❌ No | Never set |
| `TRIALING` | Subscription | ✅ Yes | Checkout flow |
| `PAST_DUE` | Subscription | ❌ No | Never set (needs billing engine) |
| `PAUSED` | Subscription | ✅ Yes | Pause feature |
| `CANCELED` | Subscription | ✅ Yes | Cancel feature |
| `ON_HOLD` | Subscription | ❌ No | Never set (needs dunning) |
| `OPEN` | Invoice | ✅ Yes | Trial invoices, new invoices |
| `PAID` | Invoice, Payment | ✅ Yes | Successful payments |
| `VOID` | Invoice | ✅ Yes | Void endpoint exists |
| `UNCOLLECTIBLE` | Invoice | ❌ No | Never set (needs dunning) |
| `PENDING` | Payment | ❌ No | Never set |
| `SUCCESS` | Payment | ✅ Yes | Mock gateway |
| `FAILED` | Payment | ❌ No | Never set (needs billing engine retry) |
| `REFUNDED` | Payment | ❌ No | Never set (needs refund endpoint) |
| `PARTIALLY_REFUNDED` | Payment | ❌ No | Never set |
| `ISSUED` | CreditNote | ✅ Yes | Default on creation |
| `APPLIED` | CreditNote | ❌ No | Never set |
| `VOIDED` | CreditNote | ❌ No | Never set |
| `EXPIRED` | Coupon | ❌ No | Never set (needs expiration check) |
| `REVOKED` | PaymentMethod | ❌ No | Never set |
| `DISABLED` | Coupon | ✅ Yes | Toggle method |

**Finding:** Approximately **40% of status values are never used**, primarily because the automated billing engine, dunning system, refund workflow, and notification service are not yet implemented.

---

*End of Document*
