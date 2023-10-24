package com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodType
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Singleton
class OverallPeriodTypeRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(OverallPeriodTypeRepository::class.java)

   @ReadOnly
   fun exists(value: String): Boolean {
      logger.trace("Check to see if value {} exists", value)

      val exists = jdbc.queryForObject(
         "SELECT EXISTS (SELECT id FROM overall_period_type_domain WHERE UPPER(value) = :value)",
         mapOf(
            "value" to value.uppercase()
         ),
         Boolean::class.java
      )

      logger.trace("Checking if OverallPeriodType: {} exists resulting in {}", value, exists)

      return exists
   }

   @ReadOnly
   fun findOne(id: Long): OverallPeriodType? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "SELECT * FROM overall_period_type_domain WHERE id = :id"
      logger.trace("Searching for OverallPeriodTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for OverallPeriodTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @ReadOnly
   fun findOne(value: String): OverallPeriodType? {
      val params = mutableMapOf<String, Any?>("value" to value.uppercase())
      val query = "SELECT * FROM overall_period_type_domain WHERE UPPER(value) = :value"
      logger.trace("Searching for OverallPeriodTypeDomain {}: \nQuery {}", params, query)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for OverallPeriodTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   @ReadOnly
   fun findAll(): List<OverallPeriodType> =
      jdbc.query("SELECT * FROM overall_period_type_domain ORDER BY id") { rs, _ -> mapRow(rs) }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): OverallPeriodType =
      OverallPeriodType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         abbreviation = rs.getString("${columnPrefix}abbreviation"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   fun mapRowOrNull(rs: ResultSet, columnPrefix: String = EMPTY): OverallPeriodType? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, columnPrefix)
      } else {
         null
      }
}
