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

import groovy.cli.picocli.CliBuilder
import groovy.sql.Sql
import org.apache.commons.io.FileUtils

import static org.apache.commons.csv.CSVFormat.POSTGRESQL_CSV

// https://relentlesscoding.com/posts/how-to-use-groovys-clibuilder/
final cli = new CliBuilder(name: 'export-cynergidb')

cli.width = 80
cli.with {
   u(longOpt: 'user', args: 1, defaultValue: 'cynergiuser', 'db username')
   p(longOpt: 'password', args: 1, defaultValue: 'password', 'db password')
   P(longOpt: 'port', args: 1, defaultValue: '5432', 'db port')
   d(longOpt: 'database', defaultValue: 'cynergidb', args: 1, 'cynergidb to migrate')
   H(longOpt: 'host', args: 1, defaultValue: 'localhost', 'Host database is running on')
   o(longOpt: 'out', args: 1, defaultValue: '/tmp/cynergidb', 'Location to dump CSV\'s')
   h(longOpt: 'help', 'this help message')
}

final options = cli.parse(args)
def exitCode = 0

if (options != null && !options.h) {
   final sql = Sql.newInstance([url: "jdbc:postgresql://${options.H}:${options.P}/${options.d}", user: options.u, password: options.p, driver: 'org.postgresql.Driver'])
   final tables = new LinkedHashSet<String>()

   sql.eachRow("""
WITH RECURSIVE
    fkeys AS (
        SELECT conrelid  AS source,
               confrelid AS target
        FROM pg_constraint
        WHERE contype = 'f'
    ),
    tables AS (
        (
            SELECT oid         AS table_name,
                   1           AS level,
                   ARRAY [oid] AS trail,
                   FALSE       AS circular
            FROM pg_class
            WHERE relkind = 'r'
              AND NOT relnamespace::regnamespace::text LIKE ANY
                      (ARRAY ['pg_catalog', 'information_schema', 'pg_temp_%'])
            EXCEPT
            SELECT source,
                   1,
                   ARRAY [ source ],
                   FALSE
            FROM fkeys
        )
        UNION ALL
        SELECT fkeys.source,
               tables.level + 1,
               tables.trail || fkeys.source,
               tables.trail @> ARRAY [fkeys.source]
        FROM fkeys JOIN tables ON tables.table_name = fkeys.target
        WHERE cardinality(array_positions(tables.trail, fkeys.source)) < 2
    ),
    ordered_tables AS (
        SELECT DISTINCT ON (table_name) table_name, level, circular
        FROM tables
        ORDER BY table_name, level DESC
    )
SELECT table_name::regclass,
       level
FROM ordered_tables
WHERE NOT circular
ORDER BY level, table_name
""") { rs ->
      final tableName = rs.getString("table_name");

      if (!tableName.endsWith("_type_domain") && !tableName.startsWith("flyway")) {
         tables.add(tableName)
      }
   }

   final outDir = new File(options.o)

   outDir.mkdirs()

   if (outDir.exists() && outDir.isDirectory()) {
      FileUtils.cleanDirectory(outDir)

      tables.eachWithIndex { table, i ->
         final fileNumber = String.format('%04d', i)
         final file = new File(outDir, "${fileNumber}__${table}.csv")

         file.withWriter {
            final tableQuery = "SELECT * FROM ${table}".toString()

            println "Exporting $table to $file"

            sql.query(tableQuery) { rs ->
               POSTGRESQL_CSV
                  .withHeader(rs)
                  .print(it)
                  .printRecords(rs)
            }
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
