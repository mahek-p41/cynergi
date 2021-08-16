package com.cynergisuite.middleware.shipping.freight.onboard.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardType
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreightOnboardTypeRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(FreightOnboardTypeRepository::class.java)

   @ReadOnly fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM freight_on_board_type_domain WHERE UPPER(value) = :value)",
         mapOf(
            "value" to value.uppercase()
         ),
         Boolean::class.java
      )

      logger.trace("Checking if FreightOnboardType: {} exists resulted in {}", value, exists)

      return exists
   }

   fun doesNotExist(freightOnboardType: String): Boolean = !exists(freightOnboardType)

   @ReadOnly fun findOne(id: Long): FreightOnboardType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM freight_on_board_type_domain WHERE id = :id", mapOf("id" to id)) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for FreightOnboardType: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly fun findOne(value: String): FreightOnboardType? {
      val found = jdbc.findFirstOrNull(
         "SELECT * FROM freight_on_board_type_domain WHERE UPPER(value) = :value",
         mapOf(
            "value" to value.uppercase(
               Locale.getDefault()
            )
         )
      ) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for FreightOnboardTypeDomain: {} resulted in {}", value, found)

      return found
   }

   @ReadOnly
   fun findAll(): List<FreightOnboardType> =
      jdbc.query("SELECT * FROM freight_on_board_type_domain ORDER BY id") { rs, _ -> mapRow(rs) }

   fun mapRow(rs: ResultSet, rowNum: Int, columnPrefix: String = EMPTY): FreightOnboardType =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): FreightOnboardType =
      FreightOnboardType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
