alter table employee_to_security_group add column emp_number integer;

alter table employee_to_security_group add constraint unique_employee_security_group

unique (employee_id_sfk, security_group_id, emp_number);
