package com.cynergisuite.domain.infrastructure

import io.micronaut.context.annotation.Requires
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowCallbackHandler

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Requires(env = ["test"])
class TruncateDatabaseService {
   private static final Logger logger = LoggerFactory.getLogger(TruncateDatabaseService)
   private final JdbcTemplate jdbc

   @Inject
   TruncateDatabaseService(JdbcTemplate jdbc) {
      this.jdbc = jdbc
   }

   @Transactional
   void truncate() {
      final Set<String> exclusionTables = Collections.unmodifiableSet([ 'menu', 'module' ] as Set)
      final List<String> tables = []

      logger.debug("Querying for tables to cleanup")
      jdbc.query("SELECT table_name AS tableName FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE' AND table_name <> 'flyway_schema_history' AND table_name NOT LIKE '%_type_domain'", {
         final String table = it.getString("tableName")

         tables.add("TRUNCATE TABLE $table CASCADE".toString())
      } as RowCallbackHandler)

      logger.debug("Cleaning up tables {}", tables);
      jdbc.batchUpdate(tables.toArray(new String[tables.size()]))
   }
}
