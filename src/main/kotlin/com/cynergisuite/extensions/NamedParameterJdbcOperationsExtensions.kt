package com.cynergisuite.extensions

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.error.DataAccessException
import com.cynergisuite.domain.infrastructure.PagedResultSetExtractor
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.domain.infrastructure.SimpleResultSetExtractor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import java.sql.ResultSet

private val logger: Logger = LoggerFactory.getLogger("com.cynergisuite.extensions.NamedParameterJdbcOperationsExtensions")

fun <ENTITY> NamedParameterJdbcOperations.findFirstOrNull(query: String, params: Map<String, *> = emptyMap<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY? {
   val resultList: List<ENTITY> = this.query(query, params, rowMapper)

   return mineListForFirstElement(query, resultList, params)
}

fun <ENTITY> NamedParameterJdbcOperations.findFirstOrNull(query: String, params: Map<String, *> = emptyMap<String, Any>(), mapper: (rs: ResultSet) -> ENTITY): ENTITY? {
   val resultList = this.query(query, params) { rs, _ -> mapper(rs) }

   return mineListForFirstElement(query, resultList, params)
}

fun <ENTITY> NamedParameterJdbcOperations.findFirst(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY {
   val resultList: List<ENTITY> = this.query(query, params, rowMapper)

   return mineListForFirstElement(query, resultList, params)!!
}

fun <ENTITY : Identifiable> NamedParameterJdbcOperations.queryFullList(sql: String, params: Map<String, *>, mapper: (rs: ResultSet, elements: MutableList<ENTITY>) -> Unit): List<ENTITY> {
   return this.query(sql, params, SimpleResultSetExtractor(mapper))!!
}

fun <ENTITY : Identifiable, REQUESTED : PageRequest> NamedParameterJdbcOperations.queryPaged(sql: String, params: Map<String, *>, pageRequest: REQUESTED, mapper: (rs: ResultSet, elements: MutableList<ENTITY>) -> Unit): RepositoryPage<ENTITY, REQUESTED> {
   return this.query(sql, params, PagedResultSetExtractor(pageRequest, mapper))!!
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

private fun <ENTITY> mineListForFirstElement(sql: String, elements: List<ENTITY>, params: Map<String, *>? = null): ENTITY? {
   return when (elements.size) {
      0 -> null
      1 -> elements.first()
      else -> {
         logger.error("Query returned more than 1 result {} {} {}", sql, params, elements)

         throw DataAccessException("Query $sql returned ${elements.size} elements when only 1 was expected")
      }
   }
}
