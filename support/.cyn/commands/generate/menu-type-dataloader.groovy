#!/usr/bin/env groovy
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.postgresql', module = 'postgresql', version = '42.2.15')

import groovy.sql.Sql

Sql.newInstance('jdbc:postgresql://localhost:7432/cynergitestdb', 'postgres', 'password', 'org.postgresql.Driver').withCloseable {sql ->
   sql.eachRow("SELECT id, value, description, localization_code, area_type_id, order_number FROM menu_type_domain") { row ->
      print """
MenuType(
   id = ${row.id},
   value = "${row.value}",
   description = "${row.description}",
   localizationCode = "${row.localization_code}",
   orderNumber = ${row.order_number},
   areaType = AreaDataLoader.areaTypes().forId(${row.area_type_id})
),"""
   }
}
