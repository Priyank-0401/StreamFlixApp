-- StreamFlix Comprehensive Seed Data Script
-- Populates 20 diverse customer personas across all 15 operational, billing, and analytical tables

USE subscription_billing;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 1. USER TABLE (20 Customer Users, IDs 4 to 23)
-- ============================================================================
INSERT INTO user (user_id, full_name, email, password_hash, role, status, created_at, updated_at) VALUES
(4, 'Aarav Sharma', 'aarav.sharma@gmail.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-10-01 10:00:00', '2025-10-01 10:00:00'),
(5, 'Priya Patel', 'priya.patel@yahoo.in', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-10-05 11:30:00', '2025-10-05 11:30:00'),
(6, 'Rohan Verma', 'rohan.verma@outlook.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-10-12 09:15:00', '2025-10-12 09:15:00'),
(7, 'Ananya Gupta', 'ananya.gupta@gmail.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-10-20 14:20:00', '2025-10-20 14:20:00'),
(8, 'Vikram Singh', 'vikram.singh@rediffmail.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-11-01 16:45:00', '2025-11-01 16:45:00'),
(9, 'Neha Mehta', 'neha.mehta@gmail.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-11-10 12:10:00', '2025-11-10 12:10:00'),
(10, 'Aditya Joshi', 'aditya.joshi@hotmail.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-11-15 15:30:00', '2025-11-15 15:30:00'),
(11, 'Kavita Nair', 'kavita.nair@outlook.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-12-01 08:40:00', '2025-12-01 08:40:00'),
(12, 'Amitabh Rao', 'amitabh.rao@yahoo.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-12-10 17:25:00', '2025-12-10 17:25:00'),
(13, 'Sneha Iyer', 'sneha.iyer@gmail.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-12-20 19:50:00', '2025-12-20 19:50:00'),
(14, 'John Doe', 'john.doe@techcorp.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-01-05 10:15:00', '2026-01-05 10:15:00'),
(15, 'Jane Smith', 'jane.smith@innovate.io', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-01-12 11:45:00', '2026-01-12 11:45:00'),
(16, 'Michael Johnson', 'mjohnson@devventures.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-01-18 14:20:00', '2026-01-18 14:20:00'),
(17, 'Emily Davis', 'emily.davis@designstudio.org', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-02-01 16:10:00', '2026-02-01 16:10:00'),
(18, 'David Wilson', 'david.wilson@cloudnet.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-02-10 09:35:00', '2026-02-10 09:35:00'),
(19, 'Sarah Miller', 'sarah.miller@fastmail.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-02-20 13:55:00', '2026-02-20 13:55:00'),
(20, 'Oliver Brown', 'oliver.brown@londonmedia.co.uk', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-03-01 11:00:00', '2026-03-01 11:00:00'),
(21, 'Charlotte Jones', 'cjones@oxfordconsult.gb', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-03-10 15:20:00', '2026-03-10 15:20:00'),
(22, 'William Taylor', 'william.taylor@bristolarts.org', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-03-15 17:40:00', '2026-03-15 17:40:00'),
(23, 'Sophia Evans', 'sophia.evans@welshbites.co.uk', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-03-25 12:30:00', '2026-03-25 12:30:00');

-- ============================================================================
-- 2. CUSTOMER TABLE (IDs 1 to 20)
-- ============================================================================
INSERT INTO customer (customer_id, user_id, phone, currency, country, state, city, address_line1, postal_code, credit_balance_minor, status, created_at, updated_at) VALUES
(1, 4, '+919876543210', 'INR', 'IN', 'Maharashtra', 'Mumbai', 'Flat 402, Sea Breeze, Bandra West', '400050', 0, 'ACTIVE', '2025-10-01 10:00:00', '2025-10-01 10:00:00'),
(2, 5, '+919822012345', 'INR', 'IN', 'Gujarat', 'Ahmedabad', '12 SG Highway, Vastrapur', '380015', 50000, 'ACTIVE', '2025-10-05 11:30:00', '2025-10-05 11:30:00'),
(3, 6, '+919911223344', 'INR', 'IN', 'Delhi', 'New Delhi', '78 Connaught Place, Block B', '110001', 0, 'ACTIVE', '2025-10-12 09:15:00', '2025-10-12 09:15:00'),
(4, 7, '+918887776655', 'INR', 'IN', 'Karnataka', 'Bengaluru', '5th Block, Koramangala', '560095', 0, 'ACTIVE', '2025-10-20 14:20:00', '2025-10-20 14:20:00'),
(5, 8, '+919001100220', 'INR', 'IN', 'Rajasthan', 'Jaipur', 'C-Scheme, Ashok Nagar', '302001', 0, 'ACTIVE', '2025-11-01 16:45:00', '2025-11-01 16:45:00'),
(6, 9, '+919711223344', 'INR', 'IN', 'Maharashtra', 'Pune', 'Kalyani Nagar, Gold Adlabs', '411014', 0, 'ACTIVE', '2025-11-10 12:10:00', '2025-11-10 12:10:00'),
(7, 10, '+919422334455', 'INR', 'IN', 'Uttar Pradesh', 'Noida', 'Sector 62, Pinnacle Tower', '201309', 0, 'ACTIVE', '2025-11-15 15:30:00', '2025-11-15 15:30:00'),
(8, 11, '+919566778899', 'INR', 'IN', 'Tamil Nadu', 'Chennai', 'Anna Nagar East, 2nd Avenue', '600102', 0, 'ACTIVE', '2025-12-01 08:40:00', '2025-12-01 08:40:00'),
(9, 12, '+919845012345', 'INR', 'IN', 'Telangana', 'Hyderabad', 'Jubilee Hills, Road No 36', '500033', 0, 'ACTIVE', '2025-12-10 17:25:00', '2025-12-10 17:25:00'),
(10, 13, '+919830012345', 'INR', 'IN', 'West Bengal', 'Kolkata', 'Park Street, Queen Mansions', '700016', 0, 'ACTIVE', '2025-12-20 19:50:00', '2025-12-20 19:50:00'),
(11, 14, '+14155552671', 'USD', 'US', 'California', 'San Francisco', '100 Market Street, Suite 400', '94105', 0, 'ACTIVE', '2026-01-05 10:15:00', '2026-01-05 10:15:00'),
(12, 15, '+12125558910', 'USD', 'US', 'New York', 'New York', '350 5th Avenue, Empire Bldg', '10118', 0, 'ACTIVE', '2026-01-12 11:45:00', '2026-01-12 11:45:00'),
(13, 16, '+15125553412', 'USD', 'US', 'Texas', 'Austin', '500 Congress Ave', '78701', 0, 'ACTIVE', '2026-01-18 14:20:00', '2026-01-18 14:20:00'),
(14, 17, '+13125556789', 'USD', 'US', 'Illinois', 'Chicago', 'Wacker Drive, River Tower', '60606', 0, 'ACTIVE', '2026-02-01 16:10:00', '2026-02-01 16:10:00'),
(15, 18, '+12065551234', 'USD', 'US', 'Washington', 'Seattle', 'Pike Street, Space Center', '98101', 0, 'ACTIVE', '2026-02-10 09:35:00', '2026-02-10 09:35:00'),
(16, 19, '+13055559876', 'USD', 'US', 'Florida', 'Miami', 'Ocean Drive, South Beach', '33139', 0, 'ACTIVE', '2026-02-20 13:55:00', '2026-02-20 13:55:00'),
(17, 20, '+442071234567', 'GBP', 'GB', 'Greater London', 'London', '10 Downing St, Westminster', 'SW1A 2AA', 0, 'ACTIVE', '2026-03-01 11:00:00', '2026-03-01 11:00:00'),
(18, 21, '+441865987654', 'GBP', 'GB', 'Oxfordshire', 'Oxford', 'Broad Street, Trinity College', 'OX1 3BS', 0, 'ACTIVE', '2026-03-10 15:20:00', '2026-03-10 15:20:00'),
(19, 22, '+441179234567', 'GBP', 'GB', 'Bristol', 'Bristol', 'Clifton Suspension Road', 'BS8 4ED', 0, 'ACTIVE', '2026-03-15 17:40:00', '2026-03-15 17:40:00'),
(20, 23, '+442920123456', 'GBP', 'GB', 'Cardiff', 'Cardiff', 'Cardiff Castle, Castle St', 'CF10 3RB', 0, 'ACTIVE', '2026-03-25 12:30:00', '2026-03-25 12:30:00');

-- ============================================================================
-- 3. PAYMENT_METHOD TABLE (IDs 1 to 20)
-- ============================================================================
INSERT INTO payment_method (payment_method_id, customer_id, payment_type, card_last4, card_brand, gateway_token, upi_id, is_default, expiry_month, expiry_year, status, created_at) VALUES
(1, 1, 'CARD', '4242', 'Visa', 'tok_visa_12345', NULL, TRUE, 12, 2028, 'ACTIVE', '2025-10-01 10:00:00'),
(2, 2, 'CARD', '5555', 'MasterCard', 'tok_mc_98765', NULL, TRUE, 10, 2027, 'ACTIVE', '2025-10-05 11:30:00'),
(3, 3, 'UPI', NULL, NULL, 'tok_upi_rohan@okaxis', 'rohan@okaxis', TRUE, NULL, NULL, 'ACTIVE', '2025-10-12 09:15:00'),
(4, 4, 'CARD', '1234', 'Visa', 'tok_visa_ananya', NULL, TRUE, 5, 2029, 'ACTIVE', '2025-10-20 14:20:00'),
(5, 5, 'CARD', '9876', 'Amex', 'tok_amex_vikram', NULL, TRUE, 8, 2026, 'ACTIVE', '2025-11-01 16:45:00'),
(6, 6, 'UPI', NULL, NULL, 'tok_upi_neha@paytm', 'neha@paytm', TRUE, NULL, NULL, 'ACTIVE', '2025-11-10 12:10:00'),
(7, 7, 'CARD', '4321', 'MasterCard', 'tok_mc_aditya', NULL, TRUE, 11, 2028, 'ACTIVE', '2025-11-15 15:30:00'),
(8, 8, 'CARD', '6789', 'Visa', 'tok_visa_kavita', NULL, TRUE, 3, 2027, 'ACTIVE', '2025-12-01 08:40:00'),
(9, 9, 'UPI', NULL, NULL, 'tok_upi_rao@icici', 'rao@icici', TRUE, NULL, NULL, 'ACTIVE', '2025-12-10 17:25:00'),
(10, 10, 'CARD', '3456', 'Amex', 'tok_amex_sneha', NULL, TRUE, 9, 2028, 'ACTIVE', '2025-12-20 19:50:00'),
(11, 11, 'CARD', '4242', 'Visa', 'tok_visa_john', NULL, TRUE, 12, 2030, 'ACTIVE', '2026-01-05 10:15:00'),
(12, 12, 'CARD', '5454', 'MasterCard', 'tok_mc_jane', NULL, TRUE, 7, 2029, 'ACTIVE', '2026-01-12 11:45:00'),
(13, 13, 'CARD', '8888', 'Visa', 'tok_visa_michael', NULL, TRUE, 4, 2027, 'ACTIVE', '2026-01-18 14:20:00'),
(14, 14, 'CARD', '3000', 'Amex', 'tok_amex_emily', NULL, TRUE, 1, 2028, 'ACTIVE', '2026-02-01 16:10:00'),
(15, 15, 'CARD', '9999', 'MasterCard', 'tok_mc_david', NULL, TRUE, 6, 2026, 'ACTIVE', '2026-02-10 09:35:00'),
(16, 16, 'CARD', '1111', 'Visa', 'tok_visa_sarah', NULL, TRUE, 2, 2029, 'ACTIVE', '2026-02-20 13:55:00'),
(17, 17, 'CARD', '0000', 'MasterCard', 'tok_mc_oliver', NULL, TRUE, 8, 2028, 'ACTIVE', '2026-03-01 11:00:00'),
(18, 18, 'CARD', '7777', 'Visa', 'tok_visa_charlotte', NULL, TRUE, 10, 2027, 'ACTIVE', '2026-03-10 15:20:00'),
(19, 19, 'CARD', '6666', 'MasterCard', 'tok_mc_william', NULL, TRUE, 12, 2029, 'ACTIVE', '2026-03-15 17:40:00'),
(20, 20, 'CARD', '2222', 'Visa', 'tok_visa_sophia', NULL, TRUE, 5, 2030, 'ACTIVE', '2026-03-25 12:30:00');

-- ============================================================================
-- 4. SUBSCRIPTION TABLE (IDs 1 to 20)
-- ============================================================================
-- Status distribution: 15 ACTIVE, 2 CANCELED (Sub 3 & 15), 3 PAST_DUE (Sub 7, 13, 19)
INSERT INTO subscription (subscription_id, customer_id, plan_id, status, start_date, trial_end_date, current_period_start, current_period_end, cancel_at_period_end, canceled_at, paused_from, paused_to, payment_method_id, currency, created_at, updated_at) VALUES
(1, 1, 1, 'ACTIVE', '2025-10-01', '2025-10-08', '2026-05-01', '2026-06-01', FALSE, NULL, NULL, NULL, 1, 'INR', '2025-10-01 10:00:00', '2026-05-01 10:00:00'),
(2, 2, 2, 'ACTIVE', '2025-10-05', '2025-10-12', '2025-10-05', '2026-10-05', FALSE, NULL, NULL, NULL, 2, 'INR', '2025-10-05 11:30:00', '2025-10-05 11:30:00'),
(3, 3, 1, 'CANCELED', '2025-10-12', '2025-10-19', '2026-03-12', '2026-04-12', TRUE, '2026-04-10 15:00:00', NULL, NULL, 3, 'INR', '2025-10-12 09:15:00', '2026-04-10 15:00:00'),
(4, 4, 3, 'ACTIVE', '2025-10-20', '2025-11-03', '2026-05-20', '2026-06-20', FALSE, NULL, NULL, NULL, 4, 'INR', '2025-10-20 14:20:00', '2026-05-20 14:20:00'),
(5, 5, 5, 'ACTIVE', '2025-11-01', '2025-11-15', '2026-05-01', '2026-06-01', FALSE, NULL, NULL, NULL, 5, 'INR', '2025-11-01 16:45:00', '2026-05-01 16:45:00'),
(6, 6, 6, 'ACTIVE', '2025-11-10', '2025-11-24', '2025-11-10', '2026-11-10', FALSE, NULL, NULL, NULL, 6, 'INR', '2025-11-10 12:10:00', '2025-11-10 12:10:00'),
(7, 7, 3, 'PAST_DUE', '2025-11-15', '2025-11-29', '2026-04-15', '2026-05-15', FALSE, NULL, NULL, NULL, 7, 'INR', '2025-11-15 15:30:00', '2026-05-15 09:00:00'),
(8, 8, 1, 'ACTIVE', '2025-12-01', '2025-12-08', '2026-05-01', '2026-06-01', FALSE, NULL, NULL, NULL, 8, 'INR', '2025-12-01 08:40:00', '2026-05-01 08:40:00'),
(9, 9, 5, 'ACTIVE', '2025-12-10', '2025-12-24', '2026-05-10', '2026-06-10', FALSE, NULL, NULL, NULL, 9, 'INR', '2025-12-10 17:25:00', '2026-05-10 17:25:00'),
(10, 10, 3, 'ACTIVE', '2025-12-20', '2026-01-03', '2026-04-20', '2026-05-20', FALSE, NULL, NULL, NULL, 10, 'INR', '2025-12-20 19:50:00', '2026-04-20 19:50:00'),
(11, 11, 4, 'ACTIVE', '2026-01-05', '2026-01-19', '2026-01-05', '2027-01-05', FALSE, NULL, NULL, NULL, 11, 'USD', '2026-01-05 10:15:00', '2026-01-05 10:15:00'),
(12, 12, 3, 'ACTIVE', '2026-01-12', '2026-01-26', '2026-05-12', '2026-06-12', FALSE, NULL, NULL, NULL, 12, 'USD', '2026-01-12 11:45:00', '2026-05-12 11:45:00'),
(13, 13, 1, 'PAST_DUE', '2026-01-18', '2026-01-25', '2026-04-18', '2026-05-18', FALSE, NULL, NULL, NULL, 13, 'USD', '2026-01-18 14:20:00', '2026-05-18 10:00:00'),
(14, 14, 5, 'ACTIVE', '2026-02-01', '2026-02-15', '2026-05-01', '2026-06-01', FALSE, NULL, NULL, NULL, 14, 'USD', '2026-02-01 16:10:00', '2026-05-01 16:10:00'),
(15, 15, 3, 'CANCELED', '2026-02-10', '2026-02-24', '2026-03-10', '2026-04-10', TRUE, '2026-04-05 11:00:00', NULL, NULL, 15, 'USD', '2026-02-10 09:35:00', '2026-04-05 11:00:00'),
(16, 16, 5, 'ACTIVE', '2026-02-20', '2026-03-06', '2026-04-20', '2026-05-20', FALSE, NULL, NULL, NULL, 16, 'USD', '2026-02-20 13:55:00', '2026-04-20 13:55:00'),
(17, 17, 3, 'ACTIVE', '2026-03-01', '2026-03-15', '2026-05-01', '2026-06-01', FALSE, NULL, NULL, NULL, 17, 'GBP', '2026-03-01 11:00:00', '2026-05-01 11:00:00'),
(18, 18, 6, 'ACTIVE', '2026-03-10', '2026-03-24', '2026-03-10', '2027-03-10', FALSE, NULL, NULL, NULL, 18, 'GBP', '2026-03-10 15:20:00', '2026-03-10 15:20:00'),
(19, 19, 5, 'PAST_DUE', '2026-03-15', '2026-03-29', '2026-04-15', '2026-05-15', FALSE, NULL, NULL, NULL, 19, 'GBP', '2026-03-15 17:40:00', '2026-05-15 12:00:00'),
(20, 20, 1, 'ACTIVE', '2026-03-25', '2026-04-01', '2026-04-25', '2026-05-25', FALSE, NULL, NULL, NULL, 20, 'GBP', '2026-03-25 12:30:00', '2026-04-25 12:30:00');

-- ============================================================================
-- 5. SUBSCRIPTION_ITEM TABLE (Base Plans + Add-ons + Metered Component Linkages)
-- ============================================================================
INSERT INTO subscription_item (item_id, subscription_id, item_type, plan_id, addon_id, component_id, unit_price_minor, quantity, tax_mode, discount_id, created_at) VALUES
-- Subscription 1 (Base Plan)
(1, 1, 'PLAN', 1, NULL, NULL, 19900, 1, 'INCLUSIVE', NULL, '2025-10-01 10:00:00'),
-- Subscription 2 (Yearly Plan + Ad-Free Yearly Addon)
(2, 2, 'PLAN', 2, NULL, NULL, 199900, 1, 'INCLUSIVE', NULL, '2025-10-05 11:30:00'),
(3, 2, 'ADDON', NULL, 2, NULL, 99900, 1, 'INCLUSIVE', NULL, '2025-10-05 11:30:00'),
-- Subscription 3 (Canceled Plan)
(4, 3, 'PLAN', 1, NULL, NULL, 19900, 1, 'INCLUSIVE', NULL, '2025-10-12 09:15:00'),
-- Subscription 4 (Premium Plan + Ad-Free Monthly Addon + Metered Tracker)
(5, 4, 'PLAN', 3, NULL, NULL, 49900, 1, 'INCLUSIVE', NULL, '2025-10-20 14:20:00'),
(6, 4, 'ADDON', NULL, 1, NULL, 9900, 1, 'INCLUSIVE', NULL, '2025-10-20 14:20:00'),
(7, 4, 'METERED', NULL, NULL, 3, 2000, 1, 'INCLUSIVE', NULL, '2025-10-20 14:20:00'),
-- Subscription 5 (Standard Plan + Metered Tracker)
(8, 5, 'PLAN', 5, NULL, NULL, 29900, 1, 'INCLUSIVE', NULL, '2025-11-01 16:45:00'),
(9, 5, 'METERED', NULL, NULL, 5, 2000, 1, 'INCLUSIVE', NULL, '2025-11-01 16:45:00'),
-- Subscription 6 (Standard Yearly)
(10, 6, 'PLAN', 6, NULL, NULL, 299900, 1, 'INCLUSIVE', NULL, '2025-11-10 12:10:00'),
-- Subscription 7 (Premium Past Due)
(11, 7, 'PLAN', 3, NULL, NULL, 49900, 1, 'INCLUSIVE', NULL, '2025-11-15 15:30:00'),
-- Subscription 8 (Basic Monthly)
(12, 8, 'PLAN', 1, NULL, NULL, 19900, 1, 'INCLUSIVE', NULL, '2025-12-01 08:40:00'),
-- Subscription 9 (Standard Monthly)
(13, 9, 'PLAN', 5, NULL, NULL, 29900, 1, 'INCLUSIVE', NULL, '2025-12-10 17:25:00'),
-- Subscription 10 (Premium Monthly with Coupon)
(14, 10, 'PLAN', 3, NULL, NULL, 49900, 1, 'INCLUSIVE', 3, '2025-12-20 19:50:00'),
-- Subscription 11 (US Premium Yearly)
(15, 11, 'PLAN', 4, NULL, NULL, 9999, 1, 'EXCLUSIVE', NULL, '2026-01-05 10:15:00'),
-- Subscription 12 (US Premium Monthly + Metered Tracker)
(16, 12, 'PLAN', 3, NULL, NULL, 999, 1, 'EXCLUSIVE', NULL, '2026-01-12 11:45:00'),
(17, 12, 'METERED', NULL, NULL, 3, 199, 1, 'EXCLUSIVE', NULL, '2026-01-12 11:45:00'),
-- Subscription 13 (US Basic Past Due)
(18, 13, 'PLAN', 1, NULL, NULL, 499, 1, 'EXCLUSIVE', NULL, '2026-01-18 14:20:00'),
-- Subscription 14 (US Standard Monthly + Addon)
(19, 14, 'PLAN', 5, NULL, NULL, 799, 1, 'EXCLUSIVE', NULL, '2026-02-01 16:10:00'),
(20, 14, 'ADDON', NULL, 1, NULL, 199, 1, 'EXCLUSIVE', NULL, '2026-02-01 16:10:00'),
-- Subscription 15 (US Canceled Premium)
(21, 15, 'PLAN', 3, NULL, NULL, 999, 1, 'EXCLUSIVE', NULL, '2026-02-10 09:35:00'),
-- Subscription 16 (US Standard Monthly)
(22, 16, 'PLAN', 5, NULL, NULL, 799, 1, 'EXCLUSIVE', NULL, '2026-02-20 13:55:00'),
-- Subscription 17 (UK Premium Monthly)
(23, 17, 'PLAN', 3, NULL, NULL, 799, 1, 'INCLUSIVE', NULL, '2026-03-01 11:00:00'),
-- Subscription 18 (UK Standard Yearly)
(24, 18, 'PLAN', 6, NULL, NULL, 5999, 1, 'INCLUSIVE', NULL, '2026-03-10 15:20:00'),
-- Subscription 19 (UK Standard Monthly Past Due)
(25, 19, 'PLAN', 5, NULL, NULL, 599, 1, 'INCLUSIVE', NULL, '2026-03-15 17:40:00'),
-- Subscription 20 (UK Basic Monthly)
(26, 20, 'PLAN', 1, NULL, NULL, 399, 1, 'INCLUSIVE', NULL, '2026-03-25 12:30:00');

-- ============================================================================
-- 6. SUBSCRIPTION_COUPON TABLE
-- ============================================================================
INSERT INTO subscription_coupon (id, subscription_id, coupon_id, applied_at, applied_by, expires_at, status) VALUES
(1, 1, 1, '2025-10-01 10:00:00', 1, NULL, 'ACTIVE'), -- WELCOME10
(2, 4, 2, '2025-10-20 14:20:00', 1, NULL, 'ACTIVE'), -- FLAT100
(3, 10, 3, '2025-12-20 19:50:00', 1, '2026-03-20 19:50:00', 'EXPIRED'), -- PREMIUM20
(4, 12, 1, '2026-01-12 11:45:00', 1, NULL, 'ACTIVE');

-- ============================================================================
-- 7. USAGE_RECORD TABLE (Metered Downloads over Free Tier)
-- ============================================================================
INSERT INTO usage_record (usage_id, subscription_id, component_id, quantity, recorded_at, billing_period_start, billing_period_end, idempotency_key) VALUES
(1, 4, 3, 15, '2026-05-18 10:00:00', '2026-05-01', '2026-06-01', 'usage_sub4_may2026'),
(2, 5, 5, 8, '2026-05-15 12:00:00', '2026-05-01', '2026-06-01', 'usage_sub5_may2026'),
(3, 12, 3, 20, '2026-05-10 14:00:00', '2026-05-01', '2026-06-01', 'usage_sub12_may2026');

-- ============================================================================
-- 8. INVOICE TABLE (Historical & Current Billing Invoices)
-- ============================================================================
INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, idempotency_key, created_at, updated_at) VALUES
-- Customer 1 Invoices (Paid history)
(1, 'INV-2025-1001', 1, 1, 'PAID', 'SUBSCRIPTION_CREATE', '2025-10-01', '2025-10-01', 19900, 3036, 1990, 20946, 0, 'INR', 'idemp_inv_1001', '2025-10-01 10:00:00', '2025-10-01 10:05:00'),
(2, 'INV-2025-1101', 1, 1, 'PAID', 'SUBSCRIPTION_CYCLE', '2025-11-01', '2025-11-01', 19900, 3036, 1990, 20946, 0, 'INR', 'idemp_inv_1101', '2025-11-01 10:00:00', '2025-11-01 10:05:00'),
(3, 'INV-2025-1201', 1, 1, 'PAID', 'SUBSCRIPTION_CYCLE', '2025-12-01', '2025-12-01', 19900, 3036, 1990, 20946, 0, 'INR', 'idemp_inv_1201', '2025-12-01 10:00:00', '2025-12-01 10:05:00'),
(4, 'INV-2026-0101', 1, 1, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-01-01', '2026-01-01', 19900, 3036, 1990, 20946, 0, 'INR', 'idemp_inv_0101', '2026-01-01 10:00:00', '2026-01-01 10:05:00'),
(5, 'INV-2026-0201', 1, 1, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-02-01', '2026-02-01', 19900, 3036, 1990, 20946, 0, 'INR', 'idemp_inv_0201', '2026-02-01 10:00:00', '2026-02-01 10:05:00'),
(6, 'INV-2026-0301', 1, 1, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-03-01', '2026-03-01', 19900, 3036, 1990, 20946, 0, 'INR', 'idemp_inv_0301', '2026-03-01 10:00:00', '2026-03-01 10:05:00'),
(7, 'INV-2026-0401', 1, 1, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-01', '2026-04-01', 19900, 3036, 1990, 20946, 0, 'INR', 'idemp_inv_0401', '2026-04-01 10:00:00', '2026-04-01 10:05:00'),
(8, 'INV-2026-0501', 1, 1, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-01', '2026-05-01', 19900, 3036, 1990, 20946, 0, 'INR', 'idemp_inv_0501', '2026-05-01 10:00:00', '2026-05-01 10:05:00'),
-- Customer 2 Invoice (Yearly)
(9, 'INV-2025-1005', 2, 2, 'PAID', 'SUBSCRIPTION_CREATE', '2025-10-05', '2025-10-05', 299800, 45732, 0, 345532, 0, 'INR', 'idemp_inv_1005', '2025-10-05 11:30:00', '2025-10-05 11:35:00'),
-- Customer 3 Invoices (Canceled account history)
(10, 'INV-2025-1012', 3, 3, 'PAID', 'SUBSCRIPTION_CREATE', '2025-10-12', '2025-10-12', 19900, 3036, 0, 22936, 0, 'INR', 'idemp_inv_1012', '2025-10-12 09:15:00', '2025-10-12 09:20:00'),
(11, 'INV-2025-1112', 3, 3, 'PAID', 'SUBSCRIPTION_CYCLE', '2025-11-12', '2025-11-12', 19900, 3036, 0, 22936, 0, 'INR', 'idemp_inv_1112', '2025-11-12 09:15:00', '2025-11-12 09:20:00'),
(12, 'INV-2025-1212', 3, 3, 'PAID', 'SUBSCRIPTION_CYCLE', '2025-12-12', '2025-12-12', 19900, 3036, 0, 22936, 0, 'INR', 'idemp_inv_1212', '2025-12-12 09:15:00', '2025-12-12 09:20:00'),
(13, 'INV-2026-0112', 3, 3, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-01-12', '2026-01-12', 19900, 3036, 0, 22936, 0, 'INR', 'idemp_inv_0112', '2026-01-12 09:15:00', '2026-01-12 09:20:00'),
(14, 'INV-2026-0212', 3, 3, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-02-12', '2026-02-12', 19900, 3036, 0, 22936, 0, 'INR', 'idemp_inv_0212', '2026-02-12 09:15:00', '2026-02-12 09:20:00'),
(15, 'INV-2026-0312', 3, 3, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-03-12', '2026-03-12', 19900, 3036, 0, 22936, 0, 'INR', 'idemp_inv_0312', '2026-03-12 09:15:00', '2026-03-12 09:20:00'),
-- Customer 4 Invoices
(16, 'INV-2026-0420', 4, 4, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-20', '2026-04-20', 59800, 9122, 10000, 58922, 0, 'INR', 'idemp_inv_0420', '2026-04-20 14:20:00', '2026-04-20 14:25:00'),
-- Customer 7 Invoice (Past Due Delinquent)
(17, 'INV-2026-0415', 7, 7, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-04-15', '2026-04-15', 49900, 7612, 0, 57512, 57512, 'INR', 'idemp_inv_0415', '2026-04-15 15:30:00', '2026-04-15 15:30:00'),
-- Customer 11 Invoice (US Yearly)
(18, 'INV-2026-0105', 11, 11, 'PAID', 'SUBSCRIPTION_CREATE', '2026-01-05', '2026-01-05', 9999, 725, 0, 10724, 0, 'USD', 'idemp_inv_0105', '2026-01-05 10:15:00', '2026-01-05 10:20:00'),
-- Customer 12 Invoices (US Monthly)
(19, 'INV-2026-0412', 12, 12, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-12', '2026-04-12', 1198, 87, 100, 1185, 0, 'USD', 'idemp_inv_us412', '2026-04-12 11:45:00', '2026-04-12 11:50:00'),
-- Customer 13 Invoice (US Past Due)
(20, 'INV-2026-0418', 13, 13, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-04-18', '2026-04-18', 499, 36, 0, 535, 535, 'USD', 'idemp_inv_us418', '2026-04-18 14:20:00', '2026-04-18 14:20:00'),
-- Customer 17 Invoice (UK Monthly)
(21, 'INV-2026-0401_UK', 17, 17, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-01', '2026-04-01', 799, 133, 0, 799, 0, 'GBP', 'idemp_inv_uk401', '2026-04-01 11:00:00', '2026-04-01 11:05:00'),
-- Customer 19 Invoice (UK Past Due)
(22, 'INV-2026-0415_UK', 19, 19, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-04-15', '2026-04-15', 599, 100, 0, 599, 599, 'GBP', 'idemp_inv_uk415', '2026-04-15 17:40:00', '2026-04-15 17:40:00');

-- ============================================================================
-- 9. INVOICE_LINE_ITEM TABLE
-- ============================================================================
INSERT INTO invoice_line_item (line_item_id, invoice_id, description, line_type, quantity, unit_price_minor, amount_minor, tax_rate_id, discount_id, period_start, period_end, metadata) VALUES
-- Invoice 1 Line Items
(1, 1, 'Basic Monthly Subscription', 'PLAN', 1, 19900, 19900, 1, NULL, '2025-10-01', '2025-11-01', NULL),
(2, 1, 'Coupon: WELCOME10 (10% Off)', 'DISCOUNT', 1, -1990, -1990, NULL, 1, NULL, NULL, NULL),
(3, 1, 'GST (18%)', 'TAX', 1, 3036, 3036, 1, NULL, NULL, NULL, NULL),
-- Invoice 9 Line Items
(4, 9, 'Basic Yearly Subscription', 'PLAN', 1, 199900, 199900, 1, NULL, '2025-10-05', '2026-10-05', NULL),
(5, 9, 'Ad-Free Experience (Yearly)', 'ADDON', 1, 99900, 99900, 1, NULL, '2025-10-05', '2026-10-05', NULL),
(6, 9, 'GST (18%)', 'TAX', 1, 45732, 45732, 1, NULL, NULL, NULL, NULL),
-- Invoice 17 Line Items (Delinquent)
(7, 17, 'Premium Monthly Subscription', 'PLAN', 1, 49900, 49900, 1, NULL, '2026-04-15', '2026-05-15', NULL),
(8, 17, 'GST (18%)', 'TAX', 1, 7612, 7612, 1, NULL, NULL, NULL, NULL),
-- Invoice 18 Line Items (US Yearly)
(9, 18, 'Premium Yearly Subscription', 'PLAN', 1, 9999, 9999, 3, NULL, '2026-01-05', '2027-01-05', NULL),
(10, 18, 'Sales Tax (7.25%)', 'TAX', 1, 725, 725, 3, NULL, NULL, NULL, NULL),
-- Invoice 20 Line Items (US Delinquent)
(11, 20, 'Basic Monthly Subscription', 'PLAN', 1, 499, 499, 3, NULL, '2026-04-18', '2026-05-18', NULL),
(12, 20, 'Sales Tax (7.25%)', 'TAX', 1, 36, 36, 3, NULL, NULL, NULL, NULL),
-- Invoice 21 Line Items (UK VAT Inclusive)
(13, 21, 'Premium Monthly Subscription', 'PLAN', 1, 799, 799, 2, NULL, '2026-04-01', '2026-05-01', NULL),
-- Invoice 22 Line Items (UK Delinquent)
(14, 22, 'Standard Monthly Subscription', 'PLAN', 1, 599, 599, 2, NULL, '2026-04-15', '2026-05-15', NULL);

-- ============================================================================
-- 10. PAYMENT TABLE
-- ============================================================================
INSERT INTO payment (payment_id, invoice_id, payment_method_id, idempotency_key, gateway_ref, amount_minor, currency, status, attempt_no, response_code, failure_reason, created_at, updated_at) VALUES
(1, 1, 1, 'idemp_pay_1001', 'ch_visa_success_1001', 20946, 'INR', 'SUCCESS', 1, '200', NULL, '2025-10-01 10:05:00', '2025-10-01 10:05:00'),
(2, 2, 1, 'idemp_pay_1101', 'ch_visa_success_1101', 20946, 'INR', 'SUCCESS', 1, '200', NULL, '2025-11-01 10:05:00', '2025-11-01 10:05:00'),
(3, 9, 2, 'idemp_pay_1005', 'ch_mc_success_1005', 345532, 'INR', 'SUCCESS', 1, '200', NULL, '2025-10-05 11:35:00', '2025-10-05 11:35:00'),
(4, 17, 7, 'idemp_pay_0415', 'ch_mc_fail_0415', 57512, 'INR', 'FAILED', 2, 'insufficient_funds', 'Card declined due to insufficient available funds.', '2026-04-15 15:30:00', '2026-04-17 10:00:00'),
(5, 18, 11, 'idemp_pay_us0105', 'ch_us_success_0105', 10724, 'USD', 'SUCCESS', 1, '200', NULL, '2026-01-05 10:20:00', '2026-01-05 10:20:00'),
(6, 20, 13, 'idemp_pay_us0418', 'ch_us_fail_0418', 535, 'USD', 'FAILED', 1, 'do_not_honor', 'Issuer bank declined transaction.', '2026-04-18 14:20:00', '2026-04-18 14:20:00'),
(7, 21, 17, 'idemp_pay_uk0401', 'ch_uk_success_0401', 799, 'GBP', 'SUCCESS', 1, '200', NULL, '2026-04-01 11:05:00', '2026-04-01 11:05:00'),
(8, 22, 19, 'idemp_pay_uk0415', 'ch_uk_fail_0415', 599, 'GBP', 'FAILED', 1, 'expired_card', 'Payment method expired.', '2026-04-15 17:40:00', '2026-04-15 17:40:00');

-- ============================================================================
-- 11. CREDIT_NOTE TABLE (Proration Refunds)
-- ============================================================================
INSERT INTO credit_note (credit_note_id, credit_note_number, invoice_id, reason, amount_minor, status, created_by, created_at) VALUES
(1, 'CN-2026-001', 10, 'Mid-cycle voluntary cancellation refund', 5000, 'APPLIED', 2, '2026-04-10 15:30:00');

-- ============================================================================
-- 12. DUNNING_RETRY_LOG TABLE
-- ============================================================================
INSERT INTO dunning_retry_log (retry_id, invoice_id, payment_id, attempt_no, scheduled_at, attempted_at, status, failure_reason) VALUES
(1, 17, 4, 1, '2026-04-16 10:00:00', '2026-04-16 10:00:00', 'FAILED', 'Card declined: insufficient_funds'),
(2, 17, 4, 2, '2026-04-18 10:00:00', NULL, 'SCHEDULED', NULL),
(3, 20, 6, 1, '2026-04-20 10:00:00', NULL, 'SCHEDULED', NULL),
(4, 22, 8, 1, '2026-04-17 10:00:00', '2026-04-17 10:00:00', 'FAILED', 'Payment method expired');

-- ============================================================================
-- 13. NOTIFICATION TABLE
-- ============================================================================
INSERT INTO notification (notification_id, customer_id, type, subject, body, channel, status, scheduled_at, sent_at, created_at) VALUES
(1, 1, 'INVOICE_PAID', 'Receipt for Invoice INV-2025-1001', 'Your payment of ₹209.46 was successful.', 'EMAIL', 'SENT', '2025-10-01 10:05:00', '2025-10-01 10:05:00', '2025-10-01 10:05:00'),
(2, 7, 'DUNNING_NOTICE', 'Urgent: Payment Failed for StreamFlix Premium', 'We were unable to process your monthly payment of ₹575.12.', 'EMAIL', 'SENT', '2026-04-15 15:30:00', '2026-04-15 15:35:00', '2026-04-15 15:30:00'),
(3, 11, 'INVOICE_PAID', 'Receipt for Invoice INV-2026-0105', 'Your annual payment of $107.24 was successful.', 'EMAIL', 'SENT', '2026-01-05 10:20:00', '2026-01-05 10:20:00', '2026-01-05 10:20:00');

-- ============================================================================
-- 14. AUDIT_LOG TABLE
-- ============================================================================
INSERT INTO audit_log (audit_id, actor, actor_role, action, entity_type, entity_id, old_value, new_value, request_id, ip, created_at) VALUES
(1, 'Aarav Sharma', 'CUSTOMER', 'CREATE_SUBSCRIPTION', 'SUBSCRIPTION', 1, NULL, '{"status":"ACTIVE","plan":"Basic Monthly"}', 'req_aud_1001', '192.168.1.5', '2025-10-01 10:00:00'),
(2, 'Finance Lead', 'FINANCE', 'EXECUTE_DUNNING_RETRY', 'INVOICE', 17, '{"status":"OPEN","retry":1}', '{"status":"OPEN","retry":2}', 'req_aud_0415', '10.0.0.4', '2026-04-16 10:00:00'),
(3, 'Rohan Verma', 'CUSTOMER', 'CANCEL_SUBSCRIPTION', 'SUBSCRIPTION', 3, '{"status":"ACTIVE"}', '{"status":"CANCELED"}', 'req_aud_cancel3', '172.16.0.8', '2026-04-10 15:00:00');

-- ============================================================================
-- 15. REVENUE_SNAPSHOT TABLE (Historical Financial Aggregates)
-- ============================================================================
INSERT INTO revenue_snapshot (snapshot_id, snapshot_date, mrr_minor, arr_minor, arpu_minor, active_customers, new_customers, churned_customers, gross_churn_percent, net_churn_percent, ltv_minor, total_revenue_minor, total_refunds_minor, created_at) VALUES
(1, '2026-05-01', 895000, 10740000, 59666, 15, 2, 0, 0.00, 0.00, 1193320, 4500000, 0, '2026-05-01 23:59:00'),
(2, '2026-05-02', 895000, 10740000, 59666, 15, 0, 0, 0.00, 0.00, 1193320, 4520000, 0, '2026-05-02 23:59:00'),
(3, '2026-05-03', 895000, 10740000, 59666, 15, 0, 0, 0.00, 0.00, 1193320, 4540000, 0, '2026-05-03 23:59:00'),
(4, '2026-05-04', 895000, 10740000, 59666, 15, 0, 0, 0.00, 0.00, 1193320, 4560000, 0, '2026-05-04 23:59:00'),
(5, '2026-05-05', 895000, 10740000, 59666, 15, 0, 0, 0.00, 0.00, 1193320, 4580000, 0, '2026-05-05 23:59:00'),
(6, '2026-05-06', 915000, 10980000, 61000, 15, 0, 0, 0.00, 0.00, 1220000, 4600000, 0, '2026-05-06 23:59:00'),
(7, '2026-05-07', 915000, 10980000, 61000, 15, 0, 0, 0.00, 0.00, 1220000, 4620000, 0, '2026-05-07 23:59:00'),
(8, '2026-05-08', 915000, 10980000, 61000, 15, 0, 0, 0.00, 0.00, 1220000, 4640000, 0, '2026-05-08 23:59:00'),
(9, '2026-05-09', 915000, 10980000, 61000, 15, 0, 0, 0.00, 0.00, 1220000, 4660000, 0, '2026-05-09 23:59:00'),
(10, '2026-05-10', 875000, 10500000, 58333, 15, 0, 1, 2.50, 2.50, 1166660, 4680000, 5000, '2026-05-10 23:59:00'),
(11, '2026-05-11', 875000, 10500000, 58333, 15, 0, 0, 2.50, 2.50, 1166660, 4700000, 5000, '2026-05-11 23:59:00'),
(12, '2026-05-12', 875000, 10500000, 58333, 15, 0, 0, 2.50, 2.50, 1166660, 4720000, 5000, '2026-05-12 23:59:00');

SET FOREIGN_KEY_CHECKS = 1;
-- END OF SEED DATA
