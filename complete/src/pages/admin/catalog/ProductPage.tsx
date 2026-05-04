import React, { useState, useEffect } from 'react';
import { PageHeader } from '../../../components/admin/shared/PageHeader';
import { StatusBadge } from '../../../components/admin/shared/StatusBadge';
import { AdminModal } from '../../../components/admin/shared/AdminModal';
import { FormField } from '../../../components/admin/shared/FormField';
import { Package } from 'lucide-react';
import { getAllProducts, updateProduct, toggleProductStatus } from '../../../services/admin/adminService';
import type { Product } from '../../../services/admin/adminTypes';

export const ProductPage: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState<Product | null>(null);
  const [form, setForm] = useState({ name: '', description: '' });
  const [saving, setSaving] = useState(false);

  const load = () => {
    getAllProducts().then(setProducts).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const openEdit = (p: Product) => {
    setForm({ name: p.name, description: p.description });
    setEditing(p);
  };

  const handleSave = async () => {
    if (!editing) return;
    setSaving(true);
    try {
      await updateProduct(editing.id, form);
      setEditing(null);
      load();
    } catch (e: any) { alert(e.message); }
    finally { setSaving(false); }
  };

  const handleToggle = async (id: number) => {
    await toggleProductStatus(id);
    load();
  };

  if (loading) return <div style={{ color: '#9ca3af', padding: '40px', textAlign: 'center', fontFamily: 'Inter, sans-serif' }}>Loading products...</div>;

  return (
    <>
      <PageHeader title="Product Management" subtitle="Manage the StreamFlix streaming platform product." />
      <div className="data-panel">
        {products.length === 0 ? (
          <div className="empty-state">
            <Package size={40} className="empty-state-icon" />
            <div className="empty-state-text">No products found</div>
          </div>
        ) : (
          <table className="admin-table" style={{ width: '100%' }}>
            <thead>
              <tr>
                <th style={{ padding: '16px 20px', fontSize: '10px', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.15em', color: '#6b7280', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Product</th>
                <th style={{ padding: '16px 20px', fontSize: '10px', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.15em', color: '#6b7280', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Status</th>
                <th style={{ padding: '16px 20px', fontSize: '10px', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.15em', color: '#6b7280', textAlign: 'right', borderBottom: '1px solid #e5e7eb' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td style={{ padding: '20px', borderBottom: '1px solid #e5e7eb' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                      <div style={{ width: '48px', height: '48px', border: '1px solid #e5e7eb', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#5b4fff', backgroundColor: 'rgba(91, 79, 255, 0.05)' }}>
                        <Package size={24} />
                      </div>
                      <div>
                        <div style={{ fontSize: '16px', fontWeight: 500, color: '#1f2937', fontFamily: 'Inter, sans-serif' }}>{product.name}</div>
                        <div style={{ fontSize: '13px', color: '#6b7280', marginTop: '2px' }}>{product.description}</div>
                      </div>
                    </div>
                  </td>
                  <td style={{ padding: '20px', borderBottom: '1px solid #e5e7eb' }}>
                    <StatusBadge status={product.status} />
                  </td>
                  <td style={{ padding: '20px', borderBottom: '1px solid rgba(18,18,18,0.08)', textAlign: 'right' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '12px' }}>
                      <button 
                        onClick={() => handleToggle(product.id)}
                        style={{ 
                          padding: '8px 16px', 
                          border: '1px solid #e5e7eb', 
                          background: 'transparent', 
                          color: '#6b7280', 
                          fontSize: '11px', 
                          fontWeight: 600, 
                          textTransform: 'uppercase', 
                          letterSpacing: '0.1em',
                          cursor: 'pointer',
                          transition: 'all 0.15s ease'
                        }}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.borderColor = '#5b4fff';
                          e.currentTarget.style.color = '#5b4fff';
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.borderColor = '#e5e7eb';
                          e.currentTarget.style.color = '#6b7280';
                        }}
                      >
                        {product.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                      </button>
                      <button 
                        onClick={() => openEdit(product)}
                        style={{ 
                          padding: '8px 16px', 
                          border: '1px solid #5b4fff', 
                          background: 'transparent', 
                          color: '#5b4fff', 
                          fontSize: '11px', 
                          fontWeight: 600, 
                          textTransform: 'uppercase', 
                          letterSpacing: '0.1em',
                          cursor: 'pointer',
                          transition: 'all 0.15s ease'
                        }}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.background = '#5b4fff';
                          e.currentTarget.style.color = '#FFFFFF';
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.background = 'transparent';
                          e.currentTarget.style.color = '#5b4fff';
                        }}
                      >
                        Edit
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <AdminModal isOpen={!!editing} title="Edit Product" onClose={() => setEditing(null)} onSave={handleSave} saving={saving}>
        <FormField label="Product Name" value={form.name} onChange={(v) => setForm({ ...form, name: v })} required />
        <FormField label="Description" value={form.description} onChange={(v) => setForm({ ...form, description: v })} />
      </AdminModal>
    </>
  );
};
