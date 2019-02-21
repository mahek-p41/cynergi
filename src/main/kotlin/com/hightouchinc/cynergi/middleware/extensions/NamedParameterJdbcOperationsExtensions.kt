package com.hightouchinc.cynergi.middleware.extensions

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import java.sql.ResultSet

fun <T> NamedParameterJdbcOperations.findFirstOrNull(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<T>): T? {
   val resultList: List<T> = this.query(query, params, rowMapper)

   return resultList.firstOrNull()
}

fun <T> NamedParameterJdbcOperations.findFirstOrNullWithCrossJoin(query: String, params: Map<String, *>, primaryRowMapper: RowMapper<T>, childRowCallbackHandler: (T, ResultSet) -> Unit): T? {
   var found: T? = null

   this.query(query, params) { rs ->
      val tempResult: T = found ?: primaryRowMapper.mapRow(rs, 0)!!

      found = tempResult

      childRowCallbackHandler.invoke(tempResult, rs)
   }

   return found
}

fun <T> NamedParameterJdbcOperations.findAllWithCrossJoin(query: String, params: Map<String, *>, parentIdColumn: String, primaryRowMapper: RowMapper<T>, childRowCallbackHandler: (T, ResultSet) -> Unit): List<T> {
   var currentId: Long = -1
   var currentParentEntity: T? = null
   val resultList: MutableList<T> = mutableListOf()

   this.query(query, params) { rs ->
      val tempId = rs.getLong(parentIdColumn)
      val tempParentEntity: T = if (tempId != currentId) {
         currentId = tempId
         currentParentEntity = primaryRowMapper.mapRow(rs, 0)
         resultList.add(currentParentEntity!!)
         currentParentEntity!!
      } else {
         currentParentEntity!!
      }

      childRowCallbackHandler.invoke(tempParentEntity, rs)
   }

   return resultList
}

fun <T> NamedParameterJdbcOperations.insertReturning(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<T>): T {
   return this.queryForObject(query, params, rowMapper)!!
}

fun <T> NamedParameterJdbcOperations.updateReturning(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<T>): T {
   return this.queryForObject(query, params, rowMapper)!!
}
