package com.hightouchinc.cynergi.middleware.data.access

import com.hightouchinc.cynergi.middleware.extensions.nextval
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

abstract class DataAccessObjectBase (
   private val tableName: String,
   protected val jdbc: NamedParameterJdbcTemplate
) {
   private companion object {
      val logger: Logger = LoggerFactory.getLogger(DataAccessObjectBase::class.java)
   }

   protected fun save(parameters: Map<String, Any?>, query: String): Long {
      val id: Long = jdbc.nextval(tableName)
      val params = parameters.toMutableMap()

      params["id"] = id

      logger.trace("Saving {} to {}", params, tableName)
      logger.debug("Inserted {} to {}", jdbc.update(query, params), tableName)

      return id
   }
}
