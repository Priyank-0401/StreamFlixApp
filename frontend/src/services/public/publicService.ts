const API_BASE_URL = 'http://localhost:8765/api';

export interface PlanInfo {
  planId: number;
  name: string;
  billingPeriod: 'MONTHLY' | 'YEARLY';
  priceMinor: number;
  currency: string;
  trialDays: number;
  features: string[];
  isPopular?: boolean;
}

async function publicFetch<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || 'Request failed');
  }

  const text = await response.text();
  return text ? JSON.parse(text) : ({} as T);
}

export const publicService = {
  // Fetch all active plans for public display
  getPublicPlans: async (): Promise<PlanInfo[]> => {
    try {
      return await publicFetch<PlanInfo[]>('/public/plans');
    } catch (error) {
      console.error('Failed to fetch plans:', error);
      // Return fallback plans if API fails
      return [];
    }
  },
};
