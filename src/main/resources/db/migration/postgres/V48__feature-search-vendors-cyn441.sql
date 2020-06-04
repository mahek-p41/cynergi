CREATE INDEX trgm_vendor_name_idx ON vendor USING gist (name gist_trgm_ops);
