import React from 'react';
import { CheckCircle2, XCircle, Clock, RefreshCcw, HelpCircle, FileEdit, Ban } from 'lucide-react';

interface Props {
  status: string;
}

export const StatusBadge: React.FC<Props> = ({ status }) => {
  const s = status?.toUpperCase() || '';
  
  let label = status;
  let bg = '#f1f5f9';
  let color = '#64748b';
  let border = '1px solid #e2e8f0';
  let icon = <HelpCircle size={13} />;

  switch (s) {
    // Active / Success states - Premium Green
    case 'PAID':
    case 'SUCCESS':
    case 'SUCCESSFUL':
    case 'APPROVED':
    case 'ACTIVE':
    case 'COMPLETED':
    case 'APPLIED':
      label = s === 'SUCCESS' ? 'Success' : s === 'PAID' ? 'Paid' : s.toLowerCase();
      bg = '#ecfdf5';
      color = '#059669';
      border = '1px solid #a7f3d0';
      icon = <CheckCircle2 size={13} />;
      break;

    // Warning / Pending states - Modern Amber/Orange
    case 'PENDING':
    case 'PROCESSING':
    case 'OPEN':
    case 'ISSUED':
    case 'TRIALING':
      label = s.toLowerCase();
      bg = '#fffbeb';
      color = '#d97706';
      border = '1px solid #fde68a';
      icon = <Clock size={13} />;
      break;

    // Danger / Failed states - Premium Crimson/Red
    case 'FAILED':
    case 'REJECTED':
    case 'CANCELED':
    case 'OVERDUE':
    case 'UNCOLLECTIBLE':
      label = s === 'UNCOLLECTIBLE' ? 'Uncollectible' : s.toLowerCase();
      bg = '#fef2f2';
      color = '#dc2626';
      border = '1px solid #fecaca';
      icon = <XCircle size={13} />;
      break;

    // Refunded / Neutral Alert states - Premium Purple
    case 'REFUNDED':
    case 'PARTIALLY_REFUNDED':
      label = s === 'PARTIALLY_REFUNDED' ? 'Partial Refund' : 'Refunded';
      bg = '#faf5ff';
      color = '#7c3aed';
      border = '1px solid #e9d5ff';
      icon = <RefreshCcw size={13} />;
      break;

    // Draft / Info states - Modern Indigo/Slate
    case 'DRAFT':
      label = 'Draft';
      bg = '#eef2ff';
      color = '#4f46e5';
      border = '1px solid #c7d2fe';
      icon = <FileEdit size={13} />;
      break;

    // Canceled / Voided states - Neutral Gray
    case 'VOID':
    case 'VOIDED':
    case 'INACTIVE':
      label = s === 'VOID' || s === 'VOIDED' ? 'Void' : s.toLowerCase();
      bg = '#f8fafc';
      color = '#94a3b8';
      border = '1px solid #e2e8f0';
      icon = <Ban size={13} />;
      break;

    default:
      bg = '#f1f5f9';
      color = '#64748b';
      border = '1px solid #e2e8f0';
      icon = <HelpCircle size={13} />;
  }

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '6px',
        padding: '5px 12px',
        borderRadius: '99px',
        fontSize: '12px',
        fontWeight: 600,
        fontFamily: "'Inter', sans-serif",
        textTransform: 'capitalize',
        backgroundColor: bg,
        color: color,
        border: border,
        boxShadow: '0 1px 2px rgba(0, 0, 0, 0.02)',
        transition: 'all 0.2s ease',
      }}
      className="finance-status-badge"
    >
      {icon}
      <span>{label}</span>
    </span>
  );
};
