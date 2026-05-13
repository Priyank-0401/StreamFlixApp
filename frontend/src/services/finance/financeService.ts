// API base URL — proxied to backend by both CRA and Vite
const API_BASE = '/api/finance';

export interface RevenueSnapshot {
  id: number;
  snapshotDate: string;
  mrrMinor: number;
  arrMinor: number;
  arpuMinor: number;
  activeCustomers: number;
  newCustomers: number;
  churnedCustomers: number;
  grossChurnPercent: number;
  netChurnPercent: number;
  ltvMinor: number;
  totalRevenueMinor: number;
  totalRefundsMinor: number;
  createdAt: string;
}

export interface FinanceStatsResponse {
  mrrMinor: number;
  arrMinor: number;
  arpuMinor: number;
  ltvMinor: number;
  churnRate: number;
  totalInvoices: number;
  paidInvoices: number;
  pendingInvoices: number;
  failedInvoices: number;
  totalCollectedMinor: number;
  pendingCollectionMinor: number;
  recentSnapshots: RevenueSnapshot[];
}

export interface SubscriptionFinanceDTO {
  subscriptionId: number;
  customerName: string;
  customerEmail: string;
  planName: string;
  billingPeriod: string;
  monthlyValueMinor: number;
  annualValueMinor: number;
  currency: string;
  status: string;
  startDate: string;
}

export interface CustomerFinanceDTO {
  customerId: number;
  fullName: string;
  email: string;
  country: string;
  currency: string;
  activeSubscriptionsCount: number;
  monthlyContributionMinor: number;
}

export interface ChurnFinanceDTO {
  subscriptionId: number;
  customerName: string;
  customerEmail: string;
  planName: string;
  lostMonthlyRevenueMinor: number;
  currency: string;
  canceledAt: string;
  reason: string;
}

export interface InvoiceLineItemDTO {
  lineItemId: number;
  description: string;
  lineType: string;
  quantity: number;
  unitPriceMinor: number;
  amountMinor: number;
  periodStart: string | null;
  periodEnd: string | null;
}

export interface InvoiceDTO {
  invoiceId: number;
  invoiceNumber: string;
  subscriptionId: number;
  status: string;
  billingReason: string;
  issueDate: string;
  dueDate: string | null;
  subtotalMinor: number;
  taxMinor: number;
  discountMinor: number;
  totalMinor: number;
  balanceMinor: number;
  currency: string;
  lineItems: InvoiceLineItemDTO[];
}

// Generic fetch wrapper with session cookie support
async function financeFetch<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const url = `${API_BASE}${endpoint}`;
  
  const res = await fetch(url, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!res.ok) {
    const errorText = await res.text();
    console.error(`Finance API error (${res.status}):`, errorText);
    let errorMessage = `Request failed: ${res.status}`;
    try {
      const errorJson = JSON.parse(errorText);
      errorMessage = errorJson.message || errorMessage;
    } catch {
      errorMessage = errorText || errorMessage;
    }
    throw new Error(errorMessage);
  }

  const text = await res.text();
  return text ? JSON.parse(text) : ({} as T);
}

// Endpoints
export const getFinanceStats = () => financeFetch<FinanceStatsResponse>('/stats');
export const getMrrSubscriptions = () => financeFetch<SubscriptionFinanceDTO[]>('/mrr-subscriptions');
export const getArrSubscriptions = () => financeFetch<SubscriptionFinanceDTO[]>('/arr-subscriptions');
export const getArpuCustomers = () => financeFetch<CustomerFinanceDTO[]>('/arpu-customers');
export const getChurnedSubscriptions = () => financeFetch<ChurnFinanceDTO[]>('/churned-subscriptions');
export const getFinanceInvoices = (status?: string) => 
  financeFetch<InvoiceDTO[]>(status ? `/invoices?status=${status}` : '/invoices');
export const recordSnapshot = () => financeFetch<void>('/snapshots/record', { method: 'POST' });

// Report Downloads
export const downloadRevenueSnapshot = async () => {
  const res = await fetch(`${API_BASE}/reports/revenue-snapshot`, { credentials: 'include' });
  if (!res.ok) throw new Error('Failed to download Revenue Snapshot');
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `revenue_snapshot_${new Date().toISOString().split('T')[0]}.pdf`;
  document.body.appendChild(a);
  a.click();
  a.remove();
};

export const downloadTaxReport = async () => {
  const res = await fetch(`${API_BASE}/reports/tax-compliance`, { credentials: 'include' });
  if (!res.ok) throw new Error('Failed to download Tax Report');
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `tax_compliance_report_${new Date().toISOString().split('T')[0]}.csv`;
  document.body.appendChild(a);
  a.click();
  a.remove();
};
