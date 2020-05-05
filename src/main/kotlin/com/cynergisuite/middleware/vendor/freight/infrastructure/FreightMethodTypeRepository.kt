package com.cynergisuite.middleware.vendor.freight.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.vendor.freight.method.FreightMethodTypeEntity
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreightMethodTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(FreightMethodTypeRepository::class.java)
   private val simpleFreightMethodTypeRowMapper = FreightMethodTypeRowMapper()

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM freight_calc_method_type_domain WHERE UPPER(value) = :value)", mapOf("value" to value.toUpperCase()), Boolean::class.java)!!

      logger.trace("Checking if FreightMethodType: {} exists resulted in {}", value, exists)

      return exists
   }

   fun doesNotExist(freightMethodType: String): Boolean = !exists(freightMethodType)

   fun findOne(id: Long): FreightMethodTypeEntity? {
      val found = jdbc.findFirstOrNull("SELECT * FROM freight_calc_method_type_domain WHERE id = :id", mapOf("id" to id), simpleFreightMethodTypeRowMapper)

      logger.trace("Searching for FreightMethodType: {} resulted in {}", id, found)

      return found
   }

   fun findOne(value: String): FreightMethodTypeEntity? {
      val found = jdbc.findFirstOrNull("SELECT * FROM freight_calc_method_type_domain WHERE UPPER(value) = :value", mapOf("value" to value.toUpperCase()), simpleFreightMethodTypeRowMapper)

      logger.trace("Searching for FreightMethodTypeDomain: {} resulted in {}", value, found)

      return found
   }

   fun findAll(): List<FreightMethodTypeEntity> =
      jdbc.query("SELECT * FROM freight_calc_method_type_domain ORDER BY id", simpleFreightMethodTypeRowMapper)

}

private class FreightMethodTypeRowMapper(
   private val columnPrefix: String = EMPTY
) : RowMapper<FreightMethodTypeEntity> {
   override fun mapRow(rs: ResultSet, rowNum: Int): FreightMethodTypeEntity =
      mapRow(rs, columnPrefix)

   fun mapRow(rs: ResultSet, columnPrefix: String): FreightMethodTypeEntity =
      FreightMethodTypeEntity(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
