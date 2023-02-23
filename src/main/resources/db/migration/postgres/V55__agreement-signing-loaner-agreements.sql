ALTER TABLE agreement_signing
   DROP CONSTRAINT company_customer_agreement_uq;

ALTER TABLE agreement_signing
   ADD CONSTRAINT company_customer_agreement_type_uq UNIQUE (company_id, primary_customer_number, secondary_customer_number, agreement_number, agreement_type);
