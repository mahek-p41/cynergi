ALTER TABLE agreement_signing
   ADD COLUMN external_signature_id_uuid UUID;

UPDATE agreement_signing
SET external_signature_id_uuid = uuid(external_signature_id);

ALTER TABLE agreement_signing
   DROP COLUMN external_signature_id;

ALTER TABLE agreement_signing
   RENAME COLUMN external_signature_id_uuid TO external_signature_id;

ALTER TABLE agreement_signing
   ALTER COLUMN external_signature_id SET NOT NULL;

CREATE INDEX external_signature_id_idx ON agreement_signing (external_signature_id);
