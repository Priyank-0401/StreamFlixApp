import React, { useState, useRef, useEffect } from 'react';
import { Link, useLocation, useNavigate, Outlet } from 'react-router-dom';
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
  ChevronRight,
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
  { path: '/dashboard', label: 'Overview', icon: <LayoutDashboard size={20} />, requiresCustomer: false },
  { path: '/dashboard/subscription', label: 'Subscription', icon: <CreditCard size={20} />, requiresCustomer: false },
  { path: '/dashboard/billing', label: 'Billing', icon: <FileText size={20} />, requiresCustomer: true },
  { path: '/dashboard/payment-methods', label: 'Payment Methods', icon: <Wallet size={20} />, requiresCustomer: true },
  { path: '/dashboard/support', label: 'Support', icon: <HelpCircle size={20} />, requiresCustomer: false },
  { path: '/dashboard/profile', label: 'Profile', icon: <User size={20} />, requiresCustomer: true },
];

export const CustomerLayout: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthContext();
  const { isCustomer, loading: customerLoading } = useCustomerContext();
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
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
    <div className="d-flex min-vh-100" style={{ backgroundColor: '#ffffff' }}>
      {/* Sidebar */}
      <aside
        className={`d-flex flex-column position-fixed h-100 ${isSidebarCollapsed ? 'sidebar-collapsed' : ''}`}
        style={{
          width: isSidebarCollapsed ? '80px' : '280px',
          backgroundColor: '#0F172A', // Deep slate/dark theme
          borderRight: '1px solid rgba(255, 255, 255, 0.1)',
          zIndex: 100,
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          boxShadow: '10px 0 30px rgba(0,0,0,0.1)'
        }}
      >
        {/* Sidebar Logo Section */}
        <div
          className="d-flex align-items-center justify-content-between p-4"
          style={{ height: '80px', borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}
        >
          <Link
            to="/"
            className="d-flex align-items-center gap-2 text-decoration-none"
            style={{ color: '#ffffff' }}
          >
            <div 
              className="d-flex align-items-center justify-content-center rounded-3"
              style={{ 
                width: '32px', 
                height: '32px', 
                background: 'linear-gradient(135deg, #5B4FFF 0%, #8B5CF6 100%)',
                boxShadow: '0 4px 12px rgba(91, 79, 255, 0.3)'
              }}
            >
              <span style={{ color: 'white', fontWeight: 800, fontSize: '18px' }}>S</span>
            </div>
            {!isSidebarCollapsed && (
              <span
                className="fs-4 fw-bold"
                style={{ 
                  fontFamily: 'Outfit, sans-serif', 
                  letterSpacing: '-0.5px',
                  background: 'linear-gradient(to right, #fff, #94a3b8)',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent'
                }}
              >
                StreamFlix
              </span>
            )}
          </Link>
        </div>

        {/* Navigation Items */}
        <nav className="flex-grow-1 py-4 px-3 d-flex flex-column gap-2">
          {customerLoading ? (
            <div className="text-center py-3">
              <Loader2 className="spin" size={20} color="rgba(255,255,255,0.5)" />
            </div>
          ) : (
            visibleNavItems.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                className="d-flex align-items-center gap-3 px-3 py-3 text-decoration-none rounded-3"
                style={{
                  color: isActive(item.path) ? '#ffffff' : '#94a3b8',
                  backgroundColor: isActive(item.path) ? 'rgba(91, 79, 255, 0.15)' : 'transparent',
                  fontSize: '14px',
                  fontWeight: isActive(item.path) ? 600 : 500,
                  transition: 'all 0.2s ease',
                  fontFamily: 'Inter, sans-serif',
                  border: isActive(item.path) ? '1px solid rgba(91, 79, 255, 0.2)' : '1px solid transparent'
                }}
                onMouseEnter={(e) => {
                  if (!isActive(item.path)) {
                    e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.05)';
                    e.currentTarget.style.color = '#ffffff';
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isActive(item.path)) {
                    e.currentTarget.style.backgroundColor = 'transparent';
                    e.currentTarget.style.color = '#94a3b8';
                  }
                }}
              >
                <span className={`d-flex align-items-center justify-content-center flex-shrink-0 ${isActive(item.path) ? 'text-primary' : ''}`} style={{ color: isActive(item.path) ? '#8B5CF6' : 'inherit' }}>
                  {item.icon}
                </span>
                {!isSidebarCollapsed && (
                  <span className="text-truncate">{item.label}</span>
                )}
                {isActive(item.path) && !isSidebarCollapsed && (
                  <div className="ms-auto rounded-circle" style={{ width: '6px', height: '6px', background: '#5B4FFF' }}></div>
                )}
              </Link>
            ))
          )}
        </nav>

        {/* Sidebar Footer with Collapse Toggle */}
        <div className="p-3 mt-auto" style={{ borderTop: '1px solid rgba(255, 255, 255, 0.05)' }}>
          <button
            className="btn w-100 d-flex align-items-center justify-content-center gap-2"
            onClick={() => setIsSidebarCollapsed(!isSidebarCollapsed)}
            style={{ 
              color: '#94a3b8', 
              background: 'rgba(255,255,255,0.03)', 
              borderRadius: '12px',
              padding: '12px',
              border: '1px solid rgba(255,255,255,0.05)',
              transition: 'all 0.2s'
            }}
            onMouseEnter={(e) => (e.currentTarget.style.color = '#ffffff')}
            onMouseLeave={(e) => (e.currentTarget.style.color = '#94a3b8')}
          >
            <ChevronRight
              size={18}
              style={{ transform: isSidebarCollapsed ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.4s' }}
            />
            {!isSidebarCollapsed && <span style={{ fontSize: '13px', fontWeight: 500 }}>Collapse Sidebar</span>}
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main
        className="d-flex flex-column min-vh-100"
        style={{
          marginLeft: isSidebarCollapsed ? '80px' : '280px',
          flex: 1,
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          background: '#ffffff'
        }}
      >
        {/* Top Header */}
        <header
          className="d-flex align-items-center justify-content-between px-5 position-sticky top-0"
          style={{
            height: '80px',
            backgroundColor: 'rgba(255, 255, 255, 0.8)',
            backdropFilter: 'blur(12px)',
            borderBottom: '1px solid #f1f5f9',
            zIndex: 50
          }}
        >
          <div className="d-flex align-items-center gap-2">
            <h2 style={{ fontSize: '18px', fontWeight: 700, color: '#1E293B', margin: 0, fontFamily: 'Outfit, sans-serif' }}>
              {visibleNavItems.find(i => isActive(i.path))?.label || 'Dashboard'}
            </h2>
          </div>

          <div className="d-flex align-items-center gap-4">
            <form
              onSubmit={handleSearch}
              className="d-none d-md-flex align-items-center"
              style={{ width: '300px', position: 'relative' }}
            >
              <Search
                size={16}
                style={{ position: 'absolute', left: '16px', color: '#94a3b8' }}
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

            <div className="customer-profile position-relative" ref={dropdownRef}>
              <button
                className="d-flex align-items-center gap-3 p-2 bg-transparent border-0"
                onClick={() => setDropdownOpen(!dropdownOpen)}
                style={{ cursor: 'pointer' }}
              >
                <div className="text-end d-none d-sm-block">
                  <p style={{ margin: 0, fontSize: '13px', fontWeight: 600, color: '#1E293B' }}>{user?.fullName || 'Customer'}</p>
                  <p style={{ margin: 0, fontSize: '11px', color: '#64748B', fontWeight: 500 }}>Customer Plan</p>
                </div>
                <div
                  className="d-flex align-items-center justify-content-center fw-bold"
                  style={{
                    width: '42px',
                    height: '42px',
                    borderRadius: '12px',
                    backgroundColor: '#5B4FFF',
                    color: 'white',
                    fontSize: '14px',
                    boxShadow: '0 4px 12px rgba(91, 79, 255, 0.2)'
                  }}
                >
                  {(user?.fullName?.[0] || 'C').toUpperCase()}
                </div>
              </button>

              {dropdownOpen && (
                <div
                  className="customer-dropdown shadow-lg"
                  style={{
                    position: 'absolute',
                    top: 'calc(100% + 12px)',
                    right: 0,
                    width: '240px',
                    background: '#ffffff',
                    border: '1px solid #f1f5f9',
                    borderRadius: '16px',
                    zIndex: 100,
                    overflow: 'hidden'
                  }}
                >
                  <div className="p-4 bg-light border-bottom">
                    <p style={{ fontSize: '14px', fontWeight: 700, color: '#1E293B', margin: 0 }}>{user?.fullName}</p>
                    <p style={{ fontSize: '12px', color: '#64748B', margin: 0 }}>{user?.email}</p>
                  </div>
                  <div className="p-2">
                    <button
                      className="w-100 d-flex align-items-center gap-3 px-3 py-2 bg-transparent border-0 rounded-3 text-start"
                      onClick={handleLogout}
                      style={{ fontSize: '14px', color: '#EF4444', fontWeight: 600, transition: 'all 0.2s' }}
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
        <div className="flex-grow-1 p-5" style={{ overflowY: 'auto' }}>
          <Outlet />
        </div>
      </main>
    </div>
  );
};

