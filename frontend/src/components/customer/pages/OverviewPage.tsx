import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  CreditCard,
  FileText,
  AlertCircle,
  Check,
  Play,
  Calendar,
  ArrowRight,
  Zap
} from 'lucide-react';
import * as customerService from '../../../services/customer/customerService';
import type { Subscription, Invoice, CustomerProfile } from '../../../services/customer/customerService';

export const OverviewPage: React.FC = () => {
  const [subscription, setSubscription] = useState<Subscription | null>(null);
  const [recentInvoice, setRecentInvoice] = useState<Invoice | null>(null);
  const [_profile, setProfile] = useState<CustomerProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const [sub, invoices, prof] = await Promise.all([
          customerService.getCurrentSubscription(),
          customerService.getInvoices({ status: 'OPEN' }),
          customerService.getCustomerProfile()
        ]);
        setSubscription(sub);
        setRecentInvoice(invoices[0] || null);
        setProfile(prof);
      } catch (err: any) {
        setError(err.message || 'Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  const formatCurrency = (amountMinor: number, currency: string) => {
    const amount = amountMinor / 100;
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  };

  const getDaysRemaining = (endDate: string) => {
    const end = new Date(endDate);
    const now = new Date();
    const diff = Math.ceil((end.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    return diff > 0 ? diff : 0;
  };

  if (loading) {
    return (
      <div className="overview-page">
        <div className="overview-loading">
          <div className="spinner" />
          <p>Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  // NEW CUSTOMER - No subscription yet
  if (!subscription) {
    return (
      <div className="overview-page">
        <div className="overview-new-customer">
          <div className="welcome-hero">
            <div className="welcome-icon">
              <Play fill="currentColor" size={40} />
            </div>
            <h1>Welcome to StreamFlix!</h1>
            <p>Start your entertainment journey today. Choose a plan that works for you.</p>
            <Link to="/dashboard/subscription" className="btn btn-primary btn-lg">
              <Zap size={20} />
              Choose Your Plan
            </Link>
            <div className="trust-badges">
              <span><Check size={14} /> 7-day free trial</span>
              <span><Check size={14} /> No credit card required</span>
              <span><Check size={14} /> Cancel anytime</span>
            </div>
          </div>

          <div className="grid grid-3">
            <div className="card feature-card">
              <div className="feature-icon">
                <Play size={24} />
              </div>
              <h3>Unlimited Streaming</h3>
              <p>Watch thousands of movies and TV shows anytime, anywhere.</p>
            </div>
            <div className="card feature-card">
              <div className="feature-icon">
                <Calendar size={24} />
              </div>
              <h3>Flexible Billing</h3>
              <p>Monthly or yearly plans. Upgrade, downgrade, or cancel anytime.</p>
            </div>
            <div className="card feature-card">
              <div className="feature-icon">
                <CreditCard size={24} />
              </div>
              <h3>Secure Payments</h3>
              <p>Multiple payment options with enterprise-grade security.</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // EXISTING CUSTOMER - Has subscription
  return (
    <div className="overview-page">
      <div className="overview-existing">
      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          {error}
        </div>
      )}

      {/* Stats Row */}
      <div className="grid grid-4">
        <div className="stat-card">
          <div className="stat-label">Current Plan</div>
          <div className="stat-value" style={{ fontSize: '1.25rem' }}>
            {subscription.planName}
          </div>
          <div className={`badge ${subscription.status === 'ACTIVE' ? 'badge-success' : 'badge-warning'}`}>
            {subscription.status}
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-label">Days Remaining</div>
          <div className="stat-value">{getDaysRemaining(subscription.currentPeriodEnd)}</div>
          <div className="stat-change">
            Until {formatDate(subscription.currentPeriodEnd)}
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-label">Next Billing</div>
          <div className="stat-value" style={{ fontSize: '1.25rem' }}>
            {formatDate(subscription.currentPeriodEnd)}
          </div>
          {recentInvoice && (
            <div className="stat-change">
              {formatCurrency(recentInvoice.totalMinor, recentInvoice.currency)}
            </div>
          )}
        </div>

        <div className="stat-card">
          <div className="stat-label">Trial Status</div>
          <div className="stat-value" style={{ fontSize: '1.25rem' }}>
            {subscription.trialEndDate && new Date(subscription.trialEndDate) > new Date()
              ? 'Active'
              : 'Ended'}
          </div>
          {subscription.trialEndDate && new Date(subscription.trialEndDate) > new Date() && (
            <div className="stat-change positive">
              Until {formatDate(subscription.trialEndDate)}
            </div>
          )}
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-2" style={{ marginTop: '1.5rem' }}>
        {/* Subscription Details */}
        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">Subscription Details</h2>
              <p className="card-subtitle">Manage your plan and add-ons</p>
            </div>
            <Link to="/dashboard/subscription" className="btn btn-secondary btn-sm">
              Manage
            </Link>
          </div>

          <div className="subscription-info">
            <div className="info-row">
              <span className="info-label">Plan</span>
              <span className="info-value">{subscription.planName}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Status</span>
              <span className={`badge ${subscription.status === 'ACTIVE' ? 'badge-success' : 'badge-warning'}`}>
                {subscription.status}
              </span>
            </div>
            <div className="info-row">
              <span className="info-label">Started</span>
              <span className="info-value">{formatDate(subscription.startDate)}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Current Period</span>
              <span className="info-value">
                {formatDate(subscription.currentPeriodStart)} - {formatDate(subscription.currentPeriodEnd)}
              </span>
            </div>
            {subscription.cancelAtPeriodEnd && (
              <div className="info-row">
                <span className="info-label">Cancellation</span>
                <span className="badge badge-error">Cancels at period end</span>
              </div>
            )}
          </div>

          {/* Add-ons */}
          {subscription.addOns.length > 0 && (
            <div className="add-ons-section">
              <h4>Active Add-ons</h4>
              {subscription.addOns.map((addon) => (
                <div key={addon.itemId} className="add-on-item">
                  <span>{addon.addonName}</span>
                  <span>{formatCurrency(addon.unitPriceMinor * addon.quantity, subscription.currency)}/mo</span>
                </div>
              ))}
            </div>
          )}

          {/* Metered Usage */}
          {subscription.meteredUsage.length > 0 && (
            <div className="metered-section">
              <h4>Usage This Period</h4>
              {subscription.meteredUsage.map((usage) => (
                <div key={usage.componentId} className="metered-item">
                  <div className="metered-info">
                    <span>{usage.componentName}</span>
                    <span className="metered-amount">
                      {usage.quantityUsed} {usage.unitName}
                    </span>
                  </div>
                  <div className="metered-bar">
                    <div
                      className="metered-fill"
                      style={{
                        width: `${Math.min((usage.quantityUsed / (usage.freeTierQuantity || 1)) * 100, 100)}%`,
                        background: usage.quantityUsed > usage.freeTierQuantity ? 'var(--error)' : 'var(--success)'
                      }}
                    />
                  </div>
                  <span className="metered-cost">
                    {usage.costMinor > 0 ? formatCurrency(usage.costMinor, subscription.currency) : 'Free tier'}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent Activity */}
        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">Recent Activity</h2>
              <p className="card-subtitle">Latest invoices and payments</p>
            </div>
            <Link to="/dashboard/billing" className="btn btn-secondary btn-sm">
              View All
            </Link>
          </div>

          {recentInvoice ? (
            <div className="recent-invoice">
              <div className="invoice-header">
                <div className="invoice-icon">
                  <FileText size={24} />
                </div>
                <div className="invoice-info">
                  <h4>Invoice #{recentInvoice.invoiceNumber}</h4>
                  <p>Due {formatDate(recentInvoice.dueDate || recentInvoice.issueDate)}</p>
                </div>
                <div className={`badge ${
                  recentInvoice.status === 'PAID' ? 'badge-success' :
                  recentInvoice.status === 'OPEN' ? 'badge-warning' :
                  'badge-error'
                }`}>
                  {recentInvoice.status}
                </div>
              </div>
              <div className="invoice-amount">
                {formatCurrency(recentInvoice.totalMinor, recentInvoice.currency)}
              </div>
              <div className="invoice-actions">
                <Link to={`/dashboard/billing?invoice=${recentInvoice.invoiceId}`} className="btn btn-primary btn-sm">
                  View Details
                </Link>
                <button
                  className="btn btn-outline btn-sm"
                  onClick={() => customerService.downloadInvoice(recentInvoice.invoiceId)}
                >
                  Download PDF
                </button>
              </div>
            </div>
          ) : (
            <div className="empty-state-small">
              <p>No recent invoices</p>
            </div>
          )}

          {/* Quick Actions */}
          <div className="quick-actions">
            <h4>Quick Actions</h4>
            <div className="action-buttons">
              <Link to="/dashboard/subscription" className="action-btn">
                <CreditCard size={18} />
                <span>Upgrade Plan</span>
                <ArrowRight size={16} />
              </Link>
              <Link to="/dashboard/payment-methods" className="action-btn">
                <Zap size={18} />
                <span>Update Payment</span>
                <ArrowRight size={16} />
              </Link>
              <Link to="/dashboard/support" className="action-btn">
                <AlertCircle size={18} />
                <span>Get Help</span>
                <ArrowRight size={16} />
              </Link>
            </div>
          </div>
        </div>
      </div>
      </div>
    </div>
  );
};
