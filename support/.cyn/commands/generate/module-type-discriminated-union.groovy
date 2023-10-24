#!/usr/bin/env groovy
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.postgresql', module = 'postgresql', version = '42.2.15')

import groovy.sql.Sql

Sql.newInstance('jdbc:postgresql://localhost:7432/cynergitestdb', 'postgres', 'password', 'org.postgresql.Driver').withCloseable {sql ->
   println("object TypeListing {")
   sql.eachRow("SELECT id, value, description, localization_code, program, menu_type_id FROM module_type_domain ORDER BY id") { row ->
      println """   object ${row.value}: ModuleType(${row.id}, "${row.value}", "${row.description}", "${row.localization_code}", "${row.program}")"""
   }
   println("}")

   print("""
enum class ModuleTypes(
   val type: ModuleType,
) {
""")
   sql.eachRow("SELECT id, value, description, localization_code, program, menu_type_id FROM module_type_domain ORDER BY id") { row ->
      println """   ${row.value}(TypeListing.${row.value}),"""
   }
   println("}")
}
