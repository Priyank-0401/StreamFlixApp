import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, CheckCircle, CreditCard, User, Check } from 'lucide-react';
import { CustomerDetailsStep } from '../../components/customer/subscription/CustomerDetailsStep';
import { PaymentMethodStep } from '../../components/customer/subscription/PaymentMethodStep';
import { MockPaymentStep } from '../../components/customer/subscription/MockPaymentStep';
import * as CustomerService from '../../services/customer/customerService';
import { useAuthContext } from '../../context/AuthContext';

export const SubscriptionFlow: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthContext();
  
  const planId = parseInt(searchParams.get('planId') || '0');
  const currentStep = parseInt(searchParams.get('step') || '1');
  
  const [customerId, setCustomerId] = useState<number | null>(null);
  const [paymentMethodId, setPaymentMethodId] = useState<number | null>(null);
  const [plan, setPlan] = useState<CustomerService.Plan | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/register');
      return;
    }

    if (!planId) {
      navigate('/plans');
      return;
    }

    // Load plan details
    const loadPlan = async () => {
      try {
        setLoading(true);
        const plans = await CustomerService.getAllPlans();
        const selectedPlan = plans.find(p => p.planId === planId);
        if (selectedPlan) {
          setPlan(selectedPlan);
        } else {
          setError('Plan not found');
        }
      } catch (err: any) {
        setError(err.message || 'Failed to load plan');
      } finally {
        setLoading(false);
      }
    };

    loadPlan();
  }, [isAuthenticated, planId, navigate]);

  const handleStepComplete = (step: number, data: any) => {
    if (step === 1) {
      setCustomerId(data.customerId);
      navigate(`/subscribe?planId=${planId}&step=2`);
    } else if (step === 2) {
      setPaymentMethodId(data.paymentMethodId);
      navigate(`/subscribe?planId=${planId}&step=3`);
    } else if (step === 3) {
      // Subscription complete - redirect to landing page with success
      navigate('/?subscription=success');
    }
  };

  const handleBack = () => {
    if (currentStep > 1) {
      navigate(`/subscribe?planId=${planId}&step=${currentStep - 1}`);
    } else {
      navigate('/plans');
    }
  };

  if (loading) {
    return (
      <div className="min-vh-100 d-flex align-items-center justify-content-center" style={{ background: '#f8f9fa' }}>
        <div className="text-center">
          <div className="spinner-border" role="status" style={{ color: '#5b4fff' }}>
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-3" style={{ color: '#6b7280' }}>Loading...</p>
        </div>
      </div>
    );
  }

  if (error || !plan) {
    return (
      <div className="min-vh-100 d-flex align-items-center justify-content-center" style={{ background: '#f8f9fa' }}>
        <div className="text-center">
          <p style={{ color: '#5b4fff' }}>{error || 'Plan not found'}</p>
          <Link to="/plans" className="btn mt-3" style={{ background: '#5b4fff', color: 'white', border: 'none' }}>
            Back to Plans
          </Link>
        </div>
      </div>
    );
  }

  const steps = [
    { number: 1, title: 'Your Details', icon: User },
    { number: 2, title: 'Payment Method', icon: CreditCard },
    { number: 3, title: 'Confirm & Pay', icon: CheckCircle },
  ];

  return (
    <div style={{ background: '#f8f9fa', minHeight: '100vh' }}>
      {/* Header */}
      <header className="py-4 px-4 border-bottom" style={{ borderColor: '#e5e7eb', background: 'white' }}>
        <div className="mx-auto" style={{ maxWidth: '800px' }}>
          <div className="d-flex align-items-center justify-content-between">
            <button 
              onClick={handleBack}
              className="btn btn-link text-decoration-none d-flex align-items-center gap-2 p-0"
              style={{ color: '#1f2937' }}
            >
              <ArrowLeft size={20} />
              <span style={{ fontSize: '14px', fontWeight: 500 }}>
                {currentStep === 1 ? 'Back to Plans' : 'Previous Step'}
              </span>
            </button>
            
            <h1 style={{ 
              fontFamily: '"Playfair Display", serif', 
              fontSize: '22px', 
              fontWeight: 600, 
              color: '#1f2937',
              margin: 0 
            }}>
              Complete Your Subscription
            </h1>
            
            <div style={{ width: '120px' }} />
          </div>
        </div>
      </header>

      {/* Step Indicator */}
      <div className="py-4 px-4" style={{ background: 'white', borderBottom: '1px solid #e5e7eb' }}>
        <div className="mx-auto" style={{ maxWidth: '600px' }}>
          <div className="d-flex justify-content-between">
            {steps.map((step, index) => {
              const Icon = step.icon;
              const isActive = currentStep === step.number;
              const isCompleted = currentStep > step.number;
              const isPending = currentStep < step.number;

              return (
                <div key={step.number} className="d-flex flex-column align-items-center" style={{ flex: 1 }}>
                  {/* Connector Line */}
                  {index > 0 && (
                    <div 
                      style={{
                        position: 'absolute',
                        left: 0,
                        right: 0,
                        top: '20px',
                        height: '2px',
                        background: isCompleted ? '#5b4fff' : '#e5e7eb',
                        transform: `translateX(${(index - 1) * -50}%)`,
                        width: '100%',
                        zIndex: 0
                      }}
                    />
                  )}

                  {/* Step Circle */}
                  <div
                    className="d-flex align-items-center justify-content-center rounded-circle"
                    style={{
                      width: '40px',
                      height: '40px',
                      background: isCompleted ? '#5b4fff' : isActive ? '#1f2937' : 'white',
                      border: isPending ? '2px solid #e5e7eb' : 'none',
                      zIndex: 1,
                      position: 'relative'
                    }}
                  >
                    {isCompleted ? (
                      <Check size={20} color="white" />
                    ) : (
                      <Icon size={18} color={isActive ? 'white' : isPending ? '#999' : 'white'} />
                    )}
                  </div>

                  {/* Step Title */}
                  <span 
                    className="mt-2"
                    style={{
                      fontSize: '12px',
                      fontWeight: isActive ? 600 : 400,
                      color: isActive ? '#1f2937' : isPending ? '#9ca3af' : '#6b7280'
                    }}
                  >
                    {step.title}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Step Content */}
      <div className="py-5 px-4">
        <div className="mx-auto" style={{ maxWidth: '600px' }}>
          {currentStep === 1 && (
            <CustomerDetailsStep 
              plan={plan} 
              onComplete={(data) => handleStepComplete(1, data)} 
            />
          )}
          {currentStep === 2 && customerId && (
            <PaymentMethodStep 
              plan={plan}
              customerId={customerId}
              onComplete={(data) => handleStepComplete(2, data)} 
            />
          )}
          {currentStep === 3 && customerId && paymentMethodId && (
            <MockPaymentStep 
              plan={plan}
              customerId={customerId}
              paymentMethodId={paymentMethodId}
              onComplete={(data) => handleStepComplete(3, data)} 
            />
          )}
        </div>
      </div>
    </div>
  );
};
