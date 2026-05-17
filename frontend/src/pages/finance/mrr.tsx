import React, { useEffect, useState } from 'react';
import { TrendingUp, TrendingDown, Plus, RotateCcw } from 'lucide-react';
import { FinanceMetricCard } from '../../components/finance/components/financeMetricCards';
import { RevenueTrendChart } from '../../components/finance/charts/revenueTrendchart';
import { AnalyticsChartCard } from '../../components/finance/components/analyticsChartCards';
import { PageHeader } from '../../components/admin/shared/PageHeader';
import { financeService } from '../../services/finance/financeService';
import type { MRRAnalytics } from '../../types/financeTypes';

export const MRRAnalyticsPage: React.FC = () => {
  const [data, setData] = useState<MRRAnalytics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const result = await financeService.getMRRAnalytics();
        setData(result);
      } catch (error) {
        console.error('Error fetching MRR data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading || !data) return <div style={{ color: '#9ca3af', padding: '40px', textAlign: 'center', fontFamily: 'Inter, sans-serif' }}>Loading MRR data...</div>;

  return (
    <>
      <PageHeader subtitle="Monthly Recurring Revenue breakdown and trends." />

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '16px', marginBottom: '32px' }}>
        <FinanceMetricCard title="Total MRR" metric={{ value: data.total, trend: 15.2, trendDirection: 'up' }} icon={<TrendingUp />} prefix="₹" />
        <FinanceMetricCard title="New Biz MRR" metric={{ value: data.newMRR, trend: 8.4, trendDirection: 'up' }} icon={<Plus />} prefix="₹" />
        <FinanceMetricCard title="Expansion MRR" metric={{ value: data.expansion, trend: 12.1, trendDirection: 'up' }} icon={<TrendingUp />} prefix="₹" />
        <FinanceMetricCard title="Contraction MRR" metric={{ value: data.contraction, trend: 2.1, trendDirection: 'down' }} icon={<TrendingDown />} prefix="₹" />
        <FinanceMetricCard title="Reactivation MRR" metric={{ value: data.reactivation, trend: 5.0, trendDirection: 'up' }} icon={<RotateCcw />} prefix="₹" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '24px' }}>
        <AnalyticsChartCard title="MRR Trend Over Time">
          <RevenueTrendChart data={data.history} color="#10b981" />
        </AnalyticsChartCard>
      </div>
    </>
  );
};
