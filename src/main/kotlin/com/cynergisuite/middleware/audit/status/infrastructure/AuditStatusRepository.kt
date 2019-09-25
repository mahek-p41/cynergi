package com.cynergisuite.middleware.audit.status.infrastructure

import com.cynergisuite.domain.infrastructure.TypeDomainRepository
import com.cynergisuite.middleware.audit.status.AuditStatus
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditStatusRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : TypeDomainRepository<AuditStatus> {
   private val logger: Logger = LoggerFactory.getLogger(AuditStatusRepository::class.java)
   private val simpleAuditStatusRowMapper = AuditStatusRowMapper()

   override fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_status_type_domain WHERE value = :value)", mapOf("value" to value), Boolean::class.java)!!

      logger.trace("Checking if Audit: {} exists resulted in {}", value, exists)

      return exists
   }

   override fun findOne(id: Long): AuditStatus? =
      executeFindQuery(mapOf("id" to id))

   override fun findOne(value: String): AuditStatus? =
      executeFindQuery(mapOf("value" to value))

   override fun findAll(): List<AuditStatus> =
      jdbc.query("SELECT * FROM audit_status_type_domain ORDER BY value", simpleAuditStatusRowMapper)

   fun mapRow(rs: ResultSet, rowPrefix: String = "astd_"): AuditStatus =
      simpleAuditStatusRowMapper.mapRow(rs, rowPrefix)

   private fun executeFindQuery(params: Map<String, Any>): AuditStatus? {
      var root: AuditStatus? = null
      val existingStatuses = mutableMapOf<Long, AuditStatus>()
      val whereClause = if (params.keys.first() == "id") "WHERE astd.id = :id" else "WHERE astd.value = UPPER(:value)"

      jdbc.query("""
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
      """.trimIndent(),
         params
      ) { rs: ResultSet ->
         if (root == null) {
            val r = simpleAuditStatusRowMapper.mapRow(rs, "parent_")
            existingStatuses[r.id] = r
            root = r
         }

         val nxt = rs.getString("child_id")

         if (nxt != null) {
            var audit = simpleAuditStatusRowMapper.mapRow(rs, "child_")
            val fromId: Long = rs.getLong("frm")
            val from: AuditStatus = existingStatuses[fromId]!!

            if ( existingStatuses.containsKey(audit.id) ) {
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
   override fun mapRow(rs: ResultSet, rowNum: Int): AuditStatus =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): AuditStatus =
      AuditStatus(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         color = rs.getString("${columnPrefix}color"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
