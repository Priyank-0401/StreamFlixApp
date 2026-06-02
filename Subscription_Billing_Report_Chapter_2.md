# CHAPTER 2: REQUIREMENT ANALYSIS

## 2.1 Functional Requirements

The Subscription Billing & Revenue Management System was analyzed in detail to outline functional requirements across all five operational roles (Customer, Admin, Finance Manager, Support Agent, and Billing Engine System). The functional specifications defined below were established through backlog grooming sessions, database relationship design, and iterative Agile sprints during the internship at Infosys Limited.

### 2.1.1 Customer Portal Requirements
The Customer Portal provides the primary user-facing interface, allowing clients to purchase plans and self-manage their subscriptions. The functional requirements for this module are:
* The system shall allow new customers to register by entering their full name, email address, password, phone number, currency preference, and billing address.
* The system shall allow customers to log in using their credentials and check their personalized dashboard.
* The system shall display a Customer Dashboard summarizing active subscriptions, upcoming renewal dates, transaction history, and links to download historical invoice PDFs.
* The system shall present an onboarding wizard displaying a list of available products and plans, allowing the customer to select a base plan, choose optional add-on services, apply discount coupon codes, and enter mock payment card credentials.
* The system shall allow customers to request mid-cycle subscription changes (upgrades or downgrades), automatically displaying computed proration lines and charging or crediting their account balance accordingly.
* The system shall allow customers to cancel active subscriptions immediately or schedule cancellation to take effect at the end of the current billing period.
* The system shall allow customers to view and update default payment methods and configure notification preferences.

### 2.1.2 Admin Console Requirements
The Admin Console provides catalog setup and management tools for platform operators. The functional requirements are:
* The system shall allow Admins to perform CRUD operations on the Product catalog, defining attributes like product name, description, and status.
* The system shall allow Admins to define Plans linked to products, including parameters like base price, billing interval (monthly, quarterly, yearly), trial period days, setup fee, and tax mode (inclusive or exclusive).
* The system shall allow Admins to configure Price Book entries to support multi-currency plans (USD, INR, EUR) with effective start and end dates.
* The system shall allow Admins to create and manage Add-Ons (recurring/one-time) and Metered Components (charged per unit of consumption).
* The system shall allow Admins to configure region-based Tax Rates specifying the tax percentage (GST/VAT) and whether the rate is applied inclusively or exclusively.
* The system shall allow Admins to create Coupons (fixed-amount or percentage-off) with restrictions like coupon code, duration (once, repeating, forever), maximum redemptions, and valid date ranges.
* The system shall display a dashboard to monitor manual billing runs and inspect execution logs of system schedulers.

### 2.1.3 Finance Manager Requirements
The Finance Manager Portal enables financial auditing, revenue monitoring, and dispute resolution. The requirements are:
* The system shall allow Finance Managers to view all system-generated invoices with status filters (Draft, Open, Paid, Past Due, Void).
* The system shall display a real-time Finance Dashboard tracking critical metrics, including Monthly Recurring Revenue (MRR), Annual Recurring Revenue (ARR), Average Revenue Per User (ARPU), and Churn Rate (Gross & Net).
* The system shall allow Finance Managers to view payment collections, transaction logs, and retry attempts for delinquent accounts.
* The system shall allow Finance Managers to record manual adjustments, issue Credit Notes against specific invoices, and initiate full or partial refunds for billing disputes.
* The system shall support exporting revenue analytics and transaction history lists to CSV and PDF formats.

### 2.1.4 Support Agent Requirements
The Support Console helps agents resolve customer account issues. The requirements are:
* The system shall allow Support Agents to lookup customer profiles using email address, customer name, or customer ID.
* The system shall display a customer timeline showing subscription changes, billing cycles, invoices, and communication records.
* The system shall allow Support Agents to pause or resume customer subscriptions and adjust next renewal dates based on manager approvals.
* The system shall allow Support Agents to process refunds and log customer notes for audit tracking.

