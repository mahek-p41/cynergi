ALTER TABLE vendor_payment_term_schedule DROP CONSTRAINT vendor_payment_term_schedule_vendor_payment_term_id_schedul_key;


ALTER TABLE vendor_payment_term_schedule ADD CONSTRAINT vendor_payment_term_schedule_vendor_payment_term_id_schedul_key UNIQUE (vendor_payment_term_id, schedule_order_number);
