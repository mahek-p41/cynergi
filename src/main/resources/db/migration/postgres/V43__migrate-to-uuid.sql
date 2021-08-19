CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- borrowed from this Gist https://gist.github.com/devodo/8b39748d65e8185fbd89
CREATE OR REPLACE FUNCTION max (uuid, uuid)
    RETURNS uuid AS
$$
BEGIN
    IF $1 IS NULL OR $1 < $2 THEN
        RETURN $2;
    END IF;

    RETURN $1;
END;
$$ LANGUAGE plpgsql;

CREATE AGGREGATE max (uuid)
(
    sfunc = max,
    stype = uuid
);

-- create wrapper function for hashing passwords
CREATE OR REPLACE FUNCTION hash_passcode(TEXT)
   RETURNS TEXT AS
$$
BEGIN
   IF $1 IS NOT NULL AND length($1) > 2 THEN
      RETURN crypt($1, gen_salt('bf', 10));
   ELSE
      RAISE EXCEPTION 'Pass code provided does not meet length requirement of 3';
   END IF;
END;
$$ LANGUAGE plpgsql;

-- Create the replacement for what last_updated_column_fn currently does, but using the new id type of UUID
CREATE OR REPLACE FUNCTION update_user_table_fn()
    RETURNS TRIGGER AS
$$
BEGIN
    IF new.id <> old.id THEN -- help ensure that the id can't be updated once it is created
        RAISE EXCEPTION 'cannot update id once it has been created';
    END IF;

    new.time_updated := clock_timestamp();

    RETURN new;
END;
$$
    LANGUAGE plpgsql;

-- Drop triggers
DROP TRIGGER IF EXISTS update_audit_trg ON audit;
DROP TRIGGER IF EXISTS update_audit_action_trg ON audit_action;
DROP TRIGGER IF EXISTS update_audit_detail_trg ON audit_detail;
DROP TRIGGER IF EXISTS update_audit_exception_trg ON audit_exception;
DROP TRIGGER IF EXISTS update_audit_inventory_trg ON audit_inventory;
DROP TRIGGER IF EXISTS update_audit_permission_trg ON audit_permission;
DROP TRIGGER IF EXISTS update_audit_scan_area_trg ON audit_scan_area;
DROP TRIGGER IF EXISTS update_company_trg ON company;
DROP TRIGGER IF EXISTS update_division_trg ON division;
DROP TRIGGER IF EXISTS update_employee_trg ON employee;
DROP TRIGGER IF EXISTS update_region_trg ON region;
DROP TRIGGER IF EXISTS update_schedule_trg ON schedule;
DROP TRIGGER IF EXISTS update_schedule_arg_trg ON schedule_arg;
DROP TRIGGER IF EXISTS update_audit_exception_note_trg ON audit_exception_note;

-- Drop foreign keys
ALTER TABLE audit DROP CONSTRAINT IF EXISTS audit_company_id_fkey;
ALTER TABLE audit_permission DROP CONSTRAINT IF EXISTS audit_permission_company_id_fkey;
ALTER TABLE audit_scan_area DROP CONSTRAINT IF EXISTS audit_scan_area_company_id_fkey;
ALTER TABLE audit_detail DROP CONSTRAINT IF EXISTS audit_detail_scan_area_id_2_fkey;
ALTER TABLE audit_exception DROP CONSTRAINT IF EXISTS audit_exception_scan_area_id_2_fkey;
ALTER TABLE audit_action DROP CONSTRAINT IF EXISTS audit_action_audit_id_fkey;
ALTER TABLE audit_detail DROP CONSTRAINT IF EXISTS audit_detail_audit_id_fkey;
ALTER TABLE audit_exception DROP CONSTRAINT IF EXISTS audit_exception_audit_id_fkey;
ALTER TABLE audit_inventory DROP CONSTRAINT IF EXISTS audit_inventory_audit_id_fkey;
ALTER TABLE division DROP CONSTRAINT IF EXISTS division_company_id_fkey;
ALTER TABLE employee DROP CONSTRAINT IF EXISTS employee_company_id_fkey;
ALTER TABLE schedule DROP CONSTRAINT IF EXISTS schedule_company_id_fkey;
ALTER TABLE schedule_arg DROP CONSTRAINT IF EXISTS schedule_arg_schedule_id_fkey;
ALTER TABLE audit_exception_note DROP CONSTRAINT audit_exception_note_audit_exception_id_fkey;
ALTER TABLE region_to_store DROP CONSTRAINT region_to_store_region_id_fkey;
ALTER TABLE region DROP CONSTRAINT IF EXISTS region_division_id_fkey;

