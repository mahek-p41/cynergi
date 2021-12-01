package com.cynergisuite.middleware.audit.status.infrastructure

import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.audit.status.AuditStatusEntity
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AuditStatusRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditStatusRepository::class.java)
   private val simpleAuditStatusRowMapper = AuditStatusRowMapper()

   @ReadOnly
   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM audit_status_type_domain WHERE value = :value)",
         mapOf("value" to value),
         Boolean::class.java
      )

      logger.trace("Checking if Audit: {} exists resulted in {}", value, exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: Int): AuditStatus? =
      executeFindQuery(mapOf("id" to id))

   @ReadOnly
   fun findOne(value: String): AuditStatus? =
      executeFindQuery(mapOf("value" to value))

   @ReadOnly
   fun findAll(): List<AuditStatus> =
      jdbc.query("SELECT * FROM audit_status_type_domain ORDER BY value", rowMapper = simpleAuditStatusRowMapper)

   fun mapRow(rs: ResultSet, rowPrefix: String = "astd_"): AuditStatus =
      simpleAuditStatusRowMapper.mapRow(rs, rowPrefix)

   private fun executeFindQuery(params: Map<String, Any>): AuditStatus? {
      var root: AuditStatus? = null
      val existingStatuses = mutableMapOf<Int, AuditStatus>()
      val whereClause = if (params.keys.first() == "id") "WHERE astd.id = :id" else "WHERE astd.value = UPPER(:value)"
      val sql = """
         WITH RECURSIVE transition(depth, id, value, description, color, localization_code, frm, nxt) AS (
            SELECT
               1 AS depth,
               astd.id AS id,
               astd.value AS value,
               astd.description AS description,
               astd.color AS color,
               astd.localization_code AS localization_code,
               astd.id AS frm,
               asttd.status_to AS nxt
            FROM audit_status_type_domain astd
                 LEFT OUTER JOIN audit_status_transitions_type_domain asttd
                              ON astd.id = asttd.status_from
            $whereClause
            UNION
            SELECT
               depth + 1 AS depth,
               astd.id AS id,
               astd.value AS value,
               astd.description AS description,
               astd.color AS color,
               astd.localization_code AS localization_code,
               asttd.status_from AS frm,
               asttd.status_to AS nxt
            FROM transition t
                 JOIN audit_status_type_domain astd
                   ON t.nxt = astd.id
                 LEFT OUTER JOIN audit_status_transitions_type_domain asttd
                              ON astd.id = asttd.status_from
            WHERE t.nxt IS NOT NULL
         )
         SELECT
            id AS parent_id,
            value AS parent_value,
            description AS parent_description,
            localization_code AS parent_localization_code,
            color AS parent_color,
            (SELECT id FROM audit_status_type_domain where id = nxt) as child_id,
            (SELECT value FROM audit_status_type_domain where id = nxt) as child_value,
            (SELECT color FROM audit_status_type_domain where id = nxt) as child_color,
            (SELECT description FROM audit_status_type_domain where id = nxt) as child_description,
            (SELECT localization_code FROM audit_status_type_domain where id = nxt) as child_localization_code,
            frm
         FROM transition
         ORDER BY depth, id, frm, nxt
      """.trimIndent()

      logger.trace("{}/{}", sql, params)

      jdbc.query(sql, params) { rs: ResultSet, _ ->
         if (root == null) {
            val r = simpleAuditStatusRowMapper.mapRow(rs, "parent_")
            existingStatuses[r.id] = r
            root = r
         }

         val nxt = rs.getString("child_id")

         if (nxt != null) {
            var audit = simpleAuditStatusRowMapper.mapRow(rs, "child_")
            val fromId: Int = rs.getInt("frm")
            val from: AuditStatus = existingStatuses[fromId]!!

            if (existingStatuses.containsKey(audit.id)) {
               audit = existingStatuses[audit.id]!!
            }

            from.nextStates.add(audit)
            existingStatuses[audit.id] = audit
         }
      }

      return root
   }
}

private class AuditStatusRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<AuditStatus> {
   override fun map(rs: ResultSet, ctx: StatementContext): AuditStatus =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): AuditStatus =
      AuditStatusEntity(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         color = rs.getString("${columnPrefix}color"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
