package com.hightouchinc.cynergi.middleware.service

import io.micronaut.context.annotation.Requires
import io.micronaut.spring.tx.annotation.Transactional
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Singleton

@Singleton
@Requires(env = ["test"])
class TruncateDatabaseService(
   private val jdbc: NamedParameterJdbcTemplate
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

      tables.forEach {tableName ->
         jdbc.update("TRUNCATE TABLE :tableName CASCADE", mapOf("tableName" to tableName ))
      }
   }
}
