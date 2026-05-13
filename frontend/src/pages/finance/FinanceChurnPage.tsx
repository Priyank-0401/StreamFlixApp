import React, { useState, useEffect } from 'react';
import { PageHeader } from '../../components/admin/shared/PageHeader';
import { getChurnedSubscriptions, type ChurnFinanceDTO } from '../../services/finance/financeService';

export const FinanceChurnPage: React.FC = () => {
  const [churned, setChurned] = useState<ChurnFinanceDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const data = await getChurnedSubscriptions();
        setChurned(data);
      } catch (err) {
        console.error('Failed to load churned subscriptions:', err);
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

  const totalLostMonthlyINR = churned.reduce((sum, c) => {
    const curr = c.currency?.toUpperCase();
    if (curr === 'USD') return sum + c.lostMonthlyRevenueMinor * 95;
    if (curr === 'GBP') return sum + c.lostMonthlyRevenueMinor * 129;
    return sum + c.lostMonthlyRevenueMinor;
  }, 0);

  return (
    <>
      <PageHeader subtitle="Detailed read-only tracking of canceled subscriptions, lost recurring value, and churn dates" />

      <div className="data-panel" style={{ padding: '24px', marginBottom: '24px', background: '#fef2f2', borderColor: '#fecaca' }}>
        <h3 style={{ fontSize: '14px', fontWeight: 600, color: '#991b1b', textTransform: 'uppercase', margin: '0 0 8px 0' }}>Total Monthly Revenue Lost to Churn</h3>
        <p style={{ fontSize: '32px', fontWeight: 700, color: '#b91c1c', margin: 0 }}>{formatCurrencyINR(totalLostMonthlyINR)}</p>
      </div>

      <div className="data-panel">
        <div className="data-panel-header" style={{ padding: '20px 24px', borderBottom: '1px solid #e5e7eb' }}>
          <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#1f2937', margin: 0 }}>Canceled Subscriptions ({churned.length})</h2>
        </div>
        <div className="admin-table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Subscription ID</th>
                <th>Customer</th>
                <th>Plan</th>
                <th>Cancellation Date</th>
                <th>Reason</th>
                <th style={{ textAlign: 'right' }}>Lost Monthly Revenue</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>Loading churn records...</td>
                </tr>
              ) : churned.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>No churned subscriptions found.</td>
                </tr>
              ) : (
                churned.map(item => (
                  <tr key={item.subscriptionId}>
                    <td style={{ fontWeight: 600, color: '#1e293b' }}>SUB-{item.subscriptionId}</td>
                    <td>
                      <div>{item.customerName}</div>
                      <div style={{ fontSize: '12px', color: '#64748b' }}>{item.customerEmail}</div>
                    </td>
                    <td><span style={{ fontWeight: 500 }}>{item.planName}</span></td>
                    <td style={{ color: '#64748b', fontSize: '13px' }}>{item.canceledAt}</td>
                    <td><span style={{ fontSize: '12px', color: '#ef4444', fontWeight: 500 }}>{item.reason}</span></td>
                    <td style={{ textAlign: 'right', fontWeight: 700, color: '#ef4444' }}>{formatCurrencyRow(item.lostMonthlyRevenueMinor, item.currency)}</td>
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
