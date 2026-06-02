# CHAPTER 1: INTRODUCTION

## 1.1 Introduction to Work Done

The rapid transition of the global digital economy toward SaaS (Software as a Service), streaming platforms, and subscription-oriented business models has fundamentally altered how enterprises manage customer billing and revenue recognition. Traditional billing frameworks, which heavily rely on manual invoicing, ad-hoc payment collection, and separate spreadsheets for bookkeeping, are no longer viable for modern digital services. These legacy billing processes are highly inefficient, error-prone, struggle to compute complex proration during mid-cycle changes, and fail to handle payment collection failures gracefully. Consequently, modern software businesses require specialized, high-performance, and automated subscription billing and revenue management systems to guarantee transactional reliability, reduce customer churn, and provide accurate financial reporting.

The **Subscription Billing & Revenue Management System** was developed as a comprehensive full-stack web application during the internship at Infosys Limited. The platform is designed to automate the complete recurring billing lifecycle of customers. It supports plan creation, billing cycle automation, proration calculations for upgrades and downgrades, invoice generation, discount coupons, tax calculation (GST/VAT), payment retry strategies (dunning), and real-time revenue analytics dashboards. The system provides secure portals for four primary actors: Customers, Admins, Finance Managers, and Support Agents, ensuring a streamlined and role-based workflow across the entire organization.

The internship at Infosys was structured to combine academic foundation with practical enterprise application engineering. The initial phase of the internship focused on core programming, system design, and database fundamentals. Intensive training was conducted on core Java, object-oriented programming (OOP) principles, relational database management systems (DBMS), SQL querying, and relational database schema design. This phase was critical for building analytical thinking and problem-solving skills, establishing a strong understanding of backend transaction management and data integrity, which served as the foundation for building a robust billing system.

The second phase of the internship transitioned into specialized stream training under the Java Full-Stack Developer Track. During this phase, practical expertise was acquired in React, TypeScript, Spring Boot, Spring Data JPA, Hibernate, Spring Security (Session-based Authentication, DaoAuthenticationProvider, UserDetailsService, and BCryptPasswordEncoder configurations), MySQL, and Maven. In the final phase, an Agile Scrum development approach was followed to design, build, and deploy the Subscription Billing & Revenue Management System. This final phase simulated a professional development environment involving collaborative sprints, daily stand-up meetings, feature backlog refinement, REST API design, frontend-backend integration, unit testing, and Git-based version control workflows.

The system incorporates several core functional components, starting with a Plan Catalog containing products, plans, add-ons, and metered units, and extending to an automated Billing Engine that uses Spring's scheduling capabilities to run renewal runs. The system dynamically computes taxes and promotional discount coupons, issues invoices and credit notes, and processes sandbox charges using a mock payment gateway adapter. In the event of a failed transaction, a robust dunning scheduler automatically enqueues the subscription for retries at scheduled intervals before taking final lifecycle actions (e.g., cancellation or holding).

To verify the integration and stability of the system, modern testing frameworks and documentation tools were integrated. Backend APIs were thoroughly validated using JUnit and Mockito to meet a strict code coverage target of over 80%. Extensive API testing was conducted using Swagger/OpenAPI documentation and Postman. On the frontend, components were structured using modern React hooks, TypeScript interfaces, and dynamic state management to render user-specific consoles. Version control was managed systematically using Git, and software quality was monitored through SonarQube quality gates to ensure code reliability, security, and maintainability.

---

### Figure 1.1: Internship Training and Project Development Workflow
```
[Foundation Training] ──> [Java Stream Training] ──> [Agile Project Development] 
 (Java, DBMS, SQL)       (React, Spring Boot, JPA)    (Sprints, API, Dunning Engine)
```

