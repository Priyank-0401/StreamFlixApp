import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  CreditCard,
  Zap,
  ShieldCheck,
  Globe,
  Check
} from 'lucide-react';
import '../../styles/LandingPage.css';
import { useAuthContext } from '../../context/AuthContext';
import { publicService } from '../../services/public/publicService';
import type { PlanInfo } from '../../services/public/publicService';

type Region = 'IN' | 'US' | 'GB';

interface RegionConfig {
  code: Region;
  name: string;
  flag: string;
  currency: string;
  currencySymbol: string;
  exchangeRate: number; // Against INR
}

const REGIONS: RegionConfig[] = [
  { code: 'IN', name: 'India', flag: '🇮🇳', currency: 'INR', currencySymbol: '₹', exchangeRate: 1 },
  { code: 'US', name: 'United States', flag: '🇺🇸', currency: 'USD', currencySymbol: '$', exchangeRate: 84 },
  { code: 'GB', name: 'United Kingdom', flag: '🇬🇧', currency: 'GBP', currencySymbol: '£', exchangeRate: 105 },
];

// Features based on Streaming Platform
const FEATURES = [
  { icon: ShieldCheck, title: 'Download and go', desc: 'Save your data, watch offline on a train, plane, or submarine.' },
  { icon: Globe, title: 'Watch everywhere', desc: 'Stream on smart TVs, PlayStation, Xbox, Chromecast, Apple TV, Blu-ray players, and more.' },
  { icon: Zap, title: 'Create profiles for kids', desc: 'Send kids on adventures with their favorite characters in a space made just for them.' },
  { icon: CreditCard, title: 'Cancel anytime', desc: 'Join today, cancel anytime. No commitments.' },
];

// FAQ data
const FAQS = [
  { q: 'What is StreamFlix?', a: 'StreamFlix is a streaming service that offers a wide variety of award-winning TV shows, movies, anime, documentaries, and more on thousands of internet-connected devices.' },
  { q: 'How much does StreamFlix cost?', a: 'Watch StreamFlix on your smartphone, tablet, Smart TV, laptop, or streaming device, all for one fixed monthly fee. Plans range from ₹199 to ₹499 a month. No extra costs, no contracts.' },
  { q: 'Where can I watch?', a: 'Watch anywhere, anytime. Sign in with your StreamFlix account to watch instantly on the web at streamflix.com from your personal computer or on any internet-connected device.' },
  { q: 'How do I cancel?', a: 'StreamFlix is flexible. There are no pesky contracts and no commitments. You can easily cancel your account online in two clicks. There are no cancellation fees – start or stop your account anytime.' },
  { q: 'What can I watch on StreamFlix?', a: 'StreamFlix has an extensive library of feature films, documentaries, TV shows, anime, award-winning StreamFlix originals, and more. Watch as much as you want, anytime you want.' },
];

// Helper to determine hardcoded features based on plan base name
const getPlanFeatures = (planName: string) => {
  if (planName.includes('Basic')) return ['HD (720p)', '1 Screen', 'Mobile + Tablet', '7-day Trial'];
  if (planName.includes('Standard')) return ['Full HD (1080p)', '2 Screens', 'All Devices', 'Downloads'];
  if (planName.includes('Premium')) return ['4K + HDR', '4 Screens', 'All Devices', 'Downloads', 'Spatial Audio'];
  return ['Unlimited Content'];
};

const getPlanDescription = (planName: string) => {
  if (planName.includes('Basic')) return 'A great way to enjoy our content on your phone or tablet.';
  if (planName.includes('Standard')) return 'Great video quality in 1080p. Perfect for couples.';
  if (planName.includes('Premium')) return 'Our best video quality in 4K and HDR.';
  return 'The perfect streaming plan for you.';
};

