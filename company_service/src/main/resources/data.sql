SET SCHEMA 'company';

INSERT INTO companies (name, budget) VALUES
('Company A', 1000000.00),
('Company B', 2000000.00),
('Company C', 3000000.00),
('Company D', 1500000.00),
('Company E', 500000.00),
('Company F', 750000.00),
('Company G', 1200000.00),
('Company H', 900000.00),
('Company I', 1300000.00),
('Company J', 1100000.00)
ON CONFLICT (name) DO NOTHING;

INSERT INTO company_employee_ids (company_id, employee_id) VALUES
(1, 1),
(1, 2),
(2, 3),
(2, 4),
(3, 5),
(3, 6),
(4, 7),
(4, 8),
(5, 9),
(5, 10),
(6, 11),
(6, 12),
(7, 13),
(7, 14),
(8, 15),
(8, 16),
(9, 17),
(9, 18),
(10, 19),
(10, 20)
ON CONFLICT ON CONSTRAINT uk_company_employee DO NOTHING;