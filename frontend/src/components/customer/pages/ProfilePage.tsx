import React, { useEffect, useState } from 'react';
import { User, Save, AlertCircle, Check } from 'lucide-react';
import * as customerService from '../../../services/customer/customerService';
import type { CustomerProfile } from '../../../services/customer/customerService';

export const ProfilePage: React.FC = () => {
  const [profile, setProfile] = useState<CustomerProfile | null>(null);
  const [formData, setFormData] = useState<Partial<CustomerProfile>>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      setLoading(true);
      const data = await customerService.getCustomerProfile();
      setProfile(data);
      setFormData(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      const updated = await customerService.updateCustomerProfile(formData);
      setProfile(updated);
      setSuccess('Profile updated successfully');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="profile-loading"><div className="spinner" /></div>;
  if (!profile) return <div className="alert alert-error"><AlertCircle size={18} /> Failed to load profile</div>;

  return (
    <div className="profile-page">
      {error && <div className="alert alert-error"><AlertCircle size={18} /> {error}</div>}
      {success && <div className="alert alert-success"><Check size={18} /> {success}</div>}

      <form onSubmit={handleSubmit}>
        <div className="grid grid-2">
          {/* Personal Info */}
          <div className="card">
            <div className="card-header">
              <div><h2 className="card-title"><User size={20} /> Personal Information</h2></div>
            </div>
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input className="form-input" value={formData.fullName || ''} onChange={e => setFormData({...formData, fullName: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="form-label">Email</label>
              <input className="form-input" type="email" value={formData.email || ''} disabled style={{ opacity: 0.6 }} />
              <small style={{ color: 'var(--text-muted)' }}>Email cannot be changed</small>
            </div>
            <div className="form-group">
              <label className="form-label">Phone</label>
              <input className="form-input" value={formData.phone || ''} onChange={e => setFormData({...formData, phone: e.target.value})} placeholder="+91 98765 43210" />
            </div>
          </div>

          {/* Address */}
          <div className="card">
            <div className="card-header">
              <div><h2 className="card-title">Address</h2></div>
            </div>
            <div className="form-group">
              <label className="form-label">Address Line 1</label>
              <input className="form-input" value={formData.addressLine1 || ''} onChange={e => setFormData({...formData, addressLine1: e.target.value})} placeholder="Street address" />
            </div>
            <div className="form-row" style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem'}}>
              <div className="form-group"><label className="form-label">City</label><input className="form-input" value={formData.city || ''} onChange={e => setFormData({...formData, city: e.target.value})} /></div>
              <div className="form-group"><label className="form-label">State</label><input className="form-input" value={formData.state || ''} onChange={e => setFormData({...formData, state: e.target.value})} /></div>
            </div>
            <div className="form-row" style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem'}}>
              <div className="form-group"><label className="form-label">Postal Code</label><input className="form-input" value={formData.postalCode || ''} onChange={e => setFormData({...formData, postalCode: e.target.value})} /></div>
              <div className="form-group"><label className="form-label">Country</label><input className="form-input" value={formData.country || ''} disabled style={{ opacity: 0.6 }} /></div>
            </div>
          </div>
        </div>

        {/* Billing Preferences */}
        <div className="card" style={{ marginTop: '1.5rem' }}>
          <div className="card-header">
            <div><h2 className="card-title">Billing Preferences</h2></div>
          </div>
          <div className="form-row" style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem'}}>
            <div className="form-group">
              <label className="form-label">Currency</label>
              <input className="form-input" value={formData.currency || ''} disabled style={{ opacity: 0.6 }} />
              <small style={{ color: 'var(--text-muted)' }}>Currency is set based on your region</small>
            </div>
            <div className="form-group">
              <label className="form-label">Customer Since</label>
              <input className="form-input" value={new Date(profile.createdAt).toLocaleDateString()} disabled style={{ opacity: 0.6 }} />
            </div>
          </div>
        </div>

        <div style={{ marginTop: '1.5rem' }}>
          <button type="submit" className="btn btn-primary btn-lg" disabled={saving}>
            {saving ? <><div className="spinner" /> Saving...</> : <><Save size={18} /> Save Changes</>}
          </button>
        </div>
      </form>
    </div>
  );
};
