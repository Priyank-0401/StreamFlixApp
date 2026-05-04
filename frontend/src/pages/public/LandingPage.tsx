import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  Play, 
  Monitor,
  Download,
  Users,
  Coffee,
  Check
} from 'lucide-react';
import '../../styles/LandingPage.css';
import { useAuthContext } from '../../context/AuthContext';

type Region = 'IN' | 'US' | 'GB';

interface RegionConfig {
  code: Region;
  name: string;
  flag: string;
  currency: string;
  currencySymbol: string;
}

const REGIONS: RegionConfig[] = [
  { code: 'IN', name: 'India', flag: '🇮🇳', currency: 'INR', currencySymbol: '₹' },
  { code: 'US', name: 'United States', flag: '🇺🇸', currency: 'USD', currencySymbol: '$' },
  { code: 'GB', name: 'United Kingdom', flag: '🇬🇧', currency: 'GBP', currencySymbol: '£' },
];

// Regional pricing data
const REGIONAL_PRICING: Record<Region, Record<string, { monthly: number; yearly: number; features: string[] }>> = {
  IN: {
    'Basic': { 
      monthly: 199, yearly: 1999, 
      features: ['HD (720p)', '1 Screen', 'Mobile + Tablet', '7-day Trial'] 
    },
    'Premium': { 
      monthly: 499, yearly: 4999, 
      features: ['4K + HDR', '4 Screens', 'All Devices', 'Downloads', '14-day Trial'] 
    },
  },
  US: {
    'Basic': { 
      monthly: 6.99, yearly: 69.99, 
      features: ['HD (720p)', '1 Screen', 'Mobile + Tablet', '7-day Trial'] 
    },
    'Premium': { 
      monthly: 14.99, yearly: 149.99, 
      features: ['4K + HDR', '4 Screens', 'All Devices', 'Downloads', '14-day Trial'] 
    },
  },
  GB: {
    'Basic': { 
      monthly: 5.99, yearly: 59.99, 
      features: ['HD (720p)', '1 Screen', 'Mobile + Tablet', '7-day Trial'] 
    },
    'Premium': { 
      monthly: 11.99, yearly: 119.99, 
      features: ['4K + HDR', '4 Screens', 'All Devices', 'Downloads', '14-day Trial'] 
    },
  },
};

// Features based on schema
const FEATURES = [
  { icon: Monitor, title: '4K + HDR', desc: 'Premium video quality' },
  { icon: Download, title: 'Offline Downloads', desc: '5-10GB free storage included' },
  { icon: Users, title: 'Multi-Screen', desc: '1-4 screens based on plan' },
  { icon: Coffee, title: 'Ad-Free Add-on', desc: 'Optional upgrade available' },
];

