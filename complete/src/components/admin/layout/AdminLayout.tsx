import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { TopBar } from './TopBar';
import '../../../styles/admin.css';
import '../../../styles/admin-modal.css';

// Map route paths to human-readable page titles
const pageTitles: Record<string, string> = {
  '/admin': 'Dashboard',
  '/admin/product': 'Product Management',
  '/admin/plans': 'Plans',
  '/admin/addons': 'Add-ons',
  '/admin/metered': 'Metered Components',
  '/admin/pricebooks': 'Price Books',
  '/admin/taxrates': 'Tax Rates',
  '/admin/coupons': 'Coupons',
  '/admin/customers': 'Customers',
  '/admin/staff': 'Staff Accounts',
  '/admin/subscriptions': 'Subscriptions',
};

export const AdminLayout: React.FC = () => {
  const location = useLocation();

  // Resolve the title — check exact match first, then fall back to prefix match for detail pages
  const getTitle = () => {
    if (pageTitles[location.pathname]) return pageTitles[location.pathname];
    
    // Handle detail routes like /admin/subscriptions/123
    const basePath = Object.keys(pageTitles).find(path => 
      path !== '/admin' && location.pathname.startsWith(path)
    );
    return basePath ? pageTitles[basePath] : 'Admin';
  };

  return (
    <>
      {/* Google Fonts for Cinematic Dark Theme */}
      <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet" />
      <div className="admin-shell">
        <Sidebar />
        <div className="admin-content-area">
          <TopBar title={getTitle()} />
          <main className="admin-main">
            <Outlet />
          </main>
        </div>
      </div>
    </>
  );
};
