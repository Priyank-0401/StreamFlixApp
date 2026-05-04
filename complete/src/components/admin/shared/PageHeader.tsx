import React from 'react';
import { Plus } from 'lucide-react';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  actionLabel?: string;
  onAction?: () => void;
}

export const PageHeader: React.FC<PageHeaderProps> = ({ title, subtitle, actionLabel, onAction }) => {
  return (
    <div className="page-header">
      <div className="page-header-left">
        <h2>{title}</h2>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {actionLabel && onAction && (
        <button className="btn-admin-primary" onClick={onAction}>
          <Plus size={18} />
          {actionLabel}
        </button>
      )}
    </div>
  );
};
