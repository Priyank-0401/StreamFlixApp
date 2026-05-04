# Customer Signup and Onboarding Flow

## Overview
This document describes the complete flow of how a user signs up for StreamFlix and becomes a customer with an active subscription.

## User Registration Flow

### Step 1: User Account Creation
- **Endpoint**: `/register` (CustomerAuthPage)
- **Component**: `CustomerSignupForm`
- **Process**:
  1. User fills out registration form with:
     - Full Name
     - Email Address
     - Password
     - Confirm Password
  2. Form validation ensures passwords match
  3. Call to `authService.customerRegister()` creates user account
  4. User is assigned `CUSTOMER` role in the system
  5. User is redirected to `/login` page after successful registration

### Step 2: User Login
- **Endpoint**: `/login` (CustomerAuthPage)
- **Component**: `CustomerLoginForm`
- **Process**:
  1. User enters email and password
  2. Call to `authService.customerLogin()` authenticates user
  3. Upon successful login, user receives authentication token
  4. User is now authenticated with `CUSTOMER` role
  5. User can access protected routes like `/plans`

### Step 3: View Available Plans
- **Endpoint**: `/plans` (PlansPage)
- **Access**: Requires `CUSTOMER` role (protected by RoleGuard)
- **Process**:
  1. User views available subscription plans (Basic, Premium)
  2. Plans display pricing based on selected region (IN, US, GB)
  3. User can toggle between monthly and yearly billing
  4. Each plan shows features, trial period, and pricing
  5. User clicks "Subscribe Now" on a plan to begin subscription flow

### Step 4: Subscription Flow (3-Step Process)
- **Endpoint**: `/subscribe?planId={id}&step={1-3}` (SubscriptionFlow)
- **Access**: Requires `CUSTOMER` role (protected by RoleGuard)

#### Step 1: Customer Details
- **Component**: `CustomerDetailsStep`
- **Process**:
  1. User enters customer information:
     - Country
     - Currency
     - Phone number (optional)
  2. Form validation ensures required fields are filled
  3. User proceeds to payment method step

#### Step 2: Payment Method
- **Component**: `PaymentMethodStep`
- **Process**:
  1. User adds payment method:
     - Card number
     - Expiry date
     - CVV
     - Cardholder name
  2. Payment method is validated and tokenized
  3. User proceeds to payment step

#### Step 3: Mock Payment
- **Component**: `MockPaymentStep`
- **Process**:
  1. User reviews subscription details:
     - Selected plan
     - Billing period
     - Trial period (if applicable)
     - Total amount
  2. User confirms payment
  3. Mock payment processing simulates payment gateway
  4. On successful payment, backend creates:
     - Customer record in `customer` table
     - Subscription record in `subscription` table
     - Invoice record in `invoice` table

### Step 5: Post-Subscription
- **Endpoint**: `/dashboard` (CustomerLayout)
- **Access**: Requires `CUSTOMER` role AND customer record in database
- **Process**:
  1. `CustomerContext` checks for customer profile on mount
  2. If customer record exists, user can access dashboard
  3. User is shown:
     - Overview page with subscription details
     - Billing information
     - Payment methods
     - Profile settings
     - Support options

## Customer Status Check

### CustomerContext
The `CustomerContext` (`context/CustomerContext.tsx`) manages customer status:

```typescript
interface CustomerContextType {
  isCustomer: boolean | null;  // null = loading, true = customer exists, false = not a customer
  loading: boolean;
  refreshCustomerStatus: () => Promise<void>;
}
```

### How It Works
1. On mount, `CustomerContext` calls `getCustomerProfile()` API
2. If successful (200 OK): `isCustomer` is set to `true`
3. If failed (400/404/Customer not found): `isCustomer` is set to `false`
4. Components can check `isCustomer` to conditionally render content

### Non-Customer Behavior
- Users without customer record are redirected to `/plans` when trying to access `/dashboard`
- Landing page navbar shows "Explore Plans" instead of "Dashboard" for non-customers
- Customer sidebar navigation hides customer-specific pages (Billing, Payment Methods, Profile)

## Key API Endpoints

### Authentication
- `POST /api/auth/customer/register` - Create user account
- `POST /api/auth/customer/login` - Authenticate user

### Customer
- `GET /api/customer/profile` - Get customer profile (checks if customer exists)
- `POST /api/customer/create` - Create customer record (called during subscription)

### Subscription
- `GET /api/customer/subscription/current` - Get current subscription
- `POST /api/subscription/create` - Create new subscription
- `GET /api/plans` - Get available plans

## Database Schema Impact

### User Table
- Stores user account information
- Contains `role` field (CUSTOMER, ADMIN, etc.)
- Created during registration

### Customer Table
- Stores customer-specific information
- Linked to User table via user_id
- Created only after successful subscription payment
- Contains: country, currency, phone, etc.

### Subscription Table
- Stores subscription details
- Linked to Customer table via customer_id
- Created only after successful subscription payment
- Contains: plan_id, status, billing period, etc.

## Important Notes

1. **User vs Customer**: A "user" is someone who has registered an account. A "customer" is a user who has completed subscription and has a customer record in the database.

2. **Access Control**: 
   - `/plans` requires CUSTOMER role
   - `/dashboard` requires CUSTOMER role AND customer record
   - Non-customers are automatically redirected to `/plans`

3. **Trial Period**: Plans include trial periods (7 days for Basic, 14 days for Premium) that start after subscription creation.

4. **Payment**: Current implementation uses mock payment. In production, this would integrate with a real payment gateway (Stripe, Razorpay, etc.).

5. **Customer Status Refresh**: `CustomerContext.refreshCustomerStatus()` can be called to re-check customer status after subscription completion.