### Figure 1.2: High-Level Architecture of Subscription Billing System
```
┌────────────────────────────────────────────────────────────────────────┐
│                          React Frontend Client                         │
│   (Customer Portal, Admin Console, Finance Dashboard, Support Panel)   │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                    HTTP Requests  │  Set-Cookie: JSESSIONID (Session ID)
               with Credentials    │  Cookie: JSESSIONID
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                 Spring Boot Backend (REST API Server)                  │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                         Security Filter                        │   │
│   │   (Spring Security, CorsFilter, DaoAuthenticationProvider)     │   │
│   └──────────────────────────────┬─────────────────────────────────┘   │
│                                  │ Session Validated                   │
│                                  ▼                                     │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                         Controller Layer                       │   │
│   │  (Customer, Admin, Finance, Support, Auth REST Controllers)    │   │
│   └──────────────────────────────┬─────────────────────────────────┘   │
│                                  │ Service calls                       │
│                                  ▼                                     │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                          Service Layer                         │   │
│   │  (BillingEngine, SubscriptionFlow, DunningRetry, Analytics)    │   │
│   └──────────────────────────────┬─────────────────────────────────┘   │
│                                  │ Repository DB actions               │
│                                  ▼                                     │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                        Data Access Layer                       │   │
│   │              (Spring Data JPA & Hibernate ORM)                 │   │
│   └────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                      SQL Queries  │  Data Records
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             MySQL Database                             │
│       (Customer, Subscriptions, Invoices, Payments, Users, Logs)      │
└────────────────────────────────────────────────────────────────────────┘
```

#### Detailed Explanation of Figure 1.2:
Figure 1.2 represents the three-tier system architecture and session authentication flow of the Subscription Billing System:
* **Presentation Tier (React Frontend)**: A single-page application (SPA) built with React and TypeScript. It triggers HTTP requests containing user inputs and credentials. Because session credentials must be transmitted securely across origins, CORS is configured to explicit allow credentials (`setAllowCredentials(true)`) targeting client origins (`localhost:3000`).
* **Application Tier (Spring Boot Backend)**:
  * **Security Filter**: Incoming requests hit the Spring Security chain. Anonymous login requests `/api/customer/login` are bypassed to authenticate credentials using `DaoAuthenticationProvider`, verify passwords with `BCryptPasswordEncoder`, and match emails via `UserDetailsService`.
  * **Session Management**: Upon successful authentication, a servlet container session is generated, returning a standard `JSESSIONID` cookie to the client browser in the `Set-Cookie` header. Subsequent requests transmit the `JSESSIONID` cookie to validate security context.
  * **Controllers and Services**: Authenticated requests are routed to specific REST controllers (e.g., `CustomerSubscriptionController`, `FinanceController`), executing core services (such as proration calculation or daily renewal engines).
* **Database Tier (MySQL)**: Spring Data JPA maps entities to MySQL database tables, managing transaction boundaries, indexing, and raw billing ledger storage.

### Table 1.1: Technologies and Learning Areas During Internship
| Phase | Technologies / Concepts | Learning Outcome |
| :--- | :--- | :--- |
| **Foundation Training** | Java, DBMS, SQL, Object-Oriented Principles | Strengthened fundamental programming logic and database transaction management concepts. |
| **Java Full-Stack Training** | React, TypeScript, Spring Boot, JPA, Spring Security | Gained expertise in building secure RESTful microservices and reactive frontend applications. |
| **Database Development** | MySQL, Indexing, Schema Normalization | Designed relational schemas for handling transactional billing tables (Invoices, Subscriptions). |
| **DevOps & Collaboration** | Maven, Git, Agile Scrum, SonarQube | Mastered collaborative coding practices, version control, automated unit testing, and quality gates. |
| **Project Implementation** | Automated Billing, Dunning, Proration Engine | Designed and developed a transactionally safe subscription management application. |

---

## 1.2 Project Statement / Objectives of the Project

Modern SaaS and streaming enterprises involve multiple actors who interact with the subscription lifecycle, including customers subscribing to plans, admins managing catalog pricing, finance managers evaluating cash flows, and support agents handling refunds. Traditional billing frameworks often rely on manual invoices, batch calculations, and isolated payment gateways, which regularly lead to delayed renewals, incorrect tax calculations, and a high volume of voluntary and involuntary churn. Handling mid-cycle subscription changes (such as upgrades and downgrades) requires complicated manual proration calculations, which are prone to mathematical errors and operational delays.

The **Subscription Billing & Revenue Management System** is developed as a centralized web application to automate and streamline the lifecycle of recurring subscriptions. The system integrates catalog setup, billing schedules, payment gateway requests, tax/coupon evaluation, failed payment dunning, and support refunds into a single unified platform. By organizing access through role-based consoles, the application ensures that each stakeholder gets optimized access to the system while guaranteeing that billing data is synchronized and secure.

The project is built on a modern software architecture, leveraging **React and TypeScript** on the frontend and **Spring Boot, Spring Data JPA, and MySQL** on the backend. It uses Scheduled Cron Jobs to run daily renewal runs and automate invoicing. A sandbox payment service mimics real payment collection to test retry rules and dunning notifications.

