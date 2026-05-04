import React, { createContext, useContext, useState, useEffect } from 'react';
import type { AuthState } from '../types/auth.types';
import { authService } from '../services/auth/authService';
import * as CustomerService from '../services/customer/customerService';

interface AuthContextType {
  user: any | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (user: any) => Promise<{ isCustomer: boolean }>;
  logout: () => void;
  isCustomer: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [authState, setAuthState] = useState<AuthState>({
    user: null,
    token: null, // Left for legacy types support, but completely unused
    isAuthenticated: false,
    isLoading: true,
  });
  // Initialize isCustomer from sessionStorage if available (default to false, never undefined)
  const [isCustomer, setIsCustomer] = useState<boolean>(() => {
    try {
      const cached = sessionStorage.getItem('isCustomer');
      console.log('Initializing isCustomer from sessionStorage:', cached);
      return cached === 'true';
    } catch (e) {
      console.error('Error reading isCustomer from sessionStorage:', e);
      return false;
    }
  });

  useEffect(() => {
    // On page load, ask the Spring Boot server if our JSESSIONID cookie is still valid
    const verifySession = async () => {
      try {
        console.log('Checking session...');
        const user = await authService.checkSession();
        console.log('Session check success:', user);
        if (user && user.email) {
          // Check if user is a customer FIRST (before setting auth state)
          try {
            console.log('Session valid, checking customer status...');
            const customerStatus = await CustomerService.getCustomerStatus();
            console.log('Session customer status FULL RESPONSE:', JSON.stringify(customerStatus));
            console.log('Session customer status.isCustomer:', customerStatus.isCustomer, 'type:', typeof customerStatus.isCustomer);
            setIsCustomer(customerStatus.isCustomer);
            sessionStorage.setItem('isCustomer', String(customerStatus.isCustomer));
          } catch (err: any) {
            // If customer status check fails, use cached value or assume not a customer
            console.log('Customer status check failed in session:', err.message || err);
            console.log('Error details:', err);
            const cached = sessionStorage.getItem('isCustomer');
            console.log('Using cached isCustomer from sessionStorage:', cached);
            setIsCustomer(cached === 'true');
          }
          
          // NOW set auth state after customer status is known
          setAuthState({
            user,
            token: null,
            isAuthenticated: true,
            isLoading: false,
          });
          // Store auth flag for cross-tab sync
          sessionStorage.setItem('isAuthenticated', 'true');
        } else {
          console.log('No user in session response');
          setAuthState(prev => ({ ...prev, isLoading: false }));
          setIsCustomer(false);
          sessionStorage.removeItem('isAuthenticated');
          sessionStorage.removeItem('isCustomer');
        }
      } catch (err: any) {
        // 401 is expected when not logged in - don't log as error
        if (err.message?.includes('401') || err.message?.includes('Unauthorized')) {
          console.log('Session: Not authenticated (401 expected)');
        } else {
          console.error('Session check error:', err.message || err);
        }
        setAuthState(prev => ({ ...prev, isLoading: false }));
        setIsCustomer(false);
        sessionStorage.removeItem('isAuthenticated');
        sessionStorage.removeItem('isCustomer');
      }
    };

    // Always verify session on page load, regardless of current page
    // This ensures consistent auth state across reloads and tabs
    verifySession();
  }, []);

  // Listen for storage changes (cross-tab sync)
  useEffect(() => {
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === 'isAuthenticated') {
        // Another tab logged in or out, reload to sync state
        window.location.reload();
      }
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  const login = async (user: any) => {
    console.log('Login called with user:', user);
    // Ensure role is properly set (backend sends 'ADMIN', we need 'ADMIN')
    if (user && user.role) {
      console.log('User role from backend:', user.role);
    }
    
    // Set sessionStorage for cross-tab sync FIRST
    sessionStorage.setItem('isAuthenticated', 'true');
    
    // Check if user is a customer BEFORE setting auth state
    let customerStatusValue = false;
    try {
      console.log('Checking customer status after login...');
      const customerStatus = await CustomerService.getCustomerStatus();
      console.log('Login customer status FULL RESPONSE:', JSON.stringify(customerStatus));
      console.log('Login customer status.isCustomer:', customerStatus.isCustomer, 'type:', typeof customerStatus.isCustomer);
      customerStatusValue = customerStatus.isCustomer;
      console.log('Customer status result:', customerStatusValue);
      setIsCustomer(customerStatusValue);
      sessionStorage.setItem('isCustomer', String(customerStatusValue));
    } catch (err: any) {
      // If customer status check fails, assume not a customer
      console.log('Customer status check failed:', err.message || err);
      setIsCustomer(false);
      sessionStorage.setItem('isCustomer', 'false');
    }
    
    // NOW set auth state after customer status is known
    setAuthState({
      user,
      token: null,
      isAuthenticated: true,
      isLoading: false,
    });
    
    console.log('Login complete. isCustomer:', customerStatusValue);
    return { isCustomer: customerStatusValue };
  };

  const logout = async () => {
    console.log('Logout called, redirecting to /');
    try {
      // Try to call backend logout, but don't fail if session already expired
      await authService.logout();
      console.log('Backend logout successful');
    } catch (e: any) {
      // 401 means session was already invalid - that's fine, we still want to logout client-side
      console.log('Logout API call completed (may have been 401):', e.message);
    }
    // Clear sessionStorage flags
    sessionStorage.removeItem('isAuthenticated');
    sessionStorage.removeItem('isCustomer');
    // Immediate hard redirect - page reload will reset auth state naturally
    window.location.href = '/';
  };

  // Debug: log what we're providing to context
  const contextValue = {
    user: authState.user,
    token: authState.token,
    isAuthenticated: authState.isAuthenticated,
    isLoading: authState.isLoading,
    login,
    logout,
    isCustomer
  };
  console.log('AuthProvider rendering, isCustomer:', isCustomer, 'type:', typeof isCustomer);
  
  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuthContext = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuthContext must be used within an AuthProvider');
  }
  console.log('useAuthContext called, isCustomer:', context.isCustomer, 'type:', typeof context.isCustomer);
  return context;
};
