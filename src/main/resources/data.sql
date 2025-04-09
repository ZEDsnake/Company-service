INSERT INTO companies (name, budget) VALUES
('Company A', 1000000.00),
('Company B', 2000000.00),
('Company C', 3000000.00)
ON CONFLICT (name) DO NOTHING;


INSERT INTO users (first_name, last_name, phone_number, company_id) VALUES
('John', 'Doe', '+79123456789', 1),
('Jane', 'Smith', '+79234567890', 1),
('Mike', 'Johnson', '+79012345678', 2)
ON CONFLICT (phone_number) DO NOTHING;