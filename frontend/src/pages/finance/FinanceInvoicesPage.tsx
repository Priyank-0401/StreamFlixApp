import React, { useState, useEffect } from 'react';
import { PageHeader } from '../../components/admin/shared/PageHeader';
import { StatusBadge } from '../../components/admin/shared/StatusBadge';
import { getFinanceInvoices, type InvoiceDTO } from '../../services/finance/financeService';

export const FinanceInvoicesPage: React.FC = () => {
  const [invoices, setInvoices] = useState<InvoiceDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>('');

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        const data = await getFinanceInvoices(statusFilter);
        setInvoices(data);
      } catch (err) {
        console.error('Failed to load invoices:', err);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [statusFilter]);

  const formatCurrency = (minor: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(minor / 100);
  };

  return (
    <>
      <PageHeader subtitle="Invoice aging, collections tracking, and billing status center" />

      {/* Filter Toolbar */}
      <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
        {['', 'PAID', 'OPEN', 'DRAFT', 'VOID', 'UNCOLLECTIBLE'].map(st => (
          <button
            key={st}
            onClick={() => setStatusFilter(st)}
            style={{
              padding: '8px 16px',
              borderRadius: '8px',
              border: '1px solid #e2e8f0',
              background: statusFilter === st ? '#10b981' : '#ffffff',
              color: statusFilter === st ? '#ffffff' : '#475569',
              fontWeight: 600,
              fontSize: '13px',
              cursor: 'pointer',
              transition: 'all 0.2s',
            }}
          >
            {st === '' ? 'All Invoices' : st}
          </button>
        ))}
      </div>

      <div className="data-panel">
        <div className="data-panel-header" style={{ padding: '20px 24px', borderBottom: '1px solid #e5e7eb' }}>
          <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#1f2937', margin: 0 }}>Invoices Center ({invoices.length})</h2>
        </div>
        <div className="admin-table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Invoice Number</th>
                <th>Sub. ID</th>
                <th>Issue Date</th>
                <th>Due Date</th>
                <th style={{ textAlign: 'center' }}>Status</th>
                <th style={{ textAlign: 'right' }}>Total</th>
                <th style={{ textAlign: 'right' }}>Balance</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>Loading invoice aging records...</td>
                </tr>
              ) : invoices.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>No invoices found.</td>
                </tr>
              ) : (
                invoices.map(inv => (
                  <tr key={inv.invoiceId}>
                    <td style={{ fontWeight: 600, color: '#1e293b' }}>{inv.invoiceNumber}</td>
                    <td><span style={{ fontWeight: 500 }}>SUB-{inv.subscriptionId}</span></td>
                    <td style={{ color: '#64748b', fontSize: '13px' }}>{inv.issueDate}</td>
                    <td style={{ color: '#64748b', fontSize: '13px' }}>{inv.dueDate || 'N/A'}</td>
                    <td style={{ textAlign: 'center' }}>
                      <StatusBadge status={inv.status} />
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 600 }}>{formatCurrency(inv.totalMinor)}</td>
                    <td style={{ textAlign: 'right', fontWeight: 700, color: inv.balanceMinor > 0 ? '#f59e0b' : '#10b981' }}>
                      {formatCurrency(inv.balanceMinor)}
                    </td>
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
