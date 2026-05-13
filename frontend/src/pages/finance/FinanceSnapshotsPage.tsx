import React, { useState, useEffect } from 'react';
import { PageHeader } from '../../components/admin/shared/PageHeader';
import { getFinanceStats, recordSnapshot, type RevenueSnapshot } from '../../services/finance/financeService';
import { RefreshCw } from 'lucide-react';

export const FinanceSnapshotsPage: React.FC = () => {
  const [snapshots, setSnapshots] = useState<RevenueSnapshot[]>([]);
  const [loading, setLoading] = useState(true);
  const [recording, setRecording] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const data = await getFinanceStats();
      setSnapshots(data.recentSnapshots || []);
    } catch (err) {
      console.error('Failed to load revenue snapshots:', err);
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

  const formatCurrencyINR = (minor: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(minor / 100);
  };

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <PageHeader subtitle="Historical daily audit trail of SaaS revenue snapshots, MRR, ARR, and active customer counts" />
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

      <div className="data-panel">
        <div className="data-panel-header" style={{ padding: '20px 24px', borderBottom: '1px solid #e5e7eb' }}>
          <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#1f2937', margin: 0 }}>Revenue Snapshots History ({snapshots.length})</h2>
        </div>
        <div className="admin-table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Snapshot Date</th>
                <th style={{ textAlign: 'right' }}>MRR (INR)</th>
                <th style={{ textAlign: 'right' }}>ARR (INR)</th>
                <th style={{ textAlign: 'right' }}>ARPU (INR)</th>
                <th style={{ textAlign: 'center' }}>Active Customers</th>
                <th style={{ textAlign: 'center' }}>Churn Rate</th>
                <th style={{ textAlign: 'right' }}>LTV (INR)</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>Loading snapshot logs...</td>
                </tr>
              ) : snapshots.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>No daily revenue snapshots recorded yet. Click 'Record Daily Snapshot' to create one.</td>
                </tr>
              ) : (
                snapshots.map(s => (
                  <tr key={s.id}>
                    <td style={{ fontWeight: 600, color: '#1e293b' }}>{s.snapshotDate}</td>
                    <td style={{ textAlign: 'right', fontWeight: 700, color: '#10b981' }}>{formatCurrencyINR(s.mrrMinor)}</td>
                    <td style={{ textAlign: 'right', fontWeight: 600, color: '#5b4fff' }}>{formatCurrencyINR(s.arrMinor)}</td>
                    <td style={{ textAlign: 'right', fontWeight: 600, color: '#3b82f6' }}>{formatCurrencyINR(s.arpuMinor)}</td>
                    <td style={{ textAlign: 'center', fontWeight: 600 }}>{s.activeCustomers}</td>
                    <td style={{ textAlign: 'center', fontWeight: 600, color: s.grossChurnPercent > 5 ? '#ef4444' : '#475569' }}>
                      {s.grossChurnPercent?.toFixed(2)}%
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 600, color: '#64748b' }}>{formatCurrencyINR(s.ltvMinor)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
};
