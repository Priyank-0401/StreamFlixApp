import React, { useState } from 'react';
import { Loader2, CheckCircle, Shield } from 'lucide-react';
import * as CustomerService from '../../../services/customer/customerService';

interface MockPaymentStepProps {
  plan: CustomerService.Plan;
  customerId: number;
  paymentMethodId: number;
  onComplete: (data: any) => void;
}

export const MockPaymentStep: React.FC<MockPaymentStepProps> = ({ 
  plan, 
  customerId, 
  paymentMethodId, 
  onComplete 
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showSuccess, setShowSuccess] = useState(false);

  const handlePay = async () => {
    setError('');
    setLoading(true);

    try {
      const result = await CustomerService.completeSubscription(customerId, {
        planId: plan.planId,
        paymentMethodId: paymentMethodId,
        billingPeriod: plan.billingPeriod,
      });

      setShowSuccess(true);
      setTimeout(() => {
        onComplete({ subscriptionId: result.subscriptionId });
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Payment failed');
    } finally {
      setLoading(false);
    }
  };

  const formatPrice = (amount: number, currency: string): string => {
    const symbols: Record<string, string> = {
      'INR': '₹',
      'USD': '$',
      'GBP': '£',
    };
    return `${symbols[currency] || '₹'}${(amount / 100).toFixed(0)}`;
  };

  if (showSuccess) {
    return (
      <div className="card border-0" style={{ borderRadius: '24px', background: '#FFFFFF', border: '1px solid #E5E7EB', boxShadow: '0 12px 32px -8px rgba(0,0,0,0.05)' }}>
        <div className="card-body p-4 p-md-5 text-center">
          <div 
            className="d-inline-flex align-items-center justify-content-center rounded-circle mb-4"
            style={{ 
              width: '80px', 
              height: '80px', 
              background: '#ECFDF5'
            }}
          >
            <CheckCircle size={40} color="#059669" />
          </div>
          <h2 style={{ 
            fontFamily: 'Outfit, sans-serif', 
            fontSize: '32px', 
            fontWeight: 700, 
            color: '#111827',
            marginBottom: '12px',
            letterSpacing: '-1px'
          }}>
            Payment Successful!
          </h2>
          <p style={{ color: '#6B7280', fontSize: '16px' }}>
            Your subscription is now active. Redirecting you home...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="card border-0" style={{ borderRadius: '24px', background: '#FFFFFF', border: '1px solid #E5E7EB', boxShadow: '0 12px 32px -8px rgba(0,0,0,0.05)' }}>
      <div className="card-body p-4 p-md-5">
        <h2 style={{ 
          fontFamily: 'Outfit, sans-serif', 
          fontSize: '28px', 
          fontWeight: 700, 
          color: '#111827',
          marginBottom: '8px',
          letterSpacing: '-0.5px'
        }}>
          Confirm & Pay
        </h2>
        <p style={{ color: '#6B7280', fontSize: '15px', marginBottom: '32px' }}>
          Review your order and complete the payment.
        </p>

        {error && (
          <div className="alert alert-danger border-0 mb-4" style={{ borderRadius: '12px', background: '#FEF2F2', color: '#DC2626', fontSize: '14px' }}>
            {error}
          </div>
        )}

        {/* Order Summary */}
        <div 
          className="p-4 mb-4" 
          style={{ background: '#F9FAFB', borderRadius: '16px', border: '1px solid #E5E7EB' }}
        >
          <h4 style={{ fontFamily: 'Outfit, sans-serif', fontSize: '14px', fontWeight: 700, color: '#111827', marginBottom: '16px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Order Summary
          </h4>
          
          <div className="d-flex justify-content-between align-items-start mb-3">
            <div>
              <p style={{ fontSize: '16px', fontWeight: 600, color: '#111827', margin: 0 }}>
                {plan.name}
              </p>
              <p style={{ fontSize: '14px', color: '#6B7280', margin: 0 }}>
                {plan.billingPeriod === 'YEARLY' ? 'Annual Subscription' : 'Monthly Subscription'}
              </p>
            </div>
            <p style={{ fontSize: '16px', fontWeight: 600, color: '#111827', margin: 0 }}>
              {formatPrice(plan.defaultPriceMinor, plan.defaultCurrency)}
            </p>
          </div>

          {plan.trialDays > 0 && (
            <div 
              className="p-3 mb-3" 
              style={{ background: '#ECFDF5', borderRadius: '12px', border: '1px solid #A7F3D0' }}
            >
              <p style={{ fontSize: '13px', color: '#065F46', margin: 0, fontWeight: 600 }}>
                🎉 {plan.trialDays}-day free trial included!
              </p>
              <p style={{ fontSize: '12px', color: '#047857', margin: '2px 0 0 0' }}>
                You won't be charged until your trial ends.
              </p>
            </div>
          )}

          <div 
            className="pt-3 mt-3"
            style={{ borderTop: '1px dashed #D1D5DB' }}
          >
            <div className="d-flex justify-content-between align-items-center">
              <p style={{ fontSize: '16px', fontWeight: 600, color: '#111827', margin: 0 }}>
                Total (today)
              </p>
              <p style={{ fontSize: '24px', fontWeight: 700, color: '#5B4FFF', margin: 0 }}>
                {plan.trialDays > 0 ? 'FREE' : formatPrice(plan.defaultPriceMinor, plan.defaultCurrency)}
              </p>
            </div>
          </div>
        </div>

        {/* Security Badge */}
        <div className="d-flex align-items-center justify-content-center gap-2 mb-4 p-2" style={{ background: '#F0FDF4', borderRadius: '8px' }}>
          <Shield size={16} color="#059669" />
          <span style={{ fontSize: '13px', color: '#065F46', fontWeight: 500 }}>
            Secure, encrypted payment processing
          </span>
        </div>

        {/* Pay Button */}
        <button
          onClick={handlePay}
          disabled={loading}
          className="btn w-100 d-flex align-items-center justify-content-center gap-2"
          style={{
            background: '#5B4FFF',
            color: 'white',
            border: 'none',
            padding: '18px 24px',
            borderRadius: '9999px',
            fontSize: '16px',
            fontWeight: 600,
            transition: 'all 0.2s',
            opacity: loading ? 0.7 : 1,
            boxShadow: '0 4px 12px rgba(91, 79, 255, 0.2)'
          }}
        >
          {loading ? (
            <>
              <Loader2 size={24} className="spin" />
              Processing...
            </>
          ) : (
            <>
              {plan.trialDays > 0 ? 'Start Free Trial' : `Pay ${formatPrice(plan.defaultPriceMinor, plan.defaultCurrency)}`}
            </>
          )}
        </button>

        {/* Terms */}
        <p className="text-center mt-4 mb-0" style={{ fontSize: '12px', color: '#9CA3AF', lineHeight: '1.5' }}>
          By clicking above, you agree to our <a href="#" style={{ color: '#5B4FFF', textDecoration: 'none' }}>Terms of Service</a> and authorize recurring charges.
        </p>
      </div>
    </div>
  );
};
