package com.cynergisuite.middleware.purchase.order.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagType
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApprovalRequiredFlagTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(ApprovalRequiredFlagTypeRepository::class.java)
   private val rowMapper = ApprovalRequiredFlagTypeRowMapper()

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM approval_required_flag_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), Boolean::class.java)!!

      logger.trace("Checking if ApprovalRequiredFlagType: {} exists resulting in {}", value, exists)

      return exists
   }

   fun findOne(id: Long): ApprovalRequiredFlagType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM approval_required_flag_type_domain WHERE id = :id", mapOf("id" to id), rowMapper)

      logger.trace("Searching for ApprovalRequiredFlagTypeDomain: {} resulted in {}", id, found)

      return found
   }

   fun findOne(value: String): ApprovalRequiredFlagType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM approval_required_flag_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), rowMapper)

      logger.trace("Searching for ApprovalRequiredFlagTypeDomain: {} resulted in {}", value, found)

      return found
   }

   fun findAll(): List<ApprovalRequiredFlagType> =
      jdbc.query("SELECT * FROM approval_required_flag_type_domain ORDER BY id", rowMapper)

}

private class ApprovalRequiredFlagTypeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<ApprovalRequiredFlagType> {
   override fun mapRow(rs: ResultSet, rowNum: Int): ApprovalRequiredFlagType =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): ApprovalRequiredFlagType =
      ApprovalRequiredFlagType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
