INSERT INTO users (first_name, last_name, phone_number, company_id) VALUES
('John', 'Doe', '1234567890', 1),
('Jane', 'Smith', '9876543210', 1),
('Mike', 'Johnson', '5551234567', 2)
ON CONFLICT (id) DO NOTHING;