-- Drop primary keys
ALTER TABLE employee DROP CONSTRAINT IF EXISTS employee_pkey;
ALTER TABLE company DROP CONSTRAINT IF EXISTS company_pkey;
ALTER TABLE audit DROP CONSTRAINT IF EXISTS audit_pkey;
ALTER TABLE audit_permission DROP CONSTRAINT IF EXISTS audit_permission_pkey;
ALTER TABLE schedule DROP CONSTRAINT IF EXISTS schedule_pkey;
ALTER TABLE audit_scan_area DROP CONSTRAINT IF EXISTS audit_scan_area_pkey;
ALTER TABLE region DROP CONSTRAINT region_pkey;
ALTER TABLE audit_exception DROP CONSTRAINT IF EXISTS audit_exception_pkey;
ALTER TABLE division DROP CONSTRAINT IF EXISTS division_pkey;

-- drop unique constraints
ALTER TABLE region_to_store DROP CONSTRAINT uq_region_to_store;

-- move id to old_id
ALTER TABLE company RENAME COLUMN id TO old_id;
ALTER TABLE audit RENAME company_id TO old_company_id;
ALTER TABLE audit RENAME id to old_id;
ALTER TABLE audit_permission RENAME company_id TO old_company_id;
ALTER TABLE audit_permission RENAME id to old_id;
ALTER TABLE audit_scan_area RENAME company_id TO old_company_id;
ALTER TABLE audit_scan_area RENAME id to old_id;
ALTER TABLE audit_detail RENAME id TO old_id;
ALTER TABLE audit_detail RENAME audit_id TO old_audit_id;
ALTER TABLE audit_detail RENAME scan_area_id TO old_scan_area_id;
ALTER TABLE audit_action RENAME id TO old_id;
ALTER TABLE audit_action RENAME audit_id to old_audit_id;
ALTER TABLE audit_exception RENAME id TO old_id;
ALTER TABLE audit_exception RENAME audit_id TO old_audit_id;
ALTER TABLE audit_exception RENAME scan_area_id TO old_scan_area_id;
ALTER TABLE audit_exception_note RENAME id TO old_id;
ALTER TABLE audit_exception_note RENAME audit_exception_id TO old_audit_exception_id;
ALTER TABLE audit_inventory RENAME audit_id TO old_audit_id;
ALTER TABLE division RENAME company_id TO old_company_id;
ALTER TABLE division RENAME id to old_id;
ALTER TABLE region RENAME division_id to old_division_id;
ALTER TABLE region RENAME id to old_id;
ALTER TABLE region_to_store RENAME region_id TO old_region_id;
ALTER TABLE employee RENAME company_id TO old_company_id;
ALTER TABLE schedule RENAME company_id TO old_company_id;
ALTER TABLE schedule RENAME id to old_id;
ALTER TABLE schedule_arg RENAME id to old_id;
ALTER TABLE schedule_arg RENAME schedule_id TO old_schedule_id;

-- rename uu_row_id to id;
ALTER TABLE company RENAME COLUMN uu_row_id TO id;
ALTER TABLE audit RENAME uu_row_id TO id;
ALTER TABLE audit_permission RENAME uu_row_id TO id;
ALTER TABLE audit_scan_area RENAME uu_row_id TO id;
ALTER TABLE audit_detail RENAME uu_row_id TO id;
ALTER TABLE audit_action RENAME uu_row_id TO id;
ALTER TABLE audit_exception RENAME uu_row_id TO id;
ALTER TABLE audit_exception_note RENAME uu_row_id TO id;
ALTER TABLE division RENAME uu_row_id TO id;
ALTER TABLE region RENAME uu_row_id to id;
ALTER TABLE schedule RENAME uu_row_id TO id;
ALTER TABLE schedule_arg RENAME uu_row_id TO id;

