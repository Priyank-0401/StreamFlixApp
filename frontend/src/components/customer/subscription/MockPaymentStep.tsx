import React, { useState } from 'react';
import { Loader2, CheckCircle, CreditCard, Shield } from 'lucide-react';
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
      <div className="card border-0" style={{ borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }}>
        <div className="card-body p-4 p-md-5 text-center">
          <div 
            className="d-inline-flex align-items-center justify-content-center rounded-circle mb-4"
            style={{ 
              width: '80px', 
              height: '80px', 
              background: 'rgba(209, 77, 40, 0.1)'
            }}
          >
            <CheckCircle size={40} color="#D14D28" />
          </div>
          <h2 style={{ 
            fontFamily: '"Playfair Display", serif', 
            fontSize: '28px', 
            fontWeight: 600, 
            color: '#121212',
            marginBottom: '12px'
          }}>
            Payment Successful!
          </h2>
          <p style={{ color: '#666', fontSize: '16px' }}>
            Your subscription is now active. Redirecting you to the home page...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="card border-0" style={{ borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }}>
      <div className="card-body p-4 p-md-5">
        <h2 style={{ 
          fontFamily: '"Playfair Display", serif', 
          fontSize: '24px', 
          fontWeight: 600, 
          color: '#121212',
          marginBottom: '8px'
        }}>
          Confirm & Pay
        </h2>
        <p style={{ color: '#666', marginBottom: '32px' }}>
          Review your order and complete the payment.
        </p>

        {error && (
          <div className="alert alert-danger mb-4" role="alert">
            {error}
          </div>
        )}

        {/* Order Summary */}
        <div 
          className="p-4 rounded-3 mb-4" 
          style={{ background: '#F9F7F2', border: '1px solid rgba(18,18,18,0.05)' }}
        >
          <h4 style={{ fontSize: '14px', fontWeight: 600, color: '#121212', marginBottom: '16px' }}>
            Order Summary
          </h4>
          
          <div className="d-flex justify-content-between align-items-start mb-3">
            <div>
              <p style={{ fontSize: '16px', fontWeight: 500, color: '#121212', margin: 0 }}>
                {plan.name}
              </p>
              <p style={{ fontSize: '13px', color: '#666', margin: 0 }}>
                {plan.billingPeriod} subscription
              </p>
            </div>
            <p style={{ fontSize: '16px', fontWeight: 500, color: '#121212', margin: 0 }}>
              {formatPrice(plan.defaultPriceMinor, plan.defaultCurrency)}
            </p>
          </div>

          {plan.trialDays > 0 && (
            <div 
              className="p-3 rounded-2 mb-3" 
              style={{ background: 'rgba(209, 77, 40, 0.08)' }}
            >
              <p style={{ fontSize: '13px', color: '#D14D28', margin: 0, fontWeight: 500 }}>
                🎉 {plan.trialDays}-day free trial included!
              </p>
              <p style={{ fontSize: '12px', color: '#666', margin: '4px 0 0 0' }}>
                You won't be charged until your trial ends.
              </p>
            </div>
          )}

          <div 
            className="pt-3 mt-3"
            style={{ borderTop: '1px solid rgba(18,18,18,0.1)' }}
          >
            <div className="d-flex justify-content-between align-items-center">
              <p style={{ fontSize: '16px', fontWeight: 600, color: '#121212', margin: 0 }}>
                Total (today)
              </p>
              <p style={{ fontSize: '24px', fontWeight: 600, color: '#D14D28', margin: 0 }}>
                {plan.trialDays > 0 ? 'FREE' : formatPrice(plan.defaultPriceMinor, plan.defaultCurrency)}
              </p>
            </div>
          </div>
        </div>

        {/* Payment Method Summary */}
        <div 
          className="p-4 rounded-3 mb-4" 
          style={{ background: 'white', border: '1px solid rgba(18,18,18,0.1)' }}
        >
          <div className="d-flex align-items-center gap-3">
            <div 
              className="d-flex align-items-center justify-content-center rounded-circle"
              style={{ width: '48px', height: '48px', background: '#F9F7F2' }}
            >
              <CreditCard size={24} color="#D14D28" />
            </div>
            <div>
              <p style={{ fontSize: '14px', fontWeight: 600, color: '#121212', margin: 0 }}>
                Payment Method Added
              </p>
              <p style={{ fontSize: '13px', color: '#666', margin: 0 }}>
                Your payment details are securely saved
              </p>
            </div>
            <CheckCircle size={20} color="#22C55E" style={{ marginLeft: 'auto' }} />
          </div>
        </div>

        {/* Security Badge */}
        <div className="d-flex align-items-center justify-content-center gap-2 mb-4">
          <Shield size={18} color="#22C55E" />
          <span style={{ fontSize: '13px', color: '#666' }}>
            Secure, encrypted payment processing
          </span>
        </div>

        {/* Pay Button */}
        <button
          onClick={handlePay}
          disabled={loading}
          className="btn w-100 d-flex align-items-center justify-content-center gap-2"
          style={{
            background: '#D14D28',
            color: 'white',
            border: 'none',
            padding: '18px 24px',
            borderRadius: '10px',
            fontSize: '16px',
            fontWeight: 600,
            textTransform: 'uppercase',
            letterSpacing: '0.1em',
            transition: 'all 0.3s',
            opacity: loading ? 0.7 : 1
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
        <p className="text-center mt-4" style={{ fontSize: '12px', color: '#999' }}>
          By clicking above, you agree to our Terms of Service and authorize recurring charges.
        </p>
      </div>
    </div>
  );
};
