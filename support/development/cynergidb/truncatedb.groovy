#!/usr/bin/env groovy
import groovy.sql.Sql
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.postgresql', module = 'postgresql', version = '42.2.15')
@Grab(group='info.picocli', module='picocli', version='4.6.1')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.32')
@Grab(group='org.slf4j', module='jcl-over-slf4j', version='1.7.32')
@Grab(group='org.slf4j', module='jul-to-slf4j', version='1.7.32')

import groovy.cli.picocli.CliBuilder


// https://relentlesscoding.com/posts/how-to-use-groovys-clibuilder/
final cli = new CliBuilder(name: 'migratedb')

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
   final List<String> tables = new ArrayList<>()

   sql.eachRow("""
          SELECT table_name AS tableName
          FROM information_schema.tables
          WHERE table_schema='public'
                AND table_type='BASE TABLE'
                AND table_name <> 'flyway_schema_history'
                AND table_name NOT LIKE '%_type_domain'"""
   ) {rs ->
      final String table = rs.getString("tableName")

      tables.add("TRUNCATE TABLE $table CASCADE".toString())
   }

   if (tables.size() > 0) {
      sql.withBatch(tables.size()) {statement ->
         tables.forEach {statement.addBatch(it) }
      }
   }
}

