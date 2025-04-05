INSERT INTO companies (name, budget) VALUES
('Company A', 1000000.00),
('Company B', 2000000.00),
('Company C', 3000000.00)
ON CONFLICT (name) DO NOTHING;


INSERT INTO users (first_name, last_name, phone_number) VALUES
('John', 'Doe', '+79123456789'),
('Jane', 'Smith', '+79234567890'),
('Mike', 'Johnson', '+79012345678')
ON CONFLICT (phone_number) DO NOTHING;