### 2.1.5 Billing Engine (System) Requirements
The automated Billing Engine is a background scheduler that handles renewals and payment collection. The requirements are:
* The system shall automatically execute a daily billing cycle run (via a scheduler) to identify all active subscriptions due for renewal within the next 24 hours.
* The system shall consolidate base plan prices, add-on rates, metered usage records, taxes, and coupons to generate detailed line-item invoices.
* The system shall submit payment requests to the payment gateway using an idempotency key to prevent double charging.
* On payment success, the system shall mark the invoice as "Paid," extend the subscription's current period end date, and create a new billing cycle state.
* On payment failure, the system shall transition the subscription status to "Past Due" and enqueue the invoice for dunning retries.

### 2.1.6 Payment Gateway Integration (Mock) Requirements
The gateway integration simulates electronic card processing. The requirements are:
* The system shall securely tokenize mock credit card details, returning a token rather than storing raw card numbers (PAN).
* The system shall support a Mock Charge API that returns simulated transaction results (Success, Insufficient Funds, Expired Card, Network Error) based on mock card patterns.
* The system shall validate transaction idempotency keys to prevent duplicate transactions within the gateway.
* The system shall expose endpoints for simulating payment webhooks to test asynchronous transaction updates.

### 2.1.7 Dunning & Retry Strategy Requirements
The dunning system recovers revenue from failed payments automatically. The requirements are:
* On invoice payment failure, the system shall initiate the dunning cycle, scheduling automated payment retries at set intervals (e.g., T+1 day, T+3 days, and T+7 days).
* The system shall send payment failure notifications and card update links to customers at each retry attempt.
* The system shall automatically update subscription states: keeping it "Past Due" during retries, and transitioning it to "Canceled" or "On Hold" if payment fails on the final attempt.

### 2.1.8 Notification Engine Requirements
The notification system keeps users informed of key billing milestones. The requirements are:
* The system shall send renewal reminders T-7 and T-1 days before billing charges occur.
* The system shall send email receipts containing line-item tax and coupon details upon successful payment.
* The system shall maintain an audit log of all sent notifications (email and SMS), tracking timestamp, recipient, delivery status, and retry history.

---

## 2.2 Non-Functional Requirements

The following non-functional requirements define the quality, performance, security, and architectural standards for the Subscription Billing System.

### 2.2.1 Performance
The Subscription Billing System is designed to offer high throughput and minimal latency, particularly during high-volume batch executions like daily renewal cycles. The billing engine is optimized to process up to 10,000 active subscriptions within a 10-minute window, ensuring that recurring charges are computed and completed without backend timeouts.

Furthermore, the customer-facing REST APIs, such as the plan catalogs and subscription detail endpoints, are designed to respond within 1.5 seconds under a simulated load of 500 concurrent requests. On the database tier, complex aggregate queries used to compute revenue statistics (like MRR, ARR, and Churn rates) are optimized by applying index mapping to foreign keys and status columns in the subscription and invoice tables, reducing query latency as records scale.

### 2.2.2 Security
Security is a core priority of the system due to the sensitive nature of user credentials, billing records, and payment profiles. To secure endpoints, the system implements session-based authentication utilizing Spring Security filters and secure `JSESSIONID` cookies. Authorization interceptors and route permissions are enforced at both client and server levels to restrict API endpoints strictly to their respective user roles (Customer, Admin, Finance, Support).

To protect sensitive user data, passwords are salted and hashed using the BCrypt algorithm before being saved to the database. The system prevents the storage of raw cardholder data (PAN) by relying on mock tokenization endpoints, ensuring compliance with payment processing security paradigms. Additionally, the application requires transport-layer security (TLS/HTTPS) for all data exchanges and custom CORS policies to prevent unauthorized cross-origin requests.

### 2.2.3 Usability
The user interface is built using React to provide a fluid, single-page application experience for customers and internal staff. A clean and cohesive styling system is established across all dashboards using responsive layout guidelines, allowing users to navigate catalogs, pay bills, and view revenue metrics seamlessly on desktop, tablet, and mobile screens.

To simplify complex actions, processes like subscription onboarding are structured as step-by-step wizards that guide the customer through plan selection, add-on options, and payment input. The application also provides immediate feedback through client-side input validation and dynamic notification popups, backed by server-side validation using standard annotations like `@NotNull` and `@Email` to return localized validation errors.

