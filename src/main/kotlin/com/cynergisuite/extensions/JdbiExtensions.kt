package com.cynergisuite.extensions

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.error.DataAccessException
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.middleware.error.infrastructure.ErrorHandlerController
import io.micronaut.data.exceptions.EmptyResultException
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.SqlStatement
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

private val logger: Logger = LoggerFactory.getLogger("com.cynergisuite.extensions.JdbiExtensions")

/**
 * query to mirror Spring's NamedParameterJdbcOperations as much as possible.
 */
fun <ENTITY> Jdbi.query(sql: String, params: Map<String, *> = emptyMap<String, Any?>(), rowMapper: RowMapper<ENTITY>): List<ENTITY> {
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

/**
 * This method check if referenced data in foreign keys are still referenced and throw an exception
 * @param sql soft delete queries.
 * @param params queries params to filter deleted rows.
 * @param tableName name of the table, is used to find foreign keys.
 * @param idColumn default value is "id" for delete by id, pass foreign column name if delete by foreign key.
 * @return deleted row count
 * @exception UnableToExecuteStatementException
 */
fun Jdbi.softDelete(sql: String, params: Map<String, *>, tableName: String, idColumn: String? = "id"): Int {
   return this.withHandle<Int, Exception> { handle ->
      val findReferenceQuery = """
      SELECT conrelid::regclass AS "FK_Table"
         ,CASE WHEN pg_get_constraintdef(c.oid) LIKE 'FOREIGN KEY %' THEN substring(pg_get_constraintdef(c.oid), 14, position(')' in pg_get_constraintdef(c.oid))-14) END AS "FK_Column"
         ,CASE WHEN pg_get_constraintdef(c.oid) LIKE 'FOREIGN KEY %' THEN substring(pg_get_constraintdef(c.oid), position(' REFERENCES ' in pg_get_constraintdef(c.oid))+12, position('(' in substring(pg_get_constraintdef(c.oid), 14))-position(' REFERENCES ' in pg_get_constraintdef(c.oid))+1) END AS "PK_Table"
         ,CASE WHEN pg_get_constraintdef(c.oid) LIKE 'FOREIGN KEY %' THEN substring(pg_get_constraintdef(c.oid), position('(' in substring(pg_get_constraintdef(c.oid), 14))+14, position(')' in substring(pg_get_constraintdef(c.oid), position('(' in substring(pg_get_constraintdef(c.oid), 14))+14))-1) END AS "PK_Column"
      FROM   pg_constraint c
      JOIN   pg_namespace n ON n.oid = c.connamespace
      WHERE  contype IN ('f', 'p ')
      AND pg_get_constraintdef(c.oid) LIKE 'FOREIGN KEY %'
      AND substring(pg_get_constraintdef(c.oid), position(' REFERENCES ' in pg_get_constraintdef(c.oid))+12, position('(' in substring(pg_get_constraintdef(c.oid), 14))-position(' REFERENCES ' in pg_get_constraintdef(c.oid))+1) = :tableName
      ORDER  BY pg_get_constraintdef(c.oid), conrelid::regclass::text, contype DESC;
      """.trimIndent()

      val query = handle.createQuery(findReferenceQuery)

      bindParameters(mapOf("tableName" to tableName), query)

      query.execute { statementSupplier, _ ->
         val rs = statementSupplier.get().executeQuery()
         var hasReferences = false
         var referencedTables = mutableListOf<String>()

         while (rs.next()) {
            val fkColumn = rs.getString("FK_Column")
            val pkColumn = rs.getString("PK_Column")
            val fkTable = rs.getString("FK_Table")
            val pkTable = rs.getString("PK_Table")
            val isPK = idColumn == pkColumn
            logger.info("Checking $idColumn=${params["id"]} in referenced column: $fkTable.$fkColumn")

            val deletedColumnExistsQuery = handle.createQuery(
               """
               SELECT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
		         WHERE TABLE_NAME = '$fkTable' AND COLUMN_NAME = 'deleted')
            """
            )
            val deletedColumnExists = deletedColumnExistsQuery.mapTo(Boolean::class.java).first()

            val checkExistQuery = if (isPK) {
               if (deletedColumnExists)
                  handle.createQuery("SELECT EXISTS(SELECT * FROM $fkTable WHERE $fkColumn = :id AND deleted = false)")
               else
                  handle.createQuery("SELECT EXISTS(SELECT * FROM $fkTable WHERE $fkColumn = :id)")
            } else {
               if (deletedColumnExists)
                  handle.createQuery("SELECT EXISTS(SELECT * FROM $fkTable WHERE $fkColumn IN (SELECT id FROM $pkTable WHERE $idColumn = :id) AND deleted = false)")
               else
                  handle.createQuery("SELECT EXISTS(SELECT * FROM $fkTable WHERE $fkColumn = IN (SELECT id FROM $pkTable WHERE $idColumn = :id))")
            }

            bindParameters(mapOf("id" to params["id"]), checkExistQuery)

            val exist = checkExistQuery.mapTo(Boolean::class.java).findFirst().orElseThrow { EmptyResultException() }

            if (exist) {
               referencedTables.add("$fkTable.$fkColumn")
               hasReferences = true
            }
         }

         if (hasReferences) {
            logger.info("Key id=${params["id"]} is still referenced in $referencedTables")
            throw UnableToExecuteStatementException(ErrorHandlerController.SOFT_DELETE_ERROR)
         }

         null
      }

      val update = handle.createUpdate(sql)

      bindParameters(params, update)

      update.execute()
   }
}

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

fun <ENTITY> Jdbi.insertReturningList(query: String, params: Map<String, *> = mapOf<String, Any>(), rowMapper: (rs: ResultSet, ctx: StatementContext, elements: MutableList<ENTITY>) -> Unit): List<ENTITY>? {
   return this.queryFullList(query, params, rowMapper)
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
