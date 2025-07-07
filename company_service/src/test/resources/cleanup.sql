DELETE FROM company_employee_ids;
DELETE FROM companies;
ALTER SEQUENCE companies_id_seq RESTART WITH 1;