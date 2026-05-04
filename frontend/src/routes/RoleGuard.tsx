import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthContext } from '../context/AuthContext';
import { ROLES } from '../constants/roles';

interface RoleGuardProps {
  children: React.ReactNode;
  allowedRoles: ROLES[];
  redirectTo: string;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({ children, allowedRoles, redirectTo }) => {
  const { isAuthenticated, user, isLoading } = useAuthContext();

  console.log('RoleGuard check:', { isAuthenticated, userRole: user?.role, allowedRoles, isLoading });

  if (isLoading) {
    return (
      <div className="min-vh-100 d-flex justify-content-center align-items-center bg-dark">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    console.log('RoleGuard: Not authenticated, redirecting to', redirectTo);
    return <Navigate to="/login" replace />;
  }

  if (!allowedRoles.includes(user.role)) {
    console.log(`RoleGuard: Role mismatch. User has ${user.role}, needs one of`, allowedRoles);
    return <Navigate to={redirectTo} replace />;
  }

  console.log('RoleGuard: Access granted for role', user.role);
  return <>{children}</>;
};
