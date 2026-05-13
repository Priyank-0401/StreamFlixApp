import React, { useState, useRef, useEffect } from 'react';
import { NavLink, useLocation, useNavigate, Outlet, Link } from 'react-router-dom';
import { useAuthContext } from '../../../context/AuthContext';
import {
  LayoutDashboard,
  TrendingUp,
  DollarSign,
  Users,
  UserX,
  FileText,
  Search,
  LogOut,
  History,
} from 'lucide-react';
import '../../../styles/admin.css';

interface NavItem {
  section: string;
  label: string;
  path: string;
  icon: React.ReactNode;
}

const navItems: NavItem[] = [
  { section: 'Overview', label: 'Revenue Dashboard', path: '/finance', icon: <LayoutDashboard size={18} /> },
  { section: 'Overview', label: 'Revenue Snapshots', path: '/finance/snapshots', icon: <History size={18} /> },
  { section: 'Analytics', label: 'MRR Breakdown', path: '/finance/mrr', icon: <TrendingUp size={18} /> },
  { section: 'Analytics', label: 'ARR Projections', path: '/finance/arr', icon: <DollarSign size={18} /> },
  { section: 'Analytics', label: 'ARPU Contribution', path: '/finance/arpu', icon: <Users size={18} /> },
  { section: 'Analytics', label: 'Churn Analysis', path: '/finance/churn', icon: <UserX size={18} /> },
  { section: 'Collections', label: 'Invoice Aging', path: '/finance/invoices', icon: <FileText size={18} /> },
];

const pageTitles: Record<string, string> = {
  '/finance': 'Revenue Dashboard',
  '/finance/snapshots': 'Historical Revenue Snapshots',
  '/finance/mrr': 'MRR Breakdown',
  '/finance/arr': 'ARR Projections',
  '/finance/arpu': 'ARPU Contribution',
  '/finance/churn': 'Churn Analysis',
  '/finance/invoices': 'Invoice Aging & Collections',
};

// Group items by section
const sections = navItems.reduce<Record<string, typeof navItems>>((acc, item) => {
  if (!acc[item.section]) acc[item.section] = [];
  acc[item.section].push(item);
  return acc;
}, {});

export const FinanceLayout: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthContext();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<NavItem[]>([]);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLFormElement>(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setSearchResults([]);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Update search results
  useEffect(() => {
    if (searchQuery.trim().length > 0) {
      const filtered = navItems.filter(item =>
        item.label.toLowerCase().includes(searchQuery.toLowerCase())
      );
      setSearchResults(filtered);
    } else {
      setSearchResults([]);
    }
  }, [searchQuery]);

  const handleLogout = async () => {
    await logout();
    navigate('/');
    setDropdownOpen(false);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchResults.length > 0) {
      navigate(searchResults[0].path);
      setSearchQuery('');
      setSearchResults([]);
    }
  };

  const handleSelectResult = (path: string) => {
    navigate(path);
    setSearchQuery('');
    setSearchResults([]);
  };

  const getTitle = () => {
    return pageTitles[location.pathname] || 'Finance Portal';
  };

  const isActive = (path: string) => {
    if (path === '/finance') return location.pathname === '/finance';
    return location.pathname.startsWith(path);
  };

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map(w => w[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  return (
    <div className="admin-shell">
      {/* Google Fonts */}
      <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Outfit:wght@700&display=swap" rel="stylesheet" />

      {/* Sidebar - Modern White theme integrated */}
      <aside className="admin-sidebar" style={{ boxShadow: 'none' }}>
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
                color: '#5b4fff', // Original StreamFlix Indigo theme
                textTransform: 'uppercase',
                letterSpacing: '-0.5px',
              }}
            >
              FLIX
            </span>
          </Link>
        </div>

        <nav className="sidebar-nav">
          {Object.entries(sections).map(([sectionTitle, items]) => (
            <div key={sectionTitle} className="sidebar-section">
              <div className="sidebar-section-label">{sectionTitle}</div>
              {items.map((item) => (
                <NavLink
                  key={item.path}
                  to={item.path}
                  end={item.path === '/finance'}
                  className={`sidebar-link ${isActive(item.path) ? 'active' : ''}`}
                >
                  <span className="sidebar-link-icon">{item.icon}</span>
                  {item.label}
                </NavLink>
              ))}
            </div>
          ))}
        </nav>
      </aside>

      {/* Main Content Area */}
      <div className="admin-content-area">
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
              {getTitle()}
            </h2>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <form
              onSubmit={handleSearch}
              ref={searchRef}
              style={{ width: '300px', position: 'relative' }}
            >
              <Search
                size={16}
                style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }}
              />
              <input
                type="text"
                placeholder="Search Finance..."
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
              {/* Search Results Dropdown */}
              {searchResults.length > 0 && (
                <div style={{
                  position: 'absolute',
                  top: 'calc(100% + 8px)',
                  left: 0,
                  right: 0,
                  background: 'white',
                  borderRadius: '12px',
                  boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
                  border: '1px solid #e5e7eb',
                  padding: '8px',
                  zIndex: 100
                }}>
                  {searchResults.map((result) => (
                    <div
                      key={result.path}
                      onClick={() => handleSelectResult(result.path)}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '10px',
                        padding: '10px 12px',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        fontSize: '13px',
                        color: '#475569',
                        transition: 'background 0.2s'
                      }}
                      onMouseEnter={(e) => e.currentTarget.style.background = '#f1f5f9'}
                      onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                    >
                      <span style={{ color: '#94a3b8' }}>{result.icon}</span>
                      <span style={{ fontWeight: 500 }}>{result.label}</span>
                    </div>
                  ))}
                </div>
              )}
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
                  <p style={{ margin: 0, fontSize: '13px', fontWeight: 600, color: '#1E293B' }}>{user?.fullName || 'Finance Lead'}</p>
                  <p style={{ margin: 0, fontSize: '11px', color: '#64748B', fontWeight: 500 }}>{user?.role || 'FINANCE'}</p>
                </div>
                <div
                  style={{
                    width: '42px',
                    height: '42px',
                    borderRadius: '12px',
                    backgroundColor: '#10b981', // Emerald theme
                    color: 'white',
                    fontSize: '14px',
                    fontWeight: 700,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 4px 12px rgba(16, 185, 129, 0.2)'
                  }}
                >
                  {user ? getInitials(user.fullName || user.email) : 'FI'}
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
                    <p style={{ fontSize: '14px', fontWeight: 700, color: '#1E293B', margin: 0 }}>{user?.fullName || 'Finance Lead'}</p>
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

        {/* Page Content */}
        <main className="admin-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
