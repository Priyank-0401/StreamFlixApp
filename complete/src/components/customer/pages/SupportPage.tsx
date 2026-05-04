import React, { useState } from 'react';
import { HelpCircle, Mail, MessageSquare, Send, Check, AlertCircle } from 'lucide-react';
import * as customerService from '../../../services/customer/customerService';

const FAQS = [
  { q: 'How do I change my plan?', a: 'Go to the Subscription page and click "Change Plan" to upgrade or downgrade your subscription.' },
  { q: 'Can I cancel anytime?', a: 'Yes! You can cancel your subscription at any time. Your access continues until the end of your billing period.' },
  { q: 'How do I update my payment method?', a: 'Visit the Payment Methods page to add, edit, or remove your payment options.' },
  { q: 'What happens when my trial ends?', a: 'Your subscription will automatically start billing based on your selected plan.' },
  { q: 'How do I download my invoices?', a: 'Go to the Billing page, find your invoice, and click the download button.' },
  { q: 'What is the Ad-Free add-on?', a: 'The Ad-Free add-on removes all advertisements from your streaming experience for ₹99/month.' },
  { q: 'How is download storage calculated?', a: 'You get 5-10GB free depending on your plan. Additional storage is charged at ₹20/GB.' },
  { q: 'Can I pause my subscription?', a: 'Yes, you can pause for up to 1 month. You won\'t be charged during the pause period.' }
];

export const SupportPage: React.FC = () => {
  const [expandedFaq, setExpandedFaq] = useState<number | null>(null);
  const [contactForm, setContactForm] = useState({ subject: '', message: '', category: 'GENERAL' });
  const [submitting, setSubmitting] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      setError('');
      await customerService.sendSupportMessage(contactForm);
      setSent(true);
      setContactForm({ subject: '', message: '', category: 'GENERAL' });
      setTimeout(() => setSent(false), 5000);
    } catch (err: any) {
      setError(err.message || 'Failed to send message');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="support-page">
      <div className="grid grid-2">
        {/* FAQ Section */}
        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">
                <HelpCircle size={20} style={{ color: 'var(--netflix-red)' }} /> Frequently Asked Questions
              </h2>
              <p className="card-subtitle">Find answers to common questions</p>
            </div>
          </div>
          <div className="faq-list">
            {FAQS.map((faq, idx) => (
              <div key={idx} className={`faq-item ${expandedFaq === idx ? 'expanded' : ''}`}>
                <button className="faq-question" onClick={() => setExpandedFaq(expandedFaq === idx ? null : idx)}>
                  <span>{faq.q}</span>
                  <span className="faq-icon">{expandedFaq === idx ? '−' : '+'}</span>
                </button>
                {expandedFaq === idx && <div className="faq-answer"><p>{faq.a}</p></div>}
              </div>
            ))}
          </div>
        </div>

        {/* Contact Form */}
        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">
                <MessageSquare size={20} style={{ color: 'var(--netflix-red)' }} /> Contact Us
              </h2>
              <p className="card-subtitle">Send us a message and we'll get back to you</p>
            </div>
          </div>

          {sent && <div className="alert alert-success"><Check size={18} /> Message sent successfully!</div>}
          {error && <div className="alert alert-error"><AlertCircle size={18} /> {error}</div>}

          <div className="contact-form-container">
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Category</label>
                <select className="form-select" value={contactForm.category} onChange={e => setContactForm({...contactForm, category: e.target.value})}>
                  <option value="GENERAL">General Inquiry</option>
                  <option value="BILLING">Billing Issue</option>
                  <option value="TECHNICAL">Technical Support</option>
                  <option value="CANCELLATION">Cancellation Request</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Subject</label>
                <input className="form-input" placeholder="How can we help?" value={contactForm.subject} onChange={e => setContactForm({...contactForm, subject: e.target.value})} required />
              </div>
              <div className="form-group">
                <label className="form-label">Message</label>
                <textarea className="form-input" rows={5} placeholder="Describe your issue in detail..." value={contactForm.message} onChange={e => setContactForm({...contactForm, message: e.target.value})} required />
              </div>
              <button type="submit" className="btn btn-primary btn-full" disabled={submitting}>
                {submitting ? <><div className="spinner" /> Sending...</> : <><Send size={16} /> Send Message</>}
              </button>
            </form>
          </div>

          <div className="contact-email-section">
            <Mail size={24} />
            <p>Or email us directly at</p>
            <a href="mailto:support@streamflix.com">support@streamflix.com</a>
          </div>
        </div>
      </div>
    </div>
  );
};
