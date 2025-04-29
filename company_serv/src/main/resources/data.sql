INSERT INTO companies (name, budget) VALUES
('Company A', 1000000.00),
('Company B', 2000000.00),
('Company C', 3000000.00)
ON CONFLICT (name) DO NOTHING;