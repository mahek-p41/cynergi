package com.cynergisuite.middleware.shipping.freight.calc.method.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodType
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
class FreightCalcMethodTypeRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(FreightCalcMethodTypeRepository::class.java)
   private val simpleFreightCalcMethodTypeRowMapper = FreightCalcMethodTypeRowMapper()

   @ReadOnly fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM freight_calc_method_type_domain WHERE UPPER(value) = :value)",
         mapOf(
            "value" to value.uppercase()
         ),
         Boolean::class.java
      )

      logger.trace("Checking if FreightMethodType: {} exists resulted in {}", value, exists)

      return exists
   }

   fun doesNotExist(freightCalcMethodType: String): Boolean = !exists(freightCalcMethodType)

   @ReadOnly fun findOne(id: Long): FreightCalcMethodType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM freight_calc_method_type_domain WHERE id = :id", mapOf("id" to id), simpleFreightCalcMethodTypeRowMapper)

      logger.trace("Searching for FreightMethodType: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly fun findOne(value: String): FreightCalcMethodType? {
      val found = jdbc.findFirstOrNull(
         "SELECT * FROM freight_calc_method_type_domain WHERE UPPER(value) = :value",
         mapOf(
            "value" to value.uppercase()
         ),
         simpleFreightCalcMethodTypeRowMapper
      )

      logger.trace("Searching for FreightMethodTypeDomain: {} resulted in {}", value, found)

      return found
   }

   @ReadOnly
   fun findAll(): List<FreightCalcMethodType> =
      jdbc.query("SELECT * FROM freight_calc_method_type_domain ORDER BY id", rowMapper = simpleFreightCalcMethodTypeRowMapper)
}

private class FreightCalcMethodTypeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<FreightCalcMethodType> {
   override fun map(rs: ResultSet, ctx: StatementContext): FreightCalcMethodType =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): FreightCalcMethodType =
      FreightCalcMethodType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
