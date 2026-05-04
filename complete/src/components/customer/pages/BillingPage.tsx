import React, { useEffect, useState } from 'react';
import { Download, Eye, CreditCard, AlertCircle, Check, FileText } from 'lucide-react';
import * as customerService from '../../../services/customer/customerService';
import type { Invoice, Payment, CreditNote } from '../../../services/customer/customerService';

export const BillingPage: React.FC = () => {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [creditNotes, setCreditNotes] = useState<CreditNote[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
  const [activeTab, setActiveTab] = useState<'invoices' | 'payments' | 'credits'>('invoices');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [invData, payData, creditData] = await Promise.all([
        customerService.getInvoices(),
        customerService.getPayments(),
        customerService.getCreditNotes()
      ]);
      setInvoices(invData);
      setPayments(payData);
      setCreditNotes(creditData);
    } catch (err: any) {
      setError(err.message || 'Failed to load billing data');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amountMinor: number, currency: string) => {
    const amount = amountMinor / 100;
    return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  };

  const handleDownload = async (invoiceId: number) => {
    try {
      const blob = await customerService.downloadInvoice(invoiceId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `invoice-${invoiceId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err: any) {
      setError('Failed to download invoice');
    }
  };

  const getStatusBadge = (status: string) => {
    const classes: Record<string, string> = {
      PAID: 'badge-success',
      OPEN: 'badge-warning',
      VOID: 'badge-error',
      UNCOLLECTIBLE: 'badge-error',
      SUCCESS: 'badge-success',
      FAILED: 'badge-error',
      PENDING: 'badge-warning',
      REFUNDED: 'badge-info',
      ISSUED: 'badge-success',
      APPLIED: 'badge-success'
    };
    return <span className={`badge ${classes[status] || 'badge-info'}`}>{status}</span>;
  };

  if (loading) return <div className="billing-loading"><div className="spinner" /></div>;

  return (
    <div className="billing-page">
      {error && <div className="alert alert-error"><AlertCircle size={18} /> {error}</div>}

      <div className="tabs">
        <button className={`tab ${activeTab === 'invoices' ? 'active' : ''}`} onClick={() => setActiveTab('invoices')}>
          Invoices ({invoices.length})
        </button>
        <button className={`tab ${activeTab === 'payments' ? 'active' : ''}`} onClick={() => setActiveTab('payments')}>
          Payments ({payments.length})
        </button>
        <button className={`tab ${activeTab === 'credits' ? 'active' : ''}`} onClick={() => setActiveTab('credits')}>
          Credit Notes ({creditNotes.length})
        </button>
      </div>

      {activeTab === 'invoices' && (
        <div className="card">
          {invoices.length > 0 ? (
            <table className="data-table">
              <thead>
                <tr><th>Invoice #</th><th>Date</th><th>Amount</th><th>Status</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {invoices.map(inv => (
                  <tr key={inv.invoiceId}>
                    <td>{inv.invoiceNumber}</td>
                    <td>{formatDate(inv.issueDate)}</td>
                    <td>{formatCurrency(inv.totalMinor, inv.currency)}</td>
                    <td>{getStatusBadge(inv.status)}</td>
                    <td>
                      <button className="btn btn-sm btn-secondary" onClick={() => setSelectedInvoice(inv)}><Eye size={14} /></button>
                      <button className="btn btn-sm btn-outline" onClick={() => handleDownload(inv.invoiceId)} style={{ marginLeft: '0.5rem' }}><Download size={14} /></button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <div className="empty-state"><FileText size={40} /><h3>No invoices yet</h3></div>}
        </div>
      )}

      {activeTab === 'payments' && (
        <div className="card">
          {payments.length > 0 ? (
            <table className="data-table">
              <thead>
                <tr><th>Payment ID</th><th>Invoice</th><th>Date</th><th>Amount</th><th>Status</th></tr>
              </thead>
              <tbody>
                {payments.map(pay => (
                  <tr key={pay.paymentId}>
                    <td>#{pay.paymentId}</td>
                    <td>{pay.invoiceNumber}</td>
                    <td>{formatDate(pay.createdAt)}</td>
                    <td>{formatCurrency(pay.amountMinor, pay.currency)}</td>
                    <td>{getStatusBadge(pay.status)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <div className="empty-state"><CreditCard size={40} /><h3>No payments yet</h3></div>}
        </div>
      )}

      {activeTab === 'credits' && (
        <div className="card">
          {creditNotes.length > 0 ? (
            <table className="data-table">
              <thead>
                <tr><th>Credit Note #</th><th>Invoice</th><th>Reason</th><th>Amount</th><th>Status</th></tr>
              </thead>
              <tbody>
                {creditNotes.map(cn => (
                  <tr key={cn.creditNoteId}>
                    <td>{cn.creditNoteNumber}</td>
                    <td>{cn.invoiceNumber}</td>
                    <td>{cn.reason}</td>
                    <td>{formatCurrency(cn.amountMinor, 'INR')}</td>
                    <td>{getStatusBadge(cn.status)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <div className="empty-state"><Check size={40} /><h3>No credit notes</h3></div>}
        </div>
      )}

      {selectedInvoice && (
        <div className="modal-overlay" onClick={() => setSelectedInvoice(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header"><h3>Invoice #{selectedInvoice.invoiceNumber}</h3></div>
            <div className="modal-body">
              <div className="invoice-details">
                <div className="detail-row"><span>Issue Date:</span><span>{formatDate(selectedInvoice.issueDate)}</span></div>
                <div className="detail-row"><span>Due Date:</span><span>{selectedInvoice.dueDate ? formatDate(selectedInvoice.dueDate) : 'N/A'}</span></div>
                <div className="detail-row"><span>Status:</span>{getStatusBadge(selectedInvoice.status)}</div>
                <div className="detail-row"><span>Subtotal:</span><span>{formatCurrency(selectedInvoice.subtotalMinor, selectedInvoice.currency)}</span></div>
                <div className="detail-row"><span>Tax:</span><span>{formatCurrency(selectedInvoice.taxMinor, selectedInvoice.currency)}</span></div>
                <div className="detail-row"><span>Discount:</span><span>{formatCurrency(selectedInvoice.discountMinor, selectedInvoice.currency)}</span></div>
                <div className="detail-row total"><span>Total:</span><span>{formatCurrency(selectedInvoice.totalMinor, selectedInvoice.currency)}</span></div>
              </div>
              <h4 style={{ marginTop: '1.5rem', marginBottom: '1rem' }}>Line Items</h4>
              {selectedInvoice.lineItems.map(item => (
                <div key={item.lineItemId} className="line-item">
                  <span>{item.description}</span>
                  <span>{formatCurrency(item.amountMinor, selectedInvoice.currency)}</span>
                </div>
              ))}
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline" onClick={() => setSelectedInvoice(null)}>Close</button>
              <button className="btn btn-primary" onClick={() => handleDownload(selectedInvoice.invoiceId)}><Download size={14} /> Download PDF</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
