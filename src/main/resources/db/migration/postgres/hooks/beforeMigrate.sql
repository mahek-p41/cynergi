DO $$
BEGIN
   IF EXISTS(SELECT 1 FROM pg_matviews where matviewname ='system_employees_fimvw') THEN
      DROP MATERIALIZED VIEW system_employees_fimvw;
   END IF;

   IF EXISTS(SELECT 1 FROM pg_matviews where matviewname ='system_stores_fimvw') THEN
      DROP MATERIALIZED VIEW system_stores_fimvw;
   END IF;
END $$;

DROP VIEW IF EXISTS authenticated_user_vw;
DROP VIEW IF EXISTS system_employees_vw;
DROP VIEW IF EXISTS bank_recon_vendor_vw;
