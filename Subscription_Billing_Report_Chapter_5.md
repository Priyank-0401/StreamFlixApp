# CHAPTER 5: IMPLEMENTATION

## 5.1 Project Overview
The Subscription Billing & Revenue Management System is a full-stack enterprise web application developed during the internship at Infosys Limited. The platform was designed to automate complex, recurring billing lifecycles, proration accounting, dunning recovery workflows, and financial analytics reporting. The system was built using React and TypeScript for the frontend, Spring Boot for the backend REST APIs, and MySQL as the relational database. This three-tier client-server structure ensures a clean separation of concerns between the user interface, business logic execution, and transactional persistence layers.

The development was conducted using the Agile Scrum methodology, distributing tasks across the team based on the role-based module structure. Each developer was responsible for the full-stack implementation of their assigned user stories, covering everything from the database tables and JPA entities to the React user interface pages. The project backlog, sprint tasks, code branches, and integration pull requests were managed through Git and a shared collaborative workspace, ensuring continuous delivery and validation.

The backend implementation was structured into a Maven project with three logical layers: the data access layer (comprising JPA entities, custom repositories, and data transfer objects), the service layer (housing proration math, scheduled billing cycle cron runs, and payment retry dunning handlers), and the presentation layer (exposing secure REST endpoints via Spring Boot controllers). The React frontend was built as a single-page application (SPA), featuring lazy-loaded route configurations, Context-based global session management, and role-specific navigation menus tailored for the system's four primary user roles: Customer, Admin, Finance Manager, and Support Agent.

---

## 5.2 Core Module Implementation

### 5.2.1 Authentication & User Security
The Authentication and Security Module acts as the entry gate to the system, managing access and validating identity across the application. The login interface supports authentication for all users, validating credentials against the database. User passwords are encrypted at rest using the BCrypt hashing algorithm. Upon successful authentication, the backend initializes a secure servlet session, returning a `JSESSIONID` cookie that is validated by the Spring Security filter chain on subsequent REST requests.

#### Figure 5.1: Login Page
![Login Page Layout](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/login_page.png)
*(Caption: Figure 5.1 shows the user login interface where customers and internal staff input their email credentials and password. Spring Security intercepts the input, verifies the BCrypt hash in the database, and returns a secure session JSESSIONID cookie.)*

#### Figure 5.2: Self-Registration Form
![Registration Form](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/registration_page.png)
*(Caption: Figure 5.2 displays the customer registration wizard with client-side field validation for email format, phone numbers, and default billing currencies.)*

---

### 5.2.2 Customer Portal Module
The Customer Portal acts as the client-facing cockpit, providing a personalized dashboard showing subscription statuses, next renewal dates, active plan fees, and a history table of past transactions. It houses a multi-step checkout wizard for browsing plans, selecting optional add-ons, applying discount codes, and inputting sandbox credit card details.

#### Figure 5.3: Customer Dashboard
![Customer Dashboard](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/customer_dashboard.png)
*(Caption: Figure 5.3 shows the active Customer Dashboard, showing current subscription status (ACTIVE), billing cycle dates, payment card token references, and quick links to update default payment details.)*

#### Figure 5.4: Plan Catalog and Subscription Wizard
![Subscription Wizard](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/subscription_wizard.png)
*(Caption: Figure 5.4 displays the plan catalog onboarding flow, allowing customers to choose standard or premium tiers, select optional add-ons, validate coupon codes, and enter card credentials.)*

#### Figure 5.5: Subscription Mid-Cycle Plan Modification (Proration Screen)
![Proration Screen](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/proration_page.png)
*(Caption: Figure 5.5 details the mid-cycle upgrade/downgrade confirmation window, presenting the calculated proration credits, new plan debits, and the net immediate charge amount.)*

#### Figure 5.6: Customer Invoices and Receipt PDF Download Tab
![Invoices History](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/invoice_history.png)
*(Caption: Figure 5.6 shows the customer billing ledger, listing invoices with status labels (Paid, Draft, Past Due) and action buttons to trigger PDF receipt downloads.)*

---

### 5.2.3 Admin Catalog Configuration Module
The Admin Catalog Module enables administrators to configure platform pricing, base plans, optional add-ons, metered components, region-based tax rates, and promotional coupons.

#### Figure 5.7: Product and Plan Creation Panel
![Plan Creator Form](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/plan_creation.png)
*(Caption: Figure 5.7 shows the admin configuration form used to define base plans, billing periods, setup fees, and tax behaviors.)*

#### Figure 5.8: Multi-Currency Price Book Mappings
![Price Book Matrix](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/price_book.png)
*(Caption: Figure 5.8 shows the multi-currency price book setup, allowing admins to establish specific product rates for USD, INR, and EUR with active date ranges.)*

#### Figure 5.9: Coupons and Promotion Setup Console
![Coupon Creator Console](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/coupon_creation.png)
*(Caption: Figure 5.9 displays the admin coupon configuration console used to set up percentage-off or fixed-amount discounts, max redemption limits, and validity dates.)*

#### Figure 5.10: Regional Tax Rates Configurator
![Tax Configurator Page](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/tax_rates.png)
*(Caption: Figure 5.10 highlights the region-specific tax configurations (e.g., GST or VAT) applied to invoice line items during billing runs.)*

---

### 5.2.4 Finance Manager Portal
The Finance Manager Portal enables financial auditing, revenue monitoring, and dispute resolution. It features real-time MRR, ARR, and Churn graphs, alongside invoice lists and payment collections trackers.