-- modify company table
ALTER TABLE company ADD CONSTRAINT company_pkey PRIMARY KEY (id);

-- modify audit table
ALTER TABLE audit ADD COLUMN company_id UUID;
ALTER TABLE audit ADD CONSTRAINT audit_pkey PRIMARY KEY (id);

UPDATE audit a SET company_id = comp.id FROM company comp WHERE a.old_company_id = comp.old_id;

-- modify audit_permission table
ALTER TABLE audit_permission ADD COLUMN company_id UUID;
ALTER TABLE audit_permission ADD CONSTRAINT audit_permission_pkey PRIMARY KEY (id);

UPDATE audit_permission a SET company_id = comp.id FROM company comp WHERE a.old_company_id = comp.old_id;

-- modify audit_scan_area table
ALTER TABLE audit_scan_area ADD COLUMN company_id UUID;
ALTER TABLE audit_scan_area ADD CONSTRAINT audit_scan_area_pkey PRIMARY KEY (id);

UPDATE audit_scan_area a SET company_id = comp.id FROM company comp WHERE a.old_company_id = comp.old_id;

-- modify audit_detail table
ALTER TABLE audit_detail ADD COLUMN audit_id UUID;
ALTER TABLE audit_detail ADD COLUMN scan_area_id UUID;

UPDATE audit_detail ad SET audit_id = a.id FROM audit a WHERE a.old_id = ad.old_audit_id;
UPDATE audit_detail ad SET scan_area_id = asa.id FROM audit_scan_area asa WHERE asa.old_id = ad.old_scan_area_id;


-- modify audit_action table
ALTER TABLE audit_action ADD COLUMN audit_id UUID;

UPDATE audit_action ac SET audit_id = a.id FROM audit a WHERE a.old_id = ac.old_audit_id;

-- update audit_inventory table
ALTER TABLE audit_inventory ADD COLUMN audit_id UUID;

UPDATE audit_inventory ai SET audit_id = a.id FROM audit a WHERE a.old_id = ai.old_audit_id;

-- modify audit_exception
ALTER TABLE audit_exception ADD COLUMN audit_id UUID;
ALTER TABLE audit_exception ADD COLUMN scan_area_id UUID;

UPDATE audit_exception ad SET audit_id = a.id FROM audit a WHERE a.old_id = ad.old_audit_id;
UPDATE audit_exception ae SET scan_area_id = asa.id FROM audit_scan_area asa WHERE asa.old_id = ae.old_scan_area_id;

ALTER TABLE audit_exception ADD CONSTRAINT audit_exception_pkey PRIMARY KEY (id);

-- modify audit_exception_note
ALTER TABLE audit_exception_note ADD COLUMN audit_exception_id UUID;

UPDATE audit_exception_note aen SET audit_exception_id = ae.id FROM audit_exception ae WHERE ae.old_id = aen.old_audit_exception_id;

ALTER TABLE audit_exception_note DROP COLUMN IF EXISTS old_id;
ALTER TABLE audit_exception_note DROP COLUMN IF EXISTS old_audit_exception_id;
ALTER TABLE audit_exception DROP COLUMN IF EXISTS old_id;
ALTER TABLE audit_exception DROP COLUMN IF EXISTS old_scan_area_id;
ALTER TABLE audit_exception DROP COLUMN IF EXISTS old_audit_id;

-- modify division table
ALTER TABLE division ADD COLUMN company_id UUID;
ALTER TABLE division ADD CONSTRAINT division_pkey PRIMARY KEY (id);

UPDATE division a SET company_id = comp.id FROM company comp WHERE a.old_company_id = comp.old_id;


-- modify region table

ALTER TABLE region ADD COLUMN division_id UUID;

UPDATE region r SET division_id = d.id FROM division d WHERE r.old_division_id = d.old_id;

ALTER TABLE region ADD CONSTRAINT region_pkey PRIMARY KEY (id);

-- modify region_to_store
ALTER TABLE region_to_store ADD COLUMN region_id UUID;

UPDATE region_to_store rts SET region_id = r.id FROM region r WHERE r.old_id = rts.old_region_id;

