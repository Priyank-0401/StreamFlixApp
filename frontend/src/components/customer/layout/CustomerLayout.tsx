import React, { useRef, useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate, Outlet } from 'react-router-dom';
import { useAuthContext } from '../../../context/AuthContext';
import { useCustomerContext } from '../../../context/CustomerContext';
import {
  LayoutDashboard,
  CreditCard,
  FileText,
  Wallet,
  HelpCircle,
  User,
  LogOut,
  Search,
  Loader2
} from 'lucide-react';

interface NavItem {
  path: string;
  label: string;
  icon: React.ReactNode;
  requiresCustomer?: boolean;
}

// All nav items - some require customer status
const allNavItems: NavItem[] = [
  { path: '/dashboard', label: 'Overview', icon: <LayoutDashboard size={18} />, requiresCustomer: false },
  { path: '/dashboard/subscription', label: 'Subscription', icon: <CreditCard size={18} />, requiresCustomer: false },
  { path: '/dashboard/billing', label: 'Billing', icon: <FileText size={18} />, requiresCustomer: true },
  { path: '/dashboard/payment-methods', label: 'Payment Methods', icon: <Wallet size={18} />, requiresCustomer: true },
  { path: '/dashboard/support', label: 'Support', icon: <HelpCircle size={18} />, requiresCustomer: false },
  { path: '/dashboard/profile', label: 'Profile', icon: <User size={18} />, requiresCustomer: true },
];