#### Figure 5.11: Finance Revenue Analytics Dashboard
![Finance Analytics Dashboard](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/finance_dashboard.png)
*(Caption: Figure 5.11 displays the Finance Dashboard, showing key performance metrics (MRR, ARR, ARPU, Churn) and monthly growth trend lines.)*

#### Figure 5.12: System-Wide Invoice Ledger & Aging Grid
![System Invoices Grid](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/invoices_aging.png)
*(Caption: Figure 5.12 shows the system-wide invoice ledger, allowing finance managers to filter invoices by aging group and payment status.)*

---

### 5.2.5 Support Agent Console
The Support Console helps support agents resolve customer account issues, including plan adjustments, pauses, resumes, and refunds.

#### Figure 5.13: Customer Account Timeline and Support Lookup
![Support Search Console](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/support_lookup.png)
*(Caption: Figure 5.13 shows the Support Agent Lookup portal with a timeline of subscription modifications, payment attempts, and communication records.)*

#### Figure 5.14: Support Refund and Credit Note Interface
![Refund Tool Panel](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/refund_portal.png)
*(Caption: Figure 5.14 display the refund processing window used to select specific invoice line items, calculate refund values, and issue credit notes.)*

---

## 5.3 Database and API Integration

### 5.3.1 Database Implementation
The database was implemented in MySQL using structured DDL scripts. Primary keys, foreign key constraints, indexes, and unique constraints (e.g., preventing duplicate subscription coupons) were established across all 25 tables to ensure referential integrity and query speed. The database was pre-populated with seed data—such as basic/premium plans, standard add-ons, regional taxes, and test customer accounts—to support testing and validation.

#### Figure 5.15: MySQL Relational Schema (ER Diagram)
![ERD Schema Diagram](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/database_erd.png)
*(Caption: Figure 5.15 shows the MySQL Entity Relationship Diagram (ERD) mapping the relational links between Users, Customers, Subscriptions, Invoices, Payments, and Audit Logs.)*

---

### 5.3.2 Spring Data JPA Integration
Database operations are managed via Spring Data JPA and Hibernate ORM. Entities are declared as Java classes using persistence annotations, and interfaces extending `JpaRepository` manage database queries. Repositories inject helper components to build complex queries, such as retrieving subscriptions due for renewal within the next 24 hours. The business service layer uses `@Transactional` annotations to enforce transaction boundaries, rolling back database operations if any step fails.

---

### 5.3.3 API Testing with Swagger
All API endpoints were verified using Swagger UI before integration with the React client. The Swashbuckle-equivalent OpenAPI configuration reads controller annotations and builds an interactive documentation panel, enabling sandbox testing of REST requests, request payload checks, and validation of returned HTTP status codes.

#### Figure 5.16: Swagger OpenAPI Endpoint Documentation
![Swagger Documentation Page](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/swagger_docs.png)
*(Caption: Figure 5.16 shows the Swagger UI dashboard mapping the REST endpoints grouped under Auth, Customer, Subscription, Invoice, and Support controllers.)*

#### Figure 5.17: Swagger POST Sandbox Testing
![Swagger Input Field Test](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/swagger_testing.png)
*(Caption: Figure 5.17 shows a sandbox test execution of the POST `/api/v1/billing/run` endpoint to verify automated billing engine responses.)*

---

## 5.4 Key Outcomes
The implementation of the Subscription Billing & Revenue Management System over the five-month internship period at Infosys resulted in the successful delivery of a fully functional recurring billing and financial management application. The project achieved several key outcomes aligned with the initial system specifications and business goals.

All nine core user stories defined in the product backlog were completely implemented across the functional modules, covering the requirements for all system actors. The frontend comprised over twenty React standalone components, providing a cohesive layout, real-time analytics graphs, checkout wizards, and role-based views. The backend exposed a comprehensive set of RESTful API endpoints organized across ten controller classes, all documented through Swagger UI. The database implementation comprised twenty-five interrelated MySQL tables with appropriate primary keys, foreign key constraints, column indices, and pre-populated seed data.

The automated testing suite included a comprehensive set of unit and integration tests implemented using JUnit 5 and Mockito, verifying service operations and controller handlers. The testing codebase successfully executed all 578 test cases, achieving a test coverage metric of over 80% and satisfying the quality gates configured on SonarQube, which graded the application with a Security rating of A, Reliability rating of A, and code duplication under 3%. The project demonstrated the practical application of modern software paradigms including Agile Scrum development, Git branching workflows, Spring Data JPA mappings, Spring Security session control, React component structures, and mock payment gateway transactions.

#### Figure 5.18: JUnit & Mockito Test Execution Results
![JUnit STS Test Runner](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/junit_coverage.png)
*(Caption: Figure 5.18 displays the Eclipse/STS JUnit test explorer showing successful execution of unit and integration tests with green indicators.)*

#### Figure 5.19: SonarQube Code Quality and Security Analysis
![SonarQube Gate Dashboard](file:///d:/Projects/Infosys%20Project/StreamFlixApp/screenshots/sonarqube_report.png)
*(Caption: Figure 5.19 displays the SonarQube dashboard showing compliance with quality gates, including Security rating A, Reliability A, and test coverage above 80%.)*

The project demonstrated the practical application of industry-standard technologies and methodologies, including Agile Scrum development, Git branching workflows, Spring Data JPA mappings, Spring Security session control, React SPA state architectures, and unit testing. The team environment provided experience in coordinating code contributions across multiple developers using a structured version control strategy.