ALTER TABLE region_to_store ADD CONSTRAINT uq_region_to_store UNIQUE (region_id, store_number);

-- modify employee table
ALTER TABLE employee ADD COLUMN company_id UUID;

UPDATE employee a SET company_id = comp.id FROM company comp WHERE a.old_company_id = comp.old_id;

-- modify schedule table
ALTER TABLE schedule ADD COLUMN company_id UUID;
ALTER TABLE schedule ADD CONSTRAINT schedule_pkey PRIMARY KEY (id);

UPDATE schedule a SET company_id = comp.id FROM company comp WHERE a.old_company_id = comp.old_id;

-- modify schedule_arg table
ALTER TABLE schedule_arg ADD COLUMN schedule_id UUID;

UPDATE schedule_arg sa SET schedule_id = sched.id FROM schedule sched WHERE sa.old_schedule_id = sched.old_id;

-- set not null
ALTER TABLE audit_exception_note ALTER COLUMN audit_exception_id SET NOT NULL;
ALTER TABLE audit ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE audit_permission ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE audit_scan_area ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE audit_detail ALTER COLUMN audit_id SET NOT NULL;
ALTER TABLE audit_detail ALTER COLUMN scan_area_id SET NOT NULL;
ALTER TABLE audit_action ALTER COLUMN audit_id SET NOT NULL;
ALTER TABLE audit_exception ALTER COLUMN audit_id SET NOT NULL;
ALTER TABLE audit_inventory ALTER COLUMN audit_id SET NOT NULL;
ALTER TABLE division ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE region ALTER COLUMN division_id SET NOT NULL;
ALTER TABLE region_to_store ALTER COLUMN region_id SET NOT NULL;
ALTER TABLE employee ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE schedule ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE schedule_arg ALTER COLUMN schedule_id SET NOT NULL;

-- add foreign keys
ALTER TABLE audit_exception ADD CONSTRAINT audit_exception_audit_id_fkey FOREIGN KEY (audit_id) REFERENCES audit (id);
ALTER TABLE audit_exception ADD CONSTRAINT audit_exception_scan_area_id_2_fkey FOREIGN KEY (scan_area_id) REFERENCES audit_scan_area (id);
ALTER TABLE audit_detail ADD CONSTRAINT audit_detail_audit_id_fkey FOREIGN KEY (audit_id) REFERENCES audit (id);
ALTER TABLE audit_detail ADD CONSTRAINT audit_detail_scan_area_id_2_fkey FOREIGN KEY (scan_area_id) REFERENCES audit_scan_area (id);
ALTER TABLE audit_action ADD CONSTRAINT audit_action_audit_id_fkey FOREIGN KEY (audit_id) REFERENCES audit (id);
ALTER TABLE schedule ADD CONSTRAINT schedule_company_id_fkey FOREIGN KEY (company_id) REFERENCES company (id);
ALTER TABLE audit ADD CONSTRAINT audit_company_id_fkey FOREIGN KEY (company_id) REFERENCES company (id);
ALTER TABLE audit_permission ADD CONSTRAINT audit_permission_company_id_fkey FOREIGN KEY (company_id) REFERENCES company (id);
ALTER TABLE audit_scan_area ADD CONSTRAINT audit_scan_area_company_id_fkey FOREIGN KEY (company_id) REFERENCES company (id);
ALTER TABLE audit_exception_note ADD CONSTRAINT audit_exception_note_audit_id_fkey FOREIGN KEY (audit_exception_id) REFERENCES audit_exception (id);
ALTER TABLE division ADD CONSTRAINT division_company_id_fkey FOREIGN KEY (company_id) REFERENCES company (id);
ALTER TABLE region ADD CONSTRAINT region_division_id_fkey FOREIGN KEY (division_id) REFERENCES division (id);
ALTER TABLE region_to_store ADD CONSTRAINT region_to_store_region_id_fkey FOREIGN KEY (region_id) REFERENCES region (id);
ALTER TABLE employee ADD CONSTRAINT employee_company_id_fkey FOREIGN KEY (company_id) REFERENCES company (id);

