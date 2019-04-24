package com.cynergisuite.domain.infrastructure

import io.micronaut.context.annotation.Requires
import io.micronaut.spring.tx.annotation.Transactional
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowCallbackHandler

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Requires(env = ["test"])
class TruncateDatabaseService {
   private final JdbcTemplate jdbc

   @Inject
   TruncateDatabaseService(JdbcTemplate jdbc) {
      this.jdbc = jdbc
   }

   @Transactional
   void truncate() {
      final Set<String> exclusionTables = Collections.unmodifiableSet([ 'menu', 'module' ] as Set)
      final List<String> tables = []

      jdbc.query("SELECT table_name AS tableName FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'", {
         final String table = it.getString("tableName")

         if ( !table.contains("flyway") && !table.endsWith("_type_domain") && !exclusionTables.contains(table) ) {
            tables.add("TRUNCATE TABLE $table CASCADE".toString())
         }
      } as RowCallbackHandler)

      jdbc.batchUpdate(tables.toArray(new String[tables.size()]))
   }
}
