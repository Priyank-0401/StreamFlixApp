# CHAPTER 3: BACKGROUND MATERIAL

## 3.1 Conceptual Overview

### 3.1.1 Subscription Billing Systems
Subscription billing and revenue management systems are enterprise software platforms designed to automate, track, and manage recurring payments, user subscriptions, and invoicing lifecycles. Traditionally, business invoicing was transaction-oriented, where one-off payments were processed upon delivery of goods or services. However, with the massive industry transition to software-as-a-service (SaaS) and streaming models (like Netflix, Spotify, or cloud applications), enterprises must now manage continuous customer relationships. This requires specialized billing engines capable of tracking subscription transitions over time, processing recurring charges on set intervals, and handling transactional updates dynamically.

A robust subscription billing engine must automate billing cycles, apply regional taxes and promotional coupons, and manage lifecycle events like trials, cancellations, and payment failures. In contrast to manual entry, modern platforms automate invoicing, reducing administrative overhead and preventing billing discrepancies. The **Subscription Billing & Revenue Management System** was developed during the internship at Infosys to address these needs, offering a centralized platform that supports plan catalogs, billing schedules, mock payment gateway processing, and financial accounting tools.

### 3.1.2 Multi-Role Enterprise Architecture
Enterprise subscription applications require a multi-role architecture to guarantee data privacy, separation of duties, and operational workflow security. The Subscription Billing System defines four operational roles (Customer, Admin, Finance Manager, and Support Agent) along with the automated System Billing Engine. Each role is granted distinct access to the system, aligning with the security principle of least privilege, which restricts users to only the operations necessary for their daily work.

The role permissions are managed by Spring Security using role-based access control (RBAC). The Customer is restricted to self-service activities, such as viewing catalogs, upgrading/downgrading plans, managing payment methods, and reviewing invoice receipts. Platform administrators (Admins) use the Admin Console to configure plan pricing, add-ons, coupons, and tax rates. Finance Managers review collections, process refunds, and track financial reports. Support Agents assist customers by looking up accounts and modifying subscription statuses, while the automated System Engine manages daily renewals in the background.

### 3.1.3 Three-Tier Architecture
The Subscription Billing System is structured according to a classic three-tier architecture separating the presentation tier, logic tier, and data tier. This architectural pattern promotes separation of concerns, simplifies maintenance, and enables the independent scaling of each layer as traffic increases.

The presentation tier is implemented as a single-page application (SPA) using React and TypeScript, communicating with the logic tier through HTTP REST API requests. The logic tier consists of a Spring Boot REST API server that contains the business logic, proration math, security constraints, and dunning schedulers. The data tier is a MySQL relational database managed through Spring Data JPA and Hibernate ORM. This separation ensures that the frontend UI can be updated independently of backend APIs, and database tables can evolve through versioned migration schemas.

### 3.1.4 Agile Development Methodology
The project was designed, developed, and verified using the Agile Scrum framework during the internship at Infosys. Development activities were organized around user stories managed in a product backlog. Each user story was written to focus on user value, utilizing the standard template: "As a [role], I want to [action] so that [benefit]."

The backlog contained user stories covering security settings, plan setups, proration logic, invoice creation, and dunning retries. The development cycle was divided into two-week sprint intervals, with sprint planning, daily stand-up status checks, and retrospective reviews. Version control was managed systematically using Git, utilizing a feature-branching workflow where developers built individual features on standalone branches (e.g., `feature/billing-cron`) before merging them into the integration branch through reviewed pull requests.

### 3.1.5 Object-Relational Mapping (ORM) and JPA
To connect the application logic with the data layer, the system utilizes Spring Data JPA with Hibernate as the underlying Object-Relational Mapping (ORM) provider. Under this repository-pattern model, Java entities (such as `Subscription`, `Invoice`, and `Customer`) are mapped directly to MySQL database tables using JPA annotations (`@Entity`, `@Table`, `@OneToMany`, `@ManyToOne`). This abstracts raw SQL queries, enabling developers to write database transactions using object-oriented Java code.

