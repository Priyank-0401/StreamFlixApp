import React, { useState, useEffect } from 'react';
import { Package, Layers, TicketPercent, Puzzle, Percent, BookOpen, Users, UserCog, MoreHorizontal } from 'lucide-react';
import { Link } from 'react-router-dom';
import { StatsCard } from '../../components/admin/dashboard/StatsCard';
import { getDashboardStats, getAllProducts, getAllPlans } from '../../services/admin/adminService';
import type { DashboardStats, Product, PlanResponse } from '../../services/admin/adminTypes';

export const AdminDashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [plans, setPlans] = useState<PlanResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        const [statsData, productsData, plansData] = await Promise.all([
          getDashboardStats(),
          getAllProducts(),
          getAllPlans()
        ]);
        setStats(statsData);
        setProducts(productsData.slice(0, 5)); // Top 5 products
        setPlans(plansData.slice(0, 4)); // Top 4 plans
      } catch (error) {
        console.error('Failed to load dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };
    loadDashboardData();
  }, []);

  if (loading) {
    return <div style={{ color: '#9ca3af', padding: '40px', textAlign: 'center', fontFamily: 'Inter, sans-serif' }}>Loading dashboard...</div>;
  }

  const formatPrice = (minor: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(minor / 100);
  };

  return (
    <>
      {/* Header */}
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontFamily: 'Inter, sans-serif', fontSize: '28px', fontWeight: 600, color: '#1f2937', margin: 0, letterSpacing: '-0.02em' }}>
          Admin Dashboard
        </h1>
        <p style={{ fontFamily: 'Inter, sans-serif', fontSize: '14px', color: '#6b7280', margin: '8px 0 0' }}>
          Overview of your platform catalog and users
        </p>
      </div>

      {/* Stats Grid - Row 1: Catalog Management */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '20px' }}>
        <StatsCard 
          label="Total Products" 
          value={String(stats?.totalProducts ?? 0)} 
          icon={<Package size={20} />}
        />
        <StatsCard 
          label="Total Plans" 
          value={String(stats?.totalPlans ?? 0)} 
          icon={<Layers size={20} />}
        />
        <StatsCard 
          label="Active Coupons" 
          value={String(stats?.activeCoupons ?? 0)} 
          icon={<TicketPercent size={20} />}
        />
        <StatsCard 
          label="Total Add-ons" 
          value={String(stats?.totalAddOns ?? 0)} 
          icon={<Puzzle size={20} />}
        />
      </div>

      {/* Stats Grid - Row 2: Tax, Price Books, Users */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '32px' }}>
        <StatsCard 
          label="Active Tax Rates" 
          value={String(stats?.activeTaxRates ?? 0)} 
          icon={<Percent size={20} />}
        />
        <StatsCard 
          label="Total Price Books" 
          value={String(stats?.totalPriceBooks ?? 0)} 
          icon={<BookOpen size={20} />}
        />
        <StatsCard 
          label="Total Customers" 
          value={String(stats?.totalCustomers ?? 0)} 
          icon={<Users size={20} />}
        />
        <StatsCard 
          label="Total Staff" 
          value={String(stats?.totalStaff ?? 0)} 
          icon={<UserCog size={20} />}
        />
      </div>

      {/* Main Content Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>
        {/* Current Products Section */}
        <div className="data-panel">
          <div className="data-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '20px 24px', borderBottom: '1px solid #e5e7eb' }}>
            <h2 style={{ fontFamily: 'Inter, sans-serif', fontSize: '18px', fontWeight: 600, color: '#1f2937', margin: 0 }}>Current Products</h2>
            <Link to="/admin/products" style={{ fontFamily: 'Inter, sans-serif', fontSize: '14px', color: '#5b4fff', textDecoration: 'none' }}>
              View all products
            </Link>
          </div>
          <table className="admin-table" style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#f9fafb' }}>
                <th style={{ padding: '12px 24px', textAlign: 'left', fontFamily: 'Inter, sans-serif', fontSize: '12px', fontWeight: 500, color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Product Name</th>
                <th style={{ padding: '12px 24px', textAlign: 'center', fontFamily: 'Inter, sans-serif', fontSize: '12px', fontWeight: 500, color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Status</th>
                <th style={{ padding: '12px 24px', textAlign: 'center', fontFamily: 'Inter, sans-serif', fontSize: '12px', fontWeight: 500, color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Plans Count</th>
                <th style={{ padding: '12px 24px', textAlign: 'center', fontFamily: 'Inter, sans-serif', fontSize: '12px', fontWeight: 500, color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id} style={{ borderBottom: '1px solid #e5e7eb' }}>
                  <td style={{ padding: '16px 24px', fontFamily: 'Inter, sans-serif', fontSize: '14px', color: '#1f2937' }}>
                    {product.name}
                  </td>
                  <td style={{ padding: '16px 24px', textAlign: 'center' }}>
                    <span style={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      padding: '4px 12px',
                      borderRadius: '9999px',
                      fontSize: '12px',
                      fontWeight: 500,
                      fontFamily: 'Inter, sans-serif',
                      backgroundColor: product.status === 'ACTIVE' ? '#dcfce7' : '#fee2e2',
                      color: product.status === 'ACTIVE' ? '#166534' : '#991b1b'
                    }}>
                      {product.status === 'ACTIVE' ? '● ' : '○ '}
                      {product.status}
                    </span>
                  </td>
                  <td style={{ padding: '16px 24px', textAlign: 'center', fontFamily: 'Inter, sans-serif', fontSize: '14px', color: '#6b7280' }}>
                    -
                  </td>
                  <td style={{ padding: '16px 24px', textAlign: 'center' }}>
                    <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#9ca3af' }}>
                      <MoreHorizontal size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Featured Plans Section */}
        <div className="data-panel">
          <div className="data-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '20px 24px', borderBottom: '1px solid #e5e7eb' }}>
            <h2 style={{ fontFamily: 'Inter, sans-serif', fontSize: '18px', fontWeight: 600, color: '#1f2937', margin: 0 }}>Featured Plans</h2>
            <Link to="/admin/plans" style={{ fontFamily: 'Inter, sans-serif', fontSize: '14px', color: '#5b4fff', textDecoration: 'none' }}>
              View all {stats?.totalPlans ?? 0} plans
            </Link>
          </div>
          <div style={{ padding: '16px' }}>
            {plans.map((plan) => (
              <div key={plan.id} style={{ 
                background: '#ffffff',
                border: '1px solid #e5e7eb',
                borderRadius: '12px',
                padding: '16px',
                marginBottom: '12px'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                  <h3 style={{ fontFamily: 'Inter, sans-serif', fontSize: '16px', fontWeight: 600, color: '#1f2937', margin: 0 }}>
                    {plan.name}
                  </h3>
                  <span style={{
                    fontSize: '10px',
                    fontWeight: 600,
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    backgroundColor: '#5b4fff',
                    color: '#ffffff',
                    fontFamily: 'Inter, sans-serif'
                  }}>
                    {plan.billingPeriod}
                  </span>
                </div>
                <p style={{ 
                  fontFamily: 'Inter, sans-serif', 
                  fontSize: '14px', 
                  color: '#6b7280', 
                  margin: '0 0 12px 0' 
                }}>
                  Billed {plan.billingPeriod.toLowerCase()}
                </p>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ 
                    fontFamily: 'Inter, sans-serif', 
                    fontSize: '24px', 
                    fontWeight: 700, 
                    color: '#5b4fff' 
                  }}>
                    {formatPrice(plan.defaultPriceMinor, plan.defaultCurrency)}
                  </span>
                  <span style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '4px',
                    fontSize: '12px',
                    fontWeight: 500,
                    fontFamily: 'Inter, sans-serif',
                    color: plan.status === 'ACTIVE' ? '#16a34a' : '#dc2626'
                  }}>
                    ● {plan.status}
                  </span>
                </div>
                <div style={{ 
                  display: 'grid', 
                  gridTemplateColumns: '1fr 1fr', 
                  gap: '12px',
                  marginTop: '12px',
                  paddingTop: '12px',
                  borderTop: '1px solid #e5e7eb'
                }}>
                  <div>
                    <p style={{ 
                      fontFamily: 'Inter, sans-serif', 
                      fontSize: '11px', 
                      color: '#9ca3af', 
                      margin: '0 0 4px 0',
                      textTransform: 'uppercase',
                      letterSpacing: '0.05em'
                    }}>Trial Days</p>
                    <p style={{ 
                      fontFamily: 'Inter, sans-serif', 
                      fontSize: '14px', 
                      fontWeight: 600,
                      color: '#374151',
                      margin: 0
                    }}>{plan.trialDays} Days</p>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <p style={{ 
                      fontFamily: 'Inter, sans-serif', 
                      fontSize: '11px', 
                      color: '#9ca3af', 
                      margin: '0 0 4px 0',
                      textTransform: 'uppercase',
                      letterSpacing: '0.05em'
                    }}>Status</p>
                    <p style={{ 
                      fontFamily: 'Inter, sans-serif', 
                      fontSize: '14px', 
                      fontWeight: 600,
                      color: plan.status === 'ACTIVE' ? '#16a34a' : '#dc2626',
                      margin: 0
                    }}>{plan.status}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  );
};
