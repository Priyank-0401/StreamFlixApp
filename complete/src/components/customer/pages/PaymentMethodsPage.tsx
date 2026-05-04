import React, { useEffect, useState } from 'react';
import { CreditCard, Plus, Trash2, Star, AlertCircle, Check } from 'lucide-react';
import * as customerService from '../../../services/customer/customerService';
import type { PaymentMethod } from '../../../services/customer/customerService';

export const PaymentMethodsPage: React.FC = () => {
  const [methods, setMethods] = useState<PaymentMethod[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showAddForm, setShowAddForm] = useState(false);
  const [newMethod, setNewMethod] = useState({
    paymentType: 'CARD' as 'CARD' | 'UPI',
    cardNumber: '',
    expiryMonth: '',
    expiryYear: '',
    cvv: '',
    upiId: '',
    isDefault: false
  });

  useEffect(() => { loadMethods(); }, []);

  const loadMethods = async () => {
    try {
      setLoading(true);
      const data = await customerService.getPaymentMethods();
      setMethods(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load payment methods');
    } finally {
      setLoading(false);
    }
  };

  const handleSetDefault = async (id: number) => {
    try {
      await customerService.setDefaultPaymentMethod(id);
      await loadMethods();
      setSuccess('Default payment method updated');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this payment method?')) return;
    try {
      await customerService.deletePaymentMethod(id);
      await loadMethods();
      setSuccess('Payment method deleted');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await customerService.addPaymentMethod({
        paymentType: newMethod.paymentType,
        cardNumber: newMethod.paymentType === 'CARD' ? newMethod.cardNumber : undefined,
        expiryMonth: newMethod.paymentType === 'CARD' ? parseInt(newMethod.expiryMonth) : undefined,
        expiryYear: newMethod.paymentType === 'CARD' ? parseInt(newMethod.expiryYear) : undefined,
        cvv: newMethod.paymentType === 'CARD' ? newMethod.cvv : undefined,
        upiId: newMethod.paymentType === 'UPI' ? newMethod.upiId : undefined,
        isDefault: newMethod.isDefault
      });
      setShowAddForm(false);
      setNewMethod({ paymentType: 'CARD', cardNumber: '', expiryMonth: '', expiryYear: '', cvv: '', upiId: '', isDefault: false });
      await loadMethods();
      setSuccess('Payment method added successfully');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const getCardIcon = (brand?: string) => {
    const icons: Record<string, string> = { visa: 'VISA', mastercard: 'MC', amex: 'AMEX', default: 'CARD' };
    return icons[brand?.toLowerCase() || 'default'];
  };

  if (loading) return <div className="payment-loading"><div className="spinner" /></div>;

  return (
    <div className="payment-methods-page">
      {error && <div className="alert alert-error"><AlertCircle size={18} /> {error}</div>}
      {success && <div className="alert alert-success"><Check size={18} /> {success}</div>}

      <div className="card">
        <div className="card-header">
          <div><h2 className="card-title">Payment Methods</h2><p className="card-subtitle">Manage your payment options</p></div>
          <button className="btn btn-primary" onClick={() => setShowAddForm(true)}><Plus size={16} /> Add New</button>
        </div>

        {methods.length === 0 ? (
          <div className="empty-state"><CreditCard size={40} /><h3>No payment methods</h3><p>Add a payment method to start your subscription</p></div>
        ) : (
          <div className="payment-methods-list">
            {methods.map(method => (
              <div key={method.paymentMethodId} className={`payment-method-card ${method.isDefault ? 'default' : ''}`}>
                <div className="method-icon">{method.paymentType === 'CARD' ? <CreditCard size={24} /> : <span className="upi-icon">UPI</span>}</div>
                <div className="method-info">
                  {method.paymentType === 'CARD' ? (
                    <>
                      <h4>{getCardIcon(method.cardBrand ?? undefined)} •••• {method.cardLast4 ?? '****'}</h4>
                      <p>Expires {method.expiryMonth ?? '--'}/{method.expiryYear ?? '--'}</p>
                    </>
                  ) : (
                    <><h4>UPI</h4><p>{method.upiId || 'Linked'}</p></>
                  )}
                </div>
                <div className="method-actions">
                  {method.isDefault ? <span className="badge badge-success"><Star size={12} /> Default</span> : (
                    <button className="btn btn-outline btn-sm" onClick={() => handleSetDefault(method.paymentMethodId)}>Set Default</button>
                  )}
                  <button className="btn btn-danger btn-sm" onClick={() => handleDelete(method.paymentMethodId)}><Trash2 size={14} /></button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {showAddForm && (
        <div className="modal-overlay" onClick={() => setShowAddForm(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header"><h3>Add Payment Method</h3></div>
            <form onSubmit={handleAdd}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Type</label>
                  <select className="form-select" value={newMethod.paymentType} onChange={e => setNewMethod({...newMethod, paymentType: e.target.value as 'CARD' | 'UPI'})}>
                    <option value="CARD">Credit/Debit Card</option>
                    <option value="UPI">UPI</option>
                  </select>
                </div>
                {newMethod.paymentType === 'CARD' ? (
                  <>
                    <div className="form-group"><label className="form-label">Card Number</label><input className="form-input" placeholder="1234 5678 9012 3456" value={newMethod.cardNumber} onChange={e => setNewMethod({...newMethod, cardNumber: e.target.value})} maxLength={16} /></div>
                    <div className="form-row" style={{display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem'}}>
                      <div className="form-group"><label className="form-label">Month</label><input className="form-input" placeholder="MM" value={newMethod.expiryMonth} onChange={e => setNewMethod({...newMethod, expiryMonth: e.target.value})} maxLength={2} /></div>
                      <div className="form-group"><label className="form-label">Year</label><input className="form-input" placeholder="YYYY" value={newMethod.expiryYear} onChange={e => setNewMethod({...newMethod, expiryYear: e.target.value})} maxLength={4} /></div>
                      <div className="form-group"><label className="form-label">CVV</label><input className="form-input" type="password" placeholder="123" value={newMethod.cvv} onChange={e => setNewMethod({...newMethod, cvv: e.target.value})} maxLength={4} /></div>
                    </div>
                  </>
                ) : (
                  <div className="form-group"><label className="form-label">UPI ID</label><input className="form-input" placeholder="name@upi" value={newMethod.upiId} onChange={e => setNewMethod({...newMethod, upiId: e.target.value})} /></div>
                )}
                <div className="form-group" style={{display: 'flex', alignItems: 'center', gap: '0.5rem'}}>
                  <input type="checkbox" id="isDefault" checked={newMethod.isDefault} onChange={e => setNewMethod({...newMethod, isDefault: e.target.checked})} />
                  <label htmlFor="isDefault">Set as default payment method</label>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline" onClick={() => setShowAddForm(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Add Payment Method</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
