CREATE INDEX trgm_idx ON employee USING gist ((COALESCE(first_name_mi, '') || ' ' || COALESCE(last_name, '')) gist_trgm_ops);
