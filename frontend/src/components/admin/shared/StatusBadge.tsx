import React from 'react';

interface StatusBadgeProps {
  status: string;
}

const statusMap: Record<string, string> = {
  ACTIVE: 'active',
  INACTIVE: 'inactive',
  SUSPENDED: 'suspended',
  TRIALING: 'trialing',
  CANCELED: 'canceled',
  PAST_DUE: 'past_due',
  PAUSED: 'suspended',
  ON_HOLD: 'suspended',
  DRAFT: 'inactive',
  EXPIRED: 'canceled',
  DISABLED: 'canceled',
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const className = statusMap[status?.toUpperCase()] || 'inactive';

  return (
    <span className={`status-badge ${className}`}>
      {status}
    </span>
  );
};
