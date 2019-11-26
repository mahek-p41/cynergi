DELETE FROM audit_status_transitions_type_domain WHERE status_from = 5 AND status_to = 6;
DELETE FROM audit_status_type_domain WHERE ID = 6; -- delete the closed status as it isn't going to be necessary

INSERT INTO inventory_location_type_domain(id, value, description, localization_code) VALUES (9, 'ON-RENT', 'On Rent', 'inventory.value.on-rent');

ALTER TABLE employee
   ADD COLUMN department VARCHAR(10);