**The major objectives of the project are as follows:**
* To develop a secure, centralized digital platform for managing subscription billing and revenue recognition.
* To automate recurring billing cycle runs and invoice generation using cron-based scheduling.
* To implement secure, role-based authentication and authorization using Spring Security sessions.
* To support flexible plan configurations, including base plans, setup fees, trial periods, and add-ons.
* To automate complex proration mathematics for mid-cycle subscription upgrades and downgrades.
* To implement an automated dunning system with configurable retry schedules for failed payments.
* To generate real-time financial revenue analytics, tracking metrics such as MRR, ARR, ARPU, and Churn rates.
* To support tax calculations (GST/VAT) and promotional coupon code validation.
* To follow Agile software development practices, using Git for version control and JUnit/Mockito for quality assurance.


---

## 1.3 Motivation

The Subscription Billing & Revenue Management System was motivated by the massive industry shift toward recurring revenue models and the clear limitations of legacy billing platforms. Standard ERPs and traditional relational accounting software are poorly equipped to handle the continuous state transitions of a subscription (e.g., active, past due, canceled, trialing). Manual renewal tracking is highly inefficient, leading to missed billing cycles, delayed revenue collection, and customer frustration.

Furthermore, payment failures are a major source of customer churn for subscription services. When a card transaction fails, canceling the account immediately causes "involuntary churn," whereas doing nothing leads to "revenue leakage." This created a strong motivation to build a rule-based dunning engine that schedules smart payment retries and sends reminders automatically, allowing recovery of failed transactions without human intervention.

Another key motivation was the technical challenge of designing and implementing proration algorithms. When a customer changes plans mid-cycle, calculating the exact credit for unused days on the old plan and the charge for remaining days on the new plan requires precise date arithmetic and transactional security. Resolving this challenge in a clean, scalable manner provided a great learning opportunity.

The project was also motivated by the desire to implement the full-stack development skills learned during the Infosys training. Working on the backend in Spring Boot (leveraging repositories, transactional services, custom exception handlers, and security filters) and on the frontend in React (designing responsive pages and managing context state) provided hands-on experience in building enterprise-grade software.

Finally, the inclusion of a comprehensive analytics engine was motivated by the importance of financial metric transparency. Providing finance managers with real-time computations of MRR (Monthly Recurring Revenue), ARR (Annual Recurring Revenue), and Churn rates via database-level aggregations demonstrated how software engineering directly drives corporate decision-making.

---

## 1.4 Scope of Work

The scope of the Subscription Billing & Revenue Management System involves the end-to-end design, development, and testing of a web-based recurring billing and invoicing platform. The project focuses on replacing manual bookkeeping with an automated engine that handles the subscription lifecycle and coordinates financial transactions.

The system delivers distinct, role-based dashboards for Customers, Admins, Finance Managers, and Support Agents. Frontend development was completed using React, TypeScript, HTML5, and Bootstrap CSS, while backend REST endpoints and scheduled cron jobs were built on Spring Boot. Data storage, indexing, and relational logic were implemented in a MySQL database using Spring Data JPA.

**The functional scope of the project includes:**
* **Customer Portal**: Sign up, view plan catalog, purchase subscription with mock payment methods, view active subscription status, upgrade/downgrade plans mid-cycle, download invoice PDFs, and manage notifications.
* **Admin Console**: CRUD operations on the product catalog, base plans, optional add-ons, metered components, region-based tax rates, and promotional coupons.
* **Finance Manager Console**: View system-wide invoices, track collections, issue credit notes, process refunds, and view live graphs of MRR, ARR, and Churn rates.
* **Support Console**: Look up customers by email or ID, view subscription history, add support logs, and trigger manual adjustments or refunds on behalf of the customer.
* **Billing Engine (Scheduler)**: Automated daily cron jobs to detect upcoming renewals, calculate charges, generate invoices, attempt charges, and finalize transaction statuses.
* **Dunning & Retry Scheduler**: Automated retry workflows for failed payments (scheduling retries at T+1d, T+3d, and T+7d) and updating subscription states accordingly.
* **Notification Engine**: Triggering transactional emails/logs for upcoming renewals, receipt confirmations, and payment failures.
* **API Documentation & Testing**: Integrating Swagger UI for interactive API documentation, implementing custom validation exception handlers, and running automated tests using JUnit and Mockito.

The current implementation focuses on establishing a highly maintainable, secure monolithic application with decoupled services, preparing the codebase for future microservice extraction and real third-party payment gateway integration.
