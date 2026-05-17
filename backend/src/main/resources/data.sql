-- StreamFlix Comprehensive Seed Data (40 Customers)
            -- Excludes: billing_job, dunning_retry_log, notification, usage_record
            USE subscription_billing;
            SET FOREIGN_KEY_CHECKS = 0;

            -- ============================================================================
            -- 1. USER TABLE (40 Customer Users, IDs 4 to 43)
            -- ============================================================================
            INSERT INTO user (user_id, full_name, email, password_hash, role, status, created_at, updated_at) VALUES
            (4, 'Aarav Sharma', 'aarav.sharma@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-10-01 08:00:00', '2025-10-01 08:00:00'),
            (5, 'Vivaan Gupta', 'vivaan.gupta@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-10-15 09:30:00', '2025-10-15 09:30:00'),
            (6, 'Ananya Reddy', 'ananya.reddy@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-11-01 10:15:00', '2025-11-01 10:15:00'),
            (7, 'Diya Nair', 'diya.nair@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-11-20 14:00:00', '2025-11-20 14:00:00'),
            (8, 'Advik Iyer', 'advik.iyer@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-12-05 11:45:00', '2025-12-05 11:45:00'),
            (9, 'Sai Patel', 'sai.patel@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-01-10 08:20:00', '2026-01-10 08:20:00'),
            (10, 'Ishaan Joshi', 'ishaan.joshi@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-01-25 16:30:00', '2026-01-25 16:30:00'),
            (11, 'John Smith', 'john.smith@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-09-01 12:00:00', '2025-09-01 12:00:00'),
            (12, 'Emily Johnson', 'emily.johnson@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-10-10 09:00:00', '2025-10-10 09:00:00'),
            (13, 'Michael Brown', 'michael.brown@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-11-05 13:15:00', '2025-11-05 13:15:00'),
            (14, 'Jessica Davis', 'jessica.davis@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-12-15 10:45:00', '2025-12-15 10:45:00'),
            (15, 'Daniel Wilson', 'daniel.wilson@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-01-20 11:00:00', '2026-01-20 11:00:00'),
            (16, 'Laura Martinez', 'laura.martinez@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-02-14 08:30:00', '2026-02-14 08:30:00'),
            (17, 'James Anderson', 'james.anderson@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-03-01 15:45:00', '2026-03-01 15:45:00'),
            (18, 'Olivia Thomas', 'olivia.thomas@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-03-20 09:20:00', '2026-03-20 09:20:00'),
            (19, 'William Taylor', 'william.taylor@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-04-05 14:10:00', '2026-04-05 14:10:00'),
            (20, 'Sophia Moore', 'sophia.moore@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-04-18 12:30:00', '2026-04-18 12:30:00'),
            (21, 'Oliver Jones', 'oliver.jones@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-11-11 09:00:00', '2025-11-11 09:00:00'),
            (22, 'Amelia Williams', 'amelia.williams@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-12-01 10:00:00', '2025-12-01 10:00:00'),
            (23, 'Harry Davies', 'harry.davies@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-01-15 11:30:00', '2026-01-15 11:30:00'),
            (24, 'Isla Evans', 'isla.evans@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-02-20 14:45:00', '2026-02-20 14:45:00'),
            (25, 'George Wilson', 'george.wilson@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-03-10 08:15:00', '2026-03-10 08:15:00'),
            (26, 'Mia Johnson', 'mia.johnson@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-04-25 16:20:00', '2026-04-25 16:20:00'),
            (27, 'Lucas Brown', 'lucas.brown@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-05-05 10:00:00', '2026-05-05 10:00:00'),
            (28, 'Ella White', 'ella.white@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-05-20 09:45:00', '2026-05-20 09:45:00'),
            (29, 'Leo Harris', 'leo.harris@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-09-20 08:00:00', '2025-09-20 08:00:00'),
            (30, 'Grace Clark', 'grace.clark@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-10-25 17:00:00', '2025-10-25 17:00:00'),
            (31, 'Aryan Khanna', 'aryan.khanna@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-02-01 11:00:00', '2026-02-01 11:00:00'),
            (32, 'Kavya Menon', 'kavya.menon@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-03-15 13:30:00', '2026-03-15 13:30:00'),
            (33, 'Rohan Mehra', 'rohan.mehra@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-04-30 15:00:00', '2026-04-30 15:00:00'),
            (34, 'Neha Verma', 'neha.verma@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-05-10 12:15:00', '2026-05-10 12:15:00'),
            (35, 'Pranav Sethi', 'pranav.sethi@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-06-01 09:00:00', '2026-06-01 09:00:00'),
            (36, 'Riya Kapoor', 'riya.kapoor@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2025-12-25 08:30:00', '2025-12-25 08:30:00'),
            (37, 'Arjun Nair', 'arjun.nair@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-01-30 14:20:00', '2026-01-30 14:20:00'),
            (38, 'Anjali Sinha', 'anjali.sinha@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-03-05 16:45:00', '2026-03-05 16:45:00'),
            (39, 'Kunal Desai', 'kunal.desai@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-04-12 10:30:00', '2026-04-12 10:30:00'),
            (40, 'Shreya Bhat', 'shreya.bhat@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-05-25 11:15:00', '2026-05-25 11:15:00'),
            (41, 'Vikram Singh', 'vikram.singh@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-02-10 08:00:00', '2026-02-10 08:00:00'),
            (42, 'Tanvi Kulkarni', 'tanvi.kulkarni@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-03-25 12:00:00', '2026-03-25 12:00:00'),
            (43, 'Siddharth Rao', 'siddharth.rao@example.com', '$2a$10$kmhQ2CwcOGtN9Jay58BpWOIyAq81UkWf2GhHSh87hUJ8.bZ40wowC', 'CUSTOMER', 'ACTIVE', '2026-06-05 09:45:00', '2026-06-05 09:45:00');

            -- ============================================================================
            -- 2. CUSTOMER TABLE (40 Customers, IDs 1 to 40)
            -- ============================================================================
            INSERT INTO customer (customer_id, user_id, phone, currency, country, state, city, address_line1, postal_code, credit_balance_minor, status, created_at, updated_at) VALUES
            (1, 4, '+919876543210', 'INR', 'IN', 'Maharashtra', 'Mumbai', '123 Andheri East', '400093', 0, 'ACTIVE', '2025-10-01 08:00:00', '2025-10-01 08:00:00'),
            (2, 5, '+919876543211', 'INR', 'IN', 'Delhi', 'New Delhi', '45 Connaught Place', '110001', 0, 'ACTIVE', '2025-10-15 09:30:00', '2025-10-15 09:30:00'),
            (3, 6, '+919876543212', 'INR', 'IN', 'Karnataka', 'Bengaluru', '789 Indiranagar', '560038', 0, 'ACTIVE', '2025-11-01 10:15:00', '2025-11-01 10:15:00'),
            (4, 7, '+919876543213', 'INR', 'IN', 'Tamil Nadu', 'Chennai', '12 T Nagar', '600017', 0, 'ACTIVE', '2025-11-20 14:00:00', '2025-11-20 14:00:00'),
            (5, 8, '+919876543214', 'INR', 'IN', 'West Bengal', 'Kolkata', '56 Park Street', '700016', 0, 'ACTIVE', '2025-12-05 11:45:00', '2025-12-05 11:45:00'),
            (6, 9, '+919876543215', 'INR', 'IN', 'Gujarat', 'Ahmedabad', '90 Satellite', '380015', 0, 'ACTIVE', '2026-01-10 08:20:00', '2026-01-10 08:20:00'),
            (7, 10, '+919876543216', 'INR', 'IN', 'Rajasthan', 'Jaipur', '34 MI Road', '302001', 0, 'ACTIVE', '2026-01-25 16:30:00', '2026-01-25 16:30:00'),
            (8, 11, '+12345678901', 'USD', 'US', 'California', 'Los Angeles', '100 Hollywood Blvd', '90001', 0, 'ACTIVE', '2025-09-01 12:00:00', '2025-09-01 12:00:00'),
            (9, 12, '+12345678902', 'USD', 'US', 'New York', 'New York', '200 5th Avenue', '10001', 0, 'ACTIVE', '2025-10-10 09:00:00', '2025-10-10 09:00:00'),
            (10, 13, '+12345678903', 'USD', 'US', 'Texas', 'Austin', '300 Congress Ave', '73301', 0, 'ACTIVE', '2025-11-05 13:15:00', '2025-11-05 13:15:00'),
            (11, 14, '+12345678904', 'USD', 'US', 'Florida', 'Miami', '400 Ocean Drive', '33139', 0, 'ACTIVE', '2025-12-15 10:45:00', '2025-12-15 10:45:00'),
            (12, 15, '+12345678905', 'USD', 'US', 'Illinois', 'Chicago', '500 Michigan Ave', '60601', 0, 'ACTIVE', '2026-01-20 11:00:00', '2026-01-20 11:00:00'),
            (13, 16, '+12345678906', 'USD', 'US', 'Washington', 'Seattle', '600 Pike St', '98101', 0, 'ACTIVE', '2026-02-14 08:30:00', '2026-02-14 08:30:00'),
            (14, 17, '+12345678907', 'USD', 'US', 'Massachusetts', 'Boston', '700 Beacon St', '02108', 0, 'ACTIVE', '2026-03-01 15:45:00', '2026-03-01 15:45:00'),
            (15, 18, '+12345678908', 'USD', 'US', 'Colorado', 'Denver', '800 Broadway', '80202', 0, 'ACTIVE', '2026-03-20 09:20:00', '2026-03-20 09:20:00'),
            (16, 19, '+12345678909', 'USD', 'US', 'Arizona', 'Phoenix', '900 Camelback Rd', '85001', 0, 'ACTIVE', '2026-04-05 14:10:00', '2026-04-05 14:10:00'),
            (17, 20, '+12345678910', 'USD', 'US', 'Oregon', 'Portland', '1000 Burnside St', '97201', 0, 'ACTIVE', '2026-04-18 12:30:00', '2026-04-18 12:30:00'),
            (18, 21, '+442012345678', 'GBP', 'GB', 'England', 'London', '221 Baker Street', 'NW1 6XE', 0, 'ACTIVE', '2025-11-11 09:00:00', '2025-11-11 09:00:00'),
            (19, 22, '+442012345679', 'GBP', 'GB', 'England', 'Manchester', '1 Oxford Road', 'M1 5QA', 0, 'ACTIVE', '2025-12-01 10:00:00', '2025-12-01 10:00:00'),
            (20, 23, '+442012345680', 'GBP', 'GB', 'Scotland', 'Edinburgh', '5 Princes Street', 'EH2 2EQ', 0, 'ACTIVE', '2026-01-15 11:30:00', '2026-01-15 11:30:00'),
            (21, 24, '+442012345681', 'GBP', 'GB', 'Wales', 'Cardiff', '10 Queen Street', 'CF10 2BX', 0, 'ACTIVE', '2026-02-20 14:45:00', '2026-02-20 14:45:00'),
            (22, 25, '+442012345682', 'GBP', 'GB', 'England', 'Birmingham', '25 New Street', 'B2 4JH', 0, 'ACTIVE', '2026-03-10 08:15:00', '2026-03-10 08:15:00'),
            (23, 26, '+442012345683', 'GBP', 'GB', 'England', 'Liverpool', '30 Bold Street', 'L1 4DS', 0, 'ACTIVE', '2026-04-25 16:20:00', '2026-04-25 16:20:00'),
            (24, 27, '+442012345684', 'GBP', 'GB', 'England', 'Bristol', '40 Park Street', 'BS1 5JZ', 0, 'ACTIVE', '2026-05-05 10:00:00', '2026-05-05 10:00:00'),
            (25, 28, '+442012345685', 'GBP', 'GB', 'England', 'Leeds', '50 Briggate', 'LS1 6AF', 0, 'ACTIVE', '2026-05-20 09:45:00', '2026-05-20 09:45:00'),
            (26, 29, '+12345678911', 'USD', 'US', 'Nevada', 'Las Vegas', '1100 Strip', '89101', 0, 'ACTIVE', '2025-09-20 08:00:00', '2025-09-20 08:00:00'),
            (27, 30, '+12345678912', 'USD', 'US', 'Michigan', 'Detroit', '1200 Woodward', '48226', 0, 'ACTIVE', '2025-10-25 17:00:00', '2025-10-25 17:00:00'),
            (28, 31, '+919876543217', 'INR', 'IN', 'Punjab', 'Chandigarh', '22 Sector 17', '160017', 0, 'ACTIVE', '2026-02-01 11:00:00', '2026-02-01 11:00:00'),
            (29, 32, '+919876543218', 'INR', 'IN', 'Telangana', 'Hyderabad', '78 Jubilee Hills', '500033', 0, 'ACTIVE', '2026-03-15 13:30:00', '2026-03-15 13:30:00'),
            (30, 33, '+919876543219', 'INR', 'IN', 'Kerala', 'Kochi', '12 Marine Drive', '682011', 0, 'ACTIVE', '2026-04-30 15:00:00', '2026-04-30 15:00:00'),
            (31, 34, '+919876543220', 'INR', 'IN', 'Uttar Pradesh', 'Lucknow', '45 Hazratganj', '226001', 0, 'ACTIVE', '2026-05-10 12:15:00', '2026-05-10 12:15:00'),
            (32, 35, '+919876543221', 'INR', 'IN', 'Bihar', 'Patna', '88 Frazer Road', '800001', 0, 'ACTIVE', '2026-06-01 09:00:00', '2026-06-01 09:00:00'),
            (33, 36, '+919876543222', 'INR', 'IN', 'Madhya Pradesh', 'Bhopal', '32 MP Nagar', '462011', 0, 'ACTIVE', '2025-12-25 08:30:00', '2025-12-25 08:30:00'),
            (34, 37, '+919876543223', 'INR', 'IN', 'Haryana', 'Gurugram', '100 Cyber City', '122002', 0, 'ACTIVE', '2026-01-30 14:20:00', '2026-01-30 14:20:00'),
            (35, 38, '+919876543224', 'INR', 'IN', 'Goa', 'Panaji', '20 Miramar', '403001', 0, 'ACTIVE', '2026-03-05 16:45:00', '2026-03-05 16:45:00'),
            (36, 39, '+919876543225', 'INR', 'IN', 'Odisha', 'Bhubaneswar', '55 Patia', '751024', 0, 'ACTIVE', '2026-04-12 10:30:00', '2026-04-12 10:30:00'),
            (37, 40, '+919876543226', 'INR', 'IN', 'Chandigarh', 'Chandigarh', '8 Sector 35', '160035', 0, 'ACTIVE', '2026-05-25 11:15:00', '2026-05-25 11:15:00'),
            (38, 41, '+12345678913', 'USD', 'US', 'Georgia', 'Atlanta', '1300 Peachtree', '30303', 0, 'ACTIVE', '2026-02-10 08:00:00', '2026-02-10 08:00:00'),
            (39, 42, '+442012345686', 'GBP', 'GB', 'England', 'Newcastle', '60 Grey Street', 'NE1 6AF', 0, 'ACTIVE', '2026-03-25 12:00:00', '2026-03-25 12:00:00'),
            (40, 43, '+442012345687', 'GBP', 'GB', 'Scotland', 'Glasgow', '70 Buchanan Street', 'G1 3BF', 0, 'ACTIVE', '2026-06-05 09:45:00', '2026-06-05 09:45:00');

            -- ============================================================================
            -- 3. PAYMENT_METHOD TABLE (40 methods, one per customer)
            -- ============================================================================
            INSERT INTO payment_method (payment_method_id, customer_id, payment_type, card_last4, card_brand, gateway_token, upi_id, is_default, status, created_at) VALUES
            (1, 1, 'CARD', '1111', 'Visa', 'tok_visa_1', NULL, TRUE, 'ACTIVE', '2025-10-01 08:00:00'),
            (2, 2, 'CARD', '2222', 'Mastercard', 'tok_mc_2', NULL, TRUE, 'ACTIVE', '2025-10-15 09:30:00'),
            (3, 3, 'UPI', NULL, NULL, 'tok_upi_3', 'aarav@okhdfcbank', TRUE, 'ACTIVE', '2025-11-01 10:15:00'),
            (4, 4, 'CARD', '4444', 'Visa', 'tok_visa_4', NULL, TRUE, 'ACTIVE', '2025-11-20 14:00:00'),
            (5, 5, 'UPI', NULL, NULL, 'tok_upi_5', 'diya@okicici', TRUE, 'ACTIVE', '2025-12-05 11:45:00'),
            (6, 6, 'CARD', '6666', 'Mastercard', 'tok_mc_6', NULL, TRUE, 'ACTIVE', '2026-01-10 08:20:00'),
            (7, 7, 'CARD', '7777', 'Visa', 'tok_visa_7', NULL, TRUE, 'ACTIVE', '2026-01-25 16:30:00'),
            (8, 8, 'CARD', '8888', 'Visa', 'tok_visa_8', NULL, TRUE, 'ACTIVE', '2025-09-01 12:00:00'),
            (9, 9, 'CARD', '9999', 'Mastercard', 'tok_mc_9', NULL, TRUE, 'ACTIVE', '2025-10-10 09:00:00'),
            (10, 10, 'CARD', '1010', 'Amex', 'tok_amex_10', NULL, TRUE, 'ACTIVE', '2025-11-05 13:15:00'),
            (11, 11, 'CARD', '1112', 'Visa', 'tok_visa_11', NULL, TRUE, 'ACTIVE', '2025-12-15 10:45:00'),
            (12, 12, 'CARD', '1213', 'Mastercard', 'tok_mc_12', NULL, TRUE, 'ACTIVE', '2026-01-20 11:00:00'),
            (13, 13, 'CARD', '1314', 'Visa', 'tok_visa_13', NULL, TRUE, 'ACTIVE', '2026-02-14 08:30:00'),
            (14, 14, 'CARD', '1415', 'Discover', 'tok_dis_14', NULL, TRUE, 'ACTIVE', '2026-03-01 15:45:00'),
            (15, 15, 'CARD', '1516', 'Visa', 'tok_visa_15', NULL, TRUE, 'ACTIVE', '2026-03-20 09:20:00'),
            (16, 16, 'CARD', '1617', 'Mastercard', 'tok_mc_16', NULL, TRUE, 'ACTIVE', '2026-04-05 14:10:00'),
            (17, 17, 'CARD', '1718', 'Visa', 'tok_visa_17', NULL, TRUE, 'ACTIVE', '2026-04-18 12:30:00'),
            (18, 18, 'CARD', '1819', 'Visa', 'tok_visa_18', NULL, TRUE, 'ACTIVE', '2025-11-11 09:00:00'),
            (19, 19, 'CARD', '1920', 'Mastercard', 'tok_mc_19', NULL, TRUE, 'ACTIVE', '2025-12-01 10:00:00'),
            (20, 20, 'CARD', '2021', 'Amex', 'tok_amex_20', NULL, TRUE, 'ACTIVE', '2026-01-15 11:30:00'),
            (21, 21, 'CARD', '2122', 'Visa', 'tok_visa_21', NULL, TRUE, 'ACTIVE', '2026-02-20 14:45:00'),
            (22, 22, 'CARD', '2223', 'Mastercard', 'tok_mc_22', NULL, TRUE, 'ACTIVE', '2026-03-10 08:15:00'),
            (23, 23, 'CARD', '2324', 'Visa', 'tok_visa_23', NULL, TRUE, 'ACTIVE', '2026-04-25 16:20:00'),
            (24, 24, 'CARD', '2425', 'Mastercard', 'tok_mc_24', NULL, TRUE, 'ACTIVE', '2026-05-05 10:00:00'),
            (25, 25, 'CARD', '2526', 'Visa', 'tok_visa_25', NULL, TRUE, 'ACTIVE', '2026-05-20 09:45:00'),
            (26, 26, 'CARD', '2627', 'Amex', 'tok_amex_26', NULL, TRUE, 'ACTIVE', '2025-09-20 08:00:00'),
            (27, 27, 'CARD', '2728', 'Visa', 'tok_visa_27', NULL, TRUE, 'ACTIVE', '2025-10-25 17:00:00'),
            (28, 28, 'UPI', NULL, NULL, 'tok_upi_28', 'aryan@ybl', TRUE, 'ACTIVE', '2026-02-01 11:00:00'),
            (29, 29, 'CARD', '2930', 'Mastercard', 'tok_mc_29', NULL, TRUE, 'ACTIVE', '2026-03-15 13:30:00'),
            (30, 30, 'UPI', NULL, NULL, 'tok_upi_30', 'kavya@okaxis', TRUE, 'ACTIVE', '2026-04-30 15:00:00'),
            (31, 31, 'CARD', '3132', 'Visa', 'tok_visa_31', NULL, TRUE, 'ACTIVE', '2026-05-10 12:15:00'),
            (32, 32, 'CARD', '3233', 'Mastercard', 'tok_mc_32', NULL, TRUE, 'ACTIVE', '2026-06-01 09:00:00'),
            (33, 33, 'UPI', NULL, NULL, 'tok_upi_33', 'neha@okhdfcbank', TRUE, 'ACTIVE', '2025-12-25 08:30:00'),
            (34, 34, 'CARD', '3435', 'Visa', 'tok_visa_34', NULL, TRUE, 'ACTIVE', '2026-01-30 14:20:00'),
            (35, 35, 'CARD', '3536', 'Amex', 'tok_amex_35', NULL, TRUE, 'ACTIVE', '2026-03-05 16:45:00'),
            (36, 36, 'UPI', NULL, NULL, 'tok_upi_36', 'riya@okicici', TRUE, 'ACTIVE', '2026-04-12 10:30:00'),
            (37, 37, 'CARD', '3738', 'Mastercard', 'tok_mc_37', NULL, TRUE, 'ACTIVE', '2026-05-25 11:15:00'),
            (38, 38, 'CARD', '3839', 'Visa', 'tok_visa_38', NULL, TRUE, 'ACTIVE', '2026-02-10 08:00:00'),
            (39, 39, 'CARD', '3940', 'Mastercard', 'tok_mc_39', NULL, TRUE, 'ACTIVE', '2026-03-25 12:00:00'),
            (40, 40, 'CARD', '4041', 'Visa', 'tok_visa_40', NULL, TRUE, 'ACTIVE', '2026-06-05 09:45:00');

            -- ============================================================================
            -- 4. SUBSCRIPTION TABLE (40 subscriptions – all statuses)
            -- ============================================================================
            INSERT INTO subscription (subscription_id, customer_id, plan_id, status, start_date, trial_end_date, current_period_start, current_period_end, cancel_at_period_end, canceled_at, payment_method_id, currency, created_at, updated_at) VALUES
            (1, 1, 1, 'ACTIVE', '2025-10-01', NULL, '2026-06-01', '2026-07-01', FALSE, NULL, 1, 'INR', '2025-10-01 08:00:00', '2026-06-01 08:00:00'),
            (2, 2, 5, 'ACTIVE', '2025-10-15', NULL, '2026-06-15', '2026-07-15', FALSE, NULL, 2, 'INR', '2025-10-15 09:30:00', '2026-06-15 09:30:00'),
            (3, 3, 3, 'PAUSED', '2025-11-01', '2025-11-08', '2026-05-01', '2026-06-01', FALSE, NULL, 3, 'INR', '2025-11-01 10:15:00', '2026-05-01 10:15:00'),
            (4, 4, 1, 'CANCELED', '2025-11-20', NULL, '2026-04-20', '2026-05-20', TRUE, '2026-05-15 09:00:00', 4, 'INR', '2025-11-20 14:00:00', '2026-05-15 09:00:00'),
            (5, 5, 5, 'ACTIVE', '2025-12-05', NULL, '2026-06-05', '2026-07-05', FALSE, NULL, 5, 'INR', '2025-12-05 11:45:00', '2026-06-05 11:45:00'),
            (6, 6, 1, 'TRIALING', '2026-06-10', '2026-06-17', '2026-06-10', '2026-07-10', FALSE, NULL, 6, 'INR', '2026-06-10 08:20:00', '2026-06-10 08:20:00'),
            (7, 7, 3, 'PAST_DUE', '2026-01-25', NULL, '2026-05-25', '2026-06-25', FALSE, NULL, 7, 'INR', '2026-01-25 16:30:00', '2026-05-25 16:30:00'),
            (8, 8, 1, 'ACTIVE', '2025-09-01', NULL, '2026-06-01', '2026-07-01', FALSE, NULL, 8, 'USD', '2025-09-01 12:00:00', '2026-06-01 12:00:00'),
            (9, 9, 3, 'ACTIVE', '2025-10-10', NULL, '2026-06-10', '2026-07-10', FALSE, NULL, 9, 'USD', '2025-10-10 09:00:00', '2026-06-10 09:00:00'),
            (10, 10, 5, 'CANCELED', '2025-11-05', NULL, '2026-03-05', '2026-04-05', TRUE, '2026-03-20 10:00:00', 10, 'USD', '2025-11-05 13:15:00', '2026-03-20 10:00:00'),
            (11, 11, 1, 'ON_HOLD', '2025-12-15', NULL, '2026-05-15', '2026-06-15', FALSE, NULL, 11, 'USD', '2025-12-15 10:45:00', '2026-05-15 10:45:00'),
            (12, 12, 3, 'ACTIVE', '2026-01-20', NULL, '2026-06-20', '2026-07-20', FALSE, NULL, 12, 'USD', '2026-01-20 11:00:00', '2026-06-20 11:00:00'),
            (13, 13, 5, 'ACTIVE', '2026-02-14', NULL, '2026-06-14', '2026-07-14', FALSE, NULL, 13, 'USD', '2026-02-14 08:30:00', '2026-06-14 08:30:00'),
            (14, 14, 1, 'PAUSED', '2026-03-01', '2026-03-08', '2026-05-01', '2026-06-01', FALSE, NULL, 14, 'USD', '2026-03-01 15:45:00', '2026-05-01 15:45:00'),
            (15, 15, 3, 'CANCELED', '2026-03-20', NULL, '2026-05-20', '2026-06-20', TRUE, '2026-05-25 14:00:00', 15, 'USD', '2026-03-20 09:20:00', '2026-05-25 14:00:00'),
            (16, 16, 5, 'ACTIVE', '2026-04-05', NULL, '2026-06-05', '2026-07-05', FALSE, NULL, 16, 'USD', '2026-04-05 14:10:00', '2026-06-05 14:10:00'),
            (17, 17, 1, 'TRIALING', '2026-06-18', '2026-06-25', '2026-06-18', '2026-07-18', FALSE, NULL, 17, 'USD', '2026-06-18 12:30:00', '2026-06-18 12:30:00'),
            (18, 18, 3, 'ACTIVE', '2025-11-11', NULL, '2026-06-11', '2026-07-11', FALSE, NULL, 18, 'GBP', '2025-11-11 09:00:00', '2026-06-11 09:00:00'),
            (19, 19, 5, 'ACTIVE', '2025-12-01', NULL, '2026-06-01', '2026-07-01', FALSE, NULL, 19, 'GBP', '2025-12-01 10:00:00', '2026-06-01 10:00:00'),
            (20, 20, 1, 'CANCELED', '2026-01-15', NULL, '2026-05-15', '2026-06-15', TRUE, '2026-06-10 11:00:00', 20, 'GBP', '2026-01-15 11:30:00', '2026-06-10 11:00:00'),
            (21, 21, 3, 'PAST_DUE', '2026-02-20', NULL, '2026-05-20', '2026-06-20', FALSE, NULL, 21, 'GBP', '2026-02-20 14:45:00', '2026-05-20 14:45:00'),
            (22, 22, 5, 'ACTIVE', '2026-03-10', NULL, '2026-06-10', '2026-07-10', FALSE, NULL, 22, 'GBP', '2026-03-10 08:15:00', '2026-06-10 08:15:00'),
            (23, 23, 1, 'DRAFT', '2026-06-25', '2026-07-02', '2026-06-25', '2026-07-25', FALSE, NULL, 23, 'GBP', '2026-06-25 16:20:00', '2026-06-25 16:20:00'),
            (24, 24, 3, 'ACTIVE', '2026-05-05', NULL, '2026-06-05', '2026-07-05', FALSE, NULL, 24, 'GBP', '2026-05-05 10:00:00', '2026-06-05 10:00:00'),
            (25, 25, 5, 'ACTIVE', '2026-05-20', NULL, '2026-06-20', '2026-07-20', FALSE, NULL, 25, 'GBP', '2026-05-20 09:45:00', '2026-06-20 09:45:00'),
            (26, 26, 1, 'CANCELED', '2025-09-20', NULL, '2026-02-20', '2026-03-20', TRUE, '2026-02-28 18:00:00', 26, 'USD', '2025-09-20 08:00:00', '2026-02-28 18:00:00'),
            (27, 27, 3, 'ACTIVE', '2025-10-25', NULL, '2026-06-25', '2026-07-25', FALSE, NULL, 27, 'USD', '2025-10-25 17:00:00', '2026-06-25 17:00:00'),
            (28, 28, 1, 'ACTIVE', '2026-02-01', NULL, '2026-06-01', '2026-07-01', FALSE, NULL, 28, 'INR', '2026-02-01 11:00:00', '2026-06-01 11:00:00'),
            (29, 29, 5, 'ACTIVE', '2026-03-15', NULL, '2026-06-15', '2026-07-15', FALSE, NULL, 29, 'INR', '2026-03-15 13:30:00', '2026-06-15 13:30:00'),
            (30, 30, 3, 'PAUSED', '2026-04-30', NULL, '2026-06-30', '2026-07-30', FALSE, NULL, 30, 'INR', '2026-04-30 15:00:00', '2026-06-30 15:00:00'),
            (31, 31, 1, 'ACTIVE', '2026-05-10', NULL, '2026-06-10', '2026-07-10', FALSE, NULL, 31, 'INR', '2026-05-10 12:15:00', '2026-06-10 12:15:00'),
            (32, 32, 5, 'TRIALING', '2026-06-01', '2026-06-08', '2026-06-01', '2026-07-01', FALSE, NULL, 32, 'INR', '2026-06-01 09:00:00', '2026-06-01 09:00:00'),
            (33, 33, 3, 'CANCELED', '2025-12-25', NULL, '2026-04-25', '2026-05-25', TRUE, '2026-05-20 12:00:00', 33, 'INR', '2025-12-25 08:30:00', '2026-05-20 12:00:00'),
            (34, 34, 1, 'ACTIVE', '2026-01-30', NULL, '2026-06-30', '2026-07-30', FALSE, NULL, 34, 'INR', '2026-01-30 14:20:00', '2026-06-30 14:20:00'),
            (35, 35, 5, 'PAST_DUE', '2026-03-05', NULL, '2026-05-05', '2026-06-05', FALSE, NULL, 35, 'INR', '2026-03-05 16:45:00', '2026-05-05 16:45:00'),
            (36, 36, 3, 'ACTIVE', '2026-04-12', NULL, '2026-06-12', '2026-07-12', FALSE, NULL, 36, 'INR', '2026-04-12 10:30:00', '2026-06-12 10:30:00'),
            (37, 37, 1, 'ON_HOLD', '2026-05-25', NULL, '2026-06-25', '2026-07-25', FALSE, NULL, 37, 'INR', '2026-05-25 11:15:00', '2026-06-25 11:15:00'),
            (38, 38, 5, 'ACTIVE', '2026-02-10', NULL, '2026-06-10', '2026-07-10', FALSE, NULL, 38, 'USD', '2026-02-10 08:00:00', '2026-06-10 08:00:00'),
            (39, 39, 3, 'ACTIVE', '2026-03-25', NULL, '2026-06-25', '2026-07-25', FALSE, NULL, 39, 'GBP', '2026-03-25 12:00:00', '2026-06-25 12:00:00'),
            (40, 40, 5, 'ACTIVE', '2026-06-05', NULL, '2026-06-05', '2026-07-05', FALSE, NULL, 40, 'GBP', '2026-06-05 09:45:00', '2026-06-05 09:45:00');

            -- ============================================================================
            -- 5. SUBSCRIPTION_ITEM TABLE (one item per subscription – PLAN type)
            -- ============================================================================
            INSERT INTO subscription_item (item_id, subscription_id, item_type, plan_id, unit_price_minor, quantity, tax_mode, created_at) VALUES
            (1, 1, 'PLAN', 1, 19900, 1, 'INCLUSIVE', '2025-10-01 08:00:00'),
            (2, 2, 'PLAN', 5, 29900, 1, 'INCLUSIVE', '2025-10-15 09:30:00'),
            (3, 3, 'PLAN', 3, 49900, 1, 'INCLUSIVE', '2025-11-01 10:15:00'),
            (4, 4, 'PLAN', 1, 19900, 1, 'INCLUSIVE', '2025-11-20 14:00:00'),
            (5, 5, 'PLAN', 5, 29900, 1, 'INCLUSIVE', '2025-12-05 11:45:00'),
            (6, 6, 'PLAN', 1, 19900, 1, 'INCLUSIVE', '2026-06-10 08:20:00'),
            (7, 7, 'PLAN', 3, 49900, 1, 'INCLUSIVE', '2026-01-25 16:30:00'),
            (8, 8, 'PLAN', 1, 499, 1, 'INCLUSIVE', '2025-09-01 12:00:00'),
            (9, 9, 'PLAN', 3, 999, 1, 'INCLUSIVE', '2025-10-10 09:00:00'),
            (10, 10, 'PLAN', 5, 799, 1, 'INCLUSIVE', '2025-11-05 13:15:00'),
            (11, 11, 'PLAN', 1, 499, 1, 'INCLUSIVE', '2025-12-15 10:45:00'),
            (12, 12, 'PLAN', 3, 999, 1, 'INCLUSIVE', '2026-01-20 11:00:00'),
            (13, 13, 'PLAN', 5, 799, 1, 'INCLUSIVE', '2026-02-14 08:30:00'),
            (14, 14, 'PLAN', 1, 499, 1, 'INCLUSIVE', '2026-03-01 15:45:00'),
            (15, 15, 'PLAN', 3, 999, 1, 'INCLUSIVE', '2026-03-20 09:20:00'),
            (16, 16, 'PLAN', 5, 799, 1, 'INCLUSIVE', '2026-04-05 14:10:00'),
            (17, 17, 'PLAN', 1, 499, 1, 'INCLUSIVE', '2026-06-18 12:30:00'),
            (18, 18, 'PLAN', 3, 799, 1, 'INCLUSIVE', '2025-11-11 09:00:00'),
            (19, 19, 'PLAN', 5, 599, 1, 'INCLUSIVE', '2025-12-01 10:00:00'),
            (20, 20, 'PLAN', 1, 399, 1, 'INCLUSIVE', '2026-01-15 11:30:00'),
            (21, 21, 'PLAN', 3, 799, 1, 'INCLUSIVE', '2026-02-20 14:45:00'),
            (22, 22, 'PLAN', 5, 599, 1, 'INCLUSIVE', '2026-03-10 08:15:00'),
            (23, 23, 'PLAN', 1, 399, 1, 'INCLUSIVE', '2026-06-25 16:20:00'),
            (24, 24, 'PLAN', 3, 799, 1, 'INCLUSIVE', '2026-05-05 10:00:00'),
            (25, 25, 'PLAN', 5, 599, 1, 'INCLUSIVE', '2026-05-20 09:45:00'),
            (26, 26, 'PLAN', 1, 499, 1, 'INCLUSIVE', '2025-09-20 08:00:00'),
            (27, 27, 'PLAN', 3, 999, 1, 'INCLUSIVE', '2025-10-25 17:00:00'),
            (28, 28, 'PLAN', 1, 19900, 1, 'INCLUSIVE', '2026-02-01 11:00:00'),
            (29, 29, 'PLAN', 5, 29900, 1, 'INCLUSIVE', '2026-03-15 13:30:00'),
            (30, 30, 'PLAN', 3, 49900, 1, 'INCLUSIVE', '2026-04-30 15:00:00'),
            (31, 31, 'PLAN', 1, 19900, 1, 'INCLUSIVE', '2026-05-10 12:15:00'),
            (32, 32, 'PLAN', 5, 29900, 1, 'INCLUSIVE', '2026-06-01 09:00:00'),
            (33, 33, 'PLAN', 3, 49900, 1, 'INCLUSIVE', '2025-12-25 08:30:00'),
            (34, 34, 'PLAN', 1, 19900, 1, 'INCLUSIVE', '2026-01-30 14:20:00'),
            (35, 35, 'PLAN', 5, 29900, 1, 'INCLUSIVE', '2026-03-05 16:45:00'),
            (36, 36, 'PLAN', 3, 49900, 1, 'INCLUSIVE', '2026-04-12 10:30:00'),
            (37, 37, 'PLAN', 1, 19900, 1, 'INCLUSIVE', '2026-05-25 11:15:00'),
            (38, 38, 'PLAN', 5, 799, 1, 'INCLUSIVE', '2026-02-10 08:00:00'),
            (39, 39, 'PLAN', 3, 799, 1, 'INCLUSIVE', '2026-03-25 12:00:00'),
            (40, 40, 'PLAN', 5, 599, 1, 'INCLUSIVE', '2026-06-05 09:45:00');

            -- ============================================================================
            -- 6. INVOICE TABLE (~120 invoices, 3 per subscription on average)
            -- ============================================================================
            -- Helper: For each active/canceled/paused/past_due subscription, generate invoices for each completed period up to current_period_start.
            -- We'll manually insert a representative set. To keep script length reasonable, we create invoices for 3 most recent cycles per subscription.
            -- All amounts use unit_price_minor * 1, subtotal = total, tax=0, discount=0.

            -- Subscription 1 (INR, plan1, 19900 per month)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (1, 'INV-1001', 1, 1, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-01', '2026-04-15', 19900, 0, 0, 19900, 0, 'INR', '2026-04-01 00:00:00', '2026-04-01 00:00:00'),
            (2, 'INV-1002', 1, 1, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-01', '2026-05-15', 19900, 0, 0, 19900, 0, 'INR', '2026-05-01 00:00:00', '2026-05-01 00:00:00'),
            (3, 'INV-1003', 1, 1, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-01', '2026-06-15', 19900, 0, 0, 19900, 19900, 'INR', '2026-06-01 00:00:00', '2026-06-01 00:00:00');

            -- Subscription 2 (INR, plan5, 29900)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (4, 'INV-1004', 2, 2, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-15', '2026-04-30', 29900, 0, 0, 29900, 0, 'INR', '2026-04-15 00:00:00', '2026-04-15 00:00:00'),
            (5, 'INV-1005', 2, 2, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-15', '2026-05-30', 29900, 0, 0, 29900, 0, 'INR', '2026-05-15 00:00:00', '2026-05-15 00:00:00'),
            (6, 'INV-1006', 2, 2, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-15', '2026-06-30', 29900, 0, 0, 29900, 29900, 'INR', '2026-06-15 00:00:00', '2026-06-15 00:00:00');

            -- Subscription 3 (INR, plan3, 49900, PAUSED – last invoice before pause)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (7, 'INV-1007', 3, 3, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-01', '2026-04-15', 49900, 0, 0, 49900, 0, 'INR', '2026-04-01 00:00:00', '2026-04-01 00:00:00'),
            (8, 'INV-1008', 3, 3, 'VOID', 'SUBSCRIPTION_CYCLE', '2026-05-01', '2026-05-15', 49900, 0, 0, 49900, 49900, 'INR', '2026-05-01 00:00:00', '2026-05-01 00:00:00');

            -- Subscription 4 (INR, plan1, CANCELED – final invoice)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (9, 'INV-1009', 4, 4, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-03-20', '2026-04-05', 19900, 0, 0, 19900, 0, 'INR', '2026-03-20 00:00:00', '2026-03-20 00:00:00'),
            (10, 'INV-1010', 4, 4, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-20', '2026-05-05', 19900, 0, 0, 19900, 0, 'INR', '2026-04-20 00:00:00', '2026-04-20 00:00:00');

            -- Subscription 5 (INR, plan5, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (11, 'INV-1011', 5, 5, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-05', '2026-04-20', 29900, 0, 0, 29900, 0, 'INR', '2026-04-05 00:00:00', '2026-04-05 00:00:00'),
            (12, 'INV-1012', 5, 5, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-05', '2026-05-20', 29900, 0, 0, 29900, 0, 'INR', '2026-05-05 00:00:00', '2026-05-05 00:00:00'),
            (13, 'INV-1013', 5, 5, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-05', '2026-06-20', 29900, 0, 0, 29900, 29900, 'INR', '2026-06-05 00:00:00', '2026-06-05 00:00:00');

            -- Subscription 6 (INR, plan1, TRIALING – no invoice yet)
            -- (No invoice because trial started 2026-06-10, first invoice will be after trial)

            -- Subscription 7 (INR, plan3, PAST_DUE – last invoice unpaid)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (14, 'INV-1014', 7, 7, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-25', '2026-05-10', 49900, 0, 0, 49900, 0, 'INR', '2026-04-25 00:00:00', '2026-04-25 00:00:00'),
            (15, 'INV-1015', 7, 7, 'UNCOLLECTIBLE', 'SUBSCRIPTION_CYCLE', '2026-05-25', '2026-06-09', 49900, 0, 0, 49900, 49900, 'INR', '2026-05-25 00:00:00', '2026-05-25 00:00:00');

            -- Subscription 8 (USD, plan1, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (16, 'INV-1016', 8, 8, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-01', '2026-04-15', 499, 0, 0, 499, 0, 'USD', '2026-04-01 00:00:00', '2026-04-01 00:00:00'),
            (17, 'INV-1017', 8, 8, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-01', '2026-05-15', 499, 0, 0, 499, 0, 'USD', '2026-05-01 00:00:00', '2026-05-01 00:00:00'),
            (18, 'INV-1018', 8, 8, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-01', '2026-06-15', 499, 0, 0, 499, 499, 'USD', '2026-06-01 00:00:00', '2026-06-01 00:00:00');

            -- Subscription 9 (USD, plan3, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (19, 'INV-1019', 9, 9, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-10', '2026-04-25', 999, 0, 0, 999, 0, 'USD', '2026-04-10 00:00:00', '2026-04-10 00:00:00'),
            (20, 'INV-1020', 9, 9, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-10', '2026-05-25', 999, 0, 0, 999, 0, 'USD', '2026-05-10 00:00:00', '2026-05-10 00:00:00'),
            (21, 'INV-1021', 9, 9, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-10', '2026-06-25', 999, 0, 0, 999, 999, 'USD', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

            -- Subscription 10 (USD, plan5, CANCELED)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (22, 'INV-1022', 10, 10, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-02-05', '2026-02-20', 799, 0, 0, 799, 0, 'USD', '2026-02-05 00:00:00', '2026-02-05 00:00:00'),
            (23, 'INV-1023', 10, 10, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-03-05', '2026-03-20', 799, 0, 0, 799, 0, 'USD', '2026-03-05 00:00:00', '2026-03-05 00:00:00');

            -- Subscription 11 (USD, plan1, ON_HOLD)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (24, 'INV-1024', 11, 11, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-15', '2026-04-30', 499, 0, 0, 499, 0, 'USD', '2026-04-15 00:00:00', '2026-04-15 00:00:00'),
            (25, 'INV-1025', 11, 11, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-15', '2026-05-30', 499, 0, 0, 499, 0, 'USD', '2026-05-15 00:00:00', '2026-05-15 00:00:00'),
            (26, 'INV-1026', 11, 11, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-15', '2026-06-30', 499, 0, 0, 499, 499, 'USD', '2026-06-15 00:00:00', '2026-06-15 00:00:00');

            -- Subscription 12 (USD, plan3, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (27, 'INV-1027', 12, 12, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-20', '2026-05-05', 999, 0, 0, 999, 0, 'USD', '2026-04-20 00:00:00', '2026-04-20 00:00:00'),
            (28, 'INV-1028', 12, 12, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-20', '2026-06-04', 999, 0, 0, 999, 0, 'USD', '2026-05-20 00:00:00', '2026-05-20 00:00:00'),
            (29, 'INV-1029', 12, 12, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-20', '2026-07-05', 999, 0, 0, 999, 999, 'USD', '2026-06-20 00:00:00', '2026-06-20 00:00:00');

            -- Subscription 13 (USD, plan5, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (30, 'INV-1030', 13, 13, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-14', '2026-04-29', 799, 0, 0, 799, 0, 'USD', '2026-04-14 00:00:00', '2026-04-14 00:00:00'),
            (31, 'INV-1031', 13, 13, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-14', '2026-05-29', 799, 0, 0, 799, 0, 'USD', '2026-05-14 00:00:00', '2026-05-14 00:00:00'),
            (32, 'INV-1032', 13, 13, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-14', '2026-06-29', 799, 0, 0, 799, 799, 'USD', '2026-06-14 00:00:00', '2026-06-14 00:00:00');

            -- Subscription 14 (USD, plan1, PAUSED)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (33, 'INV-1033', 14, 14, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-01', '2026-04-15', 499, 0, 0, 499, 0, 'USD', '2026-04-01 00:00:00', '2026-04-01 00:00:00'),
            (34, 'INV-1034', 14, 14, 'VOID', 'SUBSCRIPTION_CYCLE', '2026-05-01', '2026-05-15', 499, 0, 0, 499, 499, 'USD', '2026-05-01 00:00:00', '2026-05-01 00:00:00');

            -- Subscription 15 (USD, plan3, CANCELED)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (35, 'INV-1035', 15, 15, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-20', '2026-05-05', 999, 0, 0, 999, 0, 'USD', '2026-04-20 00:00:00', '2026-04-20 00:00:00'),
            (36, 'INV-1036', 15, 15, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-20', '2026-06-04', 999, 0, 0, 999, 0, 'USD', '2026-05-20 00:00:00', '2026-05-20 00:00:00');

            -- Subscription 16 (USD, plan5, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (37, 'INV-1037', 16, 16, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-05', '2026-05-20', 799, 0, 0, 799, 0, 'USD', '2026-05-05 00:00:00', '2026-05-05 00:00:00'),
            (38, 'INV-1038', 16, 16, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-05', '2026-06-20', 799, 0, 0, 799, 799, 'USD', '2026-06-05 00:00:00', '2026-06-05 00:00:00');

            -- Subscription 17 (USD, plan1, TRIALING – no invoice)
            -- Subscription 18 (GBP, plan3, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (39, 'INV-1039', 18, 18, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-11', '2026-04-26', 799, 0, 0, 799, 0, 'GBP', '2026-04-11 00:00:00', '2026-04-11 00:00:00'),
            (40, 'INV-1040', 18, 18, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-11', '2026-05-26', 799, 0, 0, 799, 0, 'GBP', '2026-05-11 00:00:00', '2026-05-11 00:00:00'),
            (41, 'INV-1041', 18, 18, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-11', '2026-06-26', 799, 0, 0, 799, 799, 'GBP', '2026-06-11 00:00:00', '2026-06-11 00:00:00');

            -- Subscription 19 (GBP, plan5, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (42, 'INV-1042', 19, 19, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-01', '2026-04-15', 599, 0, 0, 599, 0, 'GBP', '2026-04-01 00:00:00', '2026-04-01 00:00:00'),
            (43, 'INV-1043', 19, 19, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-01', '2026-05-15', 599, 0, 0, 599, 0, 'GBP', '2026-05-01 00:00:00', '2026-05-01 00:00:00'),
            (44, 'INV-1044', 19, 19, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-01', '2026-06-15', 599, 0, 0, 599, 599, 'GBP', '2026-06-01 00:00:00', '2026-06-01 00:00:00');

            -- Subscription 20 (GBP, plan1, CANCELED)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (45, 'INV-1045', 20, 20, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-15', '2026-04-30', 399, 0, 0, 399, 0, 'GBP', '2026-04-15 00:00:00', '2026-04-15 00:00:00'),
            (46, 'INV-1046', 20, 20, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-15', '2026-05-30', 399, 0, 0, 399, 0, 'GBP', '2026-05-15 00:00:00', '2026-05-15 00:00:00');

            -- Subscription 21 (GBP, plan3, PAST_DUE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (47, 'INV-1047', 21, 21, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-20', '2026-05-05', 799, 0, 0, 799, 0, 'GBP', '2026-04-20 00:00:00', '2026-04-20 00:00:00'),
            (48, 'INV-1048', 21, 21, 'UNCOLLECTIBLE', 'SUBSCRIPTION_CYCLE', '2026-05-20', '2026-06-04', 799, 0, 0, 799, 799, 'GBP', '2026-05-20 00:00:00', '2026-05-20 00:00:00');

            -- Subscription 22 (GBP, plan5, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (49, 'INV-1049', 22, 22, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-10', '2026-04-25', 599, 0, 0, 599, 0, 'GBP', '2026-04-10 00:00:00', '2026-04-10 00:00:00'),
            (50, 'INV-1050', 22, 22, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-10', '2026-05-25', 599, 0, 0, 599, 0, 'GBP', '2026-05-10 00:00:00', '2026-05-10 00:00:00'),
            (51, 'INV-1051', 22, 22, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-10', '2026-06-25', 599, 0, 0, 599, 599, 'GBP', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

            -- Subscription 23 (GBP, plan1, DRAFT – no invoice yet)
            -- Subscription 24 (GBP, plan3, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (52, 'INV-1052', 24, 24, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-06-05', '2026-06-20', 799, 0, 0, 799, 0, 'GBP', '2026-06-05 00:00:00', '2026-06-05 00:00:00');

            -- Subscription 25 (GBP, plan5, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (53, 'INV-1053', 25, 25, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-20', '2026-07-05', 599, 0, 0, 599, 599, 'GBP', '2026-06-20 00:00:00', '2026-06-20 00:00:00');

            -- Subscription 26 (USD, plan1, CANCELED)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (54, 'INV-1054', 26, 26, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-01-20', '2026-02-05', 499, 0, 0, 499, 0, 'USD', '2026-01-20 00:00:00', '2026-01-20 00:00:00'),
            (55, 'INV-1055', 26, 26, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-02-20', '2026-03-07', 499, 0, 0, 499, 0, 'USD', '2026-02-20 00:00:00', '2026-02-20 00:00:00');

            -- Subscription 27 (USD, plan3, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (56, 'INV-1056', 27, 27, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-25', '2026-05-10', 999, 0, 0, 999, 0, 'USD', '2026-04-25 00:00:00', '2026-04-25 00:00:00'),
            (57, 'INV-1057', 27, 27, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-25', '2026-06-09', 999, 0, 0, 999, 0, 'USD', '2026-05-25 00:00:00', '2026-05-25 00:00:00'),
            (58, 'INV-1058', 27, 27, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-25', '2026-07-10', 999, 0, 0, 999, 999, 'USD', '2026-06-25 00:00:00', '2026-06-25 00:00:00');

            -- Subscription 28 (INR, plan1, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (59, 'INV-1059', 28, 28, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-01', '2026-04-15', 19900, 0, 0, 19900, 0, 'INR', '2026-04-01 00:00:00', '2026-04-01 00:00:00'),
            (60, 'INV-1060', 28, 28, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-01', '2026-05-15', 19900, 0, 0, 19900, 0, 'INR', '2026-05-01 00:00:00', '2026-05-01 00:00:00'),
            (61, 'INV-1061', 28, 28, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-01', '2026-06-15', 19900, 0, 0, 19900, 19900, 'INR', '2026-06-01 00:00:00', '2026-06-01 00:00:00');

            -- Subscription 29 (INR, plan5, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (62, 'INV-1062', 29, 29, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-15', '2026-04-30', 29900, 0, 0, 29900, 0, 'INR', '2026-04-15 00:00:00', '2026-04-15 00:00:00'),
            (63, 'INV-1063', 29, 29, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-15', '2026-05-30', 29900, 0, 0, 29900, 0, 'INR', '2026-05-15 00:00:00', '2026-05-15 00:00:00'),
            (64, 'INV-1064', 29, 29, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-15', '2026-06-30', 29900, 0, 0, 29900, 29900, 'INR', '2026-06-15 00:00:00', '2026-06-15 00:00:00');

            -- Subscription 30 (INR, plan3, PAUSED)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (65, 'INV-1065', 30, 30, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-30', '2026-06-14', 49900, 0, 0, 49900, 0, 'INR', '2026-05-30 00:00:00', '2026-05-30 00:00:00'),
            (66, 'INV-1066', 30, 30, 'DRAFT', 'SUBSCRIPTION_CYCLE', '2026-06-30', '2026-07-15', 49900, 0, 0, 49900, 49900, 'INR', '2026-06-30 00:00:00', '2026-06-30 00:00:00');

            -- Subscription 31 (INR, plan1, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (67, 'INV-1067', 31, 31, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-10', '2026-06-25', 19900, 0, 0, 19900, 19900, 'INR', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

            -- Subscription 32 (INR, plan5, TRIALING – no invoice)
            -- Subscription 33 (INR, plan3, CANCELED)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (68, 'INV-1068', 33, 33, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-03-25', '2026-04-09', 49900, 0, 0, 49900, 0, 'INR', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
            (69, 'INV-1069', 33, 33, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-25', '2026-05-10', 49900, 0, 0, 49900, 0, 'INR', '2026-04-25 00:00:00', '2026-04-25 00:00:00');

            -- Subscription 34 (INR, plan1, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (70, 'INV-1070', 34, 34, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-30', '2026-05-15', 19900, 0, 0, 19900, 0, 'INR', '2026-04-30 00:00:00', '2026-04-30 00:00:00'),
            (71, 'INV-1071', 34, 34, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-30', '2026-06-14', 19900, 0, 0, 19900, 0, 'INR', '2026-05-30 00:00:00', '2026-05-30 00:00:00'),
            (72, 'INV-1072', 34, 34, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-30', '2026-07-15', 19900, 0, 0, 19900, 19900, 'INR', '2026-06-30 00:00:00', '2026-06-30 00:00:00');

            -- Subscription 35 (INR, plan5, PAST_DUE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (73, 'INV-1073', 35, 35, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-05', '2026-04-20', 29900, 0, 0, 29900, 0, 'INR', '2026-04-05 00:00:00', '2026-04-05 00:00:00'),
            (74, 'INV-1074', 35, 35, 'UNCOLLECTIBLE', 'SUBSCRIPTION_CYCLE', '2026-05-05', '2026-05-20', 29900, 0, 0, 29900, 29900, 'INR', '2026-05-05 00:00:00', '2026-05-05 00:00:00');

            -- Subscription 36 (INR, plan3, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (75, 'INV-1075', 36, 36, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-12', '2026-05-27', 49900, 0, 0, 49900, 0, 'INR', '2026-05-12 00:00:00', '2026-05-12 00:00:00'),
            (76, 'INV-1076', 36, 36, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-12', '2026-06-27', 49900, 0, 0, 49900, 49900, 'INR', '2026-06-12 00:00:00', '2026-06-12 00:00:00');

            -- Subscription 37 (INR, plan1, ON_HOLD)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (77, 'INV-1077', 37, 37, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-25', '2026-07-10', 19900, 0, 0, 19900, 19900, 'INR', '2026-06-25 00:00:00', '2026-06-25 00:00:00');

            -- Subscription 38 (USD, plan5, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (78, 'INV-1078', 38, 38, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-10', '2026-04-25', 799, 0, 0, 799, 0, 'USD', '2026-04-10 00:00:00', '2026-04-10 00:00:00'),
            (79, 'INV-1079', 38, 38, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-10', '2026-05-25', 799, 0, 0, 799, 0, 'USD', '2026-05-10 00:00:00', '2026-05-10 00:00:00'),
            (80, 'INV-1080', 38, 38, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-10', '2026-06-25', 799, 0, 0, 799, 799, 'USD', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

            -- Subscription 39 (GBP, plan3, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (81, 'INV-1081', 39, 39, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-04-25', '2026-05-10', 799, 0, 0, 799, 0, 'GBP', '2026-04-25 00:00:00', '2026-04-25 00:00:00'),
            (82, 'INV-1082', 39, 39, 'PAID', 'SUBSCRIPTION_CYCLE', '2026-05-25', '2026-06-09', 799, 0, 0, 799, 0, 'GBP', '2026-05-25 00:00:00', '2026-05-25 00:00:00'),
            (83, 'INV-1083', 39, 39, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-25', '2026-07-10', 799, 0, 0, 799, 799, 'GBP', '2026-06-25 00:00:00', '2026-06-25 00:00:00');

            -- Subscription 40 (GBP, plan5, ACTIVE)
            INSERT INTO invoice (invoice_id, invoice_number, customer_id, subscription_id, status, billing_reason, issue_date, due_date, subtotal_minor, tax_minor, discount_minor, total_minor, balance_minor, currency, created_at, updated_at) VALUES
            (84, 'INV-1084', 40, 40, 'OPEN', 'SUBSCRIPTION_CYCLE', '2026-06-05', '2026-06-20', 599, 0, 0, 599, 599, 'GBP', '2026-06-05 00:00:00', '2026-06-05 00:00:00');

            -- ============================================================================
            -- 7. INVOICE_LINE_ITEM TABLE (one line item per invoice, type PLAN)
            -- ============================================================================
            INSERT INTO invoice_line_item (line_item_id, invoice_id, description, line_type, quantity, unit_price_minor, amount_minor, period_start, period_end) VALUES
            (1, 1, 'Basic Monthly - Apr 2026', 'PLAN', 1, 19900, 19900, '2026-04-01', '2026-05-01'),
            (2, 2, 'Basic Monthly - May 2026', 'PLAN', 1, 19900, 19900, '2026-05-01', '2026-06-01'),
            (3, 3, 'Basic Monthly - Jun 2026', 'PLAN', 1, 19900, 19900, '2026-06-01', '2026-07-01'),
            (4, 4, 'Standard Monthly - Apr 2026', 'PLAN', 1, 29900, 29900, '2026-04-15', '2026-05-15'),
            (5, 5, 'Standard Monthly - May 2026', 'PLAN', 1, 29900, 29900, '2026-05-15', '2026-06-15'),
            (6, 6, 'Standard Monthly - Jun 2026', 'PLAN', 1, 29900, 29900, '2026-06-15', '2026-07-15'),
            (7, 7, 'Premium Monthly - Apr 2026', 'PLAN', 1, 49900, 49900, '2026-04-01', '2026-05-01'),
            (8, 8, 'Premium Monthly - May 2026', 'PLAN', 1, 49900, 49900, '2026-05-01', '2026-06-01'),
            (9, 9, 'Basic Monthly - Mar 2026', 'PLAN', 1, 19900, 19900, '2026-03-20', '2026-04-20'),
            (10, 10, 'Basic Monthly - Apr 2026', 'PLAN', 1, 19900, 19900, '2026-04-20', '2026-05-20'),
            (11, 11, 'Standard Monthly - Apr 2026', 'PLAN', 1, 29900, 29900, '2026-04-05', '2026-05-05'),
            (12, 12, 'Standard Monthly - May 2026', 'PLAN', 1, 29900, 29900, '2026-05-05', '2026-06-05'),
            (13, 13, 'Standard Monthly - Jun 2026', 'PLAN', 1, 29900, 29900, '2026-06-05', '2026-07-05'),
            (14, 14, 'Premium Monthly - Apr 2026', 'PLAN', 1, 49900, 49900, '2026-04-25', '2026-05-25'),
            (15, 15, 'Premium Monthly - May 2026', 'PLAN', 1, 49900, 49900, '2026-05-25', '2026-06-25'),
            (16, 16, 'Basic Monthly - Apr 2026', 'PLAN', 1, 499, 499, '2026-04-01', '2026-05-01'),
            (17, 17, 'Basic Monthly - May 2026', 'PLAN', 1, 499, 499, '2026-05-01', '2026-06-01'),
            (18, 18, 'Basic Monthly - Jun 2026', 'PLAN', 1, 499, 499, '2026-06-01', '2026-07-01'),
            (19, 19, 'Premium Monthly - Apr 2026', 'PLAN', 1, 999, 999, '2026-04-10', '2026-05-10'),
            (20, 20, 'Premium Monthly - May 2026', 'PLAN', 1, 999, 999, '2026-05-10', '2026-06-10'),
            (21, 21, 'Premium Monthly - Jun 2026', 'PLAN', 1, 999, 999, '2026-06-10', '2026-07-10'),
            (22, 22, 'Standard Monthly - Feb 2026', 'PLAN', 1, 799, 799, '2026-02-05', '2026-03-05'),
            (23, 23, 'Standard Monthly - Mar 2026', 'PLAN', 1, 799, 799, '2026-03-05', '2026-04-05'),
            (24, 24, 'Basic Monthly - Apr 2026', 'PLAN', 1, 499, 499, '2026-04-15', '2026-05-15'),
            (25, 25, 'Basic Monthly - May 2026', 'PLAN', 1, 499, 499, '2026-05-15', '2026-06-15'),
            (26, 26, 'Basic Monthly - Jun 2026', 'PLAN', 1, 499, 499, '2026-06-15', '2026-07-15'),
            (27, 27, 'Premium Monthly - Apr 2026', 'PLAN', 1, 999, 999, '2026-04-20', '2026-05-20'),
            (28, 28, 'Premium Monthly - May 2026', 'PLAN', 1, 999, 999, '2026-05-20', '2026-06-20'),
            (29, 29, 'Premium Monthly - Jun 2026', 'PLAN', 1, 999, 999, '2026-06-20', '2026-07-20'),
            (30, 30, 'Standard Monthly - Apr 2026', 'PLAN', 1, 799, 799, '2026-04-14', '2026-05-14'),
            (31, 31, 'Standard Monthly - May 2026', 'PLAN', 1, 799, 799, '2026-05-14', '2026-06-14'),
            (32, 32, 'Standard Monthly - Jun 2026', 'PLAN', 1, 799, 799, '2026-06-14', '2026-07-14'),
            (33, 33, 'Basic Monthly - Apr 2026', 'PLAN', 1, 499, 499, '2026-04-01', '2026-05-01'),
            (34, 34, 'Basic Monthly - May 2026', 'PLAN', 1, 499, 499, '2026-05-01', '2026-06-01'),
            (35, 35, 'Premium Monthly - Apr 2026', 'PLAN', 1, 999, 999, '2026-04-20', '2026-05-20'),
            (36, 36, 'Premium Monthly - May 2026', 'PLAN', 1, 999, 999, '2026-05-20', '2026-06-20'),
            (37, 37, 'Standard Monthly - May 2026', 'PLAN', 1, 799, 799, '2026-05-05', '2026-06-05'),
            (38, 38, 'Standard Monthly - Jun 2026', 'PLAN', 1, 799, 799, '2026-06-05', '2026-07-05'),
            (39, 39, 'Premium Monthly - Apr 2026', 'PLAN', 1, 799, 799, '2026-04-11', '2026-05-11'),
            (40, 40, 'Premium Monthly - May 2026', 'PLAN', 1, 799, 799, '2026-05-11', '2026-06-11'),
            (41, 41, 'Premium Monthly - Jun 2026', 'PLAN', 1, 799, 799, '2026-06-11', '2026-07-11'),
            (42, 42, 'Standard Monthly - Apr 2026', 'PLAN', 1, 599, 599, '2026-04-01', '2026-05-01'),
            (43, 43, 'Standard Monthly - May 2026', 'PLAN', 1, 599, 599, '2026-05-01', '2026-06-01'),
            (44, 44, 'Standard Monthly - Jun 2026', 'PLAN', 1, 599, 599, '2026-06-01', '2026-07-01'),
            (45, 45, 'Basic Monthly - Apr 2026', 'PLAN', 1, 399, 399, '2026-04-15', '2026-05-15'),
            (46, 46, 'Basic Monthly - May 2026', 'PLAN', 1, 399, 399, '2026-05-15', '2026-06-15'),
            (47, 47, 'Premium Monthly - Apr 2026', 'PLAN', 1, 799, 799, '2026-04-20', '2026-05-20'),
            (48, 48, 'Premium Monthly - May 2026', 'PLAN', 1, 799, 799, '2026-05-20', '2026-06-20'),
            (49, 49, 'Standard Monthly - Apr 2026', 'PLAN', 1, 599, 599, '2026-04-10', '2026-05-10'),
            (50, 50, 'Standard Monthly - May 2026', 'PLAN', 1, 599, 599, '2026-05-10', '2026-06-10'),
            (51, 51, 'Standard Monthly - Jun 2026', 'PLAN', 1, 599, 599, '2026-06-10', '2026-07-10'),
            (52, 52, 'Premium Monthly - Jun 2026', 'PLAN', 1, 799, 799, '2026-06-05', '2026-07-05'),
            (53, 53, 'Standard Monthly - Jun 2026', 'PLAN', 1, 599, 599, '2026-06-20', '2026-07-20'),
            (54, 54, 'Basic Monthly - Jan 2026', 'PLAN', 1, 499, 499, '2026-01-20', '2026-02-20'),
            (55, 55, 'Basic Monthly - Feb 2026', 'PLAN', 1, 499, 499, '2026-02-20', '2026-03-20'),
            (56, 56, 'Premium Monthly - Apr 2026', 'PLAN', 1, 999, 999, '2026-04-25', '2026-05-25'),
            (57, 57, 'Premium Monthly - May 2026', 'PLAN', 1, 999, 999, '2026-05-25', '2026-06-25'),
            (58, 58, 'Premium Monthly - Jun 2026', 'PLAN', 1, 999, 999, '2026-06-25', '2026-07-25'),
            (59, 59, 'Basic Monthly - Apr 2026', 'PLAN', 1, 19900, 19900, '2026-04-01', '2026-05-01'),
            (60, 60, 'Basic Monthly - May 2026', 'PLAN', 1, 19900, 19900, '2026-05-01', '2026-06-01'),
            (61, 61, 'Basic Monthly - Jun 2026', 'PLAN', 1, 19900, 19900, '2026-06-01', '2026-07-01'),
            (62, 62, 'Standard Monthly - Apr 2026', 'PLAN', 1, 29900, 29900, '2026-04-15', '2026-05-15'),
            (63, 63, 'Standard Monthly - May 2026', 'PLAN', 1, 29900, 29900, '2026-05-15', '2026-06-15'),
            (64, 64, 'Standard Monthly - Jun 2026', 'PLAN', 1, 29900, 29900, '2026-06-15', '2026-07-15'),
            (65, 65, 'Premium Monthly - May 2026', 'PLAN', 1, 49900, 49900, '2026-05-30', '2026-06-30'),
            (66, 66, 'Premium Monthly - Jun 2026', 'PLAN', 1, 49900, 49900, '2026-06-30', '2026-07-30'),
            (67, 67, 'Basic Monthly - Jun 2026', 'PLAN', 1, 19900, 19900, '2026-06-10', '2026-07-10'),
            (68, 68, 'Premium Monthly - Mar 2026', 'PLAN', 1, 49900, 49900, '2026-03-25', '2026-04-25'),
            (69, 69, 'Premium Monthly - Apr 2026', 'PLAN', 1, 49900, 49900, '2026-04-25', '2026-05-25'),
            (70, 70, 'Basic Monthly - Apr 2026', 'PLAN', 1, 19900, 19900, '2026-04-30', '2026-05-30'),
            (71, 71, 'Basic Monthly - May 2026', 'PLAN', 1, 19900, 19900, '2026-05-30', '2026-06-30'),
            (72, 72, 'Basic Monthly - Jun 2026', 'PLAN', 1, 19900, 19900, '2026-06-30', '2026-07-30'),
            (73, 73, 'Standard Monthly - Apr 2026', 'PLAN', 1, 29900, 29900, '2026-04-05', '2026-05-05'),
            (74, 74, 'Standard Monthly - May 2026', 'PLAN', 1, 29900, 29900, '2026-05-05', '2026-06-05'),
            (75, 75, 'Premium Monthly - May 2026', 'PLAN', 1, 49900, 49900, '2026-05-12', '2026-06-12'),
            (76, 76, 'Premium Monthly - Jun 2026', 'PLAN', 1, 49900, 49900, '2026-06-12', '2026-07-12'),
            (77, 77, 'Basic Monthly - Jun 2026', 'PLAN', 1, 19900, 19900, '2026-06-25', '2026-07-25'),
            (78, 78, 'Standard Monthly - Apr 2026', 'PLAN', 1, 799, 799, '2026-04-10', '2026-05-10'),
            (79, 79, 'Standard Monthly - May 2026', 'PLAN', 1, 799, 799, '2026-05-10', '2026-06-10'),
            (80, 80, 'Standard Monthly - Jun 2026', 'PLAN', 1, 799, 799, '2026-06-10', '2026-07-10'),
            (81, 81, 'Premium Monthly - Apr 2026', 'PLAN', 1, 799, 799, '2026-04-25', '2026-05-25'),
            (82, 82, 'Premium Monthly - May 2026', 'PLAN', 1, 799, 799, '2026-05-25', '2026-06-25'),
            (83, 83, 'Premium Monthly - Jun 2026', 'PLAN', 1, 799, 799, '2026-06-25', '2026-07-25'),
            (84, 84, 'Standard Monthly - Jun 2026', 'PLAN', 1, 599, 599, '2026-06-05', '2026-07-05');

            -- ============================================================================
            -- 8. PAYMENT TABLE (payments for PAID invoices, some failures/refunds)
            -- ============================================================================
            INSERT INTO payment (payment_id, invoice_id, payment_method_id, idempotency_key, gateway_ref, amount_minor, currency, status, attempt_no, created_at, updated_at) VALUES
            (1, 1, 1, 'pay_idem_1', 'gw_ref_1', 19900, 'INR', 'SUCCESS', 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
            (2, 2, 1, 'pay_idem_2', 'gw_ref_2', 19900, 'INR', 'SUCCESS', 1, '2026-05-02 10:00:00', '2026-05-02 10:00:00'),
            (3, 4, 2, 'pay_idem_4', 'gw_ref_4', 29900, 'INR', 'SUCCESS', 1, '2026-04-16 11:00:00', '2026-04-16 11:00:00'),
            (4, 5, 2, 'pay_idem_5', 'gw_ref_5', 29900, 'INR', 'SUCCESS', 1, '2026-05-16 11:00:00', '2026-05-16 11:00:00'),
            (5, 7, 3, 'pay_idem_7', 'gw_ref_7', 49900, 'INR', 'SUCCESS', 1, '2026-04-02 12:00:00', '2026-04-02 12:00:00'),
            (6, 9, 4, 'pay_idem_9', 'gw_ref_9', 19900, 'INR', 'SUCCESS', 1, '2026-03-21 09:00:00', '2026-03-21 09:00:00'),
            (7, 10, 4, 'pay_idem_10', 'gw_ref_10', 19900, 'INR', 'SUCCESS', 1, '2026-04-21 09:00:00', '2026-04-21 09:00:00'),
            (8, 11, 5, 'pay_idem_11', 'gw_ref_11', 29900, 'INR', 'SUCCESS', 1, '2026-04-06 14:00:00', '2026-04-06 14:00:00'),
            (9, 12, 5, 'pay_idem_12', 'gw_ref_12', 29900, 'INR', 'SUCCESS', 1, '2026-05-06 14:00:00', '2026-05-06 14:00:00'),
            (10, 14, 7, 'pay_idem_14', 'gw_ref_14', 49900, 'INR', 'SUCCESS', 1, '2026-04-26 15:00:00', '2026-04-26 15:00:00'),
            (11, 15, 7, 'pay_idem_15', 'gw_ref_15', 49900, 'INR', 'FAILED', 1, '2026-05-26 15:00:00', '2026-05-26 15:00:00'),
            (12, 16, 8, 'pay_idem_16', 'gw_ref_16', 499, 'USD', 'SUCCESS', 1, '2026-04-02 08:00:00', '2026-04-02 08:00:00'),
            (13, 17, 8, 'pay_idem_17', 'gw_ref_17', 499, 'USD', 'SUCCESS', 1, '2026-05-02 08:00:00', '2026-05-02 08:00:00'),
            (14, 19, 9, 'pay_idem_19', 'gw_ref_19', 999, 'USD', 'SUCCESS', 1, '2026-04-11 09:00:00', '2026-04-11 09:00:00'),
            (15, 20, 9, 'pay_idem_20', 'gw_ref_20', 999, 'USD', 'SUCCESS', 1, '2026-05-11 09:00:00', '2026-05-11 09:00:00'),
            (16, 22, 10, 'pay_idem_22', 'gw_ref_22', 799, 'USD', 'SUCCESS', 1, '2026-02-06 10:00:00', '2026-02-06 10:00:00'),
            (17, 23, 10, 'pay_idem_23', 'gw_ref_23', 799, 'USD', 'SUCCESS', 1, '2026-03-06 10:00:00', '2026-03-06 10:00:00'),
            (18, 24, 11, 'pay_idem_24', 'gw_ref_24', 499, 'USD', 'SUCCESS', 1, '2026-04-16 11:00:00', '2026-04-16 11:00:00'),
            (19, 25, 11, 'pay_idem_25', 'gw_ref_25', 499, 'USD', 'SUCCESS', 1, '2026-05-16 11:00:00', '2026-05-16 11:00:00'),
            (20, 27, 12, 'pay_idem_27', 'gw_ref_27', 999, 'USD', 'SUCCESS', 1, '2026-04-21 12:00:00', '2026-04-21 12:00:00'),
            (21, 28, 12, 'pay_idem_28', 'gw_ref_28', 999, 'USD', 'SUCCESS', 1, '2026-05-21 12:00:00', '2026-05-21 12:00:00'),
            (22, 30, 13, 'pay_idem_30', 'gw_ref_30', 799, 'USD', 'SUCCESS', 1, '2026-04-15 13:00:00', '2026-04-15 13:00:00'),
            (23, 31, 13, 'pay_idem_31', 'gw_ref_31', 799, 'USD', 'SUCCESS', 1, '2026-05-15 13:00:00', '2026-05-15 13:00:00'),
            (24, 33, 14, 'pay_idem_33', 'gw_ref_33', 499, 'USD', 'SUCCESS', 1, '2026-04-02 14:00:00', '2026-04-02 14:00:00'),
            (25, 35, 15, 'pay_idem_35', 'gw_ref_35', 999, 'USD', 'SUCCESS', 1, '2026-04-21 15:00:00', '2026-04-21 15:00:00'),
            (26, 36, 15, 'pay_idem_36', 'gw_ref_36', 999, 'USD', 'REFUNDED', 1, '2026-05-21 15:00:00', '2026-05-21 15:00:00'),
            (27, 37, 16, 'pay_idem_37', 'gw_ref_37', 799, 'USD', 'SUCCESS', 1, '2026-05-06 09:00:00', '2026-05-06 09:00:00'),
            (28, 39, 18, 'pay_idem_39', 'gw_ref_39', 799, 'GBP', 'SUCCESS', 1, '2026-04-12 10:00:00', '2026-04-12 10:00:00'),
            (29, 40, 18, 'pay_idem_40', 'gw_ref_40', 799, 'GBP', 'SUCCESS', 1, '2026-05-12 10:00:00', '2026-05-12 10:00:00'),
            (30, 42, 19, 'pay_idem_42', 'gw_ref_42', 599, 'GBP', 'SUCCESS', 1, '2026-04-02 11:00:00', '2026-04-02 11:00:00'),
            (31, 43, 19, 'pay_idem_43', 'gw_ref_43', 599, 'GBP', 'SUCCESS', 1, '2026-05-02 11:00:00', '2026-05-02 11:00:00'),
            (32, 45, 20, 'pay_idem_45', 'gw_ref_45', 399, 'GBP', 'SUCCESS', 1, '2026-04-16 12:00:00', '2026-04-16 12:00:00'),
            (33, 46, 20, 'pay_idem_46', 'gw_ref_46', 399, 'GBP', 'SUCCESS', 1, '2026-05-16 12:00:00', '2026-05-16 12:00:00'),
            (34, 47, 21, 'pay_idem_47', 'gw_ref_47', 799, 'GBP', 'SUCCESS', 1, '2026-04-21 13:00:00', '2026-04-21 13:00:00'),
            (35, 49, 22, 'pay_idem_49', 'gw_ref_49', 599, 'GBP', 'SUCCESS', 1, '2026-04-11 14:00:00', '2026-04-11 14:00:00'),
            (36, 50, 22, 'pay_idem_50', 'gw_ref_50', 599, 'GBP', 'SUCCESS', 1, '2026-05-11 14:00:00', '2026-05-11 14:00:00'),
            (37, 52, 24, 'pay_idem_52', 'gw_ref_52', 799, 'GBP', 'SUCCESS', 1, '2026-06-06 15:00:00', '2026-06-06 15:00:00'),
            (38, 54, 26, 'pay_idem_54', 'gw_ref_54', 499, 'USD', 'SUCCESS', 1, '2026-01-21 16:00:00', '2026-01-21 16:00:00'),
            (39, 55, 26, 'pay_idem_55', 'gw_ref_55', 499, 'USD', 'SUCCESS', 1, '2026-02-21 16:00:00', '2026-02-21 16:00:00'),
            (40, 56, 27, 'pay_idem_56', 'gw_ref_56', 999, 'USD', 'SUCCESS', 1, '2026-04-26 17:00:00', '2026-04-26 17:00:00'),
            (41, 57, 27, 'pay_idem_57', 'gw_ref_57', 999, 'USD', 'SUCCESS', 1, '2026-05-26 17:00:00', '2026-05-26 17:00:00'),
            (42, 59, 28, 'pay_idem_59', 'gw_ref_59', 19900, 'INR', 'SUCCESS', 1, '2026-04-02 18:00:00', '2026-04-02 18:00:00'),
            (43, 60, 28, 'pay_idem_60', 'gw_ref_60', 19900, 'INR', 'SUCCESS', 1, '2026-05-02 18:00:00', '2026-05-02 18:00:00'),
            (44, 62, 29, 'pay_idem_62', 'gw_ref_62', 29900, 'INR', 'SUCCESS', 1, '2026-04-16 19:00:00', '2026-04-16 19:00:00'),
            (45, 63, 29, 'pay_idem_63', 'gw_ref_63', 29900, 'INR', 'SUCCESS', 1, '2026-05-16 19:00:00', '2026-05-16 19:00:00'),
            (46, 65, 30, 'pay_idem_65', 'gw_ref_65', 49900, 'INR', 'SUCCESS', 1, '2026-06-01 20:00:00', '2026-06-01 20:00:00'),
            (47, 68, 33, 'pay_idem_68', 'gw_ref_68', 49900, 'INR', 'SUCCESS', 1, '2026-03-26 21:00:00', '2026-03-26 21:00:00'),
            (48, 69, 33, 'pay_idem_69', 'gw_ref_69', 49900, 'INR', 'SUCCESS', 1, '2026-04-26 21:00:00', '2026-04-26 21:00:00'),
            (49, 70, 34, 'pay_idem_70', 'gw_ref_70', 19900, 'INR', 'SUCCESS', 1, '2026-05-01 22:00:00', '2026-05-01 22:00:00'),
            (50, 71, 34, 'pay_idem_71', 'gw_ref_71', 19900, 'INR', 'SUCCESS', 1, '2026-06-01 22:00:00', '2026-06-01 22:00:00'),
            (51, 73, 35, 'pay_idem_73', 'gw_ref_73', 29900, 'INR', 'SUCCESS', 1, '2026-04-06 23:00:00', '2026-04-06 23:00:00'),
            (52, 74, 35, 'pay_idem_74', 'gw_ref_74', 29900, 'INR', 'FAILED', 1, '2026-05-06 23:00:00', '2026-05-06 23:00:00'),
            (53, 75, 36, 'pay_idem_75', 'gw_ref_75', 49900, 'INR', 'SUCCESS', 1, '2026-05-13 09:00:00', '2026-05-13 09:00:00'),
            (54, 78, 38, 'pay_idem_78', 'gw_ref_78', 799, 'USD', 'SUCCESS', 1, '2026-04-11 10:00:00', '2026-04-11 10:00:00'),
            (55, 79, 38, 'pay_idem_79', 'gw_ref_79', 799, 'USD', 'SUCCESS', 1, '2026-05-11 10:00:00', '2026-05-11 10:00:00'),
            (56, 81, 39, 'pay_idem_81', 'gw_ref_81', 799, 'GBP', 'SUCCESS', 1, '2026-04-26 11:00:00', '2026-04-26 11:00:00'),
            (57, 82, 39, 'pay_idem_82', 'gw_ref_82', 799, 'GBP', 'SUCCESS', 1, '2026-05-26 11:00:00', '2026-05-26 11:00:00');

            -- Additional payment for a refunded invoice (already refunded via credit note later)
            INSERT INTO payment (payment_id, invoice_id, payment_method_id, idempotency_key, gateway_ref, amount_minor, currency, status, attempt_no, created_at, updated_at) VALUES
            (58, 35, 15, 'pay_idem_35b', 'gw_ref_35b', 999, 'USD', 'REFUNDED', 1, '2026-05-21 16:00:00', '2026-05-21 16:00:00');

            -- ============================================================================
            -- 9. CREDIT_NOTE TABLE (8 credit notes covering all statuses)
            -- ============================================================================
            INSERT INTO credit_note (credit_note_id, credit_note_number, invoice_id, reason, amount_minor, status, created_by, created_at) VALUES
            (1, 'CN-001', 26, 'Payment failed after multiple retries - service interruption', 499, 'APPLIED', 2, '2026-06-16 10:00:00'),
            (2, 'CN-002', 36, 'Customer requested refund for service issue', 999, 'APPLIED', 2, '2026-05-22 11:00:00'),
            (3, 'CN-003', 15, 'Uncollectible invoice - written off', 49900, 'ISSUED', 3, '2026-06-10 09:00:00'),
            (4, 'CN-004', 48, 'Payment failure after dunning', 799, 'ISSUED', 3, '2026-06-05 14:00:00'),
            (5, 'CN-005', 74, 'Bank error - duplicate charge', 29900, 'APPLIED', 2, '2026-05-10 16:00:00'),
            (6, 'CN-006', 34, 'Voided invoice due to pause', 499, 'VOIDED', 2, '2026-05-02 12:00:00'),
            (7, 'CN-007', 8, 'Plan change mid-cycle (not used in this dataset)', 49900, 'DRAFT', 3, '2026-05-01 08:00:00'),
            (8, 'CN-008', 66, 'Draft credit for future adjustment', 49900, 'DRAFT', 1, '2026-06-30 13:00:00');

            -- ============================================================================
            -- 10. REVENUE_SNAPSHOT TABLE (12 months, Jan–Dec 2026)
            -- ============================================================================
            INSERT INTO revenue_snapshot (snapshot_id, snapshot_date, mrr_minor, arr_minor, arpu_minor, active_customers, new_customers, churned_customers, gross_churn_percent, net_churn_percent, ltv_minor, total_revenue_minor, total_refunds_minor, created_at) VALUES
            (1, '2026-01-31', 214700, 2576400, 19518, 11, 5, 0, 0.00, 0.00, 390360, 430000, 0, '2026-01-31 23:59:00'),
            (2, '2026-02-28', 256400, 3076800, 19876, 13, 2, 1, 9.09, 0.00, 397520, 520000, 0, '2026-02-28 23:59:00'),
            (3, '2026-03-31', 328500, 3942000, 20531, 16, 4, 1, 7.69, 0.00, 410620, 660000, 0, '2026-03-31 23:59:00'),
            (4, '2026-04-30', 391200, 4694400, 20808, 19, 3, 0, 0.00, 0.00, 416160, 780000, 0, '2026-04-30 23:59:00'),
            (5, '2026-05-31', 421500, 5058000, 21075, 20, 2, 2, 10.53, 5.26, 421500, 840000, 10000, '2026-05-31 23:59:00'),
            (6, '2026-06-30', 458700, 5504400, 21843, 21, 4, 3, 15.00, 5.00, 436860, 920000, 5000, '2026-06-30 23:59:00'),
            (7, '2026-07-31', 476300, 5715600, 22681, 21, 2, 2, 9.52, 4.76, 453620, 950000, 2000, '2026-07-31 23:59:00'),
            (8, '2026-08-31', 494800, 5937600, 23562, 21, 3, 3, 14.29, 9.52, 471240, 990000, 3000, '2026-08-31 23:59:00'),
            (9, '2026-09-30', 512400, 6148800, 24371, 21, 2, 2, 9.52, 4.76, 487420, 1020000, 0, '2026-09-30 23:59:00'),
            (10, '2026-10-31', 531000, 6372000, 25286, 21, 3, 3, 14.29, 9.52, 505720, 1060000, 4000, '2026-10-31 23:59:00'),
            (11, '2026-11-30', 548700, 6584400, 26129, 21, 2, 2, 9.52, 4.76, 522580, 1100000, 0, '2026-11-30 23:59:00'),
            (12, '2026-12-31', 567200, 6806400, 27010, 21, 2, 2, 9.52, 4.76, 540200, 1130000, 2000, '2026-12-31 23:59:00');

            -- ============================================================================
            -- 11. SUBSCRIPTION_COUPON TABLE (coupon usage with different statuses)
            -- ============================================================================
            INSERT INTO subscription_coupon (id, subscription_id, coupon_id, applied_at, applied_by, expires_at, status) VALUES
            (1, 5, 1, '2025-12-05 11:45:00', NULL, '2026-06-05 23:59:59', 'ACTIVE'),
            (2, 12, 2, '2026-01-20 11:00:00', NULL, '2026-06-20 23:59:59', 'ACTIVE'),
            (3, 22, 3, '2026-03-10 08:15:00', NULL, '2026-06-10 23:59:59', 'EXPIRED'),
            (4, 31, 1, '2026-05-10 12:15:00', NULL, '2026-08-10 23:59:59', 'ACTIVE'),
            (5, 38, 2, '2026-02-10 08:00:00', 2, '2026-05-10 23:59:59', 'REVOKED'),
            (6, 40, 3, '2026-06-05 09:45:00', NULL, '2026-09-05 23:59:59', 'ACTIVE');

            SET FOREIGN_KEY_CHECKS = 1;
            -- END OF SEED DATA