export const LandingPage: React.FC = () => {
  const { isAuthenticated, logout, isCustomer } = useAuthContext();

  const [isYearly, setIsYearly] = useState(false);
  const [selectedRegion, setSelectedRegion] = useState<Region>('IN');
  const [expandedFaq, setExpandedFaq] = useState<number | null>(null);
  const [plans, setPlans] = useState<PlanInfo[]>([]);

  const currentRegion = REGIONS.find(r => r.code === selectedRegion) || REGIONS[0];

  useEffect(() => {
    publicService.getPublicPlans().then(setPlans);
  }, []);

  const formatPrice = (priceMinor: number) => {
    // Backend returns INR in paise (minor units)
    const baseInr = priceMinor / 100;
    
    // If it's India, keep the exact base price, otherwise convert
    if (currentRegion.code === 'IN') {
      return baseInr.toLocaleString('en-IN');
    }
    
    // Simple mock conversion for UI demonstration
    const converted = baseInr / currentRegion.exchangeRate;
    
    // Make US/GB prices look nice (e.g. $9.99 instead of $11.89)
    // Find the nearest .99 if it's over 1
    if (converted > 1) {
       return (Math.floor(converted) + 0.99).toFixed(2);
    }
    return converted.toFixed(2);
  };

  const currentBillingPeriod = isYearly ? 'YEARLY' : 'MONTHLY';
  
  // Sort plans so Basic is first, Standard is middle, Premium is last
  const displayedPlans = [...plans]
    .filter(p => p.billingPeriod === currentBillingPeriod)
    .sort((a, b) => a.defaultPriceMinor - b.defaultPriceMinor);

  return (
    <div className="sf-root">
      {/* Navbar */}
      <nav className="sf-nav">
        <div className="sf-nav-logo">Stream<span>Flix</span></div>
        <div className="sf-nav-right">
          {isAuthenticated ? (
            <>
              {isCustomer === true ? (
                <Link to="/dashboard" className="sf-nav-login">Customer Dashboard</Link>
              ) : (
                <Link to="/admin" className="sf-nav-login">Admin Console</Link>
              )}
              <button onClick={logout} className="sf-nav-login" style={{ background: 'none', border: 'none', cursor: 'pointer' }}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/login" className="sf-nav-login">Sign in</Link>
              <Link to="/register" className="sf-nav-signup">Start Free Trial</Link>
            </>
          )}
        </div>
      </nav>

      {/* Modern Streaming Hero with Blue Gradient Blob and Poster Carousel */}
      <section className="sf-hero-section">
        <div className="sf-hero-blob"></div>
        <div className="sf-hero-content">
          <div className="sf-hero-eyebrow">
            <span className="sf-eyebrow-badge">New</span> Discover the Series Streaming Experience
          </div>
          <h1 className="sf-hero-title">Unlimited movies,<br />TV shows, and more.</h1>
          <p className="sf-hero-subtitle">
            Our expert admins prepare amazing and trending series for you to watch online. Watch anywhere. Cancel anytime.
          </p>
          <div className="sf-hero-actions">
            {isAuthenticated ? (
               <Link to={isCustomer ? "/dashboard" : "/admin"} className="sf-btn-primary">Go to Dashboard</Link>
            ) : (
               <>
                 <Link to="/register" className="sf-btn-primary">Get started</Link>
                 <button onClick={() => document.getElementById('pricing')?.scrollIntoView({ behavior: 'smooth' })} className="sf-btn-secondary">View plans</button>
               </>
            )}
          </div>
        </div>
        
        {/* Curved 3D Poster Carousel */}
        <div className="sf-hero-carousel-container">
          <div className="sf-hero-carousel">
            <div className="sf-carousel-card left">
              <img src="https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&q=80" alt="Cinematic Movie Scene" />
            </div>
            <div className="sf-carousel-card center">
              <img src="https://images.unsplash.com/photo-1626814026160-2237a95fc5a0?w=800&q=80" alt="Family Movie Scene" />
            </div>
            <div className="sf-carousel-card right">
              <img src="https://images.unsplash.com/photo-1574267432553-4b462808152a?w=600&q=80" alt="Animated Movie Scene" />
            </div>
          </div>
        </div>
      </section>

      {/* Features Strip */}
      <section className="sf-features-strip">
        {FEATURES.map((feat, idx) => (
          <div key={idx} className="sf-feat-item">
            <div className="sf-feat-icon">
              <feat.icon size={24} strokeWidth={2} />
            </div>
            <div className="sf-feat-title">{feat.title}</div>
            <div className="sf-feat-desc">{feat.desc}</div>
          </div>
        ))}
      </section>

      {/* Pricing Section */}
      <section id="pricing" className="sf-pricing-section">
        <div className="sf-pricing-header">
          <h2 className="sf-pricing-title">Simple, transparent pricing</h2>
          <div className="sf-pricing-controls" style={{ justifyContent: 'center' }}>
            <div className="sf-toggle-wrap">
              <span className={`sf-toggle-label ${!isYearly ? 'active' : ''}`}>Monthly</span>
              <div className={`sf-toggle ${isYearly ? 'active' : ''}`} onClick={() => setIsYearly(!isYearly)}>
                <div className="sf-toggle-thumb"></div>
              </div>
              <span className={`sf-toggle-label ${isYearly ? 'active' : ''}`}>Yearly</span>
            </div>
            
            <select
              className="sf-region-select"
              value={selectedRegion}
              onChange={(e) => setSelectedRegion(e.target.value as Region)}
            >
              {REGIONS.map(r => (
                <option key={r.code} value={r.code}>{r.flag} {r.name}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="sf-pricing-grid">
          {displayedPlans.length === 0 ? (
            <p>Loading plans...</p>
          ) : (
            displayedPlans.map(plan => {
              const isPopular = plan.name.includes('Standard');
              const isFeatured = plan.name.includes('Premium');
              const baseName = plan.name.replace(' Monthly', '').replace(' Yearly', '');
              
              return (
                <div key={plan.planId} className={`sf-plan-card ${isFeatured ? 'featured' : ''}`}>
                  {isPopular && <div className="sf-plan-badge" style={{ backgroundColor: '#4B5563' }}>Popular</div>}
                  {isFeatured && <div className="sf-plan-badge">Best Value</div>}
                  <div className="sf-plan-name">{baseName}</div>
                  <div className="sf-plan-desc">{getPlanDescription(baseName)}</div>
                  <div className="sf-plan-price">
                    {currentRegion.currencySymbol}{formatPrice(plan.defaultPriceMinor)}
                    <span>/{isYearly ? 'year' : 'month'}</span>
                  </div>
                  <div className="sf-plan-trial">{plan.trialDays}-day free trial</div>
                  <ul className="sf-plan-features">
                    {getPlanFeatures(baseName).map((feat, idx) => (
                      <li key={idx}><Check size={16} /> {feat}</li>
                    ))}
                  </ul>
                  <Link to={`/register?planId=${plan.planId}`} className="sf-plan-btn">Choose {baseName}</Link>
                </div>
              );
            })
          )}
        </div>
      </section>

      {/* FAQ Section */}
      <section className="sf-faq-section">
        <div className="sf-faq-header">
          <h2 className="sf-faq-title">Frequently Asked Questions</h2>
          <p className="sf-faq-subtitle">Everything you need to know about StreamFlix and billing.</p>
        </div>
        <div className="sf-faq-grid">
          {FAQS.map((faq, idx) => (
            <div
              key={idx}
              className={`sf-faq-item ${expandedFaq === idx ? 'expanded' : ''}`}
              onClick={() => setExpandedFaq(expandedFaq === idx ? null : idx)}
            >
              <div className="sf-faq-q">
                {faq.q}
                <div className="sf-faq-icon"></div>
              </div>
              <div className="sf-faq-a">
                <div className="sf-faq-a-inner">{faq.a}</div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Footer */}
      <footer className="sf-footer">
        <div className="sf-footer-logo">Stream<span>Flix</span></div>
        <div className="sf-footer-links">
          <Link to="/privacy" className="sf-footer-link">Privacy</Link>
          <Link to="/terms" className="sf-footer-link">Terms</Link>
          <Link to="/help" className="sf-footer-link">Help</Link>
          <Link to="/careers" className="sf-footer-link">Careers</Link>
          {!isAuthenticated && (
            <Link to="/management/login" className="sf-footer-link sf-staff-login">
              Staff Login →
            </Link>
          )}
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
