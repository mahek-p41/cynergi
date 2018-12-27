package com.hightouchinc.cynergi.middleware.extensions

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import java.sql.ResultSet

fun <T> NamedParameterJdbcOperations.findFirstOrNull(query: String, params: Map<String, Any> = mapOf(), rowMapper: (rs: ResultSet, rowNum: Int) -> T): T? {
   val resultList: List<T> = this.query(query, params, rowMapper)

   return resultList.firstOrNull()
}

fun NamedParameterJdbcOperations.nextval(tableName: String): Long {
   return this.queryForObject(
      "SELECT nextval(:name)",
      mapOf<String, String>("name" to "${tableName}_id_seq"),
      Long::class.java
   )!!
}
