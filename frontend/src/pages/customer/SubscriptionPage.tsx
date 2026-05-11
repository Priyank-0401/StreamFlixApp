import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import * as CustomerService from '../../services/customer/customerService';
import {
  Check,
  Pause,
  Play,
  Plus,
  Trash2,
  ArrowRight,
  AlertCircle,
  Calendar,
  Package,
  Zap,
  TrendingUp,
  X
} from 'lucide-react';
import './SubscriptionPage.css';

export const SubscriptionPage: React.FC = () => {
  const [subscription, setSubscription] = useState<CustomerService.Subscription | null>(null);
  const [availablePlans, setAvailablePlans] = useState<CustomerService.Plan[]>([]);
  const [availableAddOns, setAvailableAddOns] = useState<CustomerService.AddOn[]>([]);
  const [loading, setLoading] = useState(true);
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [showPauseModal, setShowPauseModal] = useState(false);
  const [pauseDate, setPauseDate] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [sub, plans, addons] = await Promise.all([
        CustomerService.getCurrentSubscription(),
        CustomerService.getAvailablePlans(),
        CustomerService.getAvailableAddOns()
      ]);
      setSubscription(sub);
      setAvailablePlans(plans);
      setAvailableAddOns(addons);
    } catch (error) {
      console.error('Failed to load subscription data:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatAmount = (amount: number, currency: string) => {
    const locale = currency === 'USD' ? 'en-US' : currency === 'GBP' ? 'en-GB' : 'en-IN';
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: currency || 'INR'
    }).format(amount / 100);
  };

  const handleUpgrade = async (planId: number) => {
    setActionLoading(true);
    try {
      await CustomerService.upgradeSubscription({ planId, proration: true });
      await loadData();
      setShowUpgradeModal(false);
    } catch (error) {
      console.error('handleUpgrade error:', error);
      alert('Failed to upgrade plan. Please try again.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async (atPeriodEnd: boolean) => {
    if (!window.confirm(`Are you sure you want to cancel? ${atPeriodEnd ? 'You can use until period end.' : 'This takes effect immediately.'}`)) {
      return;
    }
    setActionLoading(true);
    try {
      await CustomerService.cancelSubscription({ atPeriodEnd });
      await loadData();
    } catch (error) {
      alert('Failed to cancel subscription.');
    } finally {
      setActionLoading(false);
    }
  };

  const handlePause = async () => {
    if (!pauseDate) {
      alert('Please select a resume date');
      return;
    }
    setActionLoading(true);
    try {
      await CustomerService.pauseSubscription({ pausedTo: pauseDate });
      await loadData();
      setShowPauseModal(false);
    } catch (error) {
      alert('Failed to pause subscription.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleResume = async () => {
    setActionLoading(true);
    try {
      await CustomerService.resumeSubscription();
      await loadData();
    } catch (error) {
      alert('Failed to resume subscription.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleAddAddOn = async (addonId: number) => {
    setActionLoading(true);
    try {
      await CustomerService.addAddOn(addonId);
      await loadData();
    } catch (error) {
      alert('Failed to add add-on.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRemoveAddOn = async (addonId: number) => {
    if (!window.confirm('Remove this add-on?')) return;
    setActionLoading(true);
    try {
      await CustomerService.removeAddOn(addonId);
      await loadData();
    } catch (error) {
      alert('Failed to remove add-on.');
    } finally {
      setActionLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
      case 'trialing':
        return '#22c55e';
      case 'paused':
        return '#3b82f6';
      case 'past_due':
        return '#f59e0b';
      case 'canceled':
      case 'cancelled':
        return '#5b4fff';
      default:
        return '#6b7280';
    }
  };

  if (loading) {
    return (
      <div className="subscription-loading">
        <div className="spinner" style={{ width: 40, height: 40, borderColor: '#d1d5db' }}></div>
        <p className="loading-text">Loading subscription details...</p>
      </div>
    );
  }

  // No Active Subscription - Show Available Plans
  if (!subscription) {
    return (
      <div className="subscription-page">
        {/* Plans Grid */}

        <div className="plans-grid">
          {availablePlans.map((plan) => (
            <div key={plan.planId} className="plan-card">
              <div className="plan-header">
                <h3 className="plan-name">{plan.name}</h3>
                <span className="plan-badge">{plan.billingPeriod}</span>
              </div>

              <div className="plan-price">
                <span className="price-amount">{formatAmount(plan.defaultPriceMinor, plan.defaultCurrency)}</span>
                <span className="price-period">/{plan.billingPeriod.toLowerCase()}</span>
                <div style={{ fontSize: '13px', color: '#6B7280', marginTop: '4px', fontWeight: 500 }}>
                  {plan.taxMode === 'INCLUSIVE' ? '(incl. tax)' : '+ tax'}
                </div>
              </div>

              {plan.trialDays > 0 && (
                <div className="trial-badge">
                  <Zap size={16} />
                  <span>{plan.trialDays}-day free trial</span>
                </div>
              )}

              <ul className="plan-features">
                <li><Check size={16} className="feature-icon" /> All features included</li>
                <li><Check size={16} className="feature-icon" /> HD streaming</li>
                <li><Check size={16} className="feature-icon" /> Cancel anytime</li>
              </ul>

              <Link to={`/subscribe?planId=${plan.planId}`} className="btn-subscribe">
                Subscribe <ArrowRight size={16} />
              </Link>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="subscription-page">
      {/* Current Subscription Card */}

      {/* Current Subscription Card */}
      <div className="subscription-details-card">
        <div className="subscription-info-header">
          <div className="plan-info">
            <h2 className="plan-title">{subscription.planName}</h2>
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
          <div className="billing-info">
            <Calendar size={18} />
            <span>Next billing: {subscription.status === 'TRIALING' && subscription.trialEndDate
              ? new Date(subscription.trialEndDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
              : new Date(subscription.currentPeriodEnd).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
            }</span>
          </div>
        </div>

        {/* Subscription Meta */}
        <div className="subscription-meta">
          {subscription.status === 'TRIALING' ? (
            <>
              <div className="meta-item" style={{ gridColumn: 'span 3', backgroundColor: '#f3f4f6', padding: '12px', borderRadius: '8px' }}>
                <p className="meta-value" style={{ margin: 0, color: '#374151' }}>
                  <Zap size={16} style={{ display: 'inline', verticalAlign: 'text-bottom', marginRight: '6px', color: '#f59e0b' }} />
                  You're on a free trial until <strong>{new Date(subscription.trialEndDate!).toLocaleDateString()}</strong>.
                  Your first charge of <strong>{formatAmount(
                    subscription.totalDueMinor || 0,
                    subscription.currency
                  )}</strong> will occur on that date.
                </p>
              </div>
            </>
          ) : (
            <div className="meta-item">
              <p className="meta-label">Current Period</p>
              <p className="meta-value">
                {new Date(subscription.currentPeriodStart).toLocaleDateString()} - {new Date(subscription.currentPeriodEnd).toLocaleDateString()}
              </p>
            </div>
          )}
          <div className="meta-item">
            <p className="meta-label">Currency</p>
            <p className="meta-value">{subscription.currency}</p>
          </div>
          <div className="meta-item">
            <p className="meta-label">Account Credit</p>
            <p className="meta-value">{formatAmount(subscription.creditBalanceMinor || 0, subscription.currency)}</p>
          </div>
          {subscription.status !== 'TRIALING' && subscription.trialEndDate && (
            <div className="meta-item">
              <p className="meta-label">Trial Ended</p>
              <p className="meta-value">{new Date(subscription.trialEndDate).toLocaleDateString()}</p>
            </div>
          )}
        </div>

        {/* Cancel Warning - show above actions */}
        {subscription.cancelAtPeriodEnd && (
          <div className="alert-warning">
            <AlertCircle size={20} />
            <p>Your subscription will be canceled at the end of the current billing period.</p>
          </div>
        )}

        {/* Action Buttons */}
        <div className="subscription-actions">
          {!subscription.cancelAtPeriodEnd && (
            <button
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                setShowUpgradeModal(true);
              }}
              className="btn-primary"
              disabled={actionLoading}
            >
              <TrendingUp size={18} /> Change Plan
            </button>
          )}

          {subscription.status === 'ACTIVE' && (
            <button
              onClick={() => setShowPauseModal(true)}
              className="btn-secondary"
              disabled={actionLoading}
            >
              <Pause size={18} /> Pause
            </button>
          )}

          {subscription.status === 'PAUSED' && (
            <button
              onClick={handleResume}
              className="btn-success"
              disabled={actionLoading}
            >
              <Play size={18} /> Resume
            </button>
          )}

          {!subscription.cancelAtPeriodEnd && (
            <button
              onClick={() => handleCancel(true)}
              className="btn-danger-outline"
              disabled={actionLoading}
            >
              Cancel at Period End
            </button>
          )}
        </div>
      </div>

      {/* Active Add-ons */}
      <div className="addons-section">
        <div className="section-header">
          <h3 className="section-title">Active Add-ons</h3>
          <Package size={20} className="section-icon" />
        </div>
        {subscription.addOns?.length ? (
          <div className="addons-list">
            {subscription.addOns.map((addon) => (
              <div key={addon.itemId} className="addon-item">
                <div className="addon-info">
                  <p className="addon-name">{addon.addonName}</p>
                  <p className="addon-price">
                    {formatAmount(addon.unitPriceMinor, subscription.currency)} × {addon.quantity}
                  </p>
                </div>
                <button
                  onClick={() => handleRemoveAddOn(addon.addonId)}
                  className="btn-icon-danger"
                  disabled={actionLoading}
                  title="Remove add-on"
                >
                  <Trash2 size={18} />
                </button>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <Package size={40} className="empty-icon" />
            <p className="empty-text">No active add-ons</p>
            <p className="empty-hint">Add-ons enhance your subscription with extra features</p>
          </div>
        )}
      </div>

      {/* Available Add-ons */}
      {availableAddOns.filter(a => a.status === 'ACTIVE' && !subscription.addOns?.some(subAddOn => subAddOn.addonId === a.addOnId)).length > 0 && (
        <div className="addons-section">
          <div className="section-header">
            <h3 className="section-title">Available Add-ons</h3>
            <Plus size={20} className="section-icon" />
          </div>
          <div className="addons-grid">
            {availableAddOns
              .filter(a => a.status === 'ACTIVE' && !subscription.addOns?.some(subAddOn => subAddOn.addonId === a.addOnId))
              .map((addon) => (
                <div key={addon.addOnId} className="addon-card">
                  <div className="addon-details">
                    <p className="addon-name">{addon.name}</p>
                    <p className="addon-price">
                      {formatAmount(addon.priceMinor, addon.currency)}/{addon.billingPeriod.toLowerCase()}
                    </p>
                  </div>
                  <button
                    onClick={() => handleAddAddOn(addon.addOnId)}
                    className="btn-icon-add"
                    disabled={actionLoading}
                    title="Add add-on"
                  >
                    <Plus size={20} />
                  </button>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* Metered Usage Summary */}
      {subscription.meteredUsage?.length > 0 && (
        <div className="usage-section">
          <div className="section-header">
            <h3 className="section-title">Metered Usage</h3>
            <TrendingUp size={20} className="section-icon" />
          </div>
          <div className="usage-list">
            {subscription.meteredUsage.map((usage) => (
              <div key={usage.componentId} className="usage-item">
                <div className="usage-info">
                  <p className="usage-name">{usage.componentName}</p>
                  <p className="usage-details">
                    {usage.quantityUsed} {usage.unitName} used
                    {usage.freeTierQuantity > 0 && (
                      <span className="free-tier"> ({usage.freeTierQuantity} free included)</span>
                    )}
                  </p>
                </div>
                <p className="usage-cost">
                  {formatAmount(usage.costMinor, subscription.currency)}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Upgrade Modal */}
      {showUpgradeModal && (
        <div className="modal-overlay" onClick={() => setShowUpgradeModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Change Plan</h3>
              <button 
                onClick={() => setShowUpgradeModal(false)} 
                className="modal-close"
              >
                <X size={20} />
              </button>
            </div>
            <div className="plan-options">
              {availablePlans.map((plan) => (
                <div
                  key={plan.planId}
                  className={`plan-option ${plan.planId === subscription.planId ? 'current' : ''}`}
                >
                  <div>
                    <div className="plan-option-header">
                      <h4 className="plan-option-name">{plan.name}</h4>
                      {plan.planId === subscription.planId && (
                        <span className="current-badge">Current</span>
                      )}
                    </div>
                    <p className="plan-option-price">
                      {formatAmount(plan.defaultPriceMinor, plan.defaultCurrency)}
                      <span className="period">/{plan.billingPeriod.toLowerCase()}</span>
                      <span style={{ fontSize: '12px', color: '#6B7280', marginLeft: '6px' }}>
                        {plan.taxMode === 'INCLUSIVE' ? '(incl. tax)' : '+ tax'}
                      </span>
                    </p>
                  </div>
                  {plan.planId !== subscription.planId && (
                    <button 
                      className="btn-primary" 
                      onClick={() => handleUpgrade(plan.planId)}
                      disabled={actionLoading}
                    >
                      {actionLoading ? 'Updating...' : 'Select Plan'}
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Pause Modal */}
      {showPauseModal && (
        <div className="modal-overlay" onClick={() => setShowPauseModal(false)}>
          <div className="modal modal-small" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Pause Subscription</h3>
              <button 
                onClick={() => setShowPauseModal(false)} 
                className="modal-close"
              >
                <X size={20} />
              </button>
            </div>
            <p className="modal-description" style={{ color: '#64748b', marginBottom: '1.25rem' }}>
              Select resume date (up to 30 days):
            </p>
            <input
              type="date"
              value={pauseDate}
              onChange={(e) => setPauseDate(e.target.value)}
              min={new Date().toISOString().split('T')[0]}
              max={new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]}
              className="date-input"
            />
            <div className="modal-actions">
              <button onClick={handlePause} className="btn-warning" disabled={actionLoading}>
                {actionLoading ? 'Pausing...' : 'Pause Subscription'}
              </button>
              <button onClick={() => setShowPauseModal(false)} className="btn-secondary">
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
