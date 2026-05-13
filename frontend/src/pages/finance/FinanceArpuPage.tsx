import React, { useState, useEffect } from 'react';
import { PageHeader } from '../../components/admin/shared/PageHeader';
import { getArpuCustomers, type CustomerFinanceDTO } from '../../services/finance/financeService';

export const FinanceArpuPage: React.FC = () => {
  const [customers, setCustomers] = useState<CustomerFinanceDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const data = await getArpuCustomers();
        setCustomers(data);
      } catch (err) {
        console.error('Failed to load ARPU customers:', err);
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

  const totalMonthlySumINR = customers.reduce((sum, c) => {
    const curr = c.currency?.toUpperCase();
    if (curr === 'USD') return sum + c.monthlyContributionMinor * 95;
    if (curr === 'GBP') return sum + c.monthlyContributionMinor * 129;
    return sum + c.monthlyContributionMinor;
  }, 0);

  const avgArpuINR = customers.length > 0 ? totalMonthlySumINR / customers.length : 0;

  return (
    <>
      <PageHeader subtitle="Detailed read-only view of active customer accounts and their monthly average spend contribution" />

      <div className="data-panel" style={{ padding: '24px', marginBottom: '24px', background: '#eff6ff', borderColor: '#bfdbfe' }}>
        <h3 style={{ fontSize: '14px', fontWeight: 600, color: '#1e40af', textTransform: 'uppercase', margin: '0 0 8px 0' }}>Average Revenue Per User (ARPU)</h3>
        <p style={{ fontSize: '32px', fontWeight: 700, color: '#1d4ed8', margin: 0 }}>{formatCurrencyINR(avgArpuINR)}</p>
      </div>

      <div className="data-panel">
        <div className="data-panel-header" style={{ padding: '20px 24px', borderBottom: '1px solid #e5e7eb' }}>
          <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#1f2937', margin: 0 }}>Active Customers ({customers.length})</h2>
        </div>
        <div className="admin-table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Customer ID</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Country</th>
                <th style={{ textAlign: 'center' }}>Active Subscriptions</th>
                <th style={{ textAlign: 'right' }}>Monthly Spend Contribution</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>Loading ARPU records...</td>
                </tr>
              ) : customers.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>No active customers found.</td>
                </tr>
              ) : (
                customers.map(cust => (
                  <tr key={cust.customerId}>
                    <td style={{ fontWeight: 600, color: '#1e293b' }}>CUST-{cust.customerId}</td>
                    <td><span style={{ fontWeight: 600 }}>{cust.fullName}</span></td>
                    <td style={{ color: '#64748b' }}>{cust.email}</td>
                    <td>
                      <span style={{ fontSize: '12px', fontWeight: 600, padding: '2px 6px', borderRadius: '4px', backgroundColor: '#e2e8f0', color: '#475569' }}>
                        {cust.country || 'US'}
                      </span>
                    </td>
                    <td style={{ textAlign: 'center', fontWeight: 600, color: '#475569' }}>{cust.activeSubscriptionsCount}</td>
                    <td style={{ textAlign: 'right', fontWeight: 700, color: '#3b82f6' }}>{formatCurrencyRow(cust.monthlyContributionMinor, cust.currency)}</td>
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
