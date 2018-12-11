package com.hightouchinc.cynergi.middleware.data.access

import com.hightouchinc.cynergi.middleware.data.transfer.Business
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessDataAccessObject @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   fun fetchOne(id: Long): Business? {
      return jdbc.findFirstOrNull("""""", mapOf("id" to id)) { rs: ResultSet ->

      }
   }
}
