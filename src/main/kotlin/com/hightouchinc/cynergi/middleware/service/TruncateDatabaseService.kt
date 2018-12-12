package com.hightouchinc.cynergi.middleware.service

import io.micronaut.context.annotation.Requires
import io.micronaut.spring.tx.annotation.Transactional
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Singleton

@Singleton
@Requires(env = ["test"])
class TruncateDatabaseService(
   private val jdbc: JdbcTemplate
) {

   @Transactional
   fun truncate() {
      val tables = jdbc.query("""
          SELECT table_name AS tableName
          FROM information_schema.tables
          WHERE table_schema='public'
                AND table_type='BASE TABLE'
          """.trimIndent()) { rs, _ ->
         rs.getString("tableName")
      }

      tables
         .filter { !it.contains("flyway") }
         .forEach { jdbc.update("TRUNCATE TABLE $it CASCADE") }
   }
}
