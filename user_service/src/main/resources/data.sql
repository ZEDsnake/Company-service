SET SCHEMA 'user_data';

INSERT INTO users (first_name, last_name, phone_number, company_id) VALUES
('Ivan', 'Ivanov', '+79110000001', 1),
('Petr', 'Petrov', '+79110000002', 1),
('Sergey', 'Sidorov', '+79110000003', 2),
('Alexey', 'Smirnov', '+79110000004', 2),
('Dmitry', 'Kuznetsov', '+79110000005', 3),
('Mikhail', 'Popov', '+79110000006', 3),
('Andrey', 'Volkov', '+79110000007', 4),
('Nikolay', 'Lebedev', '+79110000008', 4),
('Vladimir', 'Morozov', '+79110000009', 5),
('Yuri', 'Fedorov', '+79110000010', 5),
('Oleg', 'Novikov', '+79110000011', 6),
('Igor', 'Orlov', '+79110000012', 6),
('Maxim', 'Kozlov', '+79110000013', 7),
('Konstantin', 'Stepanov', '+79110000014', 7),
('Viktor', 'Nikolaev', '+79110000015', 8),
('Roman', 'Dmitriev', '+79110000016', 8),
('Evgeny', 'Solovyov', '+79110000017', 9),
('Alexandr', 'Vasiliev', '+79110000018', 9),
('Denis', 'Zaitsev', '+79110000019', 10),
('Kirill', 'Karpov', '+79110000020', 10)
ON CONFLICT (phone_number) DO NOTHING;