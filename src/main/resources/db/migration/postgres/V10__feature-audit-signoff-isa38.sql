DELETE FROM audit_status_transitions_type_domain WHERE status_from = 5 AND status_to = 6;
DELETE FROM audit_status_type_domain WHERE ID = 6; -- delete the closed status as it isn't going to be necessary