// Hero 3D cards data
const HERO_CARDS = [
  { title: 'Echoes of Tomorrow', subtitle: 'Sci-Fi Thriller', badge: 'Featured', meta: '2h 18m · 2024', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', icon: '🚀' },
  { title: 'The Last Meridian', subtitle: 'Crime Drama', badge: 'Original', meta: '8 Episodes · 2024', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', icon: '🎭' },
  { title: 'Deep Signal', subtitle: 'Documentary', badge: 'New', meta: '1h 45m · 2024', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', icon: '📡' },
];

// FAQ data
const FAQS = [
  { q: 'What is StreamFlix?', a: 'StreamFlix is a premium streaming platform offering movies, TV shows, documentaries, and exclusive original content across all genres.' },
  { q: 'Can I cancel anytime?', a: 'Yes! You can cancel your subscription at any time with no cancellation fees. Your access continues until the end of your billing period.' },
  { q: 'How many devices can I use?', a: 'Basic plan supports 1 screen. Premium plan supports 4 simultaneous screens on any device - TV, phone, tablet, or laptop.' },
  { q: 'Is there a free trial?', a: 'Yes! All plans include a 7-day free trial for Basic and 14-day for Premium.' },
  { q: 'What payment methods are accepted?', a: 'We accept credit/debit cards, UPI (India), and digital wallets. All payments are secure and tokenized.' },
  { q: 'Can I download content?', a: 'Yes, Premium subscribers can download content on mobile devices for offline viewing.' },
];

export const LandingPage: React.FC = () => {
  const { isAuthenticated, logout, isCustomer } = useAuthContext();
  
  // Debug logging on every render
  console.log('LandingPage render:', { isAuthenticated, isCustomer, isCustomerType: typeof isCustomer });
  
  // Debug logging when isCustomer changes
  useEffect(() => {
    console.log('isCustomer changed:', isCustomer);
  }, [isCustomer]);
  
  const [isYearly, setIsYearly] = useState(false);
  const [selectedRegion, setSelectedRegion] = useState<Region>('IN');
  const [expandedFaq, setExpandedFaq] = useState<number | null>(null);

  const currentRegion = REGIONS.find(r => r.code === selectedRegion) || REGIONS[0];
  const pricing = REGIONAL_PRICING[selectedRegion];

  const formatPrice = (price: number) => {
    return price.toLocaleString('en-IN');
  };

  const calculateSavings = (monthly: number, yearly: number) => {
    const monthlyCost = monthly * 12;
    const savings = ((monthlyCost - yearly) / monthlyCost) * 100;
    return Math.round(savings);
  };

  return (
    <div className="sf-root">
      {/* Navbar */}
      <nav className="sf-nav">
        <div className="sf-nav-logo">stream<span>flix</span></div>
        <div className="sf-nav-right">
          {isAuthenticated ? (
            <>
              {isCustomer === true ? (
                <Link to="/dashboard" className="sf-nav-login">Dashboard</Link>
              ) : (
                <Link to="/plans" className="sf-nav-login">Explore Plans</Link>
              )}
              <button onClick={logout} className="sf-nav-signup" style={{ background: 'none', border: 'none', cursor: 'pointer' }}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/login" className="sf-nav-login">Sign in</Link>
              <Link to="/register" className="sf-nav-signup">Sign up</Link>
            </>
          )}
        </div>
      </nav>

      {/* Hero - Two Column with 3D Tilt */}
      <div className="sf-hero-container">
        <div className="sf-hero-left">
          <section className="sf-hero">
            <div className="sf-hero-eyebrow">
              <div className="sf-hero-eyebrow-dot"></div>
              New this week
            </div>
            <h1 className="sf-hero-headline">The stories<br />you'll <em>lose sleep</em><br />over.</h1>
            <p className="sf-hero-sub">Thousands of films, series, and originals. One dark, beautiful home for all of it.</p>
            <div className="sf-hero-actions">
              <Link to={isAuthenticated ? "/dashboard" : "/register"} className="sf-btn-watch">
                <Play size={12} fill="white" />
                Start watching
              </Link>
              <button onClick={() => document.getElementById('pricing')?.scrollIntoView({ behavior: 'smooth' })} className="sf-btn-browse">Explore catalog</button>
            </div>
            <div className="sf-hero-meta">
              <div className="sf-meta-item">
                <div className="sf-meta-num">45K+</div>
                <div className="sf-meta-lbl">Titles</div>
              </div>
              <div className="sf-meta-sep"></div>
              <div className="sf-meta-item">
                <div className="sf-meta-num">180+</div>
                <div className="sf-meta-lbl">Countries</div>
              </div>
              <div className="sf-meta-sep"></div>
              <div className="sf-meta-item">
                <div className="sf-meta-num">4K HDR</div>
                <div className="sf-meta-lbl">Quality</div>
              </div>
              <div className="sf-meta-sep"></div>
              <div className="sf-meta-item">
                <div className="sf-meta-num">Day 1</div>
                <div className="sf-meta-lbl">New releases</div>
              </div>
            </div>
          </section>
        </div>

        {/* Static Hero Cards */}
        <div className="sf-hero-right">
          <div className="sf-hero-carousel">
            {/* Left Card - Echoes of Tomorrow */}
            <div className="sf-hero-card sf-hero-card-left">
              <div className="sf-hero-card-img" style={{ background: HERO_CARDS[0].color }}>
                <div className="sf-hero-card-badge">{HERO_CARDS[0].badge}</div>
                <div className="sf-hero-card-icon">{HERO_CARDS[0].icon}</div>
                <div className="sf-hero-card-label">
                  <div className="sf-hero-card-title">{HERO_CARDS[0].title}</div>
                  <div className="sf-hero-card-meta">{HERO_CARDS[0].subtitle}</div>
                </div>
              </div>
            </div>

            {/* Center Card - The Last Meridian */}
            <div className="sf-hero-card sf-hero-card-main">
              <div className="sf-hero-card-img" style={{ background: HERO_CARDS[1].color }}>
                <div className="sf-hero-card-badge">{HERO_CARDS[1].badge}</div>
                <div className="sf-hero-card-icon">{HERO_CARDS[1].icon}</div>
                <div className="sf-hero-card-label">
                  <div className="sf-hero-card-title">{HERO_CARDS[1].title}</div>
                  <div className="sf-hero-card-meta">
                    <span>{HERO_CARDS[1].subtitle}</span>
                    <span>•</span>
                    <span>{HERO_CARDS[1].meta}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Right Card - Deep Signal */}
            <div className="sf-hero-card sf-hero-card-right">
              <div className="sf-hero-card-img" style={{ background: HERO_CARDS[2].color }}>
                <div className="sf-hero-card-badge">{HERO_CARDS[2].badge}</div>
                <div className="sf-hero-card-icon">{HERO_CARDS[2].icon}</div>
                <div className="sf-hero-card-label">
                  <div className="sf-hero-card-title">{HERO_CARDS[2].title}</div>
                  <div className="sf-hero-card-meta">{HERO_CARDS[2].subtitle}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Features Strip */}
      <div className="sf-features-strip">
        {FEATURES.map((feat, idx) => (
          <div key={idx} className="sf-feat-item">
            <div className="sf-feat-icon">
              <feat.icon size={14} strokeWidth={1.8} />
            </div>
            <div className="sf-feat-title">{feat.title}</div>
            <div className="sf-feat-desc">{feat.desc}</div>
          </div>
        ))}
      </div>

      {/* Pricing Section */}
      <section id="pricing" className="sf-pricing-section">
        <div className="sf-pricing-header">
          <h2 className="sf-pricing-title">Choose your plan</h2>
          <div className="sf-pricing-controls">
            <div className="sf-toggle-wrap">
              <span className={`sf-toggle-label ${!isYearly ? 'active' : ''}`}>Monthly</span>
              <div className={`sf-toggle ${isYearly ? 'active' : ''}`} onClick={() => setIsYearly(!isYearly)}>
                <div className="sf-toggle-thumb"></div>
              </div>
              <span className={`sf-toggle-label ${isYearly ? 'active' : ''}`}>
                Yearly
                {isYearly && <span className="sf-savings-badge"> (Save {calculateSavings(pricing.Basic.monthly, pricing.Basic.yearly)}%)</span>}
              </span>
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
          {/* Basic Plan */}
          <div className="sf-plan-card">
            <div className="sf-plan-name">Basic</div>
            <div className="sf-plan-desc">Perfect for individuals who want to stream on mobile devices.</div>
            <div className="sf-plan-price">
              {currentRegion.currencySymbol}{isYearly ? formatPrice(pricing.Basic.yearly) : formatPrice(pricing.Basic.monthly)}
              <span>/{isYearly ? 'year' : 'month'}</span>
            </div>
            <div className="sf-plan-trial">7-day free trial included</div>
            <ul className="sf-plan-features">
              {pricing.Basic.features.map((feat, idx) => (
                <li key={idx}><Check size={14} /> {feat}</li>
              ))}
            </ul>
            <Link to="/register" className="sf-plan-btn">Get Started</Link>
          </div>

          {/* Premium Plan */}
          <div className="sf-plan-card featured">
            <div className="sf-plan-badge">Most Popular</div>
            <div className="sf-plan-name">Premium</div>
            <div className="sf-plan-desc">Best value for families and 4K enthusiasts.</div>
            <div className="sf-plan-price">
              {currentRegion.currencySymbol}{isYearly ? formatPrice(pricing.Premium.yearly) : formatPrice(pricing.Premium.monthly)}
              <span>/{isYearly ? 'year' : 'month'}</span>
            </div>
            <div className="sf-plan-trial">14-day free trial included</div>
            <ul className="sf-plan-features">
              {pricing.Premium.features.map((feat, idx) => (
                <li key={idx}><Check size={14} /> {feat}</li>
              ))}
            </ul>
            <Link to="/register" className="sf-plan-btn">Get Premium</Link>
          </div>
        </div>

        {/* Explore More Plans Button - Outside cards */}
        <div className="sf-pricing-footer">
          <Link to="/plans" className="sf-explore-plans-btn">Explore More Plans</Link>
        </div>
      </section>

      {/* FAQ Section */}
      <section className="sf-faq-section">
        <div className="sf-faq-header">
          <div className="sf-faq-label">Support & Help</div>
          <h2 className="sf-faq-title">Frequently Asked Questions</h2>
        </div>
        <div className="sf-faq-grid">
          {FAQS.map((faq, idx) => (
            <div 
              key={idx} 
              className={`sf-faq-item ${expandedFaq === idx ? 'expanded' : ''}`}
              onClick={() => setExpandedFaq(expandedFaq === idx ? null : idx)}
            >
              <div className="sf-faq-q">{faq.q}</div>
              <div className="sf-faq-a">{faq.a}</div>
            </div>
          ))}
        </div>
      </section>

      {/* CTA - Cinematic Redesign */}
      <div className="sf-cta-wrap">
        <div className="sf-cta-content">
          {isAuthenticated ? (
            isCustomer === true ? (
              <>
                <div className="sf-cta-eyebrow">Welcome back</div>
                <div className="sf-cta-title">Continue watching?</div>
                <div className="sf-cta-sub">Access your dashboard to manage your subscription and watch content.</div>
              </>
            ) : (
              <>
                <div className="sf-cta-eyebrow">Almost there</div>
                <div className="sf-cta-title">Complete your setup</div>
                <div className="sf-cta-sub">Choose a plan to start streaming your favorite content.</div>
              </>
            )
          ) : (
            <>
              <div className="sf-cta-eyebrow">Start streaming today</div>
              <div className="sf-cta-title">Ready to start watching?</div>
              <div className="sf-cta-sub">Join millions of viewers. Start with a 7-14 day free trial. Cancel anytime.</div>
            </>
          )}
        </div>
        <div className="sf-cta-actions">
          <div className="sf-cta-buttons">
            {isAuthenticated ? (
              isCustomer === true ? (
                <Link to="/dashboard" className="sf-cta-btn">
                  <Play size={16} fill="white" />
                  Go to Dashboard
                </Link>
              ) : (
                <Link to="/plans" className="sf-cta-btn">
                  <Play size={16} fill="white" />
                  Choose Your Plan
                </Link>
              )
            ) : (
              <>
                <Link to="/register" className="sf-cta-btn">
                  <Play size={16} fill="white" />
                  Start Free Trial
                </Link>
                <Link to="/plans" className="sf-cta-btn-outline">View Plans</Link>
              </>
            )}
          </div>
          {!isAuthenticated && (
            <Link to="/management/login" className="sf-cta-staff">
              <span>Staff Login</span>
              <span>→</span>
            </Link>
          )}
        </div>
      </div>

      {/* Footer */}
      <footer className="sf-footer">
        <div className="sf-footer-logo">stream<span>flix</span></div>
        <div className="sf-footer-links">
          <Link to="/privacy" className="sf-footer-link">Privacy</Link>
          <Link to="/terms" className="sf-footer-link">Terms</Link>
          <Link to="/help" className="sf-footer-link">Help</Link>
          <Link to="/careers" className="sf-footer-link">Careers</Link>
          <span className="sf-footer-link">© 2025</span>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
