import React, { useState } from 'react';
import { Loader2, MapPin, Phone, Globe } from 'lucide-react';
import * as CustomerService from '../../../services/customer/customerService';

interface CustomerDetailsStepProps {
  plan: CustomerService.Plan;
  onComplete: (data: { customerId: number }) => void;
}

const countryCurrencyMap: Record<string, string> = {
  'IN': 'INR',
  'US': 'USD',
  'GB': 'GBP',
};

export const CustomerDetailsStep: React.FC<CustomerDetailsStepProps> = ({ plan, onComplete }) => {
  const [formData, setFormData] = useState({
    phone: '',
    country: 'IN',
    state: '',
    city: '',
    addressLine1: '',
    postalCode: '',
    currency: 'INR',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleCountryChange = (country: string) => {
    setFormData(prev => ({
      ...prev,
      country,
      currency: countryCurrencyMap[country] || 'INR'
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const result = await CustomerService.registerCustomerDetails(formData);
      onComplete({ customerId: result.customerId });
    } catch (err: any) {
      setError(err.message || 'Failed to save your details');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

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
          Your Details
        </h2>
        <p style={{ color: '#666', marginBottom: '32px' }}>
          Please provide your contact and address information to continue.
        </p>

        {error && (
          <div className="alert alert-danger mb-4" role="alert">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Country Selection */}
          <div className="mb-4">
            <label className="form-label d-flex align-items-center gap-2" style={{ fontSize: '14px', fontWeight: 500, color: '#121212' }}>
              <Globe size={18} color="#D14D28" />
              Country
            </label>
            <select
              name="country"
              value={formData.country}
              onChange={(e) => handleCountryChange(e.target.value)}
              className="form-select"
              style={{ 
                padding: '12px 16px', 
                border: '1px solid rgba(18,18,18,0.1)',
                borderRadius: '10px',
                fontSize: '14px'
              }}
              required
            >
              <option value="IN">🇮🇳 India</option>
              <option value="US">🇺🇸 United States</option>
              <option value="GB">🇬🇧 United Kingdom</option>
            </select>
          </div>

          {/* Phone */}
          <div className="mb-4">
            <label className="form-label d-flex align-items-center gap-2" style={{ fontSize: '14px', fontWeight: 500, color: '#121212' }}>
              <Phone size={18} color="#D14D28" />
              Phone Number
            </label>
            <input
              type="tel"
              name="phone"
              value={formData.phone}
              onChange={handleChange}
              placeholder="Enter your phone number"
              className="form-control"
              style={{ 
                padding: '12px 16px', 
                border: '1px solid rgba(18,18,18,0.1)',
                borderRadius: '10px',
                fontSize: '14px'
              }}
              required
            />
          </div>

          {/* Address */}
          <div className="mb-4">
            <label className="form-label d-flex align-items-center gap-2" style={{ fontSize: '14px', fontWeight: 500, color: '#121212' }}>
              <MapPin size={18} color="#D14D28" />
              Address
            </label>
            <input
              type="text"
              name="addressLine1"
              value={formData.addressLine1}
              onChange={handleChange}
              placeholder="Street address"
              className="form-control mb-3"
              style={{ 
                padding: '12px 16px', 
                border: '1px solid rgba(18,18,18,0.1)',
                borderRadius: '10px',
                fontSize: '14px'
              }}
              required
            />
            <div className="row g-3">
              <div className="col-md-6">
                <input
                  type="text"
                  name="city"
                  value={formData.city}
                  onChange={handleChange}
                  placeholder="City"
                  className="form-control"
                  style={{ 
                    padding: '12px 16px', 
                    border: '1px solid rgba(18,18,18,0.1)',
                    borderRadius: '10px',
                    fontSize: '14px'
                  }}
                  required
                />
              </div>
              <div className="col-md-6">
                <input
                  type="text"
                  name="state"
                  value={formData.state}
                  onChange={handleChange}
                  placeholder="State"
                  className="form-control"
                  style={{ 
                    padding: '12px 16px', 
                    border: '1px solid rgba(18,18,18,0.1)',
                    borderRadius: '10px',
                    fontSize: '14px'
                  }}
                  required
                />
              </div>
            </div>
          </div>

          {/* Postal Code */}
          <div className="mb-4">
            <label className="form-label" style={{ fontSize: '14px', fontWeight: 500, color: '#121212' }}>
              Postal Code
            </label>
            <input
              type="text"
              name="postalCode"
              value={formData.postalCode}
              onChange={handleChange}
              placeholder="Enter postal code"
              className="form-control"
              style={{ 
                padding: '12px 16px', 
                border: '1px solid rgba(18,18,18,0.1)',
                borderRadius: '10px',
                fontSize: '14px'
              }}
              required
            />
          </div>

          {/* Currency (Auto-populated) */}
          <div className="mb-4">
            <label className="form-label" style={{ fontSize: '14px', fontWeight: 500, color: '#121212' }}>
              Currency
            </label>
            <input
              type="text"
              value={formData.currency}
              disabled
              className="form-control"
              style={{ 
                padding: '12px 16px', 
                border: '1px solid rgba(18,18,18,0.1)',
                borderRadius: '10px',
                fontSize: '14px',
                background: '#F9F7F2'
              }}
            />
            <small style={{ color: '#666', fontSize: '12px' }}>
              Currency is automatically set based on your country
            </small>
          </div>

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
              'Continue to Payment'
            )}
          </button>
        </form>

        {/* Plan Summary */}
        <div 
          className="mt-4 p-4 rounded-3" 
          style={{ background: '#F9F7F2', border: '1px solid rgba(18,18,18,0.05)' }}
        >
          <h4 style={{ fontSize: '14px', fontWeight: 600, color: '#121212', marginBottom: '12px' }}>
            Selected Plan
          </h4>
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <p style={{ fontSize: '16px', fontWeight: 500, color: '#121212', margin: 0 }}>
                {plan.name}
              </p>
              <p style={{ fontSize: '13px', color: '#666', margin: 0 }}>
                {plan.billingPeriod} billing
              </p>
            </div>
            <p style={{ fontSize: '20px', fontWeight: 600, color: '#D14D28', margin: 0 }}>
              {(plan.defaultPriceMinor / 100).toFixed(0)} {plan.defaultCurrency}
            </p>
          </div>
          {plan.trialDays > 0 && (
            <p style={{ fontSize: '13px', color: '#D14D28', marginTop: '8px', marginBottom: 0 }}>
              Includes {plan.trialDays}-day free trial
            </p>
          )}
        </div>
      </div>
    </div>
  );
};
