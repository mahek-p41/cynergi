#!/usr/bin/env /opt/cyn/v01/cynmid/groovy/bin/groovy
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.postgresql', module = 'postgresql', version = '42.2.15')
@Grab(group='info.picocli', module='picocli', version='4.6.1')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.32')
@Grab(group='org.slf4j', module='jcl-over-slf4j', version='1.7.32')
@Grab(group='org.slf4j', module='jul-to-slf4j', version='1.7.32')
@Grab(group='org.glassfish.jaxb', module='jaxb-runtime', version='2.3.5')
@Grab(group='org.apache.commons', module='commons-csv', version='1.9.0')
@Grab(group='commons-io', module='commons-io', version='2.11.0')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.12.0')

import groovy.cli.picocli.CliBuilder
import groovy.sql.Sql
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils

import static org.apache.commons.csv.CSVFormat.POSTGRESQL_CSV

// https://relentlesscoding.com/posts/how-to-use-groovys-clibuilder/
final cli = new CliBuilder(name: 'export-cynergidb')

cli.width = 80
cli.with {
   u(longOpt: 'user', args: 1, defaultValue: 'fastinfo_dba', 'db username')
   p(longOpt: 'password', args: 1, defaultValue: 'password', 'db password')
   P(longOpt: 'port', args: 1, defaultValue: '5432', 'db port')
   d(longOpt: 'database', defaultValue: 'fastinfo_production', args: 1, 'fastinfo database to export')
   H(longOpt: 'host', args: 1, defaultValue: 'localhost', 'Host database is running on')
   o(longOpt: 'out', args: 1, defaultValue: '/tmp/fastinfo', 'Location to dump CSV\'s')
   h(longOpt: 'help', 'this help message')
}

final options = cli.parse(args)
def exitCode = 0

if (options != null && !options.h) {
   final sql = Sql.newInstance([url: "jdbc:postgresql://${options.H}:${options.P}/${options.d}", user: options.u, password: options.p, driver: 'org.postgresql.Driver'])
   final tables = new ArrayList<String>()
   final schemas = new ArrayList<String>()

   sql.eachRow("""
   SELECT schema_name FROM information_schema.schemata WHERE schema_name IN ('corrto', 'coravt', 'corron')
""") { rs ->
      final schemaName = rs.getString("schema_name")

      tables.add("${schemaName}.level2_departments")
      tables.add("${schemaName}.level2_stores")
      tables.add("${schemaName}.level2_employees")
      tables.add("${schemaName}.level2_inventories")
      tables.add("${schemaName}.level2_inventory_statuses")
      tables.add("${schemaName}.level2_models")
      tables.add("${schemaName}.level2_manufacturers")
      tables.add("${schemaName}.level2_location_types")
      tables.add("${schemaName}.level2_locations")
      tables.add("${schemaName}.level2_vendors")
      tables.add("${schemaName}.level2_customers")
      tables.add("${schemaName}.level1_operators")
      tables.add("${schemaName}.level1_furn_cols")

      schemas.add("CREATE SCHEMA IF NOT EXISTS ${schemaName};\n")
   }

   final outDir = new File(options.o)

   outDir.mkdirs()

   if (outDir.exists() && outDir.isDirectory()) {
      final schemaFileSql = new File(outDir, "schema-create.sql")

      println("Cleaning output directory $outDir")

      FileUtils.cleanDirectory(outDir)

      schemas.each { schemaFileSql.append(it) }

      tables.eachWithIndex { table, i ->
         final fileNumber = String.format('%04d', i + 1)
         final tempSql = File.createTempFile("${table}", ".sql")

         try {
            final outSql = new File(outDir, "${fileNumber}__${table}.sql")
            final pgDumpBuilder = new ProcessBuilder("pg_dump", "-U", "postgres", "-d", "fastinfo_production", "-s", "-t", table)
               .with { it.environment().putAll(["PGPASSWORD": options.p]); it }

            pgDumpBuilder.redirectOutput(tempSql)
            pgDumpBuilder.start().waitFor()

            tempSql.eachLine { line ->
               if (!line.contains("CREATE TRIGGER") && !line.startsWith("--") && !StringUtils.isBlank(line)) {
                  outSql.append("$line \n")
               }
            }

            final outCsv = new File(outDir, "${table}.csv")

            outCsv.withWriter {
               final tableQuery = "SELECT * FROM ${table} ORDER BY id".toString()

               println "Exporting $table to $outCsv"

               sql.query(tableQuery) { rs ->
                  POSTGRESQL_CSV
                     .withHeader(rs)
                     .print(it)
                     .printRecords(rs)
               }
            }
         } finally {
            tempSql.delete()
         }
      }
   } else {
      println "Unable to create output directory $outDir"
      exitCode = -2
   }
} else {
   println cli.usage()
   exitCode = -3
}

System.exit(exitCode)
