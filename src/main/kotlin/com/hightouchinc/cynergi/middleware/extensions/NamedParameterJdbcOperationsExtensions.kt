package com.hightouchinc.cynergi.middleware.extensions

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations

fun <T> NamedParameterJdbcOperations.findFirstOrNull(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<T>): T? {
   val resultList: List<T> = this.query(query, params, rowMapper)

   return resultList.firstOrNull()
}

fun <T> NamedParameterJdbcOperations.insertReturning(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<T>): T {
   return this.queryForObject(query, params, rowMapper)!!
}

fun <T> NamedParameterJdbcOperations.updateReturning(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<T>): T {
   return this.queryForObject(query, params, rowMapper)!!
}
