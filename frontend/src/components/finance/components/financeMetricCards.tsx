import React from 'react';
import type { RevenueMetric } from '../../../types/financeTypes';

interface Props {
  title: string;
  metric: RevenueMetric;
  icon: React.ReactNode;
  prefix?: string;
  suffix?: string;
  formatter?: (val: number) => string | number;
}

export const FinanceMetricCard: React.FC<Props> = ({ title, metric, icon, prefix = '', suffix = '', formatter }) => {
  const isUp = metric.trendDirection === 'up';
  const displayValue = formatter ? formatter(metric.value) : metric.value.toLocaleString();

  return (
    <div
      style={{
        background: '#ffffff',
        border: '1px solid #e5e7eb',
        borderRadius: '12px',
        padding: '20px',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.05)',
      }}
    >
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
        }}
      >
        <span
          style={{
            fontFamily: 'Inter, sans-serif',
            fontSize: '13px',
            fontWeight: 500,
            color: '#6b7280',
            textTransform: 'uppercase',
            letterSpacing: '0.03em',
          }}
        >
          {title}
        </span>
        <div
          style={{
            width: '36px',
            height: '36px',
            borderRadius: '8px',
            background: '#f3f0ff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#5b4fff',
          }}
        >
          {React.cloneElement(icon as React.ReactElement<any>, { size: 18 })}
        </div>
      </div>
      <div
        style={{
          fontFamily: 'Inter, sans-serif',
          fontSize: '28px',
          fontWeight: 700,
          color: '#1f2937',
          lineHeight: 1,
        }}
      >
        {prefix}{displayValue}{suffix}
      </div>
      <span
        style={{
          fontFamily: 'Inter, sans-serif',
          fontSize: '12px',
          fontWeight: 500,
          color: !isUp ? '#dc2626' : '#16a34a',
          display: 'flex',
          alignItems: 'center',
          gap: '4px',
        }}
      >
        {!isUp ? '↓' : '↑'} {metric.trend}%
      </span>
    </div>
  );
};
