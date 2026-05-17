import React, { useEffect, useState } from 'react';
import { DollarSign, UserCheck, BarChart3, Clock } from 'lucide-react';
import { RevenueTrendChart } from '../../components/finance/charts/revenueTrendchart';
import { AnalyticsChartCard } from '../../components/finance/components/analyticsChartCards';
import { PageHeader } from '../../components/admin/shared/PageHeader';
import { financeService } from '../../services/finance/financeService';
import type { ARPUAnalytics } from '../../types/financeTypes';
import { FinanceMetricCard } from '../../components/finance/components/financeMetricCards';

export const ARPUAnalyticsPage: React.FC = () => {
  const [data, setData] = useState<ARPUAnalytics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const result = await financeService.getARPUAnalytics();
        setData(result);
      } catch (error) {
        console.error('Error fetching ARPU data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading || !data) return <div style={{ color: '#9ca3af', padding: '40px', textAlign: 'center', fontFamily: 'Inter, sans-serif' }}>Loading ARPU data...</div>;

  return (
    <>
      <PageHeader subtitle="Average Revenue Per User and Lifetime Value insights." />

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '32px' }}>
        <FinanceMetricCard title="ARPU" metric={{ value: data.arpu, trend: 2.1, trendDirection: 'up' }} icon={<DollarSign />} prefix="₹" formatter={(v) => v.toFixed(2)} />
        <FinanceMetricCard title="LTV" metric={{ value: data.ltv, trend: 5.4, trendDirection: 'up' }} icon={<UserCheck />} prefix="₹" />
        
        {/* CAC:LTV Standard Custom Card */}
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
              CAC:LTV Ratio
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
              <BarChart3 size={18} />
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
            {data.cacLtvRatio}
          </div>

          <span
            style={{
              fontFamily: 'Inter, sans-serif',
              fontSize: '12px',
              fontWeight: 500,
              color: '#16a34a',
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
            }}
          >
            ↑ Healthy
          </span>
        </div>

        {/* CAC Payback Period Standard Custom Card */}
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
              Payback Period
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
              <Clock size={18} />
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
            8.2 Months
          </div>

          <span
            style={{
              fontFamily: 'Inter, sans-serif',
              fontSize: '12px',
              fontWeight: 500,
              color: '#16a34a',
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
            }}
          >
            ↑ Excellent
          </span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '24px' }}>
        <AnalyticsChartCard title="ARPU Trend">
          <RevenueTrendChart
            data={data.history.map(d => ({ date: d.date, value: d.value }))}
            color="#f59e0b"
          />
        </AnalyticsChartCard>
      </div>
    </>
  );
};