-- drop old id's
ALTER TABLE audit DROP COLUMN IF EXISTS old_company_id;
ALTER TABLE schedule DROP COLUMN IF EXISTS old_id;
ALTER TABLE schedule_arg DROP COLUMN IF EXISTS old_id;
ALTER TABLE schedule_arg DROP COLUMN IF EXISTS old_schedule_id;
ALTER TABLE schedule DROP COLUMN IF EXISTS old_company_id;
ALTER TABLE audit_permission DROP COLUMN IF EXISTS old_id;
ALTER TABLE audit_permission DROP COLUMN IF EXISTS old_company_id;
ALTER TABLE employee DROP COLUMN IF EXISTS old_company_id;
ALTER TABLE region_to_store DROP COLUMN IF EXISTS old_region_id;
ALTER TABLE region DROP COLUMN IF EXISTS old_id;
ALTER TABLE division DROP COLUMN IF EXISTS old_id;
ALTER TABLE region DROP COLUMN IF EXISTS old_division_id;
ALTER TABLE audit_detail DROP COLUMN IF EXISTS old_audit_id;
ALTER TABLE audit_detail DROP COLUMN IF EXISTS old_scan_area_id;
ALTER TABLE audit_detail DROP COLUMN IF EXISTS old_id;
ALTER TABLE audit_scan_area DROP COLUMN IF EXISTS old_company_id;
ALTER TABLE division DROP COLUMN IF EXISTS old_company_id;
ALTER TABLE audit_scan_area DROP COLUMN IF EXISTS old_id;
ALTER TABLE audit DROP COLUMN IF EXISTS old_id;
ALTER TABLE audit_exception_note DROP COLUMN IF EXISTS old_audit_id;
ALTER TABLE audit_action DROP COLUMN IF EXISTS old_id;
ALTER TABLE audit_action DROP COLUMN IF EXISTS old_audit_id;
ALTER TABLE audit_inventory DROP COLUMN IF EXISTS old_audit_id;

-- set not null

-- Recreate triggers
CREATE TRIGGER update_company_trg
    BEFORE UPDATE
    ON company
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE TRIGGER update_audit_trg
    BEFORE UPDATE
    ON audit
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE TRIGGER update_audit_detail_trg
    BEFORE UPDATE
    ON audit_detail
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE TRIGGER update_audit_exception_trg
    BEFORE UPDATE
    ON audit_exception
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE TRIGGER update_audit_exception_note_trg
    BEFORE UPDATE
    ON audit_exception_note
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE TRIGGER update_audit_action_trg
    BEFORE UPDATE
    ON audit_action
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

CREATE TRIGGER update_employee_trg
    BEFORE UPDATE
    ON employee
    FOR EACH ROW
EXECUTE PROCEDURE update_user_table_fn();

ALTER TABLE company DROP COLUMN old_id;

-- create wrapper function for hashing passwords
CREATE OR REPLACE FUNCTION hash_passcode(TEXT)
   RETURNS TEXT AS
$$
BEGIN
   IF $1 IS NOT NULL AND length($1) > 2 THEN
      RETURN crypt($1, gen_salt('bf', 10));
   ELSE
      RAISE EXCEPTION 'Pass code provided does not meet length requirement of 3';
   END IF;
END;
$$ LANGUAGE plpgsql;

-- setup a simple view for getting employees that can be authenticated against
DO $$
DECLARE
    sqlToExec VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW authenticated_user_vw AS SELECT * FROM (';

   IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name = 'employee_vw' AND table_schema = 'fastinfo_prod_import') THEN
      sqlToExec := sqlToExec || '
         SELECT
            1                               AS from_priority,
            emp.id                          AS id,
            ''sysz''                        AS type,
            emp.number                      AS number,
            emp.active                      AS active,
            false                           AS cynergi_system_admin,
            emp.department                  AS department,
            emp.pass_code                   AS pass_code,
            emp.alternative_store_indicator AS alternative_store_indicator,
            emp.alternative_area            AS alternative_area,
            emp.store_number                AS store_number,
            comp.id                         AS company_id
         FROM company comp
           JOIN fastinfo_prod_import.employee_vw emp ON comp.dataset_code = emp.dataset
         UNION
   ';
   END IF;

   sqlToExec := sqlToExec || '
      SELECT
         2                               AS from_priority,
         emp.id                          AS id,
         ''eli''                         AS type,
         emp.number                      AS number,
         emp.active                      AS active,
         emp.cynergi_system_admin        AS cynergi_system_admin,
         emp.department                  AS department,
         emp.pass_code                   AS pass_code,
         emp.alternative_store_indicator AS alternative_store_indicator,
         emp.alternative_area            AS alternative_area,
         emp.store_number                AS store_number,
         emp.company_id                  AS company_id
      FROM company comp
         JOIN employee emp ON comp.id = emp.company_id
      WHERE active = true
      ORDER BY from_priority, id
   ) AS users ';

   EXECUTE sqlToExec;
