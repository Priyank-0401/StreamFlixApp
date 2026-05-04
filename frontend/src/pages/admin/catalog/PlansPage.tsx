import React, { useState, useEffect } from 'react';
import { PageHeader } from '../../../components/admin/shared/PageHeader';
import { DataTable } from '../../../components/admin/shared/DataTable';
import { StatusBadge } from '../../../components/admin/shared/StatusBadge';
import { AdminModal } from '../../../components/admin/shared/AdminModal';
import { FormField } from '../../../components/admin/shared/FormField';
import { getAllPlans, createPlan, updatePlan, togglePlanStatus } from '../../../services/admin/adminService';
import type { PlanResponse } from '../../../services/admin/adminTypes';

const formatPrice = (minor: number, cur: string) => {
  const amt = minor / 100;
  if (cur === 'INR') return `₹${amt.toLocaleString('en-IN')}`;
  if (cur === 'USD') return `$${amt.toFixed(2)}`;
  return `${cur} ${amt}`;
};

const emptyForm = { name: '', billingPeriod: 'MONTHLY', defaultPriceMinor: '', defaultCurrency: 'INR', trialDays: '7', taxMode: 'EXCLUSIVE', effectiveFrom: new Date().toISOString().split('T')[0] };

export const PlansPage: React.FC = () => {
  const [plans, setPlans] = useState<PlanResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const load = () => { getAllPlans().then(setPlans).catch(console.error).finally(() => setLoading(false)); };
  useEffect(() => { load(); }, []);

  const openCreate = () => { setForm(emptyForm); setEditId(null); setModalOpen(true); };
  const openEdit = (row: PlanResponse) => {
    setForm({ name: row.name, billingPeriod: row.billingPeriod, defaultPriceMinor: String(row.defaultPriceMinor / 100), defaultCurrency: row.defaultCurrency, trialDays: String(row.trialDays), taxMode: row.taxMode, effectiveFrom: row.effectiveFrom || '' });
    setEditId(row.id); setModalOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    const payload = { ...form, defaultPriceMinor: Math.round(Number(form.defaultPriceMinor) * 100), trialDays: Number(form.trialDays), setupFeeMinor: 0, product: { id: 1 } };
    try {
      if (editId) await updatePlan(editId, payload);
      else await createPlan(payload);
      setModalOpen(false); load();
    } catch (e: any) { alert(e.message); }
    finally { setSaving(false); }
  };

  const handleToggle = async (id: number) => { await togglePlanStatus(id); load(); };

  const columns = [
    { key: 'name', header: 'Plan Name', render: (r: PlanResponse) => <span style={{ fontWeight: 600, color: '#1f2937', fontFamily: 'Inter, sans-serif' }}>{r.name}</span> },
    { key: 'billingPeriod', header: 'Billing' },
    { key: 'defaultPriceMinor', header: 'Price', render: (r: PlanResponse) => formatPrice(r.defaultPriceMinor, r.defaultCurrency) },
    { key: 'trialDays', header: 'Trial', render: (r: PlanResponse) => `${r.trialDays} days` },
    { key: 'status', header: 'Status', render: (r: PlanResponse) => <StatusBadge status={r.status} /> },
    { key: 'actions', header: 'Actions', render: (r: PlanResponse) => (
      <div className="table-actions">
        <button className={`btn-admin-sm ${r.status === 'ACTIVE' ? 'btn-toggle-active' : 'btn-toggle-inactive'}`} onClick={(e) => { e.stopPropagation(); handleToggle(r.id); }}>
          {r.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
        </button>
      </div>
    )},
  ];

  if (loading) return <div style={{ color: '#9ca3af', padding: '40px', textAlign: 'center', fontFamily: 'Inter, sans-serif' }}>Loading plans...</div>;

  return (
    <>
      <PageHeader title="Plans" subtitle="Create and manage subscription plans." actionLabel="Create Plan" onAction={openCreate} />
      <div className="data-panel">
        <DataTable columns={columns} data={plans} emptyMessage="No plans created yet." onRowClick={openEdit} />
      </div>

      <AdminModal isOpen={modalOpen} title={editId ? 'Edit Plan' : 'Create Plan'} onClose={() => setModalOpen(false)} onSave={handleSave} saving={saving}>
        <FormField label="Plan Name" value={form.name} onChange={(v) => setForm({ ...form, name: v })} required placeholder="e.g. Basic Monthly" />
        <div className="form-row">
          <FormField label="Billing Period" value={form.billingPeriod} onChange={(v) => setForm({ ...form, billingPeriod: v })} options={[{ value: 'MONTHLY', label: 'Monthly' }, { value: 'YEARLY', label: 'Yearly' }]} />
          <FormField label="Currency" value={form.defaultCurrency} onChange={(v) => setForm({ ...form, defaultCurrency: v })} options={[{ value: 'INR', label: 'INR (₹)' }, { value: 'USD', label: 'USD ($)' }, { value: 'GBP', label: 'GBP (£)' }]} />
        </div>
        <div className="form-row">
          <FormField label="Price" value={form.defaultPriceMinor} onChange={(v) => setForm({ ...form, defaultPriceMinor: v })} type="number" placeholder="199" required />
          <FormField label="Trial Days" value={form.trialDays} onChange={(v) => setForm({ ...form, trialDays: v })} type="number" />
        </div>
        <div className="form-row">
          <FormField label="Tax Mode" value={form.taxMode} onChange={(v) => setForm({ ...form, taxMode: v })} options={[{ value: 'EXCLUSIVE', label: 'Exclusive' }, { value: 'INCLUSIVE', label: 'Inclusive' }]} />
          <FormField label="Effective From" value={form.effectiveFrom} onChange={(v) => setForm({ ...form, effectiveFrom: v })} type="date" />
        </div>
      </AdminModal>
    </>
  );
};
