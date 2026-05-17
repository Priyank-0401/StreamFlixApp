import React from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface Props {
  data: Array<{ date: string; value: number }>;
  color?: string;
  valuePrefix?: string;
}

export const RevenueTrendChart: React.FC<Props> = ({ data, color = '#5b4fff', valuePrefix = '₹' }) => {
  const formattedData = data.map(item => ({
    ...item,
    formattedDate: new Date(item.date).toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
  }));

  // Unique linear gradient ID based on color to prevent collision across multiple charts
  const gradientId = `colorValue-${color.replace('#', '')}`;
  const shadowId = `shadow-${color.replace('#', '')}`;

  return (
    <ResponsiveContainer width="100%" height="100%">
      <AreaChart data={formattedData} margin={{ top: 15, right: 15, left: -10, bottom: 5 }}>
        <defs>
          <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor={color} stopOpacity={0.45} />
            <stop offset="95%" stopColor={color} stopOpacity={0.01} />
          </linearGradient>
          <filter id={shadowId} x="-5%" y="-5%" width="110%" height="115%">
            <feDropShadow dx="0" dy="8" stdDeviation="6" floodColor={color} floodOpacity="0.22" />
          </filter>
        </defs>
        <CartesianGrid strokeDasharray="4 4" vertical={false} stroke="#f3f4f6" />
        <XAxis dataKey="formattedDate" axisLine={false} tickLine={false} tick={{ fontSize: 12, fontWeight: 500, fill: '#9ca3af' }} dy={10} />
        <YAxis 
          axisLine={false} 
          tickLine={false} 
          tick={{ fontSize: 12, fontWeight: 500, fill: '#9ca3af' }} 
          tickFormatter={(val) => `${valuePrefix}${val >= 100000 ? (val / 1000).toFixed(0) + 'k' : val >= 1000 ? (val / 1000).toFixed(1) + 'k' : val}`} 
        />
        <Tooltip
          contentStyle={{ borderRadius: '12px', border: 'none', background: '#1e293b', color: '#ffffff', boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)', padding: '8px 12px' }}
          labelStyle={{ color: '#94a3b8', fontSize: '11px', fontWeight: 500, marginBottom: '4px' }}
          itemStyle={{ color: '#ffffff', fontSize: '13px', fontWeight: 600 }}
          formatter={(value: any) => [`${valuePrefix}${Number(value).toLocaleString()}`, 'Amount']}
        />
        <Area 
          type="monotone" 
          dataKey="value" 
          stroke={color} 
          fillOpacity={1} 
          fill={`url(#${gradientId})`} 
          strokeWidth={3.5} 
          filter={`url(#${shadowId})`}
          activeDot={{ r: 6, stroke: '#ffffff', strokeWidth: 2, fill: color }} 
        />
      </AreaChart>
    </ResponsiveContainer>
  );
};
