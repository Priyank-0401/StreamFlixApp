import React, { useState } from 'react';
import { Loader2, CreditCard, Smartphone, Check } from 'lucide-react';
import * as CustomerService from '../../../services/customer/customerService';

interface PaymentMethodStepProps {
  plan: CustomerService.Plan;
  customerId: number;
  onComplete: (data: { paymentMethodId: number }) => void;
}

export const PaymentMethodStep: React.FC<PaymentMethodStepProps> = ({ plan: _plan, customerId, onComplete }) => {
  const [paymentType, setPaymentType] = useState<'CARD' | 'UPI'>('CARD');
  const [cardData, setCardData] = useState({
    cardNumber: '',
    cardholderName: '',
    expiryMonth: '',
    expiryYear: '',
    cvv: '',
  });
  const [upiId, setUpiId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const detectCardBrand = (number: string): string => {
    if (number.startsWith('4')) return 'VISA';
    if (number.startsWith('5')) return 'MASTERCARD';
    if (number.startsWith('34') || number.startsWith('37')) return 'AMEX';
    return 'UNKNOWN';
  };

  const handleCardChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCardData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
    setFieldErrors(prev => ({ ...prev, [e.target.name]: '' }));
  };

  const handleUpiChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setUpiId(e.target.value);
    setFieldErrors(prev => ({ ...prev, upiId: '' }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setFieldErrors({});
    setLoading(true);

    try {
      let paymentData: CustomerService.PaymentMethodRequest;

      if (paymentType === 'CARD') {
        paymentData = {
          paymentType: 'CARD',
          cardNumber: cardData.cardNumber,
          cardholderName: cardData.cardholderName,
          expiryMonth: cardData.expiryMonth,
          expiryYear: cardData.expiryYear,
          cvv: cardData.cvv,
        };
      } else {
        paymentData = {
          paymentType: 'UPI',
          upiId: upiId,
        };
      }

      const result = await CustomerService.createPaymentMethod(customerId, paymentData);
      onComplete({ paymentMethodId: result.paymentMethodId });
    } catch (err: any) {
      setError(err.message || 'Failed to save payment method');
    } finally {
      setLoading(false);
    }
  };

  const cardBrand = detectCardBrand(cardData.cardNumber);

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
          Payment Method
        </h2>
        <p style={{ color: '#666', marginBottom: '32px' }}>
          Add a payment method for your subscription.
        </p>

        {error && (
          <div className="alert alert-danger mb-4" role="alert">
            {error}
          </div>
        )}

        {/* Payment Type Tabs */}
        <div className="d-flex gap-2 mb-4">
          <button
            type="button"
            onClick={() => setPaymentType('CARD')}
            className="btn flex-fill d-flex align-items-center justify-content-center gap-2"
            style={{
              padding: '16px',
              borderRadius: '10px',
              border: paymentType === 'CARD' ? '2px solid #D14D28' : '1px solid rgba(18,18,18,0.1)',
              background: paymentType === 'CARD' ? 'rgba(209, 77, 40, 0.05)' : 'white',
              color: paymentType === 'CARD' ? '#D14D28' : '#666'
            }}
          >
            <CreditCard size={20} />
            <span style={{ fontSize: '14px', fontWeight: 500 }}>Card</span>
          </button>
          <button
            type="button"
            onClick={() => setPaymentType('UPI')}
            className="btn flex-fill d-flex align-items-center justify-content-center gap-2"
            style={{
              padding: '16px',
              borderRadius: '10px',
              border: paymentType === 'UPI' ? '2px solid #D14D28' : '1px solid rgba(18,18,18,0.1)',
              background: paymentType === 'UPI' ? 'rgba(209, 77, 40, 0.05)' : 'white',
              color: paymentType === 'UPI' ? '#D14D28' : '#666'
            }}
          >
            <Smartphone size={20} />
            <span style={{ fontSize: '14px', fontWeight: 500 }}>UPI</span>
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {paymentType === 'CARD' ? (
            <>
              {/* Card Number */}
              <div className="mb-4">
                <label className="form-label" style={{ fontSize: '14px', fontWeight: 500, color: '#121212' }}>
                  Card Number
                </label>
                <div className="position-relative">
                  <input
                    type="text"
                    name="cardNumber"
                    value={cardData.cardNumber}
                    onChange={handleCardChange}
                    placeholder="1234 5678 9012 3456"
                    maxLength={16}
                    className="form-control"
                    style={{ 
                      padding: '12px 16px', 
                      border: '1px solid rgba(18,18,18,0.1)',
                      borderRadius: '10px',
                      fontSize: '14px',
                      paddingRight: '60px'
                    }}
                    required
                  />
                  {cardBrand !== 'UNKNOWN' && (
                    <span 
                      className="position-absolute"
                      style={{ 
                        right: '16px', 
                        top: '50%', 
                        transform: 'translateY(-50%)',
                        fontSize: '12px',
                        fontWeight: 600,
                        color: '#D14D28'
                      }}
                    >
                      {cardBrand}
                    </span>
                  )}
                </div>
                {fieldErrors['cardNumber'] && (
                  <span style={{ color: '#dc2626', fontSize: '12px', marginTop: '4px', display: 'block' }}>
                    {fieldErrors['cardNumber']}
                  </span>
                )}
              </div>

              {/* Cardholder Name */}
              <div className="mb-4">
                <label className="form-label" style={{ fontSize: '14px', fontWeight: 500, color: '#121212' }}>
                  Cardholder Name
                </label>
                <input
                  type="text"
                  name="cardholderName"
                  value={cardData.cardholderName}
                  onChange={handleCardChange}
                  placeholder="Name as on card"
                  className="form-control"
                  style={{ 
                    padding: '12px 16px', 
                    border: '1px solid rgba(18,18,18,0.1)',
                    borderRadius: '10px',
                    fontSize: '14px'
                  }}
                  required
                />
                {fieldErrors['cardholderName'] && (
                  <span style={{ color: '#dc2626', fontSize: '12px', marginTop: '4px', display: 'block' }}>
                    {fieldErrors['cardholderName']}
                  </span>
                )}
              </div>

              {/* Expiry and CVV */}
              <div className="row g-3 mb-4">
                <div className="col-6">
                  <label className="form-label" style={{ fontSize: '14px', fontWeight: 500, color: '#121212' }}>
                    Expiry Date
                  </label>
                  <div className="d-flex gap-2">
                    <div className="flex-1">
                      <input
                        type="text"
                        name="expiryMonth"
                        value={cardData.expiryMonth}
                        onChange={handleCardChange}
                        placeholder="MM"
                        maxLength={2}
                        className="form-control"
                        style={{ 
                          padding: '12px 16px', 
                          border: '1px solid rgba(18,18,18,0.1)',
                          borderRadius: '10px',
                          fontSize: '14px'
                        }}
                        required
                      />
                      {fieldErrors['expiryMonth'] && (
                        <span style={{ color: '#dc2626', fontSize: '12px', marginTop: '4px', display: 'block' }}>
                          {fieldErrors['expiryMonth']}
                        </span>
                      )}
                    </div>
                    <div className="flex-1">
                      <input
                        type="text"
                        name="expiryYear"
                        value={cardData.expiryYear}
                        onChange={handleCardChange}
                        placeholder="YYYY"
                        maxLength={4}
                        className="form-control"
                        style={{ 
                          padding: '12px 16px', 
                          border: '1px solid rgba(18,18,18,0.1)',
                          borderRadius: '10px',
                          fontSize: '14px'
                        }}
                        required
                      />
                      {fieldErrors['expiryYear'] && (
                        <span style={{ color: '#dc2626', fontSize: '12px', marginTop: '4px', display: 'block' }}>
                          {fieldErrors['expiryYear']}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
                <div className="col-6">
                  <label className="form-label" style={{ fontSize: '14px', fontWeight: 500, color: '#121212' }}>
                    CVV
                  </label>
                  <input
                    type="password"
                    name="cvv"
                    value={cardData.cvv}
                    onChange={handleCardChange}
                    placeholder="123"
                    maxLength={4}
                    className="form-control"
                    style={{ 
                      padding: '12px 16px', 
                      border: '1px solid rgba(18,18,18,0.1)',
                      borderRadius: '10px',
                      fontSize: '14px'
                    }}
                    required
                  />
                  {fieldErrors['cvv'] && (
                    <span style={{ color: '#dc2626', fontSize: '12px', marginTop: '4px', display: 'block' }}>
                      {fieldErrors['cvv']}
                    </span>
                  )}
                </div>
              </div>
            </>
          ) : (
            /* UPI */
            <div className="mb-4">
              <label className="form-label" style={{ fontSize: '14px', fontWeight: 500, color: '#121212' }}>
                UPI ID
              </label>
              <input
                type="text"
                value={upiId}
                onChange={handleUpiChange}
                placeholder="yourname@upi"
                className="form-control"
                style={{ 
                  padding: '12px 16px', 
                  border: '1px solid rgba(18,18,18,0.1)',
                  borderRadius: '10px',
                  fontSize: '14px'
                }}
                required
              />
              {fieldErrors['upiId'] && (
                <span style={{ color: '#dc2626', fontSize: '12px', marginTop: '4px', display: 'block' }}>
                  {fieldErrors['upiId']}
                </span>
              )}
              <small style={{ color: '#666', fontSize: '12px', marginTop: '4px', display: 'block' }}>
                Enter your UPI ID (e.g., name@upi)
              </small>
            </div>
          )}

          {/* Submit Button */}
          <button
            type="submit"
            disabled={loading}
            className="btn w-100 d-flex align-items-center justify-content-center gap-2"
            style={{
              background: '#D14D28',
              color: 'white',
              border: 'none',
              padding: '16px 24px',
              borderRadius: '10px',
              fontSize: '14px',
              fontWeight: 600,
              textTransform: 'uppercase',
              letterSpacing: '0.1em',
              transition: 'all 0.3s',
              opacity: loading ? 0.7 : 1
            }}
          >
            {loading ? (
              <>
                <Loader2 size={20} className="spin" />
                Saving...
              </>
            ) : (
              <>
                <Check size={20} />
                Confirm Payment Method
              </>
            )}
          </button>
        </form>

        {/* Security Note */}
        <div className="mt-4 text-center">
          <p style={{ fontSize: '12px', color: '#666' }}>
            Your payment information is securely stored and encrypted.
          </p>
        </div>
      </div>
    </div>
  );
};