### 2.2.4 Reliability and Availability
The application implements strict error-handling and transaction-management mechanisms to ensure stability. Billing cycle runs and payment settlements are wrapped in transactional boundaries using `@Transactional` annotations, guaranteeing that database updates are completely rolled back in the event of an unexpected runtime failure to prevent data corruption.

To prevent duplicate charges, payment requests require a unique idempotency key validation at the controller level before the charge service communicates with the payment gateway. Centralized exception handling is implemented via a Spring `@ControllerAdvice` controller, which intercepts application-specific exceptions (such as payment failures or invalid coupon errors) and converts them into standardized, user-friendly JSON responses without exposing internal stack traces.

### 2.2.5 Maintainability
The backend codebase is organized according to a clear three-tier architecture (Presentation, Business Logic, and Data Access), dividing code into controllers, services, repositories, entities, and data transfer objects (DTOs). This strict separation of concerns simplifies debugging, updates, and future microservice migration.

To verify software quality, the backend utilizes JUnit and Mockito to perform automated unit and integration testing, targeting a minimum code coverage threshold of 80%. System configurations (including database details, port numbers, CORS configurations, and scheduler cron intervals) are externalized into YAML files to support transition across development, staging, and production environments.

### 2.2.6 Scalability
The backend API layer is designed to be completely stateless, allowing horizontal scaling by deploying multiple server instances behind a standard load balancer. The MySQL database schema is normalized and indexed on active relationships (such as customer to subscription, and subscription to invoice) to ensure that analytical and billing lookups scale efficiently as customer numbers grow.

To prevent memory exhaustion during recurring runs, the daily billing cron job executes database reads in paginated batches rather than fetching the entire customer table into memory. This batch-processing model ensures that system resources scale gracefully alongside corporate growth.

---

## 2.3 Use Case Scenarios
The following use case scenarios describe the key interactions between users and the Subscription Billing & Revenue Management System across the primary functional modules.

### Use Case 1: Customer Subscribing to a Plan
* **Actors**: Customer (Primary), Billing Engine, Mock Payment Gateway (Systems)
* **Pre-conditions**: Customer has registered, logged in, and is on the plan onboarding wizard. The base plan and add-ons exist in the catalog.
* **Flow**:
  1. The Customer selects a subscription plan (e.g., premium base plan) and selects an optional add-on (e.g., streaming booster).
  2. The Customer enters coupon code "DISCOUNT20" and inputs credit card credentials.
  3. The system validates the coupon, calculates tax rates based on the customer's region, and displays the total due amount.
  4. The Customer clicks "Confirm Purchase."
  5. The system requests card tokenization, applies an idempotency key, and calls the Mock Payment Gateway to process the initial charge.
  6. The gateway returns a "SUCCESS" response.
  7. The system creates a new Subscription record, sets status to "Active," generates a finalized Invoice marked "Paid," creates a Payment record, and emails a receipt to the customer.

### Use Case 2: Scheduled Renewal Billing Engine Run
* **Actors**: Billing Engine (Scheduler), Mock Payment Gateway (Systems)
* **Pre-conditions**: The Spring `@Scheduled` cron job triggers at midnight UTC.
* **Flow**:
  1. The Billing Engine queries the database for all active subscriptions where `current_period_end` is between now and the next 24 hours.
  2. The engine loops through the matched subscriptions and retrieves the active plan, setup fees, add-ons, and coupon mappings.
  3. The engine creates an Invoice record marked "Draft" and calculates line-item totals, discounts, and regional taxes.
  4. The engine updates the Invoice status to "Open" and sets the transaction UUID.
  5. The engine submits the payment request to the Mock Payment Gateway.
  6. On successful capture, the system updates the Invoice status to "Paid," updates the Subscription's `current_period_start` and `current_period_end` dates for the next month, and logs a successful payment record.

### Use Case 3: Subscription Mid-Cycle Modifications (Upgrade/Downgrade and Pause/Resume)
* **Actors**: Customer (Primary), Billing Engine (System)
* **Pre-conditions**: Customer has an active subscription.
* **Flow A (Plan Upgrade/Downgrade with Proration)**:
  1. The Customer logs in, navigates to subscription details, and selects the "Change Plan" option.
  2. The Customer selects an upgraded tier (e.g., Standard to Premium) mid-way through their 30-day billing cycle (Day 15 of 30).
  3. The system calculates the proration credits and debits:
     * *Credit*: Computes the unused portion of the old plan: $(15 / 30) \times \text{Old Plan Price}$.
     * *Debit*: Computes the cost of the new plan for the remaining days: $(15 / 30) \times \text{New Plan Price}$.
  4. The system subtracts the credit from the debit to calculate the net proration charge.
  5. The system prompts the user with the proration summary and charge details.
  6. The Customer clicks "Confirm Change."
  7. The system charges the net amount immediately, updates the Subscription items to the new plan, issues a proration invoice, and updates the billing ledger.
* **Flow B (Pause and Resume Subscription)**:
  1. The Customer logs in, selects their active subscription, and clicks the "Pause Subscription" option.
  2. The Customer enters a pause start date and optionally a resume date (e.g., pausing for 15 days during vacation).
  3. The system validates the request and sets the subscription status to "PAUSED", recording the `paused_from` timestamp in the database.
  4. During the paused interval, the automated Billing Engine skips this subscription during daily cron renewal runs, preventing invoicing.
  5. On the pause end date (or if the customer manually clicks "Resume Subscription" early), the system updates the status back to "ACTIVE", recalculates the new billing cycle end date based on the paused duration, and schedules the next renewal invoice run.

### Use Case 4: Dunning Policy Execution on Payment Failure
* **Actors**: Billing Engine, Notification Engine (Systems), Customer (Secondary)
* **Pre-conditions**: The automated billing engine renewal run encounters a payment failure (e.g., "Insufficient Funds" or "Expired Card") from the gateway.
* **Flow**:
  1. The Billing Engine receives a "FAILED" response from the gateway.
  2. The system transitions the Subscription status from "Active" to "Past Due" and flags the invoice as unpaid.
  3. The system records the transaction error code in the `DunningRetryLog` table.
  4. The Notification Engine sends a payment failure email to the customer with a direct link to update their payment details.
  5. The dunning scheduler schedules a second retry attempt at T+1 day, a third at T+3 days, and a final attempt at T+7 days.
  6. If the customer updates their card details before the final attempt, a charge is triggered. On success, the status transitions back to "Active."
  7. If the final retry attempt fails at T+7, the dunning scheduler transitions the Subscription status to "Canceled" and updates the database records.

### Use Case 5: Finance Manager Generating Revenue Analytics Report
* **Actors**: Finance Manager (Primary), Database (System)
* **Pre-conditions**: Finance Manager is logged in and navigates to the Revenue Analytics Dashboard.
* **Flow**:
  1. The Finance Manager selects the date range and granularity (monthly).
  2. The frontend sends requests to `/api/v1/reports/revenue` and `/api/v1/reports/mrr`.
  3. The backend executes aggregations on the `RevenueSnapshot` and `Subscription` tables to calculate metrics:
     * *MRR*: Sum of active subscription unit prices adjusted to their monthly equivalent.
     * *ARR*: Computed as $\text{MRR} \times 12$.
     * *Churn*: Total lost subscriptions divided by active subscriptions.
  4. The backend returns the calculated data arrays as JSON.
  5. The frontend React dashboard renders the metrics as visual charts and graphs.
  6. The Finance Manager clicks "Export PDF" to download the formatted report.

### Use Case 6: Support Agent Processing a Refund and Issuing a Credit Note
* **Actors**: Support Agent (Primary), Customer, Billing System (Secondary)
* **Pre-conditions**: A customer requests a refund for a recent billing error.
* **Flow**:
  1. The Support Agent logs in, navigates to the Support Console, and searches for the customer's account by email.
  2. The agent selects the disputed invoice from the payment history timeline.
  3. The agent clicks "Process Refund" and selects the refund type (Full or Partial), entering "Billing Dispute" as the reason.
  4. The system calls the payment gateway refund API using the payment transaction reference ID.
  5. The gateway confirms the refund.
  6. The system generates a Credit Note record containing the refunded amount and links it to the original Invoice.
  7. The system updates the original Invoice balance and logs the action in the `AuditLog` table.
