import React, { useState, useEffect } from 'react';
import { PageHeader } from '../../../components/admin/shared/PageHeader';
import { DataTable } from '../../../components/admin/shared/DataTable';
import { StatusBadge } from '../../../components/admin/shared/StatusBadge';
import { AdminModal } from '../../../components/admin/shared/AdminModal';
import { FormField } from '../../../components/admin/shared/FormField';
import { getStaff, createStaff, deleteStaff } from '../../../services/admin/adminService';
import type { StaffResponse } from '../../../services/admin/adminTypes';

const roleStyles: Record<string, { border: string; color: string }> = {
  ADMIN: { border: '#5b4fff', color: '#5b4fff' },
  FINANCE: { border: '#e5e7eb', color: '#6b7280' },
  SUPPORT: { border: '#e5e7eb', color: '#6b7280' }
};
const emptyForm = { fullName: '', email: '', passwordHash: '', role: 'SUPPORT' };

export const StaffAccountsPage: React.FC = () => {
  const [staff, setStaff] = useState<StaffResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const load = () => { getStaff().then(setStaff).catch(console.error).finally(() => setLoading(false)); };
  useEffect(() => { load(); }, []);

  const openCreate = () => { setForm(emptyForm); setModalOpen(true); };

  const handleSave = async () => {
    setSaving(true);
    try {
      await createStaff(form);
      setModalOpen(false); load();
    } catch (e: any) { alert(e.message); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Remove this staff member?')) return;
    await deleteStaff(id); load();
  };

  const columns = [
    { key: 'fullName', header: 'Name', render: (r: StaffResponse) => <span style={{ fontWeight: 600, color: '#1f2937', fontFamily: 'Inter, sans-serif' }}>{r.fullName}</span> },
    { key: 'email', header: 'Email' },
    { key: 'role', header: 'Role', render: (r: StaffResponse) => {
      const style = roleStyles[r.role] || roleStyles.SUPPORT;
      return (
        <span style={{ border: `1px solid ${style.border}`, color: style.color, padding: '4px 10px', fontSize: '11px', fontWeight: 600, textTransform: 'uppercase' as const, letterSpacing: '0.1em' }}>
          {r.role}
        </span>
      );
    }},
    { key: 'status', header: 'Status', render: (r: StaffResponse) => <StatusBadge status={r.status} /> },
    { key: 'actions', header: 'Actions', render: (r: StaffResponse) => (
      <button className="btn-admin-sm btn-delete-sm" onClick={(e) => { e.stopPropagation(); handleDelete(r.id); }}>Remove</button>
    )},
  ];

  if (loading) return <div style={{ color: '#9ca3af', padding: '40px', textAlign: 'center', fontFamily: 'Inter, sans-serif' }}>Loading staff...</div>;

  return (
    <>
      <PageHeader title="Staff Accounts" subtitle="Manage Admin, Finance, and Support staff." actionLabel="Add Staff" onAction={openCreate} />
      <div className="data-panel">
        <DataTable columns={columns} data={staff} emptyMessage="No staff accounts." />
      </div>

      <AdminModal isOpen={modalOpen} title="Add Staff Member" onClose={() => setModalOpen(false)} onSave={handleSave} saving={saving}>
        <FormField label="Full Name" value={form.fullName} onChange={(v) => setForm({ ...form, fullName: v })} required placeholder="John Doe" />
        <FormField label="Email" value={form.email} onChange={(v) => setForm({ ...form, email: v })} type="email" required placeholder="john@streamflix.com" />
        <FormField label="Password" value={form.passwordHash} onChange={(v) => setForm({ ...form, passwordHash: v })} type="password" required placeholder="Minimum 8 characters" />
        <FormField label="Role" value={form.role} onChange={(v) => setForm({ ...form, role: v })} required options={[{ value: 'ADMIN', label: 'Admin' }, { value: 'FINANCE', label: 'Finance' }, { value: 'SUPPORT', label: 'Support' }]} />
      </AdminModal>
    </>
  );
};
