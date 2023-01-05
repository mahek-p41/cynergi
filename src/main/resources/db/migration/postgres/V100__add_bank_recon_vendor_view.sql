CREATE VIEW bank_recon_vendor_vw (bank_recon_id, vendor_id, vendor_name) AS
with bankREcon AS (SELECT *, CAST( CASE WHEN SPLIT_PART(bankRecon.description, 'A/P VND# ', 2) = '' THEN '0' ELSE SPLIT_PART(bankRecon.description, 'A/P VND# ', 2) END AS BIGINT) from bank_reconciliation bankRecon)
SELECT bankREcon.id, vendor.id, vendor.name from bankREcon
JOIN vendor on vendor.number = split_part and vendor.company_id = bankREcon.company_id
where split_part > 0
