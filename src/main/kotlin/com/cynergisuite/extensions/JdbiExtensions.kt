package com.cynergisuite.extensions

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.error.DataAccessException
import com.cynergisuite.domain.infrastructure.RepositoryPage
import io.micronaut.data.exceptions.EmptyResultException
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.SqlStatement
import org.jdbi.v3.core.statement.StatementContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

private val logger: Logger = LoggerFactory.getLogger("com.cynergisuite.extensions.JdbiExtensions")

/**
 * query to mirror Spring's NamedParameterJdbcOperations as much as possible.
 */
fun <ENTITY> Jdbi.query(sql: String, params: Map<String, *> = emptyMap<String, Any?>(), rowMapper: RowMapper<ENTITY>): List<ENTITY> {
   logger.trace("Executing query {}/{}", sql, params)

   return this.withHandle<List<ENTITY>, Exception> { handle ->
      val query = handle.createQuery(sql)

      bindParameters(params, query)

      query.map(rowMapper).toList()
   }
}

fun Jdbi.update(sql: String, params: Map<String, *>): Int {
   return this.withHandle<Int, Exception> { handle ->
      val update = handle.createUpdate(sql)

      bindParameters(params, update)

      update.execute()
   }
}

fun Jdbi.delete(sql: String, params: Map<String, *>): Int =
   this.update(sql, params)

fun <ENTITY> Jdbi.queryForObject(sql: String, params: Map<String, *>, clazz: Class<ENTITY>): ENTITY {
   return this.withHandle<ENTITY, Exception> { handle ->
      val query = handle.createQuery(sql)

      bindParameters(params, query)

      query.mapTo(clazz).findFirst().orElseThrow { EmptyResultException() }
   }
}

fun <ENTITY> Jdbi.queryForObject(sql: String, params: Map<String, *>, rowMapper: RowMapper<ENTITY>): ENTITY {
   return this.withHandle<ENTITY, Exception> { handle ->
      val query = handle.createQuery(sql)

      bindParameters(params, query)

      query.map(rowMapper).findFirst().orElseThrow { EmptyResultException() }
   }
}

fun <ENTITY> Jdbi.queryForObjectOrNull(sql: String, params: Map<String, *>, rowMapper: RowMapper<ENTITY>): ENTITY {
   return this.withHandle<ENTITY, Exception> { handle ->
      val query = handle.createQuery(sql)

      bindParameters(params, query)

      query.map(rowMapper).findFirst().orElseNull()
   }
}

fun <ENTITY> Jdbi.findFirstOrNull(query: String, params: Map<String, *> = emptyMap<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY? {
   val resultList: List<ENTITY> = this.query(query, params, rowMapper)

   return mineListForFirstElement(query, resultList, params)
}

fun <ENTITY> Jdbi.findFirstOrNull(query: String, params: Map<String, *> = emptyMap<String, Any>(), mapper: (rs: ResultSet, ctx: StatementContext) -> ENTITY): ENTITY? {
   val resultList = this.query(query, params) { rs, ctx -> mapper(rs, ctx) }

   return mineListForFirstElement(query, resultList, params)
}

fun <ENTITY> Jdbi.findFirst(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY {
   val resultList: List<ENTITY> = this.query(query, params, rowMapper)

   return mineListForFirstElement(query, resultList, params)!!
}

fun <ENTITY> Jdbi.queryFullList(sql: String, params: Map<String, *>, mapper: (rs: ResultSet, ctx: StatementContext, elements: MutableList<ENTITY>) -> Unit): List<ENTITY> {
   val toReturn = mutableListOf<ENTITY>()

   return this.withHandle<MutableList<ENTITY>, Exception> { handle ->
      val query = handle.createQuery(sql)

      bindParameters(params, query)
      query.map { rs, ctx -> mapper(rs, ctx, toReturn) }.toList()

      toReturn
   }
}

fun <ENTITY, REQUESTED : PageRequest> Jdbi.queryPaged(sql: String, params: Map<String, *>, pageRequest: REQUESTED, mapper: (rs: ResultSet, elements: MutableList<ENTITY>) -> Unit): RepositoryPage<ENTITY, REQUESTED> {
   return this.withHandle<RepositoryPage<ENTITY, REQUESTED>, Exception> { handle ->
      val query = handle.createQuery(sql)

      bindParameters(params, query)

      query.execute { statementSupplier, _ ->
         val elements = mutableListOf<ENTITY>()
         var totalElements: Long = 0
         val rs = statementSupplier.get().executeQuery()

         if (rs.next()) {
            totalElements = rs.getString("total_elements")?.toLong() ?: 0
            mapper(rs, elements)
         }

         RepositoryPage(
            requested = pageRequest,
            elements = elements,
            totalElements = totalElements
         )
      }
   }
}

fun <ENTITY> Jdbi.insertReturning(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY {
   return this.queryForObject(query, params, rowMapper)!!
}

fun <ENTITY> Jdbi.updateReturning(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY {
   return this.queryForObject(query, params, rowMapper)!!
}

fun <ENTITY> Jdbi.deleteReturning(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: RowMapper<ENTITY>): ENTITY? {
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

private fun bindParameters(params: Map<String, *>, query: SqlStatement<*>) {
   params.entries.forEach { (key, value) ->
      when (value) {
         is Iterable<*> -> query.bindList(key, value)
         else -> query.bind(key, value)
      }
   }
}