export const CustomerLayout: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthContext();
  const { isCustomer, loading: customerLoading } = useCustomerContext();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const dropdownRef = useRef<HTMLDivElement>(null);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // TODO: Implement search functionality
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Redirect non-customers to plans page
  if (!customerLoading && isCustomer === false) {
    navigate('/plans');
    return null;
  }

  // Filter nav items based on customer status
  const visibleNavItems = allNavItems.filter(item => {
    if (!item.requiresCustomer) return true;
    return isCustomer === true;
  });

  const handleLogout = async () => {
    await logout();
    navigate('/');
    setDropdownOpen(false);
  };

  const isActive = (path: string) => {
    if (path === '/dashboard') {
      return location.pathname === '/dashboard';
    }
    return location.pathname.startsWith(path);
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#f9fafb', fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif" }}>
      {/* Google Fonts */}
      <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Outfit:wght@700&display=swap" rel="stylesheet" />

      {/* Sidebar - White theme matching admin */}
      <aside
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '260px',
          height: '100vh',
          background: '#ffffff',
          borderRight: '1px solid #e5e7eb',
          display: 'flex',
          flexDirection: 'column',
          zIndex: 100,
          overflowY: 'auto',
          overflowX: 'hidden',
        }}
      >
        {/* Logo - matching landing page "Stream<span>Flix</span>" style */}
        <div
          style={{
            padding: '0 28px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            borderBottom: '1px solid #e5e7eb',
            flexShrink: 0,
            height: '72px',
            boxSizing: 'border-box',
          }}
        >
          <Link
            to="/"
            style={{
              textDecoration: 'none',
              display: 'flex',
              alignItems: 'center',
              gap: '0',
            }}
          >
            <span
              style={{
                fontFamily: "'Outfit', sans-serif",
                fontWeight: 700,
                fontSize: '1.1rem',
                color: '#1f2937',
                textTransform: 'uppercase',
                letterSpacing: '-0.5px',
              }}
            >
              STREAM
            </span>
            <span
              style={{
                fontFamily: "'Outfit', sans-serif",
                fontWeight: 700,
                fontSize: '1.1rem',
                color: '#5b4fff',
                textTransform: 'uppercase',
                letterSpacing: '-0.5px',
              }}
            >
              FLIX
            </span>
          </Link>
        </div>

        {/* Navigation - matching admin sidebar style */}
        <nav style={{ flex: 1, padding: '16px 12px' }}>
          <div style={{
            fontFamily: "'Inter', sans-serif",
            fontSize: '10px',
            fontWeight: 700,
            textTransform: 'uppercase',
            letterSpacing: '0.15em',
            color: '#9ca3af',
            padding: '16px 12px 8px',
          }}>
            Menu
          </div>

          {customerLoading ? (
            <div style={{ textAlign: 'center', padding: '16px 0' }}>
              <Loader2 className="spin" size={20} color="#9ca3af" />
            </div>
          ) : (
            visibleNavItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.path === '/dashboard'}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  padding: '10px 16px',
                  marginBottom: '2px',
                  borderRadius: '10px',
                  textDecoration: 'none',
                  fontSize: '13.5px',
                  fontWeight: isActive(item.path) ? 600 : 500,
                  color: isActive(item.path) ? '#5b4fff' : '#6b7280',
                  background: isActive(item.path) ? '#f0eeff' : 'transparent',
                  transition: 'all 0.2s ease',
                }}
                onMouseEnter={(e) => {
                  if (!isActive(item.path)) {
                    e.currentTarget.style.background = '#f9fafb';
                    e.currentTarget.style.color = '#1f2937';
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isActive(item.path)) {
                    e.currentTarget.style.background = 'transparent';
                    e.currentTarget.style.color = '#6b7280';
                  }
                }}
              >
                <span style={{ color: isActive(item.path) ? '#5b4fff' : '#9ca3af', display: 'flex', alignItems: 'center' }}>
                  {item.icon}
                </span>
                <span>{item.label}</span>
              </NavLink>
            ))
          )}
        </nav>
      </aside>

      {/* Main Content Area */}
      <main
        style={{
          marginLeft: '260px',
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          minHeight: '100vh',
          background: '#f9fafb',
        }}
      >
        {/* Top Header */}
        <header
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0 40px',
            height: '72px',
            backgroundColor: '#ffffff',
            borderBottom: '1px solid #e5e7eb',
            position: 'sticky',
            top: 0,
            zIndex: 50,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <h2 style={{ fontSize: '18px', fontWeight: 700, color: '#1f2937', margin: 0, fontFamily: "'Outfit', sans-serif" }}>
              {visibleNavItems.find(i => isActive(i.path))?.label || 'Dashboard'}
            </h2>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <form
              onSubmit={handleSearch}
              style={{ width: '300px', position: 'relative' }}
            >
              <Search
                size={16}
                style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }}
              />
              <input
                type="text"
                placeholder="Quick Search..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                style={{
                  width: '100%',
                  background: '#f8fafc',
                  border: '1px solid #e2e8f0',
                  borderRadius: '99px',
                  padding: '10px 16px 10px 42px',
                  fontSize: '13px',
                  outline: 'none',
                  transition: 'all 0.2s'
                }}
              />
            </form>

            <div style={{ position: 'relative' }} ref={dropdownRef}>
              <button
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  padding: '8px',
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                }}
                onClick={() => setDropdownOpen(!dropdownOpen)}
              >
                <div style={{ textAlign: 'right' }}>
                  <p style={{ margin: 0, fontSize: '13px', fontWeight: 600, color: '#1E293B' }}>{user?.fullName || 'Customer'}</p>
                  <p style={{ margin: 0, fontSize: '11px', color: '#64748B', fontWeight: 500 }}>Customer</p>
                </div>
                <div
                  style={{
                    width: '42px',
                    height: '42px',
                    borderRadius: '12px',
                    backgroundColor: '#5B4FFF',
                    color: 'white',
                    fontSize: '14px',
                    fontWeight: 700,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 4px 12px rgba(91, 79, 255, 0.2)'
                  }}
                >
                  {(user?.fullName?.[0] || 'C').toUpperCase()}
                </div>
              </button>

              {dropdownOpen && (
                <div
                  style={{
                    position: 'absolute',
                    top: 'calc(100% + 12px)',
                    right: 0,
                    width: '240px',
                    background: '#ffffff',
                    border: '1px solid #f1f5f9',
                    borderRadius: '16px',
                    zIndex: 100,
                    overflow: 'hidden',
                    boxShadow: '0 10px 40px rgba(0,0,0,0.1)',
                  }}
                >
                  <div style={{ padding: '16px', background: '#f8fafc', borderBottom: '1px solid #f1f5f9' }}>
                    <p style={{ fontSize: '14px', fontWeight: 700, color: '#1E293B', margin: 0 }}>{user?.fullName}</p>
                    <p style={{ fontSize: '12px', color: '#64748B', margin: 0 }}>{user?.email}</p>
                  </div>
                  <div style={{ padding: '8px' }}>
                    <button
                      onClick={handleLogout}
                      style={{
                        width: '100%',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '12px',
                        padding: '8px 12px',
                        background: 'transparent',
                        border: 'none',
                        borderRadius: '12px',
                        textAlign: 'left',
                        fontSize: '14px',
                        color: '#EF4444',
                        fontWeight: 600,
                        cursor: 'pointer',
                        transition: 'all 0.2s',
                      }}
                      onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = '#FEF2F2')}
                      onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = 'transparent')}
                    >
                      <LogOut size={18} />
                      Sign Out
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </header>

        {/* Page Content Container */}
        <div style={{ flexGrow: 1, padding: '32px 40px', overflowY: 'auto' }}>
          <Outlet />
        </div>
      </main>
    </div>
  );
};