Spring Data JPA repositories are defined as interfaces extending `JpaRepository`, which automatically provides CRUD capabilities and query derivation features. Custom query methods (e.g., locating all subscriptions expiring in the next 24 hours) are defined using JPQL or SQL annotations directly on repository methods, keeping database logic organized and separate from core service components.

---

### Figure 3.1: Agile Software Development Workflow
```
[ Product Backlog ] ➔ [ Sprint Planning ] ➔ [ 2-Week Sprint ] ➔ [ Code Review & PR ] ➔ [ Integration & Test ]
```

---

## 3.2 Technologies Used

### 3.2.1 React & TypeScript
React is a component-based frontend JavaScript library used for building interactive user interfaces, while TypeScript adds static typing to prevent runtime compilation errors. The system's frontend is developed as a React single-page application (SPA) with TypeScript to manage the dashboard interfaces for Customers, Admins, Finance Managers, and Support Agents. React's state management (using Context API and React Hooks) handles user session state, dynamically rendering pages and menus based on authenticated user roles.

### 3.2.2 Spring Boot
Spring Boot is a Java framework designed to simplify the bootstrap and development of standalone, production-ready RESTful web services. The system's backend is built on Spring Boot, leveraging its dependency injection container, component scanning, auto-configuration systems, and built-in tomcat servlet container. Spring Boot controllers expose JSON endpoints for client interactions, service classes execute proration and dunning logic, and configuration beans manage environment variables.

### 3.2.3 MySQL Database
MySQL is an open-source relational database management system (RDBMS) used to store the platform's transactional billing data. The MySQL database schema is structured to maintain relational integrity across entities like users, products, plans, subscriptions, invoices, payments, and audit logs. Database migrations and schema initializations are executed using DDL SQL scripts, and queries are optimized through indexing on foreign keys and transaction columns to handle growing data volumes.

### 3.2.4 Spring Data JPA & Hibernate
Spring Data JPA is a framework that reduces repository-layer boilerplates by providing standardized data access interfaces, while Hibernate serves as the ORM engine mapping Java classes to database tables. In this system, JPA handles data persistence, transaction execution, and relational mapping (such as linking one customer to many subscriptions). The repository-pattern abstraction decouples database interactions from business logic, simplifying service testing and database migrations.

### 3.2.5 Git and GitHub
Git is a distributed version control system used to track changes in source code files, and GitHub is a cloud hosting service for repository management. Throughout the internship, Git was used to maintain the codebase, enforce branching strategies, and log development history. Code reviews were conducted via GitHub Pull Requests, ensuring all changes met style guidelines and passed automated tests before being merged.

### 3.2.6 Spring Tool Suite (STS) & VS Code
Spring Tool Suite (STS) is an Eclipse-based integrated development environment (IDE) customized for building Spring Boot applications, used for all backend development. STS provides built-in tools for Maven build management, Java compilation, Boot dashboard monitoring, and JUnit testing. VS Code was used as the primary frontend development environment, utilizing its built-in TypeScript compiler, ESLint integrations, and Vite development server tools to write and test the React frontend.

### 3.2.7 JUnit & Mockito
JUnit is the standard testing framework for Java, and Mockito is a mocking framework used to isolate classes under test. The system's backend unit and integration tests are written in JUnit 5. Mockito is used to mock database repository layers, enabling testing of core business calculations (such as proration math or dunning retry schedules) in isolation without making real database connections.

### 3.2.8 BCrypt (Spring Security Cryptography)
BCrypt is a secure password-hashing algorithm designed to resist brute-force attacks by using an adjustable work factor. The application utilizes Spring Security's `BCryptPasswordEncoder` bean to secure password data. User passwords entered during registration are salted and hashed before persistence, and subsequent login attempts are validated by comparing the incoming password against the stored hash, protecting credential storage from database compromises.

### 3.2.9 Spring Scheduler (`@Scheduled`)
Spring's scheduling module enables the execution of background tasks at set times or intervals. The system's billing engine and dunning workflows use Spring's `@Scheduled` cron triggers. The daily renewal scheduler is configured to run automatically at midnight UTC, identifying expiring subscriptions, generating invoices, and submitting payment processing actions to the gateway without requiring manual human triggers.
