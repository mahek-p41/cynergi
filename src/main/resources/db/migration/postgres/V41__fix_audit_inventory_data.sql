INSERT INTO audit_inventory
(audit_id, dataset, serial_number, lookup_key, lookup_key_type, barcode, alt_id, brand, model_number, product_code, description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, returned_date, location, status, primary_location, location_type)
   SELECT a.id, dataset, serial_number, lookup_key, lookup_key_type, barcode, alt_id, brand, model_number, product_code, i.description, received_date, original_cost, actual_cost, model_category, times_rented, total_revenue, remaining_value, sell_price, assigned_value, idle_days, condition, returned_date, location, status, primary_location, location_type
   FROM fastinfo_prod_import.inventory_vw i
      JOIN company comp ON i.dataset = comp.dataset_code
      JOIN audit a ON a.company_id = comp.id
      JOIN audit_action action ON a.id = action.audit_id
      JOIN audit_status_type_domain status ON action.status_id = status.id
   WHERE status.value IN ('COMPLETED', 'CANCELED')
      AND i.lookup_key IN (SELECT lookup_key FROM audit_detail ad
                        WHERE audit_id = :audit_id
                           AND NOT EXISTS (SELECT lookup_key FROM audit_inventory ai WHERE ai.audit_id = ad.audit_id))
   ORDER BY a.id;

-- delete the duplicate data
--DELETE
--FROM audit_detail
--WHERE id NOT in (
--   SELECT MAX(id)
--   FROM audit_detail i
--   GROUP BY audit_id, alt_id, serial_number
--);

