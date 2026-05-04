import type { LoginRequest, RegisterRequest } from './authTypes';

// Relative URL - proxied through CRA dev server
const API_BASE_URL = '/api';

// Native fetch wrapper that ensures JSESSIONID cookies are always sent/received
// Returns the Response object so callers can check status and parse body as needed
export const fetchWithSession = async (endpoint: string, options: RequestInit = {}) => {
  const defaultOptions: RequestInit = {
    ...options,
    credentials: 'include', // Extremely vital for Stateful Spring Security Sessions!
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  };
  
  // If endpoint is already a full URL (starts with http), use it directly
  // Otherwise, prepend the base URL
  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;
  const response = await fetch(url, defaultOptions);
  
  if (!response.ok) {
    const errorText = await response.text();
    try {
      // If the backend sent our beautiful ErrorInfo JSON object, extract just the message!
      const errorJson = JSON.parse(errorText);
      const customError: any = new Error(errorJson.message || 'Authentication API error');
      
      // If GlobalExceptionHandler attached field-level constraints, pass them forward
      if (errorJson.validationErrors) {
        customError.validationErrors = errorJson.validationErrors;
      }
      throw customError;
    } catch(e) {
      // If it isn't JSON, just throw the raw error message so we still catch it
      if (e instanceof SyntaxError) {
        throw new Error(errorText || 'Authentication API error');
      }
      throw e;
    }
  }
  
  // Return the Response object so callers can use .json(), .text(), check status, etc.
  return response;
};

// Debug helper to check cookie status
export const debugCookies = () => {
  const cookies = document.cookie;
  const hasJSession = cookies.includes('JSESSIONID');
  console.log('Cookies present:', cookies);
  console.log('JSESSIONID found:', hasJSession);
  return { cookies, hasJSession };
};

export const authService = {
  customerLogin: async (request: LoginRequest): Promise<any> => {
    console.log('Attempting customer login...');
    const response = await fetchWithSession('/customer/login', {
      method: 'POST',
      body: JSON.stringify(request),
    });
    const user = await response.json();
    console.log('Login successful, checking cookies...');
    debugCookies();
    return user;
  },

  managerLogin: async (request: LoginRequest): Promise<any> => {
    console.log('Attempting manager login...');
    const response = await fetchWithSession('/manager/login', {
      method: 'POST',
      body: JSON.stringify(request),
    });
    const user = await response.json();
    console.log('Login successful, checking cookies...');
    debugCookies();
    return user;
  },
  // NOTE: There is NO manager/register endpoint - managers are created by admin only

  customerRegister: async (request: RegisterRequest): Promise<any> => {
    const response = await fetchWithSession('/customer/register', {
      method: 'POST',
      body: JSON.stringify(request),
    });
    return response.json();
  },
  
  // Special endpoint to securely resume the session when the page refreshes
  checkSession: async (): Promise<any> => {
    console.log('Checking session, cookies before request:');
    debugCookies();
    const response = await fetchWithSession('/auth/me', {
      method: 'GET',
    });
    const user = await response.json();
    console.log('Session check response:', user);
    return user;
  },
  
  logout: async (): Promise<void> => {
    // Logout endpoint is proxied
    const response = await fetch('/logout', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    if (!response.ok) {
      const text = await response.text();
      throw new Error(`Logout failed: ${response.status} ${text}`);
    }
  }
};
