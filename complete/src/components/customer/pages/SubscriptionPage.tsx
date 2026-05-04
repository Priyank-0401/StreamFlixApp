import React, { useEffect, useState } from 'react';
import {
  Check,
  AlertCircle,
  Pause,
  Play,
  Trash2,
  ArrowUp,
  ArrowDown,
  Plus,
  Minus
} from 'lucide-react';
import * as customerService from '../../../services/customer/customerService';
import type {
  Subscription,
  Plan,
  AddOn,
  UsageRecord
} from '../../../services/customer/customerService';

export const SubscriptionPage: React.FC = () => {
  const [subscription, setSubscription] = useState<Subscription | null>(null);
  const [plans, setPlans] = useState<Plan[]>([]);
  const [addOns, setAddOns] = useState<AddOn[]>([]);
  const [usage, setUsage] = useState<UsageRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showPlanChange, setShowPlanChange] = useState(false);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [showPauseConfirm, setShowPauseConfirm] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const [sub, availablePlans, availableAddOns, usageRecords] = await Promise.all([
        customerService.getCurrentSubscription(),
        customerService.getAvailablePlans(),
        customerService.getAvailableAddOns(),
        customerService.getMeteredUsage()
      ]);
      setSubscription(sub);
      setPlans(availablePlans.filter(p => p.status === 'ACTIVE'));
      setAddOns(availableAddOns.filter(a => a.status === 'ACTIVE'));
      setUsage(usageRecords);
    } catch (err: any) {
      setError(err.message || 'Failed to load subscription data');
    } finally {
      setLoading(false);
    }
  };

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

  const handleUpgrade = async (planId: number) => {
    try {
      setActionLoading(true);
      setError('');
      const updated = await customerService.upgradeSubscription({ planId, proration: true });
      setSubscription(updated);
      setSuccess('Subscription upgraded successfully!');
      setShowPlanChange(false);
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.message || 'Failed to upgrade subscription');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async (atPeriodEnd: boolean = true) => {
    try {
      setActionLoading(true);
      setError('');
      await customerService.cancelSubscription({ atPeriodEnd });
      await loadData();
      setSuccess(atPeriodEnd ? 'Subscription will cancel at period end.' : 'Subscription cancelled.');
      setShowCancelConfirm(false);
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.message || 'Failed to cancel subscription');
    } finally {
      setActionLoading(false);
    }
  };

  const handlePause = async () => {
    try {
      setActionLoading(true);
      setError('');
      const pausedTo = new Date();
      pausedTo.setMonth(pausedTo.getMonth() + 1); // Pause for 1 month by default
      const updated = await customerService.pauseSubscription({ pausedTo: pausedTo.toISOString().split('T')[0] });
      setSubscription(updated);
      setSuccess('Subscription paused successfully!');
      setShowPauseConfirm(false);
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.message || 'Failed to pause subscription');
    } finally {
      setActionLoading(false);
    }
  };

  const handleResume = async () => {
    try {
      setActionLoading(true);
      setError('');
      const updated = await customerService.resumeSubscription();
      setSubscription(updated);
      setSuccess('Subscription resumed successfully!');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.message || 'Failed to resume subscription');
    } finally {
      setActionLoading(false);
    }
  };

  const handleAddOnToggle = async (addonId: number, isAdding: boolean) => {
    try {
      setActionLoading(true);
      setError('');
      if (isAdding) {
        await customerService.addAddOn(addonId);
      } else {
        await customerService.removeAddOn(addonId);
      }
      await loadData();
      setSuccess(isAdding ? 'Add-on added successfully!' : 'Add-on removed successfully!');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.message || 'Failed to update add-on');
    } finally {
      setActionLoading(false);
    }
  };

  const getTotalUsageForComponent = (componentId: number) => {
    return usage
      .filter(u => u.componentId === componentId)
      .reduce((sum, u) => sum + u.quantity, 0);
  };

  if (loading) {
    return (
      <div className="subscription-loading">
        <div className="spinner" />
        <p>Loading subscription details...</p>
      </div>
    );
  }

  // No subscription - show plan selection
  if (!subscription) {
    return (
      <div className="subscription-new">
        <div className="section-header">
          <h2>Choose Your Plan</h2>
          <p>Select a plan that fits your entertainment needs</p>
        </div>

        {error && (
          <div className="alert alert-error">
            <AlertCircle size={18} />
            {error}
          </div>
        )}

        <div className="plans-grid">
          {plans.map((plan) => (
            <div key={plan.planId} className={`plan-card ${plan.name.includes('Premium') ? 'featured' : ''}`}>
              {plan.name.includes('Premium') && <div className="plan-badge">Best Value</div>}
              <h3 className="plan-name">{plan.name}</h3>
              <div className="plan-price">
                {formatCurrency(plan.defaultPriceMinor, plan.defaultCurrency)}
                <span>/{plan.billingPeriod.toLowerCase()}</span>
              </div>
              <p className="plan-trial">{plan.trialDays}-day free trial</p>
              <ul className="plan-features">
                <li><Check size={16} /> HD streaming</li>
                <li><Check size={16} /> {plan.name.includes('Premium') ? '4 screens' : '1 screen'}</li>
                <li><Check size={16} /> {plan.name.includes('Premium') ? 'Downloads on 10 devices' : 'Downloads on 1 device'}</li>
                <li><Check size={16} /> Cancel anytime</li>
              </ul>
              <button
                className="btn btn-primary btn-full"
                onClick={() => handleUpgrade(plan.planId)}
                disabled={actionLoading}
              >
                {actionLoading ? 'Processing...' : 'Start Free Trial'}
              </button>
            </div>
          ))}
        </div>
      </div>
    );
  }

  // Has subscription - show management interface
  return (
    <div className="subscription-manage">
      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          {error}
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          <Check size={18} />
          {success}
        </div>
      )}

      {/* Current Plan Card */}
      <div className="card current-plan-card">
        <div className="card-header">
          <div>
            <h2 className="card-title">Current Plan</h2>
            <p className="card-subtitle">Your active subscription details</p>
          </div>
          <div className={`badge ${subscription.status === 'ACTIVE' ? 'badge-success' : 'badge-warning'}`}>
            {subscription.status}
          </div>
        </div>

        <div className="plan-details-grid">
          <div className="plan-detail">
            <span className="detail-label">Plan</span>
            <span className="detail-value">{subscription.planName}</span>
          </div>
          <div className="plan-detail">
            <span className="detail-label">Started</span>
            <span className="detail-value">{formatDate(subscription.startDate)}</span>
          </div>
          <div className="plan-detail">
            <span className="detail-label">Current Period</span>
            <span className="detail-value">
              {formatDate(subscription.currentPeriodStart)} - {formatDate(subscription.currentPeriodEnd)}
            </span>
          </div>
          <div className="plan-detail">
            <span className="detail-label">Next Billing</span>
            <span className="detail-value">{formatDate(subscription.currentPeriodEnd)}</span>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="plan-actions">
          <button
            className="btn btn-secondary"
            onClick={() => setShowPlanChange(true)}
            disabled={actionLoading || subscription.status === 'CANCELED'}
          >
            <ArrowUp size={16} />
            Change Plan
          </button>

          {subscription.status !== 'PAUSED' ? (
            <button
              className="btn btn-outline"
              onClick={() => setShowPauseConfirm(true)}
              disabled={actionLoading || subscription.status === 'CANCELED'}
            >
              <Pause size={16} />
              Pause
            </button>
          ) : (
            <button
              className="btn btn-outline"
              onClick={handleResume}
              disabled={actionLoading}
            >
              <Play size={16} />
              Resume
            </button>
          )}

          <button
            className="btn btn-danger"
            onClick={() => setShowCancelConfirm(true)}
            disabled={actionLoading || subscription.status === 'CANCELED'}
          >
            <Trash2 size={16} />
            Cancel
          </button>
        </div>

        {subscription.cancelAtPeriodEnd && (
          <div className="cancellation-notice">
            <AlertCircle size={18} />
            <span>Your subscription will cancel on {formatDate(subscription.currentPeriodEnd)}</span>
          </div>
        )}
      </div>

      {/* Add-ons Section */}
      <div className="card" style={{ marginTop: '1.5rem' }}>
        <div className="card-header">
          <div>
            <h2 className="card-title">Add-ons</h2>
            <p className="card-subtitle">Enhance your subscription</p>
          </div>
        </div>

        {addOns.length > 0 ? (
          <div className="addons-list">
            {addOns.map((addon) => {
              const isActive = subscription.addOns.some(a => a.addonId === addon.addOnId);
              return (
                <div key={addon.addOnId} className={`addon-item ${isActive ? 'active' : ''}`}>
                  <div className="addon-info">
                    <h4>{addon.name}</h4>
                    <p>{formatCurrency(addon.priceMinor, addon.currency)}/{addon.billingPeriod.toLowerCase()}</p>
                  </div>
                  <button
                    className={`btn ${isActive ? 'btn-danger' : 'btn-primary'} btn-sm`}
                    onClick={() => handleAddOnToggle(addon.addOnId, !isActive)}
                    disabled={actionLoading}
                  >
                    {isActive ? (
                      <><Minus size={14} /> Remove</>
                    ) : (
                      <><Plus size={14} /> Add</>
                    )}
                  </button>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="empty-state-small">
            <p>No add-ons available</p>
          </div>
        )}
      </div>

      {/* Metered Usage Section */}
      {subscription.meteredUsage.length > 0 && (
        <div className="card" style={{ marginTop: '1.5rem' }}>
          <div className="card-header">
            <div>
              <h2 className="card-title">Usage This Period</h2>
              <p className="card-subtitle">Download storage consumption</p>
            </div>
          </div>

          <div className="metered-list">
            {subscription.meteredUsage.map((usage) => {
              const totalUsed = getTotalUsageForComponent(usage.componentId);
              const isOverFree = totalUsed > usage.freeTierQuantity;
              return (
                <div key={usage.componentId} className="metered-usage-item">
                  <div className="metered-header">
                    <div className="metered-title">
                      <h4>{usage.componentName}</h4>
                      <span className={`usage-badge ${isOverFree ? 'over' : 'within'}`}>
                        {totalUsed} / {usage.freeTierQuantity} {usage.unitName} free
                      </span>
                    </div>
                    <span className="metered-cost">
                      {isOverFree
                        ? formatCurrency((totalUsed - usage.freeTierQuantity) * usage.pricePerUnitMinor, subscription.currency)
                        : 'Included'
                      }
                    </span>
                  </div>
                  <div className="usage-bar">
                    <div
                      className="usage-fill"
                      style={{
                        width: `${Math.min((totalUsed / (usage.freeTierQuantity * 1.5)) * 100, 100)}%`,
                        background: isOverFree ? 'var(--error)' : 'var(--success)'
                      }}
                    />
                  </div>
                  <p className="usage-info">
                    {usage.freeTierQuantity} {usage.unitName} included, then {formatCurrency(usage.pricePerUnitMinor, subscription.currency)}/{usage.unitName}
                  </p>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Plan Change Modal */}
      {showPlanChange && (
        <div className="modal-overlay" onClick={() => setShowPlanChange(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Change Your Plan</h3>
            </div>
            <div className="modal-body">
              <div className="plans-comparison">
                {plans
                  .filter(p => p.planId !== subscription.planId)
                  .map(plan => (
                    <div key={plan.planId} className="plan-option">
                      <div className="plan-option-info">
                        <h4>{plan.name}</h4>
                        <p>{formatCurrency(plan.defaultPriceMinor, plan.defaultCurrency)}/{plan.billingPeriod.toLowerCase()}</p>
                      </div>
                      <button
                        className="btn btn-primary btn-sm"
                        onClick={() => handleUpgrade(plan.planId)}
                        disabled={actionLoading}
                      >
                        {plan.defaultPriceMinor > (subscription.addOns[0]?.unitPriceMinor || 0)
                          ? <><ArrowUp size={14} /> Upgrade</>
                          : <><ArrowDown size={14} /> Downgrade</>
                        }
                      </button>
                    </div>
                  ))}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline" onClick={() => setShowPlanChange(false)}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Cancel Confirmation Modal */}
      {showCancelConfirm && (
        <div className="modal-overlay" onClick={() => setShowCancelConfirm(false)}>
          <div className="modal modal-confirm" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Cancel Subscription</h3>
            </div>
            <div className="modal-body">
              <div className="confirm-icon warning">
                <AlertCircle size={32} />
              </div>
              <p>Are you sure you want to cancel your subscription?</p>
              <div className="cancel-options">
                <button
                  className="btn btn-outline btn-full"
                  onClick={() => handleCancel(true)}
                  disabled={actionLoading}
                >
                  Cancel at Period End
                </button>
                <button
                  className="btn btn-danger btn-full"
                  onClick={() => handleCancel(false)}
                  disabled={actionLoading}
                >
                  Cancel Immediately
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Pause Confirmation Modal */}
      {showPauseConfirm && (
        <div className="modal-overlay" onClick={() => setShowPauseConfirm(false)}>
          <div className="modal modal-confirm" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Pause Subscription</h3>
            </div>
            <div className="modal-body">
              <div className="confirm-icon info">
                <Pause size={32} />
              </div>
              <p>Your subscription will be paused for 1 month. You won't be charged during this period.</p>
              <div className="pause-actions">
                <button
                  className="btn btn-primary btn-full"
                  onClick={handlePause}
                  disabled={actionLoading}
                >
                  Pause for 1 Month
                </button>
                <button
                  className="btn btn-outline btn-full"
                  onClick={() => setShowPauseConfirm(false)}
                >
                  Keep Active
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
