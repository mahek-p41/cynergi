package com.hightouchinc.cynergi.middleware.extensions

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations

fun <T> NamedParameterJdbcOperations.findFirstOrNull(query: String, params: Map<String, Any> = mapOf(), rowMapper: RowMapper<T>): T? {
   val resultList: List<T> = this.query(query, params, rowMapper)

   return resultList.firstOrNull()
}
