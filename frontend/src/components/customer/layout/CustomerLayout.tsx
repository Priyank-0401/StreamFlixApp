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
  ChevronDown,
  Search
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
    console.log('Customer searching for:', searchQuery);
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
    <div className="d-flex min-vh-100" style={{ backgroundColor: '#f9fafb' }}>
      {/* Sidebar */}
      <aside
        className={`d-flex flex-column position-fixed h-100 ${isSidebarCollapsed ? 'sidebar-collapsed' : ''}`}
        style={{
          width: isSidebarCollapsed ? '70px' : '260px',
          backgroundColor: '#ffffff',
          borderRight: '1px solid #e5e7eb',
          zIndex: 100,
          transition: 'width 0.3s ease'
        }}
      >
        <div
          className="d-flex align-items-center justify-content-between p-4"
          style={{ borderBottom: '1px solid #e5e7eb', backgroundColor: '#ffffff' }}
        >
          <Link
            to="/"
            className="d-flex align-items-center gap-2 text-decoration-none"
            style={{ color: '#1f2937' }}
          >
            {!isSidebarCollapsed && (
              <span
                className="fs-5 fw-semibold"
                style={{ letterSpacing: '0.05em', fontFamily: 'Inter, sans-serif' }}
              >
                StreamFlix
              </span>
            )}
          </Link>
          <button
            className="bg-transparent border-0 p-1 d-flex align-items-center justify-content-center"
            style={{ color: '#6b7280', cursor: 'pointer', transition: 'color 0.2s' }}
            onClick={() => setIsSidebarCollapsed(!isSidebarCollapsed)}
            onMouseEnter={(e) => (e.currentTarget.style.color = '#5b4fff')}
            onMouseLeave={(e) => (e.currentTarget.style.color = '#6b7280')}
          >
            <ChevronRight
              size={16}
              style={{ transform: isSidebarCollapsed ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.3s' }}
            />
          </button>
        </div>

        <nav className="flex-grow-1 py-3 px-2 d-flex flex-column gap-1">
          {customerLoading ? (
            <div className="text-center py-3">
              <div className="spinner-border spinner-border-sm" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
            </div>
          ) : (
            visibleNavItems.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                className="d-flex align-items-center gap-2 px-3 py-2 text-decoration-none"
                style={{
                  color: isActive(item.path) ? '#5b4fff' : '#6b7280',
                  backgroundColor: isActive(item.path) ? '#f3f0ff' : 'transparent',
                  borderLeft: isActive(item.path) ? '3px solid #5b4fff' : '3px solid transparent',
                  fontSize: '0.9rem',
                  fontWeight: isActive(item.path) ? 600 : 400,
                  transition: 'all 0.2s ease',
                  fontFamily: 'Inter, sans-serif'
                }}
                onMouseEnter={(e) => {
                  if (!isActive(item.path)) {
                    e.currentTarget.style.backgroundColor = '#f3f4f6';
                    e.currentTarget.style.color = '#1f2937';
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isActive(item.path)) {
                    e.currentTarget.style.backgroundColor = 'transparent';
                    e.currentTarget.style.color = '#6b7280';
                  }
                }}
              >
                <span className="d-flex align-items-center justify-content-center flex-shrink-0">
                  {item.icon}
                </span>
                {!isSidebarCollapsed && (
                  <span className="text-truncate">{item.label}</span>
                )}
              </Link>
            ))
          )}
        </nav>
      </aside>

      {/* Main Content */}
      <main
        className="d-flex flex-column min-vh-100"
        style={{
          marginLeft: isSidebarCollapsed ? '70px' : '260px',
          flex: 1,
          transition: 'margin-left 0.3s ease'
        }}
      >
        {/* Top Header */}
        <header
          className="d-flex align-items-center justify-content-between px-4 position-sticky top-0"
          style={{
            height: '70px',
            backgroundColor: '#ffffff',
            borderBottom: '1px solid #e5e7eb',
            zIndex: 50
          }}
        >
          <form
            onSubmit={handleSearch}
            className="d-flex align-items-center"
            style={{
              flex: 1,
              maxWidth: '500px',
              position: 'relative'
            }}
          >
            <Search
              size={18}
              style={{
                position: 'absolute',
                left: '14px',
                color: '#9ca3af',
                pointerEvents: 'none'
              }}
            />
            <input
              type="text"
              placeholder="Search plans, billing, or support..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{
                width: '100%',
                background: '#f3f4f6',
                border: '1px solid #e5e7eb',
                borderRadius: '8px',
                padding: '10px 14px 10px 44px',
                color: '#1f2937',
                fontSize: '14px',
                outline: 'none',
                transition: 'all 0.2s ease',
                fontFamily: 'Inter, sans-serif'
              }}
              onFocus={(e) => {
                e.target.style.borderColor = '#5b4fff';
                e.target.style.boxShadow = '0 0 0 3px rgba(91, 79, 255, 0.1)';
                e.target.style.background = '#ffffff';
              }}
              onBlur={(e) => {
                e.target.style.borderColor = '#e5e7eb';
                e.target.style.boxShadow = 'none';
                e.target.style.background = '#f3f4f6';
              }}
            />
          </form>
          <div className="d-flex align-items-center gap-3">
            <div className="customer-profile" ref={dropdownRef}>
              <button
                className="customer-profile-btn"
                onClick={() => setDropdownOpen(!dropdownOpen)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '4px',
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease'
                }}
              >
                <div
                  className="d-flex align-items-center justify-content-center fw-semibold"
                  style={{
                    width: '36px',
                    height: '36px',
                    borderRadius: '50%',
                    backgroundColor: '#5b4fff',
                    color: 'white',
                    fontSize: '0.875rem'
                  }}
                >
                  {(user?.fullName?.[0] || 'C').toUpperCase()}
                </div>
                <ChevronDown size={16} style={{ color: '#a0a0a0' }} />
              </button>

              {dropdownOpen && (
                <div
                  className="customer-dropdown"
                  style={{
                    position: 'absolute',
                    top: 'calc(100% + 8px)',
                    right: 0,
                    width: '200px',
                    background: '#ffffff',
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.05)',
                    zIndex: 100,
                    animation: 'slideDown 0.15s ease'
                  }}
                >
                  <div
                    className="customer-dropdown-header"
                    style={{
                      padding: '12px 16px',
                      borderBottom: '1px solid #e5e7eb'
                    }}
                  >
                    <div
                      className="customer-dropdown-name"
                      style={{
                        fontFamily: 'Inter, sans-serif',
                        fontSize: '14px',
                        fontWeight: 600,
                        color: '#1f2937',
                        marginBottom: '2px'
                      }}
                    >
                      {user?.fullName || 'Customer'}
                    </div>
                    <div
                      className="customer-dropdown-role"
                      style={{
                        fontFamily: 'Inter, sans-serif',
                        fontSize: '11px',
                        fontWeight: 500,
                        textTransform: 'uppercase',
                        letterSpacing: '0.05em',
                        color: '#5b4fff'
                      }}
                    >
                      {user?.role || 'Customer'}
                    </div>
                  </div>
                  <div
                    className="customer-dropdown-divider"
                    style={{ height: '1px', background: '#e5e7eb', margin: 0 }}
                  ></div>
                  <button
                    className="customer-dropdown-item"
                    onClick={handleLogout}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px',
                      width: '100%',
                      padding: '10px 16px',
                      background: 'transparent',
                      border: 'none',
                      color: '#6b7280',
                      fontFamily: 'Inter, sans-serif',
                      fontSize: '13px',
                      fontWeight: 500,
                      cursor: 'pointer',
                      transition: 'all 0.15s ease',
                      textAlign: 'left'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.background = '#f3f4f6';
                      e.currentTarget.style.color = '#1f2937';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.background = 'transparent';
                      e.currentTarget.style.color = '#6b7280';
                    }}
                  >
                    <LogOut size={16} />
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          </div>
        </header>

        {/* Page Content */}
        <div className="flex-grow-1 p-4" style={{ backgroundColor: '#f9fafb', overflowY: 'auto' }}>
          <Outlet />
        </div>
      </main>
    </div>
  );
};
