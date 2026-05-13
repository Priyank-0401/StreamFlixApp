import React, { useState, useEffect } from 'react';
import { PageHeader } from '../../components/admin/shared/PageHeader';
import { getArrSubscriptions, type SubscriptionFinanceDTO } from '../../services/finance/financeService';

export const FinanceArrPage: React.FC = () => {
  const [subscriptions, setSubscriptions] = useState<SubscriptionFinanceDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const data = await getArrSubscriptions();
        setSubscriptions(data);
      } catch (err) {
        console.error('Failed to load ARR subscriptions:', err);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  const formatCurrencyRow = (minor: number, currency: string) => {
    return new Intl.NumberFormat(currency === 'INR' ? 'en-IN' : 'en-US', {
      style: 'currency',
      currency: currency || 'INR',
    }).format(minor / 100);
  };

  const formatCurrencyINR = (minor: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(minor / 100);
  };

  const totalArrINR = subscriptions.reduce((sum, s) => {
    const curr = s.currency?.toUpperCase();
    if (curr === 'USD') return sum + s.annualValueMinor * 95;
    if (curr === 'GBP') return sum + s.annualValueMinor * 129;
    return sum + s.annualValueMinor;
  }, 0);

  return (
    <>
      <PageHeader subtitle="Detailed read-only breakdown of annualized recurring revenue projections per subscription" />

      <div className="data-panel" style={{ padding: '24px', marginBottom: '24px', background: '#f0fdf4', borderColor: '#bbf7d0' }}>
        <h3 style={{ fontSize: '14px', fontWeight: 600, color: '#166534', textTransform: 'uppercase', margin: '0 0 8px 0' }}>Total Projected ARR</h3>
        <p style={{ fontSize: '32px', fontWeight: 700, color: '#15803d', margin: 0 }}>{formatCurrencyINR(totalArrINR)}</p>
      </div>

      <div className="data-panel">
        <div className="data-panel-header" style={{ padding: '20px 24px', borderBottom: '1px solid #e5e7eb' }}>
          <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#1f2937', margin: 0 }}>Annualized Projections</h2>
        </div>
        <div className="admin-table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Subscription ID</th>
                <th>Customer</th>
                <th>Plan</th>
                <th>Billing Period</th>
                <th>Start Date</th>
                <th style={{ textAlign: 'right' }}>Annual Projected Value</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>Loading ARR records...</td>
                </tr>
              ) : subscriptions.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>No ARR subscriptions found.</td>
                </tr>
              ) : (
                subscriptions.map(sub => (
                  <tr key={sub.subscriptionId}>
                    <td style={{ fontWeight: 600, color: '#1e293b' }}>SUB-{sub.subscriptionId}</td>
                    <td>
                      <div>{sub.customerName}</div>
                      <div style={{ fontSize: '12px', color: '#64748b' }}>{sub.customerEmail}</div>
                    </td>
                    <td><span style={{ fontWeight: 500 }}>{sub.planName}</span></td>
                    <td>
                      <span style={{ fontSize: '11px', fontWeight: 600, padding: '2px 8px', borderRadius: '4px', backgroundColor: '#f1f5f9', color: '#475569' }}>
                        {sub.billingPeriod}
                      </span>
                    </td>
                    <td style={{ color: '#64748b', fontSize: '13px' }}>{sub.startDate}</td>
                    <td style={{ textAlign: 'right', fontWeight: 700, color: '#5b4fff' }}>{formatCurrencyRow(sub.annualValueMinor, sub.currency)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
};
