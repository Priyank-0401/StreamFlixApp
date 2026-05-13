import React, { useState, useEffect } from 'react';
import { TrendingUp, DollarSign, Users, UserX, HeartHandshake, RefreshCw, FileText, PieChart as PieIcon } from 'lucide-react';
import { Link } from 'react-router-dom';
import { PageHeader } from '../../components/admin/shared/PageHeader';
import { StatsCardFinance } from '../../components/finance/dashboard/StatsCardFinance';
import { getFinanceStats, recordSnapshot, downloadRevenueSnapshot, downloadTaxReport, type FinanceStatsResponse } from '../../services/finance/financeService';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';

export const FinanceDashboardPage: React.FC = () => {
  const [stats, setStats] = useState<FinanceStatsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [recording, setRecording] = useState(false);
  const [exporting, setExporting] = useState<string | null>(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const data = await getFinanceStats();
      setStats(data);
    } catch (err) {
      console.error('Failed to load finance stats:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleRecordSnapshot = async () => {
    setRecording(true);
    try {
      await recordSnapshot();
      await loadData();
    } catch (err) {
      console.error('Failed to record snapshot:', err);
    } finally {
      setRecording(false);
    }
  };

  const handleExport = async (type: 'revenue' | 'tax') => {
    setExporting(type);
    try {
      if (type === 'revenue') await downloadRevenueSnapshot();
      else await downloadTaxReport();
    } catch (err) {
      console.error(`Failed to export ${type} report:`, err);
    } finally {
      setExporting(null);
    }
  };

  if (loading) {
    return <div style={{ color: '#9ca3af', padding: '40px', textAlign: 'center', fontFamily: 'Inter, sans-serif' }}>Loading financial analytics...</div>;
  }

  const formatCurrencyINR = (minor: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(minor / 100);
  };

  const chartData = stats?.recentSnapshots?.map(s => ({
    date: s.snapshotDate,
    mrr: s.mrrMinor / 100,
    arr: s.arrMinor / 100,
    arpu: s.arpuMinor / 100,
    churn: s.grossChurnPercent,
  })) || [];

  const collectedMinor = stats?.totalCollectedMinor ?? 0;
  const pendingMinor = stats?.pendingCollectionMinor ?? 0;
  const pieData = [
    { name: 'Collected Revenue', value: collectedMinor / 100, color: '#10b981' },
    { name: 'Pending Outstanding', value: pendingMinor / 100, color: '#f59e0b' },
  ];

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <PageHeader subtitle="Live revenue analytics, recurring projections, and SaaS metrics" />
        <div style={{ display: 'flex', gap: '12px' }}>
          <button
            onClick={() => handleExport('revenue')}
            disabled={exporting !== null}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '10px 16px',
              backgroundColor: '#5b4fff',
              color: 'white',
              border: 'none',
              borderRadius: '12px',
              fontWeight: 600,
              cursor: exporting ? 'not-allowed' : 'pointer',
              boxShadow: '0 4px 12px rgba(91, 79, 255, 0.2)',
              transition: 'all 0.2s',
            }}
          >
            <PieIcon size={16} />
            {exporting === 'revenue' ? 'Generating...' : 'Revenue Snapshot'}
          </button>
          <button
            onClick={() => handleExport('tax')}
            disabled={exporting !== null}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '10px 16px',
              backgroundColor: '#1e293b',
              color: 'white',
              border: 'none',
              borderRadius: '12px',
              fontWeight: 600,
              cursor: exporting ? 'not-allowed' : 'pointer',
              boxShadow: '0 4px 12px rgba(30, 41, 59, 0.2)',
              transition: 'all 0.2s',
            }}
          >
            <FileText size={16} />
            {exporting === 'tax' ? 'Exporting...' : 'Tax Report (CSV)'}
          </button>
          <button
            onClick={handleRecordSnapshot}
            disabled={recording}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '10px 20px',
              backgroundColor: '#10b981',
              color: 'white',
              border: 'none',
              borderRadius: '12px',
              fontWeight: 600,
              cursor: recording ? 'not-allowed' : 'pointer',
              boxShadow: '0 4px 12px rgba(16, 185, 129, 0.2)',
              transition: 'all 0.2s',
            }}
          >
            <RefreshCw size={16} className={recording ? 'spin' : ''} />
            {recording ? 'Recording Snapshot...' : 'Record Daily Snapshot'}
          </button>
        </div>
      </div>

      {/* KPI Stats Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '20px', marginBottom: '32px' }}>
        <Link to="/finance/mrr" style={{ textDecoration: 'none' }}>
          <StatsCardFinance
            label="Monthly Recurring Revenue"
            value={formatCurrencyINR(stats?.mrrMinor ?? 0)}
            icon={<TrendingUp size={20} />}
          />
        </Link>
        <Link to="/finance/arr" style={{ textDecoration: 'none' }}>
          <StatsCardFinance
            label="Annual Recurring Revenue"
            value={formatCurrencyINR(stats?.arrMinor ?? 0)}
            icon={<DollarSign size={20} />}
          />
        </Link>
        <Link to="/finance/arpu" style={{ textDecoration: 'none' }}>
          <StatsCardFinance
            label="Average Revenue Per User"
            value={formatCurrencyINR(stats?.arpuMinor ?? 0)}
            icon={<Users size={20} />}
          />
        </Link>
        <Link to="/finance/churn" style={{ textDecoration: 'none' }}>
          <StatsCardFinance
            label="Churn Rate"
            value={`${stats?.churnRate?.toFixed(2) ?? '0.00'}%`}
            icon={<UserX size={20} />}
            trend={stats?.churnRate && stats.churnRate > 5 ? 'down' : 'up'}
          />
        </Link>
        <StatsCardFinance
          label="Customer Lifetime Value"
          value={formatCurrencyINR(stats?.ltvMinor ?? 0)}
          icon={<HeartHandshake size={20} />}
        />
      </div>

      {/* Top Charts Row: Area Growth & Donut Breakdown */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px', marginBottom: '24px' }}>
        {/* MRR/ARR Growth Chart */}
        <div className="data-panel" style={{ padding: '24px', minWidth: 0, minHeight: 0, overflow: 'hidden' }}>
          <h3 style={{ fontFamily: "'Outfit', sans-serif", fontSize: '18px', fontWeight: 700, color: '#1f2937', marginBottom: '20px', margin: 0 }}>
            Recurring Revenue Growth (INR)
          </h3>
          <div style={{ height: '300px', width: '100%', minWidth: 0 }}>
            <ResponsiveContainer width="99%" height={300}>
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="colorMrr" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.8} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="colorArr" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#5b4fff" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#5b4fff" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                <XAxis dataKey="date" stroke="#94a3b8" fontSize={12} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={12} width={80} tickLine={false} />
                <Tooltip formatter={(val: any) => `₹${Number(val).toLocaleString()}`} />
                <Area type="monotone" dataKey="mrr" stroke="#10b981" strokeWidth={3} fillOpacity={1} fill="url(#colorMrr)" name="MRR (INR)" />
                <Area type="monotone" dataKey="arr" stroke="#5b4fff" strokeWidth={2} fillOpacity={1} fill="url(#colorArr)" name="ARR (INR)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Revenue Collection Donut Chart */}
        <div className="data-panel" style={{ padding: '24px', minWidth: 0, minHeight: 0, overflow: 'hidden' }}>
          <h3 style={{ fontFamily: "'Outfit', sans-serif", fontSize: '18px', fontWeight: 700, color: '#1f2937', marginBottom: '20px', margin: 0 }}>
            Revenue Collection Breakdown
          </h3>
          <div style={{ height: '300px', width: '100%', minWidth: 0 }}>
            <ResponsiveContainer width="99%" height={300}>
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="45%"
                  innerRadius={65}
                  outerRadius={95}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {pieData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip formatter={(val: any) => `₹${Number(val).toLocaleString()}`} />
                <Legend verticalAlign="bottom" height={36} formatter={(value) => <span style={{ fontSize: '13px', color: '#475569', fontWeight: 500 }}>{value}</span>} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Middle Charts Row: ARPU & Churn Trends */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', marginBottom: '24px' }}>
        {/* ARPU Bar Chart */}
        <div className="data-panel" style={{ padding: '24px', minWidth: 0, minHeight: 0, overflow: 'hidden' }}>
          <h3 style={{ fontFamily: "'Outfit', sans-serif", fontSize: '18px', fontWeight: 700, color: '#1f2937', marginBottom: '20px', margin: 0 }}>
            ARPU Trend (INR)
          </h3>
          <div style={{ height: '240px', width: '100%', minWidth: 0 }}>
            <ResponsiveContainer width="99%" height={240}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                <XAxis dataKey="date" stroke="#94a3b8" fontSize={12} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={12} width={60} tickLine={false} />
                <Tooltip formatter={(val: any) => `₹${Number(val).toLocaleString()}`} />
                <Bar dataKey="arpu" fill="#3b82f6" radius={[6, 6, 0, 0]} name="ARPU (INR)" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Churn Rate Trend Chart */}
        <div className="data-panel" style={{ padding: '24px', minWidth: 0, minHeight: 0, overflow: 'hidden' }}>
          <h3 style={{ fontFamily: "'Outfit', sans-serif", fontSize: '18px', fontWeight: 700, color: '#1f2937', marginBottom: '20px', margin: 0 }}>
            Churn Rate Trend (%)
          </h3>
          <div style={{ height: '240px', width: '100%', minWidth: 0 }}>
            <ResponsiveContainer width="99%" height={240}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                <XAxis dataKey="date" stroke="#94a3b8" fontSize={12} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={12} width={40} tickLine={false} />
                <Tooltip formatter={(val: any) => `${Number(val).toFixed(2)}%`} />
                <Line type="monotone" dataKey="churn" stroke="#ef4444" strokeWidth={3} name="Churn Rate (%)" dot={{ r: 5, fill: '#ef4444', stroke: '#fff', strokeWidth: 2 }} activeDot={{ r: 8 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Bottom Row: Invoice Collections Widget */}
      <div className="data-panel" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h3 style={{ fontFamily: "'Outfit', sans-serif", fontSize: '18px', fontWeight: 700, color: '#1f2937', margin: 0 }}>
            Invoice Collections & Aging Overview
          </h3>
          <Link to="/finance/invoices" style={{ fontSize: '14px', color: '#10b981', fontWeight: 600, textDecoration: 'none' }}>
            View Aging Center →
          </Link>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginTop: '16px' }}>
          <div style={{ padding: '16px', background: '#f8fafc', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
            <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', margin: '0 0 8px 0' }}>Paid Invoices</p>
            <p style={{ fontSize: '24px', fontWeight: 700, color: '#10b981', margin: 0 }}>{stats?.paidInvoices ?? 0}</p>
            <p style={{ fontSize: '12px', color: '#475569', marginTop: '8px', marginBottom: 0 }}>{formatCurrencyINR(stats?.totalCollectedMinor ?? 0)} Collected</p>
          </div>
          <div style={{ padding: '16px', background: '#f8fafc', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
            <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', margin: '0 0 8px 0' }}>Open / Pending</p>
            <p style={{ fontSize: '24px', fontWeight: 700, color: '#f59e0b', margin: 0 }}>{stats?.pendingInvoices ?? 0}</p>
            <p style={{ fontSize: '12px', color: '#475569', marginTop: '8px', marginBottom: 0 }}>{formatCurrencyINR(stats?.pendingCollectionMinor ?? 0)} Outstanding</p>
          </div>
          <div style={{ padding: '16px', background: '#f8fafc', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
            <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', margin: '0 0 8px 0' }}>Failed / Uncollectible</p>
            <p style={{ fontSize: '24px', fontWeight: 700, color: '#ef4444', margin: 0 }}>{stats?.failedInvoices ?? 0}</p>
            <p style={{ fontSize: '12px', color: '#ef4444', marginTop: '8px', marginBottom: 0 }}>Requires Dunning</p>
          </div>
          <div style={{ padding: '16px', background: '#f8fafc', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
            <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', margin: '0 0 8px 0' }}>Total Issued</p>
            <p style={{ fontSize: '24px', fontWeight: 700, color: '#3b82f6', margin: 0 }}>{stats?.totalInvoices ?? 0}</p>
            <p style={{ fontSize: '12px', color: '#475569', marginTop: '8px', marginBottom: 0 }}>All Billing Cycles</p>
          </div>
        </div>
      </div>
    </>
  );
};
