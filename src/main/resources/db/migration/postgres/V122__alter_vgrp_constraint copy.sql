ALTER TABLE vendor_group DROP CONSTRAINT vendor_group_value_check;

ALTER TABLE vendor_group ADD CONSTRAINT vendor_group_value_check CHECK ((char_length(btrim((value)::text)) >= 1));
