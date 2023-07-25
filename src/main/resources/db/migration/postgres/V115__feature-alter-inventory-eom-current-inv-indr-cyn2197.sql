ALTER TABLE inventory_end_of_month
   ALTER COLUMN current_inv_indr DROP DEFAULT;
ALTER TABLE inventory_end_of_month
   ALTER COLUMN current_inv_indr TYPE INTEGER USING (current_inv_indr::INTEGER);
ALTER TABLE inventory_end_of_month
   ADD FOREIGN KEY (current_inv_indr) REFERENCES inventory_end_of_month_inventory_indr_type_domain (id);
ALTER TABLE inventory_end_of_month
   ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL;
