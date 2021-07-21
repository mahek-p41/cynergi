package com.cynergisuite.middleware.shipping.freight.term.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.middleware.shipping.freight.term.FreightTermType
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreightTermTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(FreightTermTypeRepository::class.java)

   fun exists(value: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM freight_term_type_domain WHERE UPPER(value) = :value)", mapOf("value" to value.toUpperCase()), Boolean::class.java)!!

      logger.trace("Checking if FreightTermType: {} exists resulted in {}", value, exists)

      return exists
   }

   fun doesNotExist(freightTermType: String): Boolean = !exists(freightTermType)

   fun findOne(id: Long): FreightTermType? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "SELECT * FROM freight_term_type_domain WHERE id = :id"
      logger.trace("Searching for FreightTermTypeDomain {}: \nQuery", params, query)

      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ -> mapRow(rs) })

      logger.trace("Searching for FreightTermTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   fun findOne(value: String): FreightTermType? {
      val params = mutableMapOf<String, Any?>("value" to value.toUpperCase())
      val query = "SELECT * FROM freight_term_type_domain WHERE UPPER(value) = :value"
      logger.trace("Searching for FreightTermTypeDomain {}: \nQuery", params, query)

      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ -> mapRow(rs) })

      logger.trace("Searching for FreightTermTypeDomain {}: \nQuery {} \nResulted in {}", params, query, found)

      return found
   }

   fun findAll(): List<FreightTermType> =
      jdbc.query("SELECT * FROM freight_term_type_domain ORDER BY id") { rs, _ -> mapRow(rs) }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): FreightTermType =
      FreightTermType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
