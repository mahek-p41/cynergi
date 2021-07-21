#!/usr/bin/env groovy
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.postgresql', module = 'postgresql', version = '42.2.15')

import groovy.sql.Sql

Sql.newInstance('jdbc:postgresql://localhost:7432/cynergitestdb', 'postgres', 'password', 'org.postgresql.Driver').withCloseable {sql ->
   sql.eachRow("SELECT id, value, description, localization_code, program, menu_type_id FROM module_type_domain") { row ->
      print """
ModuleType(
   id = ${row.id},
   value = "${row.value}",
   program = "${row.program}",
   description = "${row.description}",
   localizationCode = "${row.localization_code}",
   menuType = MenuTypeDataLoader.menuTypes().first { it.id == ${row.menu_type_id} }
),"""
   }
}
