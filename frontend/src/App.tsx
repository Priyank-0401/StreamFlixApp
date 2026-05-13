import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CustomerAuthPage } from './pages/auth/CustomerAuthPage';
import { ManagementAuthPage } from './pages/auth/ManagementAuthPage';
import { LandingPage } from './pages/public/LandingPage';
import { PlansPage } from './pages/public/PlansPage';
import { RoleGuard } from './routes/RoleGuard';
import { ROLES } from './constants/roles';

// Admin Layout & Pages
import { AdminLayout } from './components/admin/layout/AdminLayout';
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { ProductPage } from './pages/admin/catalog/ProductPage';
import { PlansPage as AdminPlansPage } from './pages/admin/catalog/PlansPage';
import { AddOnsPage } from './pages/admin/catalog/AddOnsPage';
import { MeteredComponentsPage } from './pages/admin/catalog/MeteredComponentsPage';
import { PriceBooksPage } from './pages/admin/pricing/PriceBooksPage';
import { TaxRatesPage } from './pages/admin/pricing/TaxRatesPage';
import { CouponsPage } from './pages/admin/pricing/CouponsPage';
import { CustomersPage } from './pages/admin/users/CustomersPage';
import { StaffAccountsPage } from './pages/admin/users/StaffAccountsPage';
import { SubscriptionsPage } from './pages/admin/subscriptions/SubscriptionsPage';

// Customer Dashboard Layout & Pages
import { CustomerLayout } from './components/customer/layout/CustomerLayout';
import { OverviewPage } from './pages/customer/OverviewPage';
import { SubscriptionPage } from './pages/customer/SubscriptionPage';
import { BillingPage } from './pages/customer/BillingPage';
import { PaymentMethodsPage } from './pages/customer/PaymentMethodsPage';
import { SupportPage } from './pages/customer/SupportPage';
import { ProfilePage } from './pages/customer/ProfilePage';
import { SubscriptionFlow } from './pages/customer/SubscriptionFlow';
import { SubscriptionCheckoutPage } from './pages/customer/SubscriptionCheckoutPage';
import { CustomerProvider } from './context/CustomerContext';

// Finance Layout & Pages
import { FinanceLayout } from './components/finance/layout/FinanceLayout';
import { FinanceDashboardPage } from './pages/finance/FinanceDashboardPage';
import { FinanceSnapshotsPage } from './pages/finance/FinanceSnapshotsPage';
import { FinanceMrrPage } from './pages/finance/FinanceMrrPage';
import { FinanceArrPage } from './pages/finance/FinanceArrPage';
import { FinanceArpuPage } from './pages/finance/FinanceArpuPage';
import { FinanceChurnPage } from './pages/finance/FinanceChurnPage';
import { FinanceInvoicesPage } from './pages/finance/FinanceInvoicesPage';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Landing Page - Now inside AuthProvider */}
          <Route path="/" element={<LandingPage />} />
          
          {/* Public Auth Routes */}
          <Route path="/login" element={<CustomerAuthPage />} />
          <Route path="/register" element={<CustomerAuthPage />} />
          <Route path="/management/login" element={<ManagementAuthPage />} />

          {/* Protected Routes - Subscription Flow */}
          <Route
            path="/plans"
            element={
              <RoleGuard allowedRoles={[ROLES.CUSTOMER]} redirectTo="/register">
                <PlansPage />
              </RoleGuard>
            }
          />
          <Route
            path="/subscribe"
            element={
              <RoleGuard allowedRoles={[ROLES.CUSTOMER]} redirectTo="/register">
                <SubscriptionFlow />
              </RoleGuard>
            }
          />
          <Route
            path="/checkout"
            element={
              <RoleGuard allowedRoles={[ROLES.CUSTOMER]} redirectTo="/register">
                <SubscriptionCheckoutPage />
              </RoleGuard>
            }
          />

          {/* Customer Protected Routes — Nested under CustomerLayout */}
          <Route
            path="/dashboard"
            element={
              <RoleGuard allowedRoles={[ROLES.CUSTOMER]} redirectTo="/login">
                <CustomerProvider>
                  <CustomerLayout />
                </CustomerProvider>
              </RoleGuard>
            }
          >
            <Route index element={<OverviewPage />} />
            <Route path="subscription" element={<SubscriptionPage />} />
            <Route path="billing" element={<BillingPage />} />
            <Route path="payment-methods" element={<PaymentMethodsPage />} />
            <Route path="support" element={<SupportPage />} />
            <Route path="profile" element={<ProfilePage />} />
          </Route>

          {/* Admin Protected Routes — Nested under AdminLayout */}
          <Route
            path="/admin"
            element={
              <RoleGuard allowedRoles={[ROLES.ADMIN]} redirectTo="/management/login">
                <AdminLayout />
              </RoleGuard>
            }
          >
            <Route index element={<AdminDashboardPage />} />
            <Route path="product" element={<ProductPage />} />
            <Route path="plans" element={<AdminPlansPage />} />
            <Route path="addons" element={<AddOnsPage />} />
            <Route path="metered" element={<MeteredComponentsPage />} />
            <Route path="pricebooks" element={<PriceBooksPage />} />
            <Route path="taxrates" element={<TaxRatesPage />} />
            <Route path="coupons" element={<CouponsPage />} />
            <Route path="customers" element={<CustomersPage />} />
            <Route path="staff" element={<StaffAccountsPage />} />
            <Route path="subscriptions" element={<SubscriptionsPage />} />
          </Route>

          {/* Finance Protected Routes — Nested under FinanceLayout */}
          <Route
            path="/finance"
            element={
              <RoleGuard allowedRoles={[ROLES.FINANCE]} redirectTo="/management/login">
                <FinanceLayout />
              </RoleGuard>
            }
          >
            <Route index element={<FinanceDashboardPage />} />
            <Route path="snapshots" element={<FinanceSnapshotsPage />} />
            <Route path="mrr" element={<FinanceMrrPage />} />
            <Route path="arr" element={<FinanceArrPage />} />
            <Route path="arpu" element={<FinanceArpuPage />} />
            <Route path="churn" element={<FinanceChurnPage />} />
            <Route path="invoices" element={<FinanceInvoicesPage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
