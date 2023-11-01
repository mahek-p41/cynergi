#!/usr/bin/env groovy
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.postgresql', module = 'postgresql', version = '42.2.15')

import groovy.sql.Sql
import groovy.json.JsonOutput

final Map<Integer, Map<String, Object>> rows = [:]

Sql.newInstance('jdbc:postgresql://localhost:7432/cynergitestdb', 'postgres', 'password', 'org.postgresql.Driver').withCloseable {sql ->
   sql.eachRow("""
WITH RECURSIVE menus AS (
   SELECT
      id,
      parent_id,
      value,
      description,
      localization_code,
      area_type_id,
      order_number,
      0 AS level
   FROM menu_type_domain
   WHERE parent_id IS NULL
   UNION ALL
   SELECT
      mtd.id,
      mtd.parent_id,
      mtd.value,
      mtd.description,
      mtd.localization_code,
      mtd.area_type_id,
      mtd.order_number,
      level + 1 AS level
   FROM menu_type_domain mtd
        INNER JOIN menus m ON m.id = mtd.parent_id
)
SELECT
   atd.value AS area,
   ms.*,
   array_to_string(array(select t.value from module_type_domain t join menu_type_domain d ON t.menu_type_id = d.id where d.id = ms.id), ',') AS modules
FROM menus ms
     LEFT OUTER JOIN area_type_domain atd ON ms.area_type_id = atd.id
ORDER BY id
""") { row ->
      final modules = row.modules != "" ? row.modules.split(',') : []
      final newRow = [id: row.id, parent_id: row.parent_id, value: row.value, menus: [], modules: modules]

      if (row.area != null) {
         newRow["area"] = row.area
      }

      rows[row.id] = newRow

      if (row.parent_id != null) {
         rows[row.parent_id].menus.add(newRow)
      }
   }
}


final roots = rows
   .findAll { it.value["parent_id"] == null }
   .collect { it.value }

println JsonOutput.prettyPrint(JsonOutput.toJson(roots))
