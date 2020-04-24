\c fastinfo_production

DO $$
DECLARE
    r RECORD;
    sqlToExec VARCHAR;
    unionStr VARCHAR;
BEGIN
    IF EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = 'corptp') THEN
        sqlToExec := 'INSERT INTO corptp.level2_departments(loc_dept_code, loc_dept_desc, loc_dept_security_profile, loc_dept_default_menu)
           SELECT
              loc_dept_code,
              loc_dept_desc,
              loc_dept_security_profile,
              loc_dept_default_menu
           FROM (
        ';
            unionStr := '';

            FOR r IN
                SELECT split_part(column_name, '_', 4) AS i
                FROM information_schema.columns
                WHERE table_schema = 'corptp'
                  AND table_name = 'level1_loc_depts'
                  AND (
                        column_name LIKE 'loc_dept_code_%'
                    )
                LOOP
                    sqlToExec := sqlToExec
                                     || ' '
                                     || unionStr || '
               SELECT
                  loc_dept_code_' || r.i || ' AS loc_dept_code,
                  loc_dept_desc_' || r.i || ' AS loc_dept_desc,
                  loc_dept_security_profile_' || r.i || ' AS loc_dept_security_profile,
                  loc_dept_default_menu_' || r.i || ' AS loc_dept_default_menu
               FROM corptp.level1_loc_depts
            ';

                    unionStr := ' UNION ALL';
                END LOOP;

            sqlToExec := sqlToExec || ' ) AS departments
        WHERE loc_dept_code IS NOT NULL AND trim(loc_dept_code) <> '''' AND (SELECT EXISTS(SELECT schema_name FROM information_schema.schemata WHERE schema_name = ''corptp''))';

            EXECUTE sqlToExec;
    END IF;
END $$;
