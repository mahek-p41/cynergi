ALTER TABLE account_payable_invoice_schedule ALTER COLUMN external_payment_type_id DROP NOT NULL;
ALTER TABLE account_payable_invoice ALTER COLUMN due_date DROP NOT NULL;
