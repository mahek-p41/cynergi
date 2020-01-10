# cynergitestdb setup

## Seeding the cynergitestdb with data that is required for running the integration tests
Grab the fastinfo_production dump you want to use when developing and fire it up in the development database docker
container with `cyn db start development`.  Then connect to cynergidb using localhost:6432 and cynergiuser/password
for the host and creds.

Once connected you need to generate either SQL Inserts or a CSV.  Which one you need to generate will be documented in
this following sections.  All the inserts will need to go into the _cynerig-inittestdb.sql_ file

### fastinfo_prod_import.company_vw
Generate a set of __INSERT__ statments and place them below the definition of the fastinfo_prod_import.company_vw table
```sql
SELECT
   number, name,
   CASE
      WHEN dataset = 'corrto'
         THEN 'tstds1'
      ELSE 'tstds2'
      END
      AS dataset
FROM fastinfo_prod_import.company_vw
ORDER BY dataset;
```

### fasinfo_prod_import.store_vw
Generate a set of __INSERT__ statements and place them below the definition of the fastinfo_prod_import.store_vw table
```sql
SELECT
   id, number, name,
   CASE
      WHEN dataset = 'corrto'
         THEN 'tstds1'
      ELSE 'tstds2'
      END
      AS dataset
FROM fastinfo_prod_import.store_vw
ORDER BY dataset, number;
```

### fastinfo_prod_import.department_vw
Generate a set of __INSERT__ statements and place them below the definition of the fastinfo_prod_import.department_vw table
```sql
SELECT
   code, description,
   CASE
      WHEN dataset = 'corrto'
         THEN 'tstds1'
      ELSE 'tstds2'
      END
      AS dataset, security_profile, default_menu
FROM fastinfo_prod_import.department_vw
ORDER BY dataset, code;
```

### fastinfo_prod_import.employee_vw
Generate a set of __INSERT__ statements and place them below the definition of the fastinfo_prod_import.employee_vw table
```sql
SELECT
   number, store_number,
   CASE
      WHEN dataset = 'corrto'
         THEN 'tstds1'
      ELSE 'tstds2'
      END
      AS dataset, last_name, first_name_mi, 'pass' AS pass_code, department, active
FROM fastinfo_prod_import.employee_vw
ORDER BY dataset, number;
```

### fasinfo_prod_import.inventory_vw
Generate a CSV of the inventory that would be returned by a query to the fasinfo_prod_import.inventory_vw view and place
them in support/development/db/DatabaseDumps/test-inventory.csv

```sql
SELECT
   serial_number, lookup_key,
   lookup_key_type,
   barcode,
   alt_id,
   brand,
   model_number,
   product_code,
   description,
   received_date,
   original_cost,
   actual_cost,
   model_category,
   times_rented,
   total_revenue,
   remaining_value,
   sell_price,
   assigned_value,
   idle_days,
   condition,
   returned_date,
   location,
   status,
   primary_location,
   location_type,
   CASE
      WHEN dataset = 'corrto'
         THEN 'tstds1'
      ELSE 'tstds2'
      END
      AS dataset
FROM fastinfo_prod_import.inventory_vw;
```
