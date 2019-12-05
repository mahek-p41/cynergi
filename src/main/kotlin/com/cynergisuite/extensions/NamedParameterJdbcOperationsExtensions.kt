package com.cynergisuite.extensions

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.PagedResultSetExtractor
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.domain.infrastructure.SimpleResultSetExtractor
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import java.sql.ResultSet

fun <ENTITY> NamedParameterJdbcOperations.findFirstOrNull(query: String, params: Map<String, *> = emptyMap<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY? {
   val resultList: List<ENTITY> = this.query(query, params, rowMapper)

   return resultList.firstOrNull()
}

fun <ENTITY: Identifiable> NamedParameterJdbcOperations.findFirstOrNull(query: String, params: Map<String, *> = emptyMap<String, Any>(), mapper: (rs: ResultSet) -> ENTITY): ENTITY? {
   val resultList = this.queryFullList(query, params, { rs, elements: MutableList<ENTITY> ->  elements.add(mapper(rs)) })

   return resultList.firstOrNull()
}

fun <ENTITY> NamedParameterJdbcOperations.findFirst(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY {
   val resultList: List<ENTITY> = this.query(query, params, rowMapper)

   return resultList.first()
}

@Deprecated(message = "You should not be using this as it is more complicated to use that just doing the iteration yourself, and only fits a single use case")
fun <ENTITY> NamedParameterJdbcOperations.findFirstOrNullWithCrossJoin(query: String, params: Map<String, *>, primaryRowMapper: RowMapper<ENTITY>, childRowCallbackHandler: (ENTITY, ResultSet) -> Unit = { _: ENTITY, _: ResultSet ->}): ENTITY? {
   var found: ENTITY? = null

   this.query(query, params) { rs ->
      val tempResult: ENTITY = found ?: primaryRowMapper.mapRow(rs, 0)!!

      found = tempResult

      childRowCallbackHandler.invoke(tempResult, rs)
   }

   return found
}

@Deprecated(message = "You should not be using this as it is more complicated to use that just doing the iteration yourself, and only fits a single use case")
fun <ENTITY> NamedParameterJdbcOperations.findAllWithCrossJoin(query: String, params: Map<String, *>, parentIdColumn: String, primaryRowMapper: RowMapper<ENTITY>, childRowCallbackHandler: (ENTITY, ResultSet) -> Unit): List<ENTITY> {
   var currentId: Long = -1
   var currentParentEntity: ENTITY? = null
   val resultList: MutableList<ENTITY> = mutableListOf()

   this.query(query, params) { rs ->
      val tempId = rs.getLong(parentIdColumn)
      val tempParentEntity: ENTITY = if (tempId != currentId) {
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

fun <ENTITY: Identifiable> NamedParameterJdbcOperations.queryFullList(sql: String, params: Map<String, *>, mapper: (rs: ResultSet, elements: MutableList<ENTITY>) -> Unit): List<ENTITY> {
   return this.query(sql, params, SimpleResultSetExtractor<ENTITY>(mapper))!!
}

fun <ENTITY: Identifiable> NamedParameterJdbcOperations.queryPaged(sql: String, params: Map<String, *>, pageRequest: PageRequest, mapper: (rs: ResultSet, elements: MutableList<ENTITY>) -> Unit): RepositoryPage<ENTITY> {
   return this.query(sql, params, PagedResultSetExtractor<ENTITY>(pageRequest, mapper))!!
}

fun <ENTITY> NamedParameterJdbcOperations.insertReturning(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY {
   return this.queryForObject(query, params, rowMapper)!!
}

fun <ENTITY> NamedParameterJdbcOperations.updateReturning(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY {
   return this.queryForObject(query, params, rowMapper)!!
}

fun <ENTITY> NamedParameterJdbcOperations.deleteReturning(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY? {
   return this.findFirstOrNull(query, params, rowMapper)
}
