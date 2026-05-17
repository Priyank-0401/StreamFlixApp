import React, { useEffect, useState } from 'react';
import { PageHeader } from '../../components/admin/shared/PageHeader';
import { Download } from 'lucide-react';
import { StatusBadge } from '../../components/finance/components/statusBadge';
import { type Column, FinanceDataTable } from '../../components/finance/components/finanaceDataTable';
import { financeService } from '../../services/finance/financeService';
import type { ExportJob } from '../../types/financeTypes';

export const ExportsPage: React.FC = () => {
  const [data, setData] = useState<ExportJob[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const result = await financeService.getExports();
        setData(result);
      } catch (error) {
        console.error('Error fetching exports:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const columns: Column<ExportJob>[] = [
    { header: 'Job ID', accessor: 'id' },
    { header: 'Report Type', accessor: 'type' },
    { header: 'Format', accessor: 'format' },
    { header: 'Date Requested', accessor: (row) => new Date(row.date).toLocaleString() },
    { header: 'Status', accessor: (row) => <StatusBadge status={row.status} /> },
    {
      header: 'Action',
      accessor: (row) => (
        row.status === 'Completed' ? (
          <button className="btn-admin-sm" style={{ display: 'flex', alignItems: 'center', gap: '4px', background: 'transparent', border: '1px solid #e5e7eb' }}>
            <Download size={14} /> Download
          </button>
        ) : (
          <span style={{ color: 'var(--text-muted)', fontSize: '12px' }}>-</span>
        )
      )
    },
  ];

  return (
    <>
      <PageHeader subtitle="History of generated financial reports." />

      {loading ? (
        <div>Loading exports...</div>
      ) : (
        <FinanceDataTable columns={columns} data={data} keyExtractor={(row) => row.id} />
      )}
    </>
  );
};