END $$;

-- setup view for querying employees
DO $$
DECLARE
    sqlToExec VARCHAR;
BEGIN
   sqlToExec := 'CREATE OR REPLACE VIEW system_employees_vw AS SELECT * FROM ( SELECT * FROM (';

   IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name = 'employee_vw' AND table_schema = 'fastinfo_prod_import') THEN
      sqlToExec := sqlToExec || '
         SELECT
            1                               AS from_priority,
            emp.id                          AS emp_id,
            ''sysz''                        AS emp_type,
            emp.number                      AS emp_number,
            emp.last_name                   AS emp_last_name,
            emp.first_name_mi               AS emp_first_name_mi,
            emp.pass_code                   AS emp_pass_code,
            emp.active                      AS emp_active,
            emp.department                  AS emp_department,
            false                           AS emp_cynergi_system_admin,
            emp.alternative_store_indicator AS emp_alternative_store_indicator,
            emp.alternative_area            AS emp_alternative_area,
            comp.id                         AS comp_id,
            comp.time_created               AS comp_time_created,
            comp.time_updated               AS comp_time_updated,
            comp.name                       AS comp_name,
            comp.doing_business_as          AS comp_doing_business_as,
            comp.client_code                AS comp_client_code,
            comp.client_id                  AS comp_client_id,
            comp.dataset_code               AS comp_dataset_code,
            comp.federal_id_number          AS comp_federal_id_number,
            dept.id                         AS dept_id,
            dept.code                       AS dept_code,
            dept.description                AS dept_description,
            store.id                        AS store_id,
            store.number                    AS store_number,
            store.name                      AS store_name
         FROM fastinfo_prod_import.employee_vw emp
            JOIN company comp ON emp.dataset = comp.dataset_code
            LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
            LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
        UNION';
   END IF;

   sqlToExec := sqlToExec || '
      SELECT
         2                               AS from_priority,
         emp.id                          AS emp_id,
         ''eli''                         AS emp_type,
         emp.number                      AS emp_number,
         emp.last_name                   AS emp_last_name,
         emp.first_name_mi               AS emp_first_name_mi,
         emp.pass_code                   AS emp_pass_code,
         emp.active                      AS emp_active,
         emp.department                  AS emp_department,
         emp.cynergi_system_admin        AS emp_cynergi_system_admin,
         emp.alternative_store_indicator AS emp_alternative_store_indicator,
         emp.alternative_area            AS emp_alternative_area,
         comp.id                         AS comp_id,
         comp.time_created               AS comp_time_created,
         comp.time_updated               AS comp_time_updated,
         comp.name                       AS comp_name,
         comp.doing_business_as          AS comp_doing_business_as,
         comp.client_code                AS comp_client_code,
         comp.client_id                  AS comp_client_id,
         comp.dataset_code               AS comp_dataset_code,
         comp.federal_id_number          AS comp_federal_id_number,
         dept.id                         AS dept_id,
         dept.code                       AS dept_code,
         dept.description                AS dept_description,
         store.id                        AS store_id,
         store.number                    AS store_number,
         store.name                      AS store_name
      FROM employee emp
           JOIN company comp ON emp.company_id = comp.id
           LEFT OUTER JOIN fastinfo_prod_import.department_vw dept ON comp.dataset_code = dept.dataset AND emp.department = dept.code
           LEFT OUTER JOIN fastinfo_prod_import.store_vw store ON comp.dataset_code = store.dataset AND emp.store_number = store.number
      ) AS inner_employees
      ORDER BY from_priority
  ) AS system_employees';

   EXECUTE sqlToExec;
END $$;
