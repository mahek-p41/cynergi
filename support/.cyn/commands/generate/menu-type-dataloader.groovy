#!/usr/bin/env groovy
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.postgresql', module = 'postgresql', version = '42.2.15')

import groovy.sql.Sql

Sql.newInstance('jdbc:postgresql://localhost:7432/cynergitestdb', 'postgres', 'password', 'org.postgresql.Driver').withCloseable {sql ->
   sql.eachRow("SELECT id, parent_id, value, description, localization_code, area_type_id, order_number FROM menu_type_domain") { row ->
      print """
new MenuType(
   ${row.id},
   ${row.parent_id},
   "${row.value}",
   "${row.description}",
   "${row.localization_code}",
   ${row.order_number},
   AreaDataLoader.areaTypes().find { it.id == ${row.area_type_id} },
   [],
   []
),"""
   }
}
