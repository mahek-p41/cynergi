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

fun <ENTITY: Identifiable> NamedParameterJdbcOperations.queryFullList(sql: String, params: Map<String, *>, mapper: (rs: ResultSet, elements: MutableList<ENTITY>) -> Unit): List<ENTITY> {
   return this.query(sql, params, SimpleResultSetExtractor<ENTITY>(mapper))!!
}

fun <ENTITY: Identifiable, REQUESTED: PageRequest> NamedParameterJdbcOperations.queryPaged(sql: String, params: Map<String, *>, pageRequest: REQUESTED, mapper: (rs: ResultSet, elements: MutableList<ENTITY>) -> Unit): RepositoryPage<ENTITY, REQUESTED> {
   return this.query(sql, params, PagedResultSetExtractor<ENTITY, REQUESTED>(pageRequest, mapper))!!
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
