ALTER TABLE schedule_arg ALTER COLUMN value TYPE TEXT;
ALTER TABLE schedule_arg ADD COLUMN encrypted BOOLEAN DEFAULT FALSE NOT NULL;

INSERT INTO schedule_command_type_domain VALUES (2, 'DarwillInactiveCustomer', 'Darwill Inactive Customers', 'darwill.inactive.customers');
INSERT INTO schedule_command_type_domain VALUES (3, 'DarwillActiveCustomer', 'Darwill Active Customers', 'darwill.active.customers');
INSERT INTO schedule_command_type_domain VALUES (4, 'DarwillBirthday', 'Darwill Birthday', 'darwill.birthday');
INSERT INTO schedule_command_type_domain VALUES (5, 'DarwillCollection', 'Darwill Collection', 'darwill.collection');
INSERT INTO schedule_command_type_domain VALUES (6, 'DarwillLastWeeksDelivery', 'Darwill Last Weeks Deliveries', 'darwill.last.weeks.deliveries');
INSERT INTO schedule_command_type_domain VALUES (7, 'DarwillLastWeeksPayout', 'Darwill Last Weeks Payouts', 'darwill.last.weeks.payouts');
