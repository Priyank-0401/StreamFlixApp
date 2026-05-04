import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, Check, Loader2 } from 'lucide-react';
import * as CustomerService from '../../services/customer/customerService';
import { useAuthContext } from '../../context/AuthContext';

const countryCurrencyMap: Record<string, string> = {
  'IN': 'INR',
  'US': 'USD',
  'GB': 'GBP',
};

const currencySymbols: Record<string, string> = {
  'INR': '₹',
  'USD': '$',
  'GBP': '£',
};

export const PlansPage: React.FC = () => {
  const [plans, setPlans] = useState<CustomerService.Plan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedCountry, setSelectedCountry] = useState('IN');
  const { isAuthenticated } = useAuthContext();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/register');
      return;
    }

    const loadPlans = async () => {
      try {
        setLoading(true);
        const allPlans = await CustomerService.getAllPlans();
        setPlans(allPlans);
      } catch (err: any) {
        setError(err.message || 'Failed to load plans');
      } finally {
        setLoading(false);
      }
    };

    loadPlans();
  }, [isAuthenticated, navigate]);

  const formatPrice = (amount: number): string => {
    const currency = countryCurrencyMap[selectedCountry] || 'INR';
    const symbol = currencySymbols[currency] || '₹';
    return `${symbol}${(amount / 100).toFixed(0)}`;
  };

  const getPlanFeatures = (planName: string): string[] => {
    if (planName.toLowerCase().includes('basic')) {
      return [
        'HD (720p) Streaming',
        '1 Screen at a time',
        'Mobile + Tablet access',
        `${planName.includes('Monthly') ? '7' : '7'}-day free trial`,
        'Download Storage (5GB)'
      ];
    } else {
      return [
        '4K + HDR Streaming',
        '4 Screens simultaneously',
        'All Devices supported',
        `${planName.includes('Monthly') ? '14' : '14'}-day free trial`,
        'Priority Support',
        'Download Storage (10GB)'
      ];
    }
  };

  if (loading) {
    return (
      <div className="min-vh-100 d-flex align-items-center justify-content-center" style={{ background: '#0d0d0d' }}>
        <div className="text-center">
          <Loader2 size={48} className="spin" color="#5b4fff" />
          <p className="mt-3" style={{ color: '#a0a0a0' }}>Loading plans...</p>
        </div>
      </div>
    );
  }

  return (
    <div style={{ background: '#0d0d0d', minHeight: '100vh' }}>
      {/* Header */}
      <header className="py-4 px-4 border-bottom" style={{ borderColor: '#2a2a2a', background: '#1a1a1a' }}>
        <div className="mx-auto d-flex align-items-center justify-content-between" style={{ maxWidth: '1200px' }}>
          <Link to="/" className="text-decoration-none d-flex align-items-center gap-2">
            <ArrowLeft size={20} color="#ffffff" />
            <span style={{ fontSize: '14px', color: '#ffffff', fontWeight: 500 }}>Back to Home</span>
          </Link>
          <h1 style={{ 
            fontFamily: 'Inter, sans-serif', 
            fontSize: '24px', 
            fontWeight: 600, 
            color: '#ffffff',
            margin: 0 
          }}>
            Choose Your Plan
          </h1>
          <div style={{ width: '100px' }} />
        </div>
      </header>

      <div className="py-5 px-4">
        <div className="mx-auto" style={{ maxWidth: '1200px' }}>
          {error && (
            <div className="alert alert-danger mb-4" role="alert">
              {error}
            </div>
          )}

          {/* Country Selector */}
          <div className="mb-5 d-flex justify-content-center">
            <div className="d-flex gap-2 p-2 rounded-3" style={{ background: '#1a1a1a', border: '1px solid #2a2a2a' }}>
              {['IN', 'US', 'GB'].map((country) => (
                <button
                  key={country}
                  onClick={() => setSelectedCountry(country)}
                  className="btn"
                  style={{
                    fontSize: '13px',
                    fontWeight: selectedCountry === country ? 600 : 400,
                    background: selectedCountry === country ? '#5b4fff' : 'transparent',
                    color: selectedCountry === country ? 'white' : '#a0a0a0',
                    border: 'none',
                    padding: '8px 16px',
                    borderRadius: '6px'
                  }}
                >
                  {country === 'IN' && '🇮🇳 India'}
                  {country === 'US' && '🇺🇸 United States'}
                  {country === 'GB' && '🇬🇧 United Kingdom'}
                </button>
              ))}
            </div>
          </div>

          {/* Plans Grid */}
          <div className="row g-4">
            {plans.map((plan) => (
              <div key={plan.planId} className="col-12 col-md-6 col-lg-3">
                <div 
                  className="card h-100 border-0"
                  style={{ 
                    background: plan.name.toLowerCase().includes('premium') ? '#1a1a1a' : '#1a1a1a',
                    borderRadius: '16px',
                    boxShadow: '0 4px 20px rgba(0,0,0,0.3)',
                    transition: 'transform 0.3s, box-shadow 0.3s',
                    border: plan.name.toLowerCase().includes('premium') ? '1px solid #5b4fff' : '1px solid #2a2a2a'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-8px)';
                    e.currentTarget.style.boxShadow = '0 12px 40px rgba(91, 79, 255, 0.2)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 4px 20px rgba(0,0,0,0.3)';
                  }}
                >
                  <div className="card-body p-4">
                    {/* Plan Badge */}
                    <span 
                      className="badge mb-3"
                      style={{
                        background: plan.name.toLowerCase().includes('premium') ? 'rgba(91, 79, 255, 0.2)' : 'rgba(91, 79, 255, 0.1)',
                        color: plan.name.toLowerCase().includes('premium') ? '#5b4fff' : '#a0a0a0',
                        fontSize: '11px',
                        textTransform: 'uppercase',
                        letterSpacing: '0.1em',
                        padding: '6px 12px',
                        borderRadius: '20px'
                      }}
                    >
                      {plan.billingPeriod}
                    </span>

                    {/* Plan Name */}
                    <h3 style={{
                      fontFamily: 'Inter, sans-serif',
                      fontSize: '28px',
                      fontWeight: 600,
                      color: '#ffffff',
                      marginBottom: '8px'
                    }}>
                      {plan.name}
                    </h3>

                    {/* Description */}
                    <p style={{
                      fontSize: '13px',
                      color: '#a0a0a0',
                      marginBottom: '24px'
                    }}>
                      {plan.name.toLowerCase().includes('premium') 
                        ? 'Premium streaming experience' 
                        : 'Essential streaming access'}
                    </p>

                    {/* Price */}
                    <div className="mb-4">
                      <span style={{
                        fontFamily: 'Inter, sans-serif',
                        fontSize: '40px',
                        fontWeight: 600,
                        color: '#5b4fff'
                      }}>
                        {formatPrice(plan.defaultPriceMinor)}
                      </span>
                      <span style={{
                        fontSize: '14px',
                        color: '#a0a0a0',
                        marginLeft: '4px'
                      }}>
                        /{plan.billingPeriod.toLowerCase()}
                      </span>
                    </div>

                    {/* Trial Badge */}
                    {plan.trialDays > 0 && (
                      <div 
                        className="mb-4 p-2 rounded-2"
                        style={{ background: 'rgba(91, 79, 255, 0.1)' }}
                      >
                        <span style={{
                          fontSize: '13px',
                          color: '#5b4fff',
                          fontWeight: 600
                        }}>
                          {plan.trialDays}-day free trial
                        </span>
                      </div>
                    )}

                    {/* Features */}
                    <ul className="list-unstyled mb-4">
                      {getPlanFeatures(plan.name).map((feature, idx) => (
                        <li 
                          key={idx} 
                          className="d-flex align-items-start gap-2 mb-2"
                          style={{ fontSize: '13px', color: '#a0a0a0' }}
                        >
                          <Check size={16} color="#5b4fff" style={{ marginTop: '2px', flexShrink: 0 }} />
                          <span>{feature}</span>
                        </li>
                      ))}
                    </ul>

                    {/* Subscribe Button */}
                    <Link
                      to={`/subscribe?planId=${plan.planId}&step=1`}
                      className="btn w-100"
                      style={{
                        background: '#5b4fff',
                        color: 'white',
                        border: 'none',
                        padding: '14px 24px',
                        borderRadius: '10px',
                        fontSize: '13px',
                        fontWeight: 600,
                        textTransform: 'uppercase',
                        letterSpacing: '0.1em',
                        transition: 'all 0.3s'
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.background = '#6b5fff';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.background = '#5b4fff';
                      }}
                    >
                      Subscribe Now
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* No Plans Message */}
          {plans.length === 0 && !loading && (
            <div className="text-center py-5">
              <p style={{ color: '#a0a0a0' }}>No plans available at the moment.</p>
              <button 
                onClick={() => window.location.reload()} 
                className="btn mt-2"
                style={{ background: '#5b4fff', color: 'white', border: 'none' }}
              >
                Retry
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
