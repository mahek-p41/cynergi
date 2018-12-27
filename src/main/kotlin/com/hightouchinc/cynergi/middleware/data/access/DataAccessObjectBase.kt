package com.hightouchinc.cynergi.middleware.data.access

import com.hightouchinc.cynergi.middleware.extensions.nextval
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

abstract class DataAccessObjectBase (
   private val tableName: String,
   protected val jdbc: NamedParameterJdbcTemplate
) {
   protected fun save(parameters: Map<String, Any>, query: String): Long {
      val id: Long = jdbc.nextval(tableName)
      val params = parameters.toMutableMap()

      params["id"] = id

      jdbc.update(query, params)

      return id
   }
}
