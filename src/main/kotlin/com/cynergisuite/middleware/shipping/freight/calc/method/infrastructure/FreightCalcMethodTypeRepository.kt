package com.cynergisuite.middleware.shipping.freight.calc.method.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodType
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreightCalcMethodTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(FreightCalcMethodTypeRepository::class.java)
   private val simpleFreightCalcMethodTypeRowMapper = FreightCalcMethodTypeRowMapper()

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM freight_calc_method_type_domain WHERE UPPER(value) = :value)", mapOf("value" to value.toUpperCase()), Boolean::class.java)!!

      logger.trace("Checking if FreightMethodType: {} exists resulted in {}", value, exists)

      return exists
   }

   fun doesNotExist(freightCalcMethodType: String): Boolean = !exists(freightCalcMethodType)

   fun findOne(id: Long): FreightCalcMethodType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM freight_calc_method_type_domain WHERE id = :id", mapOf("id" to id), simpleFreightCalcMethodTypeRowMapper)

      logger.trace("Searching for FreightMethodType: {} resulted in {}", id, found)

      return found
   }

   fun findOne(value: String): FreightCalcMethodType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM freight_calc_method_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), simpleFreightCalcMethodTypeRowMapper)

      logger.trace("Searching for FreightMethodTypeDomain: {} resulted in {}", value, found)

      return found
   }

   fun findAll(): List<FreightCalcMethodType> =
      jdbc.query("SELECT * FROM freight_calc_method_type_domain ORDER BY id", simpleFreightCalcMethodTypeRowMapper)
}

private class FreightCalcMethodTypeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<FreightCalcMethodType> {
   override fun mapRow(rs: ResultSet, rowNum: Int): FreightCalcMethodType =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): FreightCalcMethodType =
      FreightCalcMethodType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
