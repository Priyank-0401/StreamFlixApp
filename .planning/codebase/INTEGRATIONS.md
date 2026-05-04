# External Integrations

## Overview
StreamFlixApp is a relatively self-contained application with minimal external service dependencies. The primary integration is between the React frontend and the Spring Boot backend via REST APIs. There are no third-party payment gateways, OAuth providers, or cloud services integrated yet.

---

## Backend REST API

### Base URL
- Backend: `http://localhost:8765`
- Frontend proxy: `/api` → `http://localhost:8765/api`

### API Endpoints by Domain

#### Authentication (`/api`)
| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/api/customer/login` | Public | Customer login |
| POST | `/api/customer/register` | Public | Customer registration |
| POST | `/api/manager/login` | Public | Staff/Admin login |
| GET | `/api/auth/me` | Public | Session verification |
| POST | `/logout` | Public | Session logout |

#### Customer APIs (`/api/customer` → `/customer`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/customer/me` | Get customer profile |
| PUT | `/customer/me` | Update customer profile |
| GET | `/customer/status` | Check customer/subscription status |
| GET | `/customer/plans` | Get available plans |
| GET | `/customer/plans/featured` | Get featured plans (public) |
| GET | `/customer/plans/all` | Get all active plans (public) |
| GET | `/customer/addons` | Get available add-ons |
| GET | `/customer/subscription` | Get current subscription |
| POST | `/customer/subscription` | Create subscription |
| PUT | `/customer/subscription/upgrade` | Upgrade plan |
| DELETE | `/customer/subscription` | Cancel subscription |
| PUT | `/customer/subscription/pause` | Pause subscription |
| PUT | `/customer/subscription/resume` | Resume subscription |
| POST | `/customer/addons/:id` | Add add-on |
| DELETE | `/customer/addons/:id` | Remove add-on |
| GET | `/customer/usage` | Get metered usage |
| GET | `/customer/invoices` | Get invoices |
| GET | `/customer/invoices/:id` | Get invoice detail |
| GET | `/customer/invoices/:id/download` | Download invoice PDF |
| GET | `/customer/payments` | Get payments |
| GET | `/customer/credit-notes` | Get credit notes |
| POST | `/customer/coupons/apply` | Apply coupon |
| GET | `/customer/payment-methods` | List payment methods |
| POST | `/customer/payment-methods` | Add payment method |
| PUT | `/customer/payment-methods/:id/default` | Set default |
| DELETE | `/customer/payment-methods/:id` | Delete payment method |
| GET | `/customer/notifications` | Get notifications |
| PUT | `/customer/notifications/:id/read` | Mark as read |
| POST | `/customer/support/contact` | Contact support |
| GET | `/customer/support/faqs` | Get FAQs |
| POST | `/customer/register-details` | Register customer details |
| POST | `/customer/payment-method` | Create payment method (flow) |
| POST | `/customer/subscription/complete` | Complete subscription (flow) |

#### Admin APIs (`/api/admin`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/admin/dashboard/stats` | Dashboard statistics |
| GET | `/admin/products` | List products |
| PUT | `/admin/products/:id` | Update product |
| PATCH | `/admin/products/:id/toggle-status` | Toggle product status |
| GET/POST | `/admin/plans` | List/Create plans |
| PUT | `/admin/plans/:id` | Update plan |
| PATCH | `/admin/plans/:id/toggle-status` | Toggle plan status |
| GET/POST | `/admin/pricebooks` | List/Create price books |
| PUT/DELETE | `/admin/pricebooks/:id` | Update/Delete price book |
| GET/POST | `/admin/addons` | List/Create add-ons |
| PUT | `/admin/addons/:id` | Update add-on |
| PATCH | `/admin/addons/:id/toggle-status` | Toggle add-on status |
| GET/POST | `/admin/metered` | List/Create metered components |
| PUT | `/admin/metered/:id` | Update metered component |
| PATCH | `/admin/metered/:id/toggle-status` | Toggle metered status |
| GET/POST | `/admin/taxrates` | List/Create tax rates |
| PUT/DELETE | `/admin/taxrates/:id` | Update/Delete tax rate |
| GET/POST | `/admin/coupons` | List/Create coupons |
| PUT | `/admin/coupons/:id` | Update coupon |
| PATCH | `/admin/coupons/:id/toggle-status` | Toggle coupon status |
| GET | `/admin/customers` | List customers |
| PATCH | `/admin/customers/:id/toggle-status` | Toggle customer status |
| GET/POST | `/admin/staff` | List/Create staff |
| DELETE | `/admin/staff/:id` | Delete staff |
| GET | `/admin/subscriptions` | List subscriptions |

#### Public APIs
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/public/plans` | Public plan listing (from `publicService.ts`) |

---

## Database

### MySQL Connection
- **JDBC URL**: `jdbc:mysql://localhost:3306/subscription_billing`
- **Username**: `root`
- **Password**: `root`
- **Driver**: MySQL JDBC (via Spring Data JPA)
- **ORM**: Hibernate (JPA)

### Session Configuration
- **Session store**: Server-side (Spring default — in-memory)
- **Cookie name**: `JSESSIONID`
- **SameSite**: Lax
- **HttpOnly**: true
- **Secure**: false (development)
- **Timeout**: 30 minutes

---

## External Services (Not Yet Integrated)

The codebase has **mock/simulated** implementations for several features that would typically need external services:

### Payment Gateway
- Payment processing is simulated — `payment_method` table stores `gateway_token` but no actual gateway integration exists
- Card data (last4, brand, expiry) is stored directly
- Supports CARD and UPI payment types
- **No Stripe/Razorpay/PayPal integration**

### Email/SMS Notifications
- `notification` table tracks EMAIL/SMS notifications with status (PENDING/SENT/FAILED/SKIPPED)
- **No actual email/SMS provider integrated** (e.g., SendGrid, Twilio)

### Invoice PDF Generation
- Frontend has `downloadInvoice()` endpoint calling `/invoices/:id/download`
- **Actual PDF generation status is unclear** — endpoint exists but may return mock data

### OAuth2/SSO
- Schema supports OAuth2 users (password_hash = NULL)
- **No OAuth2 provider configured** (Google, GitHub, etc.)

---

## CORS Configuration
- **Allowed Origins**: `http://localhost:3000`, `http://127.0.0.1:3000`
- **Allowed Methods**: GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD
- **Credentials**: Allowed (required for session cookies)
- **Exposed Headers**: Access-Control-Allow-Origin, Access-Control-Allow-Credentials, Set-Cookie

---

## Google Fonts CDN
- Loaded in `frontend/src/index.css` via `@import url('https://fonts.googleapis.com/css2?...')`
- Fonts: Outfit, Inter, JetBrains Mono
