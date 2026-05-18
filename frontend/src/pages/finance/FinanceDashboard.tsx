import React, { useEffect, useState } from 'react';
import {
  TrendingUp, TrendingDown, Users, DollarSign, Activity
} from 'lucide-react';
import { DonutChart } from '../../components/finance/charts/donut';
import { RevenueTrendChart } from '../../components/finance/charts/revenueTrendchart';
import { AnalyticsChartCard } from '../../components/finance/components/analyticsChartCards';
import { ExportButton } from '../../components/finance/components/exportButton';
import { PageHeader } from '../../components/admin/shared/PageHeader';
import { financeService } from '../../services/finance/financeService';
import type { DashboardOverview } from '../../types/financeTypes';
import { FinanceMetricCard } from '../../components/finance/components/financeMetricCards';

export const FinanceDashboard: React.FC = () => {
  const [data, setData] = useState<DashboardOverview | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const result = await financeService.getDashboardOverview();
        setData(result);
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading || !data) {
    return <div style={{ color: '#9ca3af', padding: '40px', textAlign: 'center', fontFamily: 'Inter, sans-serif' }}>Loading finance data...</div>;
  }

  return (
    <>
      <PageHeader subtitle="Key financial metrics and revenue analytics." />

      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '20px' }}>
        <ExportButton type="Finance Overview" />
      </div>

      {/* Row 1: Core Revenue Metrics */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '20px' }}>
        <FinanceMetricCard title="Monthly Recurring Revenue" metric={data.mrr} icon={<TrendingUp />} prefix="₹" />
        <FinanceMetricCard title="Annual Recurring Revenue" metric={data.arr} icon={<Activity />} prefix="₹" />
        <FinanceMetricCard title="Avg Revenue Per User" metric={data.arpu} icon={<DollarSign />} prefix="₹" />
        <FinanceMetricCard title="Lifetime Value" metric={data.ltv} icon={<TrendingUp />} prefix="₹" />
      </div>

      {/* Row 2: Health Metrics */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '32px' }}>
        <FinanceMetricCard title="Churn Rate" metric={data.churnRate} icon={<TrendingDown />} suffix="%" />
        <FinanceMetricCard title="Active Customers" metric={data.activeCustomers} icon={<Users />} />
        <FinanceMetricCard title="Failed Payments" metric={data.failedPayments} icon={<TrendingDown />} />
        <FinanceMetricCard title="Total Refunds" metric={data.refundAmount} icon={<DollarSign />} prefix="₹" />
      </div>

      {/* Charts Row */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', marginBottom: '24px' }}>
        <AnalyticsChartCard title="Revenue by Plan">
          <DonutChart data={data.revenueByPlan} />
        </AnalyticsChartCard>

        <AnalyticsChartCard title="Revenue by Region">
          <DonutChart data={data.revenueByRegion} />
        </AnalyticsChartCard>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '24px' }}>
        <AnalyticsChartCard title="Revenue Growth">
          <RevenueTrendChart data={data.revenueTrend} />
        </AnalyticsChartCard>
      </div>
    </>
  );
};
