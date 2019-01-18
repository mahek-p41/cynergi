package com.hightouchinc.cynergi.test.service

import io.micronaut.context.annotation.Requires
import io.micronaut.spring.tx.annotation.Transactional
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper

import javax.inject.Inject
import javax.inject.Singleton
import java.sql.ResultSet
import java.sql.SQLException

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
      final List<String> tables = jdbc.query("SELECT table_name AS tableName FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'", new RowMapper<String>() {

         @Override
         String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("tableName")
         }
      })

      for (table in tables) {
         if ( !table.contains("flyway") ) {
            jdbc.update("TRUNCATE TABLE $table CASCADE")
         }
      }
   }
}
