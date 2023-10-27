package com.cynergisuite.domain.infrastructure

import groovy.sql.Sql
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
@CompileStatic
@Requires(env = ["test", "load"])
class TruncateDatabaseService {

   private static final Logger logger = LoggerFactory.getLogger("TruncateDatabaseService")
   private final Sql sql

   @Inject
   TruncateDatabaseService(Sql sql) {
      this.sql = sql
   }

   @Transactional
   void truncate() {
      final List<String> tables = new ArrayList<>()

      logger.debug("Querying for tables to cleanup")
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
}

