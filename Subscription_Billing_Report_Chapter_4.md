# CHAPTER 4: METHODOLOGY

## 4.1 Detailed Methodology

### 4.1.1 Agile Scrum Framework
The Subscription Billing & Revenue Management System was developed using the Agile Scrum framework, which provided an iterative and structured approach to managing the complexity of a multi-role enterprise billing application during the internship at Infosys Limited.

The development lifecycle was organized around the following Scrum ceremonies and artifacts:
* **Product Backlog**: A comprehensive backlog was maintained containing all prioritized user stories representing system functionality. Each user story was written to express business value from the actor's perspective: "As a [role], I want to [action] so that [benefit]." User stories were prioritized based on architectural dependencies, placing foundation items (such as the database schema, User and Customer tables, and Spring Security session filters) in early sprints. User stories were mapped to unique IDs (e.g., US01 to US09) for complete lifecycle traceability.
* **Sprint Planning**: At the start of each sprint, the team reviewed the prioritized backlog to define the sprint backlog. Tasks were estimated in story points representing engineering complexity and broken down into technical implementation subtasks: database scripting, JPA entity mappings, repository interfaces, REST controllers, React components, and JUnit test cases.
* **Daily Progress Tracking**: Work progress was tracked daily by updating task states (To Do, In Progress, In Review, Done) on the project board, allowing visibility into sprint velocity, burndown rate, and highlighting immediate blockers.
* **Sprint Review and Retrospective**: At the sprint's conclusion, completed user stories were demonstrated to mentors and checked against predefined acceptance criteria. Retrospectives were conducted to identify workflow bottlenecks, improve integration routines, and optimize subsequent sprint cycles.

### 4.1.2 Development Workflow
Each user story was implemented using a systematic development workflow to ensure complete vertical slice delivery:
* **Step 1 — Relational Database Schema Design**: Database tables, indexes, primary keys, and foreign keys were designed and recorded in SQL scripts. Column structures were evaluated for transactional integrity, and key constraints were added to prevent billing data anomalies.
* **Step 2 — JPA Entity & Repository Mapping**: Java classes corresponding to the SQL schema were created inside the `entity` package (e.g., `Subscription.java`, `Invoice.java`). Annotations from `jakarta.persistence` mapped these classes to MySQL tables. Repository interfaces extending `JpaRepository` were created in the `repository` package to define database access boundaries.
* **Step 3 — Service Layer Implementation**: Core business logic and calculations (e.g., proration math, billing runs, and retry intervals) were implemented in the `service` package. Each service class used the try-catch block pattern to handle exceptions, returning dedicated custom exceptions where necessary.
* **Step 4 — Controller Layer Implementation**: Spring Boot REST controllers were built inside the `controller` package, injecting backend services via constructor injection. Controllers exposed JSON endpoints using `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, and `@DeleteMapping` annotations.
* **Step 5 — API Testing with Swagger/Postman**: Implemented endpoints were tested through Swagger UI and Postman collections. Mock payloads were sent to verify correct JSON mapping, HTTP status codes, and exception handler responses.
* **Step 6 — React UI Component Development**: The React UI was structured into modular components inside the `components` and `pages` folders. Axios service classes were implemented to handle HTTP communications with Spring Boot, using credential settings to pass session cookies. Component pages were integrated with routing tables utilizing lazy loading.
* **Step 7 — Automated Unit & Integration Testing**: Unit tests were implemented inside the `src/test/java` folder using JUnit 5 and Mockito. Test cases verified calculations (like proration logic) in isolation by mocking repository layers.

### 4.1.3 Version Control Strategy
A branching strategy was enforced using Git to manage source code changes. The `main` branch hosted the stable, production-ready release of the billing application. The `dev` branch acted as the main integration branch where completed features were consolidated. Individual features were developed on isolated branches named `feature/[feature-name]` (e.g., `feature/customer-subscriptions`, `feature/dunning-engine`). Upon feature completion, pull requests (PRs) were raised targeting the `dev` branch. Code reviews and automated check-ins were required to verify that the code compiled successfully before merging.

### 4.1.4 Team Collaboration
The project was developed in a collaborative team setup. Tasks were distributed by functional modules, with developers taking ownership of the full-stack implementation—from the MySQL tables and Spring Boot services down to the React frontend interfaces—for their assigned modules, ensuring high communication and smooth integration.

---

### Figure 4.1: Three-Tier Client-Server Architecture Diagram
```
┌────────────────────────────────────────────────────────┐
│                   Presentation Tier                    │
│                 React SPA Client (UI)                  │
└──────────────────────────┬─────────────────────────────┘
                           │ HTTP REST requests
                           │ (JSON, JSESSIONID Cookie)
                           ▼
┌────────────────────────────────────────────────────────┐
│                   Business Logic Tier                  │
│                Spring Boot API Application             │
└──────────────────────────┬─────────────────────────────┘
                           │ JPA / Hibernate ORM
                           │ (SQL Queries & Transactions)
                           ▼
┌────────────────────────────────────────────────────────┐
│                       Data Tier                        │
│                     MySQL Database                     │
└────────────────────────────────────────────────────────┘
```

---

## 4.2 Technical Architecture

### 4.2.1 System Architecture Overview
The Subscription Billing & Revenue Management System uses a three-tier client-server architecture to separate responsibilities. The Presentation Tier is a single-page application built on React and TypeScript that manages the visual dashboards for customers and operators. The Business Logic Tier is a Spring Boot REST API application hosting the billing engine, secure authentication rules, proration math, and notifications. The Data Tier is a MySQL database that stores relational data. The client communicates with the backend via JSON over HTTP. Cross-origin credential policies ensure that HTTP requests include the user session cookie (`JSESSIONID`) to authenticate against Spring Security filter chains, while JPA translates Java entity manipulations into SQL transactions.

### 4.2.2 Solution Structure
The backend Maven repository is organized into a modular package layout:
* `com.infy.billing.config`: Class files for Spring Security, CORS filter chains, and general system configurations.
* `com.infy.billing.controller`: REST controllers managing incoming HTTP JSON requests and returning structured HTTP responses.
* `com.infy.billing.service`: Service interfaces and implementations executing core algorithms, dunning retries, and analytics calculations.
* `com.infy.billing.repository`: Spring Data JPA interfaces extending `JpaRepository` for MySQL database interactions.
* `com.infy.billing.entity`: JPA persistence classes mapped to MySQL database tables.
* `com.infy.billing.dto` / `com.infy.billing.request`: Data transfer objects and validation classes handling input payloads.
* `com.infy.billing.exception`: Custom runtime exceptions and global exception handler annotations.

The React frontend utilizes a modular folder hierarchy:
* `/src/components`: Reusable UI elements like buttons, inputs, tables, and charts.
* `/src/context`: React Context state providers handling global variables like authenticated user sessions.
* `/src/layouts`: Component containers styling sidebar navigations and dashboard frameworks.
* `/src/pages`: Actor-specific portals (Customer, Admin, Finance, Support) containing distinct transaction pages.
* `/src/services`: Service scripts using Axios to communicate with REST controllers.
* `/src/routes`: Route configurations utilizing lazy-loading to optimize bundle compilation.

### 4.2.3 Authentication and Authorization Flow
The authentication sequence begins when a user inputs credentials on the login screen. The React frontend sends a POST request with the user's email, password, and requested portal view to `/api/customer/login` or `/api/manager/login`. The Spring Boot `AuthController` receives the payload and passes it to the `AuthenticationManager`. The `DaoAuthenticationProvider` retrieves user data from the database using `UserDetailsService`, hashes the incoming password using BCrypt, and compares it against the stored password hash.

On success, Spring Security initiates a session, creating a Security Context on the server and returning a secure `JSESSIONID` cookie in the `Set-Cookie` HTTP header. The React frontend stores the user metadata in Context state and includes credentials in subsequent API calls. Spring Security filters intercept these requests to validate the active session. If session authentication fails, the filter blocks API execution and returns an HTTP 401 Unauthorized status, which triggers the React client to redirect the user to the login screen.

### 4.2.4 React Application Architecture
The frontend React application utilizes component-based rendering. App routes are declared using `react-router-dom` and mapped to role-specific layouts. Lazy loading splits the frontend bundle, downloading page files only when a user navigates to them to reduce initial page load times.

Global application state (such as the authenticated user's session details) is managed using the React Context API, which exposes state variables and update functions to child components without prop drilling. Components fetch remote data from Spring Boot using Axios wrapper classes configured with `withCredentials: true` to pass the session cookie. Layout templates provide role-specific sidebar navigation, displaying links based on the active user's permissions.

---

## 4.3 Database Design

### 4.3.1 Entity Relationship Overview
The system database contains 25 tables designed to support transactional billing data. The schema ensures data integrity through primary and foreign key constraints and prevents redundancy through normalization.

The core entity is the `User` table, which stores identity and credential details. The `Customer` table links to a specific user record via the `UserId` foreign key and adds billing-specific parameters like currency preference, status, and billing address. A `Subscription` maps a `Customer` to a catalog `Plan`. An `Invoice` is created for subscription renewals and contains multiple `InvoiceLineItem` rows. Payments are recorded in the `Payment` table, referencing the corresponding invoice. Relationship rules, such as cascading deletions or constraints, ensure that deleting a product updates associated plans and subscriptions in a consistent manner.

### 4.3.2 Core Tables Description
The relational database supports distinct billing and configuration modules:
* **Identity & Accounts**: The `User` table stores core authentication credentials, while the `Customer` table maintains billing addresses and payment statuses.
* **Catalog Configuration**: The `Product`, `Plan`, `AddOn`, and `MeteredComponent` tables store plan configurations. The `PriceBookEntry` table maps currency variations, and the `TaxRate` and `Coupon` tables store regional taxes and discount rules.
* **Subscription Transactions**: The `Subscription` table tracks lifecycle states, while the `SubscriptionItem` and `SubscriptionCoupon` tables link add-ons and discounts to active subscriptions.
* **Invoicing & Ledger**: The `Invoice` and `InvoiceLineItem` tables record generated charges. The `Payment` and `PaymentMethod` tables track transactions, and the `CreditNote` table records refunds.
* **Logs & System Auditing**: The `DunningRetryLog` table tracks failed transactions and recovery progress, the `Notification` table logs sent communications, and the `AuditLog` table maintains immutable logs of financial and administrative changes.

---

## 4.4 API Workflow and Integration

### 4.4.1 RESTful API Design
The backend REST API follows resource-oriented design principles, returning structured JSON payloads and standard HTTP status codes. Controllers utilize Spring's `@RestController` annotation and map base paths under `/api`.

The endpoints are grouped into dedicated controller files to separate concerns. The `AuthController` handles logins and logouts, while the `CustomerController` manage profile updates. Subscription actions are routed to `CustomerSubscriptionController`, and invoicing and billing run processes are mapped under `CustomerBillingController`. Catalog, financial analytics, and refund operations are managed by the `AdminController`, `FinanceController`, and `SupportController` respectively. Endpoints use standard HTTP verbs: GET for record lookups, POST for creations, PUT/PATCH for adjustments, and DELETE for removals, returning status codes like 200 OK, 201 Created, 400 Bad Request, and 401 Unauthorized to indicate result status.

### 4.4.2 Repository Pattern Implementation
The backend uses the repository pattern to isolate business logic from database interactions. Repository interfaces extend `JpaRepository`, exposing standard database operations.

Custom query methods (such as fetching subscriptions due for renewal within a specific time window) are declared in the repository interfaces using JPQL or native SQL queries. Transaction boundaries are managed at the service layer using `@Transactional` annotations. This ensures that multi-step database actions—such as processing a plan change, adjusting a balance, and updating invoices—are executed as atomic transactions, reverting database changes if any step fails.

---

## 4.5 Module Design Overview

### 4.5.1 Customer Portal Module
The Customer Portal handles customer self-service actions, centering around an interactive dashboard. The dashboard displays the active subscription plan, the next renewal date, payment method details, and a billing history table with links to download invoice PDFs. It includes a plan modification page to change tiers and a card management component to update credit cards.

### 4.5.2 Catalog Management Module
The Catalog Management Module enables administrators to configure platform pricing. Admins configure products and link them to base plans with trial periods, setup fees, and tax behaviors. They can also create coupons (percentage-off or fixed-amount discounts) and regional tax rates (GST/VAT) to apply to invoices.

### 4.5.3 Subscription Lifecycle & Proration Module
The Subscription Lifecycle & Proration Module manages state transitions (Trialing, Active, Past Due, Canceled, Paused) and mid-cycle plan changes. If a user changes plans mid-cycle, the proration engine calculates credit for unused days on the old plan and cost for remaining days on the new plan. It charges the net difference immediately and updates the subscription record.

### 4.5.4 Billing & Invoicing Engine Module
The Billing & Invoicing Engine Module manages recurring invoicing. A daily scheduler checks active subscriptions due for renewal. It aggregates base prices, active add-on rates, regional taxes, and coupons to generate line-item invoices. The engine then submits the payment request to the payment gateway to process the charge.

### 4.5.5 Mock Payment Gateway & Dunning Recovery Module
The Mock Payment Gateway & Dunning Recovery Module handles payment simulation and collection failures. The mock gateway tokenizes card credentials and simulates payment results. If a charge fails, the dunning system schedules automated retries at T+1, T+3, and T+7 days, sending failure emails to the customer. If the final retry fails, the system cancels the subscription.

### 4.5.6 Finance Reports & Revenue Analytics Module
The Finance Reports & Revenue Analytics Module provides financial data visibility for managers. The analytics engine aggregates database records to calculate Monthly Recurring Revenue (MRR), Annual Recurring Revenue (ARR), and Churn rates, displaying the results in interactive charts and supporting data exports to PDF or CSV formats.

### 4.5.7 Support & Customer Adjustment Module
The Support & Customer Adjustment Module helps support agents resolve customer issues. Agents look up customer accounts to view their billing history, pause or resume subscriptions, and issue full or partial refunds for billing disputes.

### 4.5.8 Notifications & Audit Logging Module
The Notifications & Audit Logging Module manages communications and compliance. The notification service triggers emails for upcoming renewals, payment receipts, and billing failures, logging sent messages in the database. The audit log service records administrative and financial actions to ensure compliance.

---

## 4.6 Billing Lifecycle Workflow

The subscription billing lifecycle is an automated system-driven process that coordinates plan catalog selection, subscription instantiation, invoice generation, payment capture, and error recovery to manage customer accounts while reducing involuntary churn. This workflow is structured as follows:

### 4.6.1 Customer Purchase
The workflow begins when a customer browses the subscription plans, selects a billing tier (e.g., Monthly or Annual), enters payment credentials, and submits the checkout form. The React client validates inputs before transmitting the request as a JSON payload to the REST endpoint.

### 4.6.2 Subscription Creation
Upon receiving the checkout payload, the billing service initializes a new subscription entity in the database. The record is assigned a unique primary key identifier, and its state is set to an initial status of `PENDING` or `TRIALING`. The subscription maps the customer to their selected plan, configuring dates for the billing cycle start and the next renewal run.

### 4.6.3 Invoice Generation
Immediately after database creation, the invoicing engine is invoked. It aggregates the plan's base price, configurations, regional tax rates, and discount codes to generate an itemized invoice. This record, along with corresponding line items, is persisted in the ledger with a status of `UNPAID`.

### 4.6.4 Payment Capture Attempt
With the invoice created, the payment execution service initiates an API call to the payment gateway using the customer's stored payment credentials. The system records the transaction attempt in the payment ledger.

### 4.6.5 Success Flow: Active State Transition
If the gateway returns a successful response code, the payment service updates the transaction record to `SUCCESS` and marks the invoice as `PAID`. The subscription status is upgraded to `ACTIVE`. A webhook triggers the notification engine to email a PDF payment receipt generated via the OpenPDF library, and updates the customer portal UI to reflect full access.

### 4.6.6 Failure and Dunning Recovery Flow
If the gateway returns a failure (e.g., due to insufficient funds, expired cards, or network timeouts), the invoice remains `UNPAID`. The system initiates a dunning recovery process:
* **State Change**: The subscription status transitions to `PAST_DUE`.
* **Scheduled Retries**: The scheduler queue logs a dunning entry, programming retries at fixed intervals of `T+1`, `T+3`, and `T+7` days.
* **Customer Alerts**: During each retry window, the notification service sends automated email alerts requesting the customer to update their payment details.
* **Resolution Path**: If any retry payment succeeds, the subscription transitions to `ACTIVE` and the invoice is updated to `PAID`. If all dunning retries fail after `T+7` days, the subscription is updated to `CANCELLED`, terminating customer access.

---

### Figure 4.2: Billing Lifecycle Workflow
*(Refer to the Billing Lifecycle flow diagram showing the transition paths between initial customer purchase, invoice generation, payment confirmation, and the T+1, T+3, and T+7 dunning retry steps)*

