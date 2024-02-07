-- indexes will be used to speed up the audit query - CYN2628

CREATE INDEX inventory_status_idx ON inventory (status);
CREATE INDEX inventory_primary_location_idx ON inventory (primary_location);
