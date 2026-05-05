import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  CreditCard,
  Receipt,
  AlertCircle,
  CheckCircle,
  Calendar,
  ArrowRight,
  Plus,
  Package,
  Database
} from 'lucide-react';
import * as CustomerService from '../../services/customer/customerService';
import './OverviewPage.css';

export const OverviewPage: React.FC = () => {
  const [subscription, setSubscription] = useState<CustomerService.Subscription | null>(null);
  const [invoices, setInvoices] = useState<CustomerService.Invoice[]>([]);
  const [paymentMethods, setPaymentMethods] = useState<CustomerService.PaymentMethod[]>([]);
  const [isCustomer, setIsCustomer] = useState<boolean | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      // Try to get subscription first - if 400/404, user is not a customer yet
      try {
        const [sub, inv, pm] = await Promise.all([
          CustomerService.getCurrentSubscription(),
          CustomerService.getInvoices(),
          CustomerService.getPaymentMethods()
        ]);
        setSubscription(sub);
        setInvoices(inv.slice(0, 5));
        setPaymentMethods(pm);
        setIsCustomer(true);
      } catch (err: any) {
        // If we get 400 Bad Request or 404, user doesn't have customer record yet
        if (err.message?.includes('400') || err.message?.includes('404') || err.message?.includes('Customer not found')) {
          setIsCustomer(false);
        } else {
          throw err;
        }
      }
    } catch (error: any) {
      console.error('Failed to load dashboard data:', error.message || error);
    } finally {
      setLoading(false);
    }
  };

  const formatAmount = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currency || 'INR',
    }).format(amount / 100);
  };

  const formatDate = (dateString: string | null) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    });
  };

  const getNextBillingAmount = () => {
    if (!subscription) return 0;
    return subscription.addOns.reduce((sum, addon) =>
      sum + (addon.unitPriceMinor * addon.quantity), 0);
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
      case 'paid':
      case 'trialing':
        return '#22c55e';
      case 'pending':
      case 'open':
      case 'past_due':
        return '#f59e0b';
      case 'cancelled':
      case 'canceled':
      case 'failed':
        return '#dc2626';
      case 'paused':
        return '#3b82f6';
      default:
        return '#6b7280';
    }
  };

  if (loading) {
    return (
      <div className="overview-loading">
        <div className="spinner" style={{ width: 40, height: 40, borderColor: '#d1d5db' }}></div>
        <p className="loading-text">Loading your dashboard...</p>
      </div>
    );
  }

  // Non-customer view - show CTA to subscribe
  if (isCustomer === false) {
    return (
      <div className="overview-container">
        <div className="non-customer-hero">
          <div className="hero-icon">
            <Package size={48} color="#5b4fff" />
          </div>
          <h1 className="hero-title">Welcome to StreamFlix</h1>
          <p className="hero-subtitle">
            You&apos;re almost there! Complete your subscription to start streaming your favorite content.
          </p>
          <Link to="/plans" className="btn-primary-large">
            <Plus size={20} />
            Choose a Plan
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="overview-page">
      {/* Page Header */}
      <div className="page-header">
        <h1 className="page-title">Dashboard Overview</h1>
        <p className="page-subtitle">
          Welcome back! Here&apos;s what&apos;s happening with your account.
        </p>
      </div>

      {/* Stats Grid */}
      <div className="stats-grid">
        {/* Current Plan Card */}
        <div className="stat-card">
          <div className="stat-card-content">
            <div className="stat-card-header">
              <div>
                <p className="stat-label">Current Plan</p>
                <p className="stat-value">{subscription?.planName || 'No Active Plan'}</p>
              </div>
              <div className="stat-icon" style={{ color: '#5b4fff' }}>
                <CreditCard size={24} />
              </div>
            </div>
            <Link to="/dashboard/subscription" className="stat-link">
              Manage Subscription <ArrowRight size={16} />
            </Link>
          </div>
        </div>

        {/* Next Billing Card */}
        <div className="stat-card">
          <div className="stat-card-content">
            <div className="stat-card-header">
              <div>
                <p className="stat-label">Next Billing</p>
                <p className="stat-value">{subscription ? formatDate(subscription.currentPeriodEnd) : 'N/A'}</p>
              </div>
              <div className="stat-icon" style={{ color: '#5b4fff' }}>
                <Calendar size={24} />
              </div>
            </div>
            <p className="stat-subtext">
              Amount: {formatAmount(getNextBillingAmount(), subscription?.currency || 'INR')}
            </p>
          </div>
        </div>

        {/* Usage Card - Metered Storage */}
        <div className="stat-card">
          <div className="stat-card-content">
            <div className="stat-card-header">
              <div>
                <p className="stat-label">Download Storage</p>
                <p className="stat-value">7.2 GB</p>
              </div>
              <div className="stat-icon" style={{ color: '#5b4fff' }}>
                <Database size={24} />
              </div>
            </div>
            <p className="stat-subtext">
              2.2 GB of 10 GB free tier used
            </p>
          </div>
        </div>

        {/* Amount Due Card */}
        <div className="stat-card">
          <div className="stat-card-content">
            <div className="stat-card-header">
              <div>
                <p className="stat-label">Amount Due</p>
                <p className="stat-value">
                  {formatAmount(
                    invoices.filter(i => i.status === 'OPEN').reduce((sum, i) => sum + i.balanceMinor, 0),
                    subscription?.currency || 'INR'
                  )}
                </p>
              </div>
              <div className="stat-icon" style={{ color: '#5b4fff' }}>
                <Receipt size={24} />
              </div>
            </div>
            <Link to="/dashboard/billing" className="stat-link">
              View Invoices <ArrowRight size={16} />
            </Link>
          </div>
        </div>

        {/* Payment Methods Card */}
        <div className="stat-card">
          <div className="stat-card-content">
            <div className="stat-card-header">
              <div>
                <p className="stat-label">Payment Methods</p>
                <p className="stat-value">{paymentMethods.length} Saved</p>
              </div>
              <div className="stat-icon" style={{ color: '#5b4fff' }}>
                <CheckCircle size={24} />
              </div>
            </div>
            <Link to="/dashboard/payment-methods" className="stat-link">
              Manage Methods <ArrowRight size={16} />
            </Link>
          </div>
        </div>
      </div>

      {/* Subscription Status */}
      {subscription && (
        <div className="subscription-card">
          <div className="subscription-header">
            <h2 className="section-title">Subscription Status</h2>
            <span
              className="status-badge"
              style={{
                backgroundColor: `${getStatusColor(subscription.status)}20`,
                color: getStatusColor(subscription.status)
              }}
            >
              {subscription.status === 'TRIALING' ? 'FREE TRIAL' : subscription.status}
            </span>
          </div>

          <div className="subscription-details">
            <div className="detail-item">
              <p className="detail-label">Plan</p>
              <p className="detail-value">{subscription.planName}</p>
            </div>
            
            {subscription.status === 'TRIALING' ? (
              <>
                <div className="detail-item">
                  <p className="detail-label">Trial Ends On</p>
                  <p className="detail-value">{formatDate(subscription.trialEndDate)}</p>
                </div>
                <div className="detail-item">
                  <p className="detail-label">First Payment</p>
                  <p className="detail-value">
                    {formatAmount(
                      (subscription.planPriceMinor || 0) + 
                      (subscription.addOns?.reduce((sum, addon) => sum + (addon.unitPriceMinor * addon.quantity), 0) || 0), 
                      subscription.currency
                    )} due on {formatDate(subscription.trialEndDate)}
                  </p>
                </div>
              </>
            ) : (
              <div className="detail-item">
                <p className="detail-label">Current Period</p>
                <p className="detail-value">
                  {formatDate(subscription.currentPeriodStart)} - {formatDate(subscription.currentPeriodEnd)}
                </p>
              </div>
            )}
            
            <div className="detail-item">
              <p className="detail-label">Add-ons</p>
              <p className="detail-value">{subscription.addOns?.length || 0} Active</p>
            </div>
          </div>

          {subscription.cancelAtPeriodEnd && (
            <div className="alert-warning">
              <AlertCircle size={20} />
              <p>Your subscription will be canceled at the end of the current billing period.</p>
            </div>
          )}
        </div>
      )}

      {/* No Subscription CTA */}
      {!subscription && (
        <div className="empty-state-card">
          <h3 className="empty-state-title">Start Your Subscription</h3>
          <p className="empty-state-subtitle">Choose a plan to get started with StreamFlix</p>
          <Link to="/plans" className="btn-primary-large">
            <Plus size={20} /> Subscribe Now
          </Link>
        </div>
      )}

      {/* Recent Invoices */}
      <div className="invoices-section">
        <div className="section-header">
          <h2 className="section-title">Recent Invoices</h2>
          <Link to="/dashboard/billing" className="view-all-link">
            View All
          </Link>
        </div>

        <div className="invoices-list">
          {invoices.length ? (
            invoices.map((invoice) => (
              <div key={invoice.invoiceId} className="invoice-item">
                <div className="invoice-info">
                  <p className="invoice-number">{invoice.invoiceNumber}</p>
                  <p className="invoice-date">{formatDate(invoice.issueDate)}</p>
                </div>
                <div className="invoice-meta">
                  <p className="invoice-amount">{formatAmount(invoice.totalMinor, invoice.currency)}</p>
                  <span
                    className="status-badge"
                    style={{
                      backgroundColor: `${getStatusColor(invoice.status)}20`,
                      color: getStatusColor(invoice.status)
                    }}
                  >
                    {invoice.status}
                  </span>
                </div>
              </div>
            ))
          ) : (
            <div className="empty-list">
              <Receipt size={48} />
              <p>No recent invoices</